//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.infernus.idea.checkstyle.service.entities;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CheckerWithConfig implements HasChecker, HasCsConfig {
    private final Checker checker;
    private final Lock lock = new ReentrantLock();
    private final Configuration configuration;

    public CheckerWithConfig(Checker checker, Configuration configuration) {
        this.checker = checker;
        this.configuration = configuration;
    }

    public Checker getChecker() {
        return this.checker;
    }

    public Lock getCheckerLock() {
        return this.lock;
    }

    public Configuration getConfiguration() {
        return this.configuration;
    }
}
