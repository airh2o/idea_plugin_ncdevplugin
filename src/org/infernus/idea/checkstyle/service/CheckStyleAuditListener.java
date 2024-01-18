package org.infernus.idea.checkstyle.service;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiFile;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.infernus.idea.checkstyle.checker.Problem;
import org.infernus.idea.checkstyle.checks.Check;
import org.infernus.idea.checkstyle.csapi.Issue;
import org.infernus.idea.checkstyle.csapi.ProcessResultsThread;
import org.infernus.idea.checkstyle.csapi.SeverityLevel;
import org.jetbrains.annotations.NotNull;

public class CheckStyleAuditListener implements AuditListener {
    private static final Logger LOG = Logger.getInstance(CheckStyleAuditListener.class);
    private final boolean suppressErrors;
    private final List<Check> checks;
    private final int tabWidth;
    private final Optional<String> baseDir;
    private final Map<String, PsiFile> fileNamesToPsiFiles;
    private final List<Issue> errors = Collections.synchronizedList(new ArrayList());
    private Map<PsiFile, List<Problem>> problems = Collections.emptyMap();

    public CheckStyleAuditListener(@NotNull Map<String, PsiFile> fileNamesToPsiFiles, boolean suppressErrors, int tabWidth, @NotNull Optional<String> baseDir, @NotNull List<Check> checks) {
        this.fileNamesToPsiFiles = new HashMap(fileNamesToPsiFiles);
        this.checks = checks;
        this.suppressErrors = suppressErrors;
        this.tabWidth = tabWidth;
        this.baseDir = baseDir;
    }

    public void auditStarted(AuditEvent auditEvent) {
        this.errors.clear();
    }

    public void auditFinished(AuditEvent auditEvent) {
        ArrayList errorsCopy;
        synchronized(this.errors) {
            errorsCopy = new ArrayList(this.errors);
        }

        ProcessResultsThread findThread = new ProcessResultsThread(this.suppressErrors, this.checks, this.tabWidth, this.baseDir, errorsCopy, this.fileNamesToPsiFiles);
        Application application = ApplicationManager.getApplication();
        if (application != null) {
            ReadAction.run(findThread);
            this.problems = findThread.getProblems();
        }

    }

    public void fileStarted(AuditEvent auditEvent) {
    }

    public void fileFinished(AuditEvent auditEvent) {
    }

    public void addError(AuditEvent auditEvent) {
        this.errors.add(this.toIssue(auditEvent));
    }

    public void addException(AuditEvent auditEvent, Throwable throwable) {
        LOG.warn("Exception during CheckStyle execution", throwable);
        this.errors.add(this.toIssue(auditEvent));
    }

    @NotNull
    public Map<PsiFile, List<Problem>> getProblems() {
        return this.problems;
    }

    private Issue toIssue(AuditEvent auditEvent) {
        String msg = auditEvent.getMessage();
        SeverityLevel level = this.readSeverityLevel(auditEvent.getSeverityLevel());
        return new Issue(auditEvent.getFileName(), auditEvent.getLine(), auditEvent.getColumn(), msg, level, auditEvent.getSourceName());
    }

    private SeverityLevel readSeverityLevel(com.puppycrawl.tools.checkstyle.api.SeverityLevel severityLevel) {
        SeverityLevel result = null;
        if (severityLevel != null) {
            switch(severityLevel) {
            case ERROR:
                result = SeverityLevel.Error;
                break;
            case WARNING:
                result = SeverityLevel.Warning;
                break;
            case INFO:
                result = SeverityLevel.Info;
                break;
            case IGNORE:
            default:
                result = SeverityLevel.Ignore;
            }
        }

        return result;
    }
}
