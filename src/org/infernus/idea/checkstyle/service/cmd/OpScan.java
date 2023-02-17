//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.infernus.idea.checkstyle.service.cmd;

import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.infernus.idea.checkstyle.checker.Problem;
import org.infernus.idea.checkstyle.checker.ScannableFile;
import org.infernus.idea.checkstyle.checks.CheckFactory;
import org.infernus.idea.checkstyle.csapi.CheckstyleInternalObject;
import org.infernus.idea.checkstyle.exception.CheckstyleVersionMixException;
import org.infernus.idea.checkstyle.service.CheckStyleAuditListener;
import org.infernus.idea.checkstyle.service.entities.CheckerWithConfig;
import org.jetbrains.annotations.NotNull;

public class OpScan implements CheckstyleCommand<Map<PsiFile, List<Problem>>> {
    private final CheckerWithConfig checkerWithConfig;
    private final List<ScannableFile> scannableFiles;
    private final boolean suppressErrors;
    private final int tabWidth;
    private final Optional<String> baseDir;

    public OpScan(@NotNull CheckstyleInternalObject checkerWithConfig, @NotNull List<ScannableFile> scannableFiles, boolean suppressErrors, int tabWidth, Optional<String> baseDir) {
        if (!(checkerWithConfig instanceof CheckerWithConfig)) {
            throw new CheckstyleVersionMixException(CheckerWithConfig.class, checkerWithConfig);
        } else {
            this.checkerWithConfig = (CheckerWithConfig)checkerWithConfig;
            this.scannableFiles = scannableFiles;
            this.suppressErrors = suppressErrors;
            this.tabWidth = tabWidth;
            this.baseDir = baseDir;
        }
    }

    @NotNull
    public Map<PsiFile, List<Problem>> execute(@NotNull Project project) throws CheckstyleException {
        ProjectUtil.setProject(project);
        return this.scannableFiles.isEmpty() ? Collections.emptyMap() : this.processAndAudit(this.filesOf(this.scannableFiles), this.createListener(this.mapFilesToElements(this.scannableFiles), project)).getProblems();
    }

    private Map<String, PsiFile> mapFilesToElements(List<ScannableFile> filesToScan) {
        Map<String, PsiFile> filePathsToElements = new HashMap();
        Iterator var3 = filesToScan.iterator();

        while(var3.hasNext()) {
            ScannableFile scannableFile = (ScannableFile)var3.next();
            filePathsToElements.put(scannableFile.getAbsolutePath(), scannableFile.getPsiFile());
        }

        return filePathsToElements;
    }

    private List<File> filesOf(List<ScannableFile> filesToScan) {
        return (List)filesToScan.stream().map(ScannableFile::getFile).collect(Collectors.toList());
    }

    private CheckStyleAuditListener processAndAudit(List<File> files, CheckStyleAuditListener auditListener) throws CheckstyleException {
        Checker checker = this.checkerWithConfig.getChecker();
        this.checkerWithConfig.getCheckerLock().lock();
        checker.addListener(auditListener);

        try {
            checker.process(files);
        } finally {
            checker.removeListener(auditListener);
            this.checkerWithConfig.getCheckerLock().unlock();
        }

        return auditListener;
    }

    private CheckStyleAuditListener createListener(Map<String, PsiFile> filesToScan, Project project) {
        return new CheckStyleAuditListener(filesToScan, this.suppressErrors, this.tabWidth, this.baseDir, CheckFactory.getChecks(project, this.checkerWithConfig));
    }
}
