//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.infernus.idea.checkstyle.service.entities;

import com.puppycrawl.tools.checkstyle.Checker;
import java.util.concurrent.locks.Lock;
import org.infernus.idea.checkstyle.csapi.CheckstyleInternalObject;

public interface HasChecker extends CheckstyleInternalObject {
    Checker getChecker();

    Lock getCheckerLock();
}
