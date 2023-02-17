package org.infernus.idea.checkstyle.service;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.infernus.idea.checkstyle.model.ConfigurationLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface RulesContainer {
    @Nullable
    String filePath();

    InputStream inputStream(ClassLoader var1) throws IOException;

    @Nullable
    default String resolveAssociatedFile(@Nullable String fileName, @Nullable Module module, @NotNull ClassLoader checkstyleClassLoader) throws IOException {
        return null;
    }

    public static class ContentRulesContainer implements RulesContainer {
        private final String content;

        public ContentRulesContainer(String content) {
            this.content = content;
        }

        public String filePath() {
            return null;
        }

        public InputStream inputStream(ClassLoader checkstyleClassLoader) {
            return new ByteArrayInputStream(this.content.getBytes(StandardCharsets.UTF_8));
        }
    }

    public static class VirtualFileRulesContainer implements RulesContainer {
        private final VirtualFile virtualFile;

        public VirtualFileRulesContainer(VirtualFile virtualFile) {
            this.virtualFile = virtualFile;
        }

        public String filePath() {
            return this.virtualFile.getPath();
        }

        public InputStream inputStream(ClassLoader checkstyleClassLoader) throws IOException {
            return this.virtualFile.getInputStream();
        }
    }

    public static class ConfigurationLocationRulesContainer implements RulesContainer {
        private final ConfigurationLocation configurationLocation;

        public ConfigurationLocationRulesContainer(ConfigurationLocation configurationLocation) {
            this.configurationLocation = configurationLocation;
        }

        public String filePath() {
            return this.configurationLocation.getLocation();
        }

        public InputStream inputStream(@NotNull ClassLoader checkstyleClassLoader) throws IOException {
            return this.configurationLocation.resolve(checkstyleClassLoader);
        }

        public String resolveAssociatedFile(String fileName, Module module, @NotNull ClassLoader checkstyleClassLoader) throws IOException {
            return this.configurationLocation.resolveAssociatedFile(fileName, module, checkstyleClassLoader);
        }
    }
}
