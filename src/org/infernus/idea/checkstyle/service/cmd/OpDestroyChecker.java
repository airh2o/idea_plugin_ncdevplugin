//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.infernus.idea.checkstyle.service.cmd;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import java.util.concurrent.TimeUnit;
import org.infernus.idea.checkstyle.csapi.CheckstyleInternalObject;
import org.infernus.idea.checkstyle.exception.CheckstyleVersionMixException;
import org.infernus.idea.checkstyle.service.entities.HasChecker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OpDestroyChecker implements CheckstyleCommand<Void> {
    private static final Logger LOG = Logger.getInstance(OpDestroyChecker.class);
    private final HasChecker hasChecker;

    public OpDestroyChecker(@NotNull CheckstyleInternalObject checker) {
        if (!(checker instanceof HasChecker)) {
            throw new CheckstyleVersionMixException(HasChecker.class, checker);
        } else {
            this.hasChecker = (HasChecker)checker;
        }
    }

    @Nullable
    public Void execute(@NotNull Project project) {
        try {
            if (this.hasChecker.getCheckerLock().tryLock(1L, TimeUnit.SECONDS)) {
                try {
                    this.hasChecker.getChecker().destroy();
                } finally {
                    this.hasChecker.getCheckerLock().unlock();
                }
            }
        } catch (InterruptedException var6) {
            LOG.debug("Checker will not be destroyed as we couldn't lock the checker", var6);
            Thread.currentThread().interrupt();
        }

        return null;
    }
}
