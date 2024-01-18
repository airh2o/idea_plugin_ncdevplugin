package com.air.nc5dev.listeners;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.DependencyScope;
import com.intellij.openapi.roots.ExternalLibraryDescriptor;
import com.intellij.openapi.roots.JavaProjectModelModifier;
import com.intellij.pom.java.LanguageLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.concurrency.Promise;

import java.util.Collection;

public class IdeaProjectModelModifier extends JavaProjectModelModifier {
    private static final Logger LOG = Logger.getInstance(IdeaProjectModelModifier.class);
    private final Project myProject;

    public IdeaProjectModelModifier(Project project) {
        this.myProject = project;
    }

    @Override
    public @Nullable Promise<Void> addExternalLibraryDependency(@NotNull Collection<? extends Module> collection,
                                                                @NotNull ExternalLibraryDescriptor externalLibraryDescriptor
            , @NotNull DependencyScope dependencyScope) {
        return null;
    }

    @Override
    public @Nullable Promise<Void> changeLanguageLevel(@NotNull Module module, @NotNull LanguageLevel languageLevel) {
        return null;
    }
}
