package com.puppycrawl.tools.checkstyle.api;

import cn.hutool.core.util.StrUtil;
import com.air.nc5dev.enums.NcVersionEnum;
import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.alibaba.fastjson.JSON;
import lombok.Data;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * NC 三大文件夹互相导入检查 </br>
 * </br>
 * </br>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2023/2/16 0016 16:40
 * @project
 * @Version
 */
@Data
public class NCImportCheckImpl extends AbstarctCheckImpl {
    public NCImportCheckImpl() {
        setFileExtensions(".java");
    }

    @Override
    public void processFiltered(File file, FileText fileText, FileContext fileContext) throws CheckstyleException {
        String[] lines = fileText.toLinesArray();
        if (CollUtil.isEmpty(lines)) {
            return;
        }

        String line;
        boolean comment = false; //是否注释
        int fileFrom = FROM_PUBLIC;

        String path = file.getPath().toLowerCase().trim();
        if (StrUtil.contains(path, File.separatorChar + "src" + File.separatorChar + "client" + File.separatorChar)) {
            fileFrom = FROM_CLIENT;
        } else if (StrUtil.contains(path,
                File.separatorChar + "src" + File.separatorChar + "private" + File.separatorChar)) {
            fileFrom = FROM_PRIVATE;
        } else if (StrUtil.contains(path,
                File.separatorChar + "src" + File.separatorChar + "test" + File.separatorChar)) {
            fileFrom = FROM_TEST;
        }

        Violation violation;
        String error = null;
        SeverityLevel level = null;
        for (int i = 0; i < lines.length; i++) {
            line = lines[i];
            if (comment || StrUtil.isBlank(line)) {
                continue;
            }

            line = line.trim();
            if (StrUtil.startWith(line, "//")) {
                continue;
            }
            if (StrUtil.startWith(line, "/*")) {
                if (StrUtil.endWith(line, "*/")) {
                    continue;
                }
                comment = true;
            }
            if (StrUtil.endWith(line, "*/")) {
                comment = false;
                continue;
            }

            if (StrUtil.startWith(line, "import ")) {
                line = line.substring(6, line.length() - 1).trim();
                if (StrUtil.startWith(line, "static ")) {
                    line = line.substring(6, line.indexOf('.') - 1).trim();
                }

                if (StrUtil.endWith(line, '*')
                        && !line.startsWith("java.")
                        && !line.startsWith("javax.")
                        && !line.startsWith("lombok.")
                        && !line.startsWith("sun.")
                ) {
                    violation = new Violation(i + 1
                            , lines[i].length()
                            , lines[i].length()
                            , TokenTypes.IMPORT
                            , "com.puppycrawl.tools.checkstyle.messages"
                            , "general.exception"
                            , new String[]{lines[i]}
                            , SeverityLevel.WARNING
                            , null
                            , this.getClass()
                            , "Import语句中使用*包范围导入的或者代码中使用全限定名访问的类不支持NC代码导入规范检查!(请import具体类全路径!): " + line);
                    fileContext.getViolations().add(violation);
                    continue;
                }

                if ("nccloud.framework.service.ServiceLocator".equals(line)) {
                    if (FROM_PUBLIC == fileFrom) {
                        violation = new Violation(i + 1
                                , lines[i].length()
                                , lines[i].length()
                                , TokenTypes.IMPORT
                                , "com.puppycrawl.tools.checkstyle.messages"
                                , "general.exception"
                                , new String[]{lines[i]}
                                , SeverityLevel.ERROR
                                , null
                                , this.getClass()
                                , "public包中使用nccloud.framework.service.ServiceLocator很可能出错!");
                        fileContext.getViolations().add(violation);
                        continue;
                    } else if (FROM_PRIVATE == fileFrom) {
                        violation = new Violation(i + 1
                                , lines[i].length()
                                , lines[i].length()
                                , TokenTypes.IMPORT
                                , "com.puppycrawl.tools.checkstyle.messages"
                                , "general.exception"
                                , new String[]{lines[i]}
                                , SeverityLevel.ERROR
                                , null
                                , this.getClass()
                                , "private包中不允许使用nccloud.framework.service.ServiceLocator!");
                        fileContext.getViolations().add(violation);
                        continue;
                    }
                } else if ("nc.bs.framework.common.NCLocator".equals(line)) {
                    if (FROM_CLIENT == fileFrom && NcVersionEnum.NCC.equals(ProjectNCConfigUtil.getNCVersion())) {
                        violation = new Violation(i + 1
                                , lines[i].length()
                                , lines[i].length()
                                , TokenTypes.IMPORT
                                , "com.puppycrawl.tools.checkstyle.messages"
                                , "general.exception"
                                , new String[]{lines[i]}
                                , SeverityLevel.WARNING
                                , null
                                , this.getClass()
                                , "NCC(BIP)项目中,如果非重量端开发的:client包中应该使用" +
                                "nccloud.framework.service.ServiceLocator而不是nc.bs.framework.common.NCLocator!");
                        fileContext.getViolations().add(violation);
                        continue;
                    }
                }

                //想办法找到这个类所在的范围
                Map<Integer, List<String>> froms = findClassFrom(line);
                if (froms.isEmpty()) {
                    violation = new Violation(i + 1
                            , lines[i].length()
                            , lines[i].length()
                            , TokenTypes.IMPORT
                            , "com.puppycrawl.tools.checkstyle.messages"
                            , "general.exception"
                            , new String[]{lines[i]}
                            , SeverityLevel.WARNING
                            , null
                            , this.getClass()
                            , "无法找到来源信息: " + line);
                    fileContext.getViolations().add(violation);
                    continue;
                }

                if (froms.containsKey(fileFrom) || froms.containsKey(FROM_PUBLIC)) {
                    continue;
                }

                if (FROM_PUBLIC == fileFrom) {
                    if (froms.containsKey(FROM_CLIENT)) {
                        violation = new Violation(i + 1
                                , lines[i].length()
                                , lines[i].length()
                                , TokenTypes.IMPORT
                                , "com.puppycrawl.tools.checkstyle.messages"
                                , "general.exception"
                                , new String[]{lines[i]}
                                , SeverityLevel.ERROR
                                , null
                                , this.getClass()
                                , "public中不允许导入client的类: " + line + " ,来源路径:" + buildFromInfos(froms));
                        fileContext.getViolations().add(violation);
                        continue;
                    }
                    if (froms.containsKey(FROM_PRIVATE)) {
                        violation = new Violation(i + 1
                                , lines[i].length()
                                , lines[i].length()
                                , TokenTypes.IMPORT
                                , "com.puppycrawl.tools.checkstyle.messages"
                                , "general.exception"
                                , new String[]{lines[i]}
                                , SeverityLevel.ERROR
                                , null
                                , this.getClass()
                                , "public中不允许导入private的类: " + line + " ,来源路径:" + buildFromInfos(froms));
                        fileContext.getViolations().add(violation);
                        continue;
                    }
                }

                if (FROM_CLIENT == fileFrom) {
                    if (froms.containsKey(FROM_PRIVATE)) {
                        violation = new Violation(i + 1
                                , lines[i].length()
                                , lines[i].length()
                                , TokenTypes.IMPORT
                                , "com.puppycrawl.tools.checkstyle.messages"
                                , "general.exception"
                                , new String[]{lines[i]}
                                , SeverityLevel.ERROR
                                , null
                                , this.getClass()
                                , "client中不允许导入private的类: " + line + " ,来源路径:" + buildFromInfos(froms));
                        fileContext.getViolations().add(violation);
                        continue;
                    }
                }

                if (FROM_PRIVATE == fileFrom) {
                    if (froms.containsKey(FROM_CLIENT)) {
                        violation = new Violation(i + 1
                                , lines[i].length()
                                , lines[i].length()
                                , TokenTypes.IMPORT
                                , "com.puppycrawl.tools.checkstyle.messages"
                                , "general.exception"
                                , new String[]{lines[i]}
                                , SeverityLevel.ERROR
                                , null
                                , this.getClass()
                                , "private中不允许导入client的类: " + line + " ,来源路径:" + buildFromInfos(froms));
                        fileContext.getViolations().add(violation);
                        continue;
                    }
                }
            }
        }
    }

    private String buildFromInfos(Map<Integer, List<String>> froms) {
        if (CollUtil.isEmpty(froms)) {
            return "";
        }

        StringBuilder sb = new StringBuilder(4000);

        if (CollUtil.isNotEmpty(froms.get(FROM_PUBLIC))) {
            sb.append(" Public包:").append(froms.get(FROM_PUBLIC).toString());
        } else if (CollUtil.isNotEmpty(froms.get(FROM_CLIENT))) {
            sb.append(" Client包:").append(froms.get(FROM_CLIENT).toString());
        } else if (CollUtil.isNotEmpty(froms.get(FROM_PRIVATE))) {
            sb.append(" Private包:").append(froms.get(FROM_PRIVATE).toString());
        }

        return sb.toString();
    }
}

