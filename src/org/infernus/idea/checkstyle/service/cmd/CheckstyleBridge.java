//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.infernus.idea.checkstyle.service.cmd;

import com.puppycrawl.tools.checkstyle.api.Configuration;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public final class CheckstyleBridge {
    private CheckstyleBridge() {
    }

    public static Map<String, String> messagesFrom(@NotNull Configuration source) {
        Method getMessagesMethod = null;

        try {
            getMessagesMethod = Configuration.class.getDeclaredMethod("getMessages");
        } catch (NoSuchMethodException var4) {
        }

        if (getMessagesMethod == null) {
            throw new RuntimeException("Unable to find usable getMessages method on configuration");
        } else {
            try {
                return (Map)getMessagesMethod.invoke(source);
            } catch (InvocationTargetException | IllegalAccessException var3) {
                throw new RuntimeException("Unable to invoke getMessages method on configuration", var3);
            }
        }
    }
}
