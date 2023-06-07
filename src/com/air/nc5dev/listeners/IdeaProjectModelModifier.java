package com.air.nc5dev.listeners;

import com.intellij.codeInsight.daemon.impl.quickfix.LocateLibraryDialog;
import com.intellij.codeInsight.daemon.impl.quickfix.OrderEntryFix;
import com.intellij.ide.JavaUiBundle;
import com.intellij.jarRepository.JarRepositoryManager;
import com.intellij.jarRepository.RepositoryAttachDialog;
import com.intellij.jarRepository.RepositoryAttachDialog.Mode;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ex.JavaSdkUtil;
import com.intellij.openapi.roots.DependencyScope;
import com.intellij.openapi.roots.ExternalLibraryDescriptor;
import com.intellij.openapi.roots.JavaProjectModelModifier;
import com.intellij.openapi.roots.LanguageLevelModuleExtension;
import com.intellij.openapi.roots.LanguageLevelModuleExtensionImpl;
import com.intellij.openapi.roots.LanguageLevelProjectExtension;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ex.ProjectRootManagerEx;
import com.intellij.openapi.roots.impl.OrderEntryUtil;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.roots.libraries.LibraryUtil;
import com.intellij.openapi.roots.libraries.Library.ModifiableModel;
import com.intellij.openapi.roots.libraries.ui.OrderRoot;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.EmptyRunnable;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.util.PathUtil;
import com.intellij.util.containers.ContainerUtil;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.concurrency.Promise;
import org.jetbrains.concurrency.Promises;
import org.jetbrains.idea.maven.utils.library.RepositoryLibraryProperties;

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
