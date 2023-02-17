package org.infernus.idea.checkstyle.service;

import com.puppycrawl.tools.checkstyle.PropertyResolver;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SimpleResolver implements PropertyResolver {
    private final Map<String, String> properties;

    public SimpleResolver(@NotNull Map<String, String> properties) {
        this.properties = properties;
    }

    @Nullable
    public String resolve(@Nullable String name) {
        String result = null;
        if (name != null) {
            result = (String)this.properties.get(name);
        }

        return result;
    }
}
