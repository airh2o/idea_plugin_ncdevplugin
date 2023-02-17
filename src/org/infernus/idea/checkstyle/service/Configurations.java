package org.infernus.idea.checkstyle.service;

import com.intellij.application.options.CodeStyle;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.module.Module;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import java.util.Optional;
import org.infernus.idea.checkstyle.csapi.TabWidthAndBaseDirProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Configurations implements TabWidthAndBaseDirProvider {
    private static final String TREE_WALKER_ELEMENT = "TreeWalker";
    private static final int DEFAULT_CHECKSTYLE_TAB_SIZE = 8;
    private final Module module;
    private final Configuration rootElement;

    public Configurations(@Nullable Module module, @NotNull Configuration rootElement) {
        this.module = module;
        this.rootElement = rootElement;
    }

    public int tabWidth() {
        Configuration[] var1 = this.rootElement.getChildren();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            Configuration currentChild = var1[var3];
            if ("TreeWalker".equals(currentChild.getName())) {
                return this.intValueOrDefault(this.getAttributeOrNull(currentChild, "tabWidth"), this.defaultTabSize());
            }
        }

        return this.defaultTabSize();
    }

    private int defaultTabSize() {
        try {
            return this.currentCodeStyleSettings().getTabSize(JavaFileType.INSTANCE);
        } catch (AssertionError var2) {
            return 8;
        }
    }

    @NotNull
    CodeStyleSettings currentCodeStyleSettings() {
        return this.module != null ? CodeStyle.getSettings(this.module.getProject()) : CodeStyle.getDefaultSettings();
    }

    public Optional<String> baseDir() {
        String[] var1 = this.rootElement.getAttributeNames();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            String attributeName = var1[var3];
            if ("basedir".equals(attributeName)) {
                return Optional.ofNullable(this.getAttributeOrNull(this.rootElement, "basedir"));
            }
        }

        return Optional.empty();
    }

    private int intValueOrDefault(String value, int defaultValue) {
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException var4) {
            }
        }

        return defaultValue;
    }

    private String getAttributeOrNull(Configuration element, String attributeName) {
        try {
            return element.getAttribute(attributeName);
        } catch (CheckstyleException var4) {
            return null;
        }
    }
}
