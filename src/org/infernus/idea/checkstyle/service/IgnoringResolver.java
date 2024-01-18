package org.infernus.idea.checkstyle.service;

import com.puppycrawl.tools.checkstyle.PropertyResolver;

public class IgnoringResolver implements PropertyResolver {
    public IgnoringResolver() {
    }

    public String resolve(String pName) {
        return "";
    }
}
