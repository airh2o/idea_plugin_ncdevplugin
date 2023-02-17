package org.infernus.idea.checkstyle.service;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.infernus.idea.checkstyle.CheckstyleProjectService;
import org.infernus.idea.checkstyle.checker.CheckStyleChecker;
import org.infernus.idea.checkstyle.checker.Problem;
import org.infernus.idea.checkstyle.checker.ScannableFile;
import org.infernus.idea.checkstyle.csapi.CheckstyleActions;
import org.infernus.idea.checkstyle.csapi.CheckstyleInternalObject;
import org.infernus.idea.checkstyle.csapi.ConfigVisitor;
import org.infernus.idea.checkstyle.csapi.TabWidthAndBaseDirProvider;
import org.infernus.idea.checkstyle.exception.CheckStylePluginException;
import org.infernus.idea.checkstyle.exception.CheckStylePluginParseException;
import org.infernus.idea.checkstyle.exception.CheckstyleServiceException;
import org.infernus.idea.checkstyle.exception.CheckstyleToolException;
import org.infernus.idea.checkstyle.model.ConfigurationLocation;
import org.infernus.idea.checkstyle.service.cmd.CheckstyleCommand;
import org.infernus.idea.checkstyle.service.cmd.OpCreateChecker;
import org.infernus.idea.checkstyle.service.cmd.OpDestroyChecker;
import org.infernus.idea.checkstyle.service.cmd.OpLoadConfiguration;
import org.infernus.idea.checkstyle.service.cmd.OpPeruseConfiguration;
import org.infernus.idea.checkstyle.service.cmd.OpScan;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CheckstyleActionsImpl implements CheckstyleActions {
    private final Project project;
    private final CheckstyleProjectService checkstyleProjectService;

    public CheckstyleActionsImpl(@NotNull Project project, @NotNull CheckstyleProjectService checkstyleProjectService) {
        this.project = project;
        this.checkstyleProjectService = checkstyleProjectService;
    }

    public CheckStyleChecker createChecker(@Nullable Module module, @NotNull ConfigurationLocation location, Map<String, String> properties, @NotNull ClassLoader loaderOfCheckedCode) {
        return this.createChecker(module, location, properties, (TabWidthAndBaseDirProvider)null, loaderOfCheckedCode);
    }

    public CheckStyleChecker createChecker(@Nullable Module module, @NotNull ConfigurationLocation location, Map<String, String> properties, @Nullable TabWidthAndBaseDirProvider configurations, @NotNull ClassLoader loaderOfCheckedCode) {
        return (CheckStyleChecker)this.executeCommand(new OpCreateChecker(module, location, properties, configurations, loaderOfCheckedCode, this.checkstyleProjectService));
    }

    public void destroyChecker(@NotNull CheckstyleInternalObject checkerWithConfig) {
        this.executeCommand(new OpDestroyChecker(checkerWithConfig));
    }

    public Map<PsiFile, List<Problem>> scan(@NotNull CheckstyleInternalObject checkerWithConfig, @NotNull List<ScannableFile> scannableFiles, boolean isSuppressingErrors, int tabWidth, Optional<String> baseDir) {
        return (Map)this.executeCommand(new OpScan(checkerWithConfig, scannableFiles, isSuppressingErrors, tabWidth, baseDir));
    }

    public CheckstyleInternalObject loadConfiguration(@NotNull ConfigurationLocation inputFile, boolean ignoreVariables, @Nullable Map<String, String> variables) {
        OpLoadConfiguration cmd;
        if (ignoreVariables) {
            cmd = new OpLoadConfiguration(inputFile, this.checkstyleProjectService);
        } else {
            cmd = new OpLoadConfiguration(inputFile, variables, this.checkstyleProjectService);
        }

        return (CheckstyleInternalObject)this.executeCommand(cmd);
    }

    public CheckstyleInternalObject loadConfiguration(@NotNull ConfigurationLocation inputFile, @Nullable Map<String, String> variables, @Nullable Module module) {
        return (CheckstyleInternalObject)this.executeCommand(new OpLoadConfiguration(inputFile, variables, module, this.checkstyleProjectService));
    }

    public CheckstyleInternalObject loadConfiguration(@NotNull VirtualFile inputFile, boolean ignoreVariables, @Nullable Map<String, String> variables) {
        OpLoadConfiguration cmd;
        if (ignoreVariables) {
            cmd = new OpLoadConfiguration(inputFile, this.checkstyleProjectService);
        } else {
            cmd = new OpLoadConfiguration(inputFile, variables, this.checkstyleProjectService);
        }

        return (CheckstyleInternalObject)this.executeCommand(cmd);
    }

    public CheckstyleInternalObject loadConfiguration(@NotNull String pXmlConfig) {
        return (CheckstyleInternalObject)this.executeCommand(new OpLoadConfiguration(pXmlConfig, this.checkstyleProjectService));
    }

    public void peruseConfiguration(@NotNull CheckstyleInternalObject configuration, @NotNull ConfigVisitor visitor) {
        this.executeCommand(new OpPeruseConfiguration(configuration, visitor));
    }

    private <R> R executeCommand(@NotNull CheckstyleCommand<R> command) {
        CheckStylePluginException wrapped;
        try {
            R result = command.execute(this.project);
            return result;
        } catch (CheckstyleException var6) {
            wrapped = (new ExceptionWrapper()).wrap((String)null, var6);
            if (wrapped instanceof CheckStylePluginParseException) {
                throw wrapped;
            } else {
                throw new CheckstyleToolException(var6);
            }
        } catch (ExceptionInInitializerError | RuntimeException var7) {
            wrapped = (new ExceptionWrapper()).wrap((String)null, var7);
            if (wrapped instanceof CheckStylePluginParseException) {
                throw wrapped;
            } else {
                CheckstyleException csCause = this.digUpCheckstyleCause(var7);
                if (csCause != null) {
                    throw new CheckstyleToolException(csCause);
                } else {
                    throw new CheckstyleServiceException("Error executing command '" + command.getClass().getSimpleName() + "': " + var7.getMessage(), var7);
                }
            }
        }
    }

    @Nullable
    private CheckstyleException digUpCheckstyleCause(@Nullable Throwable pThrowable) {
        CheckstyleException result = null;
        if (pThrowable != null) {
            if (pThrowable.getCause() instanceof CheckstyleException) {
                result = (CheckstyleException)pThrowable.getCause();
            } else {
                result = this.digUpCheckstyleCause(pThrowable.getCause());
            }
        }

        return result;
    }
}
