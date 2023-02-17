//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.infernus.idea.checkstyle.service.cmd;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader.IgnoredModulesOptions;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.PropertyResolver;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import org.infernus.idea.checkstyle.CheckStyleBundle;
import org.infernus.idea.checkstyle.CheckstyleProjectService;
import org.infernus.idea.checkstyle.exception.CheckstyleServiceException;
import org.infernus.idea.checkstyle.model.ConfigurationLocation;
import org.infernus.idea.checkstyle.service.IgnoringResolver;
import org.infernus.idea.checkstyle.service.RulesContainer;
import org.infernus.idea.checkstyle.service.RulesContainer.ConfigurationLocationRulesContainer;
import org.infernus.idea.checkstyle.service.RulesContainer.ContentRulesContainer;
import org.infernus.idea.checkstyle.service.RulesContainer.VirtualFileRulesContainer;
import org.infernus.idea.checkstyle.service.SimpleResolver;
import org.infernus.idea.checkstyle.service.entities.CsConfigObject;
import org.infernus.idea.checkstyle.service.entities.HasCsConfig;
import org.infernus.idea.checkstyle.util.Notifications;
import org.infernus.idea.checkstyle.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class OpLoadConfiguration implements CheckstyleCommand<HasCsConfig> {
    private static final Logger LOG = Logger.getInstance(OpLoadConfiguration.class);
    private static final String TREE_WALKER_ELEMENT = "TreeWalker";
    private static final Map<String, String> FILENAME_REPLACEMENTS = buildReplacementsMap();
    private final List<OpLoadConfiguration.ConfigurationLoaderWrapper> loaderFunctions;
    private final RulesContainer rulesContainer;
    private final PropertyResolver resolver;
    private final Module module;
    private final CheckstyleProjectService checkstyleProjectService;

    public OpLoadConfiguration(@NotNull ConfigurationLocation configurationLocation,
                               @NotNull CheckstyleProjectService checkstyleProjectService) {
        this((ConfigurationLocation) configurationLocation, (Map) null, (Module) null, checkstyleProjectService);
    }

    public OpLoadConfiguration(@NotNull ConfigurationLocation configurationLocation, Map<String, String> properties,
                               @NotNull CheckstyleProjectService checkstyleProjectService) {
        this((ConfigurationLocation) configurationLocation, properties, (Module) null, checkstyleProjectService);
    }

    public OpLoadConfiguration(ConfigurationLocation configurationLocation, Map<String, String> properties,
                               Module module, @NotNull CheckstyleProjectService checkstyleProjectService) {
        this((RulesContainer) (new ConfigurationLocationRulesContainer(configurationLocation)), properties, module,
                checkstyleProjectService);
    }

    public OpLoadConfiguration(@NotNull VirtualFile rulesFile,
                               @NotNull CheckstyleProjectService checkstyleProjectService) {
        this((VirtualFile) rulesFile, (Map) null, checkstyleProjectService);
    }

    public OpLoadConfiguration(@NotNull VirtualFile rulesFile, Map<String, String> properties,
                               @NotNull CheckstyleProjectService checkstyleProjectService) {
        this((RulesContainer) (new VirtualFileRulesContainer(rulesFile)), properties, (Module) null,
                checkstyleProjectService);
    }

    public OpLoadConfiguration(@NotNull String fileContent,
                               @NotNull CheckstyleProjectService checkstyleProjectService) {
        this((RulesContainer) (new ContentRulesContainer(fileContent)), (Map) null, (Module) null,
                checkstyleProjectService);
    }

    private OpLoadConfiguration(RulesContainer rulesContainer, Map<String, String> properties, Module module,
                                CheckstyleProjectService checkstyleProjectService) {
        this.loaderFunctions = Arrays.asList(this::loadConfigurationForCheckstylePre825,
                this::loadConfigurationForBrokenCheckstyles, this::loadConfigurationForCheckstyle825AndAbove);
        this.rulesContainer = rulesContainer;
        this.module = module;
        this.checkstyleProjectService = checkstyleProjectService;
        if (properties != null) {
            this.resolver = new SimpleResolver(properties);
        } else {
            this.resolver = new IgnoringResolver();
        }

    }

    private static Map<String, String> buildReplacementsMap() {
        return Map.of("RegexpHeader", "headerFile", "com.puppycrawl.tools.checkstyle.checks.RegexpHeaderCheck",
                "headerFile", "Header", "headerFile", "com.puppycrawl.tools.checkstyle.checks.header.HeaderCheck",
                "headerFile", "SuppressionFilter", "file", "com.puppycrawl.tools.checkstyle.filters" +
                        ".SuppressionFilter", "file", "SuppressionXpathFilter", "file", "com.puppycrawl.tools" +
                        ".checkstyle.filters.SuppressionXpathFilter", "file", "ImportControl", "file", "com" +
                        ".puppycrawl.tools.checkstyle.checks.imports.ImportControlCheck", "file");
    }

    public HasCsConfig execute(@NotNull Project currentProject) throws CheckstyleException {
        try {
            InputStream is = this.rulesContainer.inputStream(this.checkstyleClassLoader());

            CsConfigObject var4;
            try {
                Configuration configuration = this.callLoadConfiguration(is);
                if (configuration == null) {
                    throw new CheckstyleException("Couldn't find root module in " + this.rulesContainer.filePath());
                }

                this.resolveFilePaths(currentProject, configuration);
                var4 = new CsConfigObject(configuration);
            } catch (Throwable var6) {
                if (is != null) {
                    try {
                        is.close();
                    } catch (Throwable var5) {
                        var6.addSuppressed(var5);
                    }
                }

                throw var6;
            }

            if (is != null) {
                is.close();
            }

            return var4;
        } catch (IOException var7) {
            throw new CheckstyleException("Error loading file", var7);
        }
    }

    Configuration callLoadConfiguration(InputStream inputStream) {
        Iterator var2 = this.loaderFunctions.iterator();

        while (var2.hasNext()) {
            OpLoadConfiguration.ConfigurationLoaderWrapper loaderFunction =
                    (OpLoadConfiguration.ConfigurationLoaderWrapper) var2.next();

            try {
                return loaderFunction.loadConfiguration(inputStream);
            } catch (NoSuchMethodException var5) {
            } catch (InvocationTargetException | IllegalAccessException var6) {
                throw new CheckstyleServiceException("internal error - Failed to call " + ConfigurationLoader.class.getName() + ".loadConfiguration()", var6);
            }
        }

        throw new CheckstyleServiceException("internal error - Could not call " + ConfigurationLoader.class.getName() + ".loadConfiguration() because the method was not found. New Checkstyle runtime?");
    }

    private Configuration loadConfigurationForCheckstylePre825(InputStream inputStream) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        Method method = ConfigurationLoader.class.getMethod("loadConfiguration", InputSource.class,
                PropertyResolver.class, Boolean.TYPE);
        return (Configuration) method.invoke((Object) null, new InputSource(inputStream), this.resolver, false);
    }

    private Configuration loadConfigurationForBrokenCheckstyles(InputStream inputStream) throws NoSuchMethodException
            , IllegalAccessException, InvocationTargetException {
        Method method = ConfigurationLoader.class.getMethod("loadConfiguration", InputStream.class,
                PropertyResolver.class, Boolean.TYPE);
        return (Configuration) method.invoke((Object) null, inputStream, this.resolver, false);
    }

    private Configuration loadConfigurationForCheckstyle825AndAbove(InputStream inputStream) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Method method = ConfigurationLoader.class.getMethod("loadConfiguration", InputSource.class,
                PropertyResolver.class, IgnoredModulesOptions.class);
        return (Configuration) method.invoke((Object) null, new InputSource(inputStream), this.resolver,
                IgnoredModulesOptions.EXECUTE);
    }

    void resolveFilePaths(Project project, @NotNull Configuration rootElement) {
        if (!(rootElement instanceof DefaultConfiguration)) {
            LOG.warn("Root element is of unknown class: " + rootElement.getClass().getName());
        } else {
            Configuration[] var3 = rootElement.getChildren();
            int var4 = var3.length;

            for (int var5 = 0; var5 < var4; ++var5) {
                Configuration currentChild = var3[var5];
                if (FILENAME_REPLACEMENTS.containsKey(currentChild.getName())) {
                    this.checkFilenameForProperty(project, (DefaultConfiguration) rootElement, currentChild,
                            (String) FILENAME_REPLACEMENTS.get(currentChild.getName()));
                } else if ("TreeWalker".equals(currentChild.getName())) {
                    this.resolveFilePaths(project, currentChild);
                }
            }

        }
    }

    private void checkFilenameForProperty(Project project, DefaultConfiguration configRoot,
                                          Configuration configModule, String propertyName) {
        String fileName = this.getAttributeOrNull(configModule, propertyName);
        if (!Strings.isBlank(fileName)) {
            try {
                this.resolveAndUpdateFile(project, configRoot, configModule, propertyName, fileName);
            } catch (IOException var7) {
                Notifications.showError(project, CheckStyleBundle.message("checkstyle.checker-failed",
                        new Object[]{var7.getMessage()}));
            }
        }

    }

    private void resolveAndUpdateFile(Project project, DefaultConfiguration configRoot, Configuration configModule,
                                      String propertyName, String fileName) throws IOException {
        String resolvedFile = this.rulesContainer.resolveAssociatedFile(fileName, this.module,
                this.checkstyleClassLoader());
        if (resolvedFile == null || !resolvedFile.equals(fileName)) {
            configRoot.removeChild(configModule);
            if (resolvedFile != null) {
                configRoot.addChild(this.elementWithUpdatedFile(resolvedFile, configModule, propertyName));
            } else if (this.isNotOptional(configModule)) {
                Notifications.showWarning(project, CheckStyleBundle.message(String.format("checkstyle.not-found.%s",
                        configModule.getName()), new Object[0]));
            }
        }

    }

    @NotNull
    private ClassLoader checkstyleClassLoader() {
        return this.checkstyleProjectService.underlyingClassLoader();
    }

    private boolean isNotOptional(Configuration configModule) {
        return !"true".equalsIgnoreCase(this.getAttributeOrNull(configModule, "optional"));
    }

    private String getAttributeOrNull(Configuration element, String attributeName) {
        try {
            return element.getAttribute(attributeName);
        } catch (CheckstyleException var4) {
            return null;
        }
    }

    private DefaultConfiguration elementWithUpdatedFile(@NotNull String filename,
                                                        @NotNull Configuration originalElement,
                                                        @NotNull String propertyName) {
        DefaultConfiguration target = new DefaultConfiguration(originalElement.getName());
        this.copyChildren(originalElement, target);
        this.copyMessages(originalElement, target);
        this.copyAttributes(originalElement, propertyName, target);
        target.addAttribute(propertyName, filename);
        return target;
    }

    private void copyAttributes(@NotNull Configuration source, @NotNull String propertyName,
                                @NotNull DefaultConfiguration target) {
        if (source.getAttributeNames() != null) {
            String[] var4 = source.getAttributeNames();
            int var5 = var4.length;

            for (int var6 = 0; var6 < var5; ++var6) {
                String attributeName = var4[var6];
                if (!attributeName.equals(propertyName)) {
                    target.addAttribute(attributeName, this.getAttributeOrNull(source, attributeName));
                }
            }
        }

    }

    private void copyMessages(@NotNull Configuration source, @NotNull DefaultConfiguration target) {
        Map<String, String> messages = CheckstyleBridge.messagesFrom(source);
        if (messages != null) {
            Iterator var4 = messages.keySet().iterator();

            while (var4.hasNext()) {
                String messageKey = (String) var4.next();
                target.addMessage(messageKey, (String) messages.get(messageKey));
            }
        }

    }

    private void copyChildren(@NotNull Configuration source, @NotNull DefaultConfiguration target) {
        if (source.getChildren() != null) {
            Configuration[] var3 = source.getChildren();
            int var4 = var3.length;

            for (int var5 = 0; var5 < var4; ++var5) {
                Configuration child = var3[var5];
                target.addChild(child);
            }
        }

    }

    @FunctionalInterface
    public static interface ConfigurationLoaderWrapper {
        Configuration loadConfiguration(InputStream var1) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException;
    }
}
