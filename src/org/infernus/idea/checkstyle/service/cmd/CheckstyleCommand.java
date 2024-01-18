//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.infernus.idea.checkstyle.service.cmd;

import com.intellij.openapi.project.Project;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import org.jetbrains.annotations.NotNull;

public interface CheckstyleCommand<R> {
    R execute(@NotNull Project var1) throws CheckstyleException;
}
