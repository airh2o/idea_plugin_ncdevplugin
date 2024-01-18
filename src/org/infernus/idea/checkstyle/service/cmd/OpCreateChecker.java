//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.infernus.idea.checkstyle.service.cmd;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import org.infernus.idea.checkstyle.CheckstyleProjectService;
import org.infernus.idea.checkstyle.checker.CheckStyleChecker;
import com.puppycrawl.tools.checkstyle.api.NCImportCheckImpl;
import org.infernus.idea.checkstyle.csapi.TabWidthAndBaseDirProvider;
import org.infernus.idea.checkstyle.exception.CheckstyleServiceException;
import org.infernus.idea.checkstyle.exception.CheckstyleToolException;
import org.infernus.idea.checkstyle.model.ConfigurationLocation;
import org.infernus.idea.checkstyle.service.Configurations;
import org.infernus.idea.checkstyle.service.entities.CheckerWithConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class OpCreateChecker implements CheckstyleCommand<CheckStyleChecker> {
    private final Module module;
    private final ConfigurationLocation location;
    private final Map<String, String> variables;
    private final TabWidthAndBaseDirProvider configurations;
    private final ClassLoader loaderOfCheckedCode;
    private final CheckstyleProjectService checkstyleProjectService;

    public OpCreateChecker(@Nullable Module module, @NotNull ConfigurationLocation location,
                           Map<String, String> variables, @Nullable TabWidthAndBaseDirProvider configurations,
                           @NotNull ClassLoader loaderOfCheckedCode,
                           @NotNull CheckstyleProjectService checkstyleProjectService) {
        this.module = module;
        this.location = location;
        this.variables = variables;
        this.configurations = configurations;
        this.loaderOfCheckedCode = loaderOfCheckedCode;
        this.checkstyleProjectService = checkstyleProjectService;
    }

    @NotNull
    public CheckStyleChecker execute(@NotNull Project project) throws CheckstyleException {
        Configuration csConfig = new DefaultConfiguration("NC");
        Checker checker = new Checker();
        checker.setModuleClassLoader(this.getClass().getClassLoader());
        this.setClassLoader(checker, this.loaderOfCheckedCode);

        try {
            checker.configure(csConfig);
        } catch (Error var6) {
            throw new CheckstyleToolException(var6);
        }

        CheckerWithConfig cwc = new CheckerWithConfig(checker, csConfig);
        TabWidthAndBaseDirProvider configs = this.configurations != null ? this.configurations :
                new Configurations(this.module, csConfig);
        CheckStyleChecker checkStyleChecker = new CheckStyleChecker(cwc,
                ((TabWidthAndBaseDirProvider) configs).tabWidth(),
                ((TabWidthAndBaseDirProvider) configs).baseDir(),
                this.checkstyleProjectService.getCheckstyleInstance(), this.location.getNamedScope());

        Checker ck = cwc.getChecker();
        ck.addFileSetCheck(new NCImportCheckImpl());

        return checkStyleChecker;
    }

    private void setClassLoader(Checker checker, ClassLoader classLoader) {
        try {
            Method classLoaderMethod = null;

            try {
                classLoaderMethod = Checker.class.getMethod("setClassloader", ClassLoader.class);
            } catch (SecurityException | NoSuchMethodException var7) {
                try {
                    classLoaderMethod = Checker.class.getMethod("setClassLoader", ClassLoader.class);
                } catch (SecurityException | NoSuchMethodException var6) {
                }
            }

            if (classLoaderMethod != null) {
                classLoaderMethod.invoke(checker, classLoader);
            }

        } catch (InvocationTargetException | IllegalAccessException var8) {
            throw new CheckstyleServiceException("Failed to set classloader", var8);
        }
    }

    private Configuration loadConfig(@NotNull Project project) throws CheckstyleException {
        return (new OpLoadConfiguration(this.location, this.variables, this.module, this.checkstyleProjectService)).execute(project).getConfiguration();
    }
}
