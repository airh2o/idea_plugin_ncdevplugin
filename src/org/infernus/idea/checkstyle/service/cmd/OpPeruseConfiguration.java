//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.infernus.idea.checkstyle.service.cmd;

import com.intellij.openapi.project.Project;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.infernus.idea.checkstyle.csapi.CheckstyleInternalObject;
import org.infernus.idea.checkstyle.csapi.ConfigVisitor;
import org.infernus.idea.checkstyle.csapi.ConfigurationModule;
import org.infernus.idea.checkstyle.csapi.KnownTokenTypes;
import org.infernus.idea.checkstyle.exception.CheckstyleVersionMixException;
import org.infernus.idea.checkstyle.service.entities.HasCsConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OpPeruseConfiguration implements CheckstyleCommand<Void> {
    private static final String TOKENS_PROP = "tokens";
    private final Configuration configuration;
    private final ConfigVisitor visitor;

    public OpPeruseConfiguration(@NotNull CheckstyleInternalObject configuration, @NotNull ConfigVisitor visitor) {
        if (!(configuration instanceof HasCsConfig)) {
            throw new CheckstyleVersionMixException(HasCsConfig.class, configuration);
        } else {
            this.configuration = ((HasCsConfig)configuration).getConfiguration();
            this.visitor = visitor;
        }
    }

    public Void execute(@NotNull Project project) throws CheckstyleException {
        this.runVisitor(this.configuration);
        return null;
    }

    private void runVisitor(@Nullable Configuration currentConfig) throws CheckstyleException {
        if (currentConfig != null) {
            ConfigurationModule moduleInfo = this.buildModuleInfo(currentConfig);
            if (moduleInfo != null) {
                this.visitor.visit(moduleInfo);
            }

            Configuration[] var3 = currentConfig.getChildren();
            int var4 = var3.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                Configuration childConfig = var3[var5];
                this.runVisitor(childConfig);
            }

        }
    }

    @Nullable
    private ConfigurationModule buildModuleInfo(@NotNull Configuration currentConfig) throws CheckstyleException {
        String name = currentConfig.getName();
        Map<String, String> messages = CheckstyleBridge.messagesFrom(currentConfig);
        Map<String, String> properties = new HashMap();
        Set<KnownTokenTypes> knownTokenTypes = EnumSet.noneOf(KnownTokenTypes.class);
        String[] var6 = currentConfig.getAttributeNames();
        int var7 = var6.length;

        for(int var8 = 0; var8 < var7; ++var8) {
            String key = var6[var8];
            if (key != null) {
                String value = currentConfig.getAttribute(key);
                if (value != null) {
                    if ("tokens".equals(key)) {
                        knownTokenTypes = this.buildKnownTokenTypesSet(value);
                    } else {
                        properties.put(key, value);
                    }
                }
            }
        }

        ConfigurationModule result = null;
        if (name != null) {
            result = new ConfigurationModule(name, properties, (Set)knownTokenTypes);
        }

        return result;
    }

    private Set<KnownTokenTypes> buildKnownTokenTypesSet(String value) {
        Set<KnownTokenTypes> result = EnumSet.noneOf(KnownTokenTypes.class);
        String[] tokenStrings = value.split("\\s*,\\s*");
        String[] var4 = tokenStrings;
        int var5 = tokenStrings.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            String tokenStr = var4[var6];

            KnownTokenTypes knownToken;
            try {
                knownToken = KnownTokenTypes.valueOf(tokenStr);
            } catch (IllegalArgumentException var10) {
                knownToken = null;
            }

            if (knownToken != null) {
                result.add(knownToken);
            }
        }

        return result;
    }
}
