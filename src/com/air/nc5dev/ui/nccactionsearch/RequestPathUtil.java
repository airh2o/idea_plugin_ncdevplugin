package com.air.nc5dev.ui.nccactionsearch;

import com.air.nc5dev.util.CollUtil;
import com.intellij.lang.jvm.annotation.JvmAnnotationArrayValue;
import com.intellij.lang.jvm.annotation.JvmAnnotationAttribute;
import com.intellij.lang.jvm.annotation.JvmAnnotationAttributeValue;
import com.intellij.lang.jvm.annotation.JvmAnnotationClassValue;
import com.intellij.lang.jvm.annotation.JvmAnnotationConstantValue;
import com.intellij.lang.jvm.annotation.JvmAnnotationEnumFieldValue;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.impl.source.tree.java.PsiArrayInitializerMemberValueImpl;
import com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl;
import com.intellij.psi.search.GlobalSearchScope;

import javax.swing.DefaultListModel;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RequestPathUtil {

    private static Map<String, RestListForm> restListForms = new HashMap<>();

    public static List<RequestPath> findAllRequestInProject(Project project) {
        List<RequestPath> requestPaths = new ArrayList<>();
        ModuleManager moduleManager = ModuleManager.getInstance(project);

        Module[] modules = moduleManager.getSortedModules();
        // 获取 request list
        for (Module module : modules) {
            requestPaths.addAll(findAllRequestInModule(project, module));
        }
        return requestPaths;
    }

    public static List<RequestPath> findAllRequestInModule(Project project, Module module) {
        GlobalSearchScope moduleScope = GlobalSearchScope.moduleScope(module);
        // search all spring @RestController annotation in module
        Collection<PsiAnnotation> psiAnnotations = JavaAnnotationIndex.getInstance().get("RestController", project, moduleScope);
        List<RequestPath> requestPaths = getRequestPaths(psiAnnotations, module);

        // search all spring @Controller annotation in module
        psiAnnotations = JavaAnnotationIndex.getInstance().get("Controller", project, moduleScope);
        requestPaths.addAll(getRequestPaths(psiAnnotations, module));
        return requestPaths;
    }

    private static List<RequestPath> getRequestPaths(Collection<PsiAnnotation> psiAnnotations, Module module) {
        List<RequestPath> requestPaths = new ArrayList<>();
        for (PsiAnnotation psiAnnotation : psiAnnotations) {
            PsiModifierList psiModifierList = (PsiModifierList) psiAnnotation.getParent();
            PsiElement psiElement = psiModifierList.getParent();
            PsiClass psiClass = (PsiClass) psiElement;

            requestPaths.addAll(getRequestPaths(psiClass, module, new ArrayList<>()));
        }

        return requestPaths;
    }

    private static List<RequestPath> getRequestPaths(PsiClass psiClass, Module module, List<String> parentRequestMapping) {
        List<RequestPath> requestPaths = new ArrayList<>();
        // 获取 class 的requestMapping路径
        if (CollUtil.isEmpty(parentRequestMapping)) {
            for (Mapping mapping : Annotations.getClasz()) {
                PsiAnnotation requestMappingAnnotation = psiClass.getAnnotation(mapping.getQualifiedName());
                parentRequestMapping = getPath(requestMappingAnnotation, mapping);
                if (CollUtil.isNotEmpty(parentRequestMapping)) {
                    break;
                }
            }
            if (CollUtil.isEmpty(parentRequestMapping)) {
                parentRequestMapping = Collections.singletonList("");
            }
        }

        PsiClass superClass = psiClass.getSuperClass();
        if (null != superClass) {
            if (!Objects.equals(superClass.getQualifiedName(), "java.lang.Object")) {
                requestPaths.addAll(getRequestPaths(superClass, module, parentRequestMapping));
            }
        }

        // 获取各方法的mapping
        PsiMethod[] psiMethods = psiClass.getMethods();
        for (PsiMethod psiMethod : psiMethods) {
            List<Mapping> mappings = Annotations.getMethods();
            for (Mapping mapping : mappings) {
                PsiAnnotation annotation = psiMethod.getAnnotation(mapping.getQualifiedName());
                if (null != annotation) {
                    List<String> path = getPath(annotation, mapping);
                    if (CollUtil.isNotEmpty(path)) {
                        for (String parentRequest : parentRequestMapping) {
                            for (String s : path) {
                                List<String> methods = getMethod(annotation, mapping);
                                for (String method : methods) {
                                    requestPaths.add(new RequestPath(parentRequest, s, method, psiMethod, module.getName()));
                                }
                            }
                        }
                    } else {
                        for (String parentRequest : parentRequestMapping) {
                            List<String> methods = getMethod(annotation, mapping);
                            for (String method : methods) {
                                requestPaths.add(new RequestPath(parentRequest, "", method, psiMethod, module.getName()));
                            }
                        }
                    }
                }
            }
        }

        return requestPaths;
    }

    private static List<String> getMethod(PsiAnnotation annotation, Mapping mapping) {
        List<String> methods = new ArrayList<>();
        PsiAnnotationMemberValue memberValue = annotation.findDeclaredAttributeValue("method");
        if (null != memberValue) {
            if (memberValue instanceof PsiReferenceExpressionImpl) {
                String method = ((PsiReferenceExpressionImpl) memberValue).getQualifiedName().substring(14).toLowerCase();
                methods.add(method);
                return methods;
            } else if (memberValue instanceof PsiArrayInitializerMemberValueImpl) {
                PsiAnnotationMemberValue[] values = ((PsiArrayInitializerMemberValueImpl) memberValue).getInitializers();
                for (PsiAnnotationMemberValue member : values) {
                    String method = ((PsiReferenceExpressionImpl) member).getQualifiedName().substring(14).toLowerCase();
                    methods.add(method);
                }
                return methods;
            }
        }

        methods.add(mapping.getMethod());
        return methods;
    }

    public static RestListForm getRestListForm(Project project) {
        if (!restListForms.containsKey(project.getLocationHash())) {
            RequestPathUtil.restListForms.put(getProjectUniqueCode(project), new RestListForm(project));
        }
        return restListForms.get(getProjectUniqueCode(project));
    }

    /**
     * 获取属性值
     *
     * @param attributeValue Psi属性
     * @return {Object | List}
     */
    public static Object getAttributeValue(JvmAnnotationAttributeValue attributeValue) {
        if (attributeValue == null) {
            return null;
        }
        if (attributeValue instanceof JvmAnnotationConstantValue) {
            return ((JvmAnnotationConstantValue) attributeValue).getConstantValue();
        } else if (attributeValue instanceof JvmAnnotationEnumFieldValue) {
            return ((JvmAnnotationEnumFieldValue) attributeValue).getFieldName();
        } else if (attributeValue instanceof JvmAnnotationArrayValue) {
            List<JvmAnnotationAttributeValue> values = ((JvmAnnotationArrayValue) attributeValue).getValues();
            List<Object> list = new ArrayList<>(values.size());
            for (JvmAnnotationAttributeValue value : values) {
                Object o = getAttributeValue(value);
                if (o != null) {
                    list.add(o);
                } else {
                    // 如果是jar包里的JvmAnnotationConstantValue则无法正常获取值
                    try {
                        Class<? extends JvmAnnotationAttributeValue> clazz = value.getClass();
                        Field myElement = clazz.getSuperclass().getDeclaredField("myElement");
                        myElement.setAccessible(true);
                        Object elObj = myElement.get(value);
                        if (elObj instanceof PsiExpression) {
                            PsiExpression expression = (PsiExpression) elObj;
                            list.add(expression.getText());
                        }
                    } catch (Exception ignore) {
                    }
                }
            }
            return list;
        } else if (attributeValue instanceof JvmAnnotationClassValue) {
            return ((JvmAnnotationClassValue) attributeValue).getQualifiedName();
        }
        return null;
    }

    private static List<String> getPath(PsiAnnotation psiAnnotation, Mapping mapping) {
        List<String> paths = new ArrayList<>();
        if (null != psiAnnotation) {
            List<JvmAnnotationAttribute> attributes = psiAnnotation.getAttributes();
            for (JvmAnnotationAttribute attribute : attributes) {
                String name = attribute.getAttributeName();
                if (name.equals("value")) {
                    Object attributeValue = getAttributeValue(attribute.getAttributeValue());
                    if (attributeValue instanceof String) {
                        paths.add((String) attributeValue);
                    } else if (attributeValue instanceof List) {
                        //noinspection unchecked,rawtypes
                        List<String> list = (List) attributeValue;
                        for (String item : list) {
                            if (item != null) {
                                item = item.substring(item.lastIndexOf(".") + 1);
                                paths.add(item);
                            }
                        }
                    }
                }
            }

            return paths;
        }
        return new ArrayList<>();
    }

    public static DefaultListModel<RequestPath> getListModel(List<RequestPath> requests) {
        DefaultListModel<RequestPath> listModel = new DefaultListModel<>();
        for (RequestPath request : requests) {
            listModel.addElement(request);
        }
        return listModel;
    }

    public static void removeRestListForm(Project project) {
        restListForms.remove(getProjectUniqueCode(project));
    }

    private static String getProjectUniqueCode(Project project) {
        return project.getLocationHash();
    }
}
