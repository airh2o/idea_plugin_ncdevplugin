//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.infernus.idea.checkstyle.service.entities;

import com.puppycrawl.tools.checkstyle.api.Configuration;

public class CsConfigObject implements HasCsConfig {
    private final Configuration configuration;

    public CsConfigObject(Configuration configuration) {
        this.configuration = configuration;
    }

    public Configuration getConfiguration() {
        return this.configuration;
    }
}
