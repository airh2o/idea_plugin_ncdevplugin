package org.infernus.idea.checkstyle.service;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.infernus.idea.checkstyle.exception.CheckStylePluginException;
import org.infernus.idea.checkstyle.exception.CheckStylePluginParseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExceptionWrapper {
    private static final Set<String> NON_JVM_PARSE_EXCEPTIONS = new HashSet(Arrays.asList("antlr.RecognitionException", "antlr.TokenStreamException", "org.antlr.v4.runtime.RecognitionException"));
    private final Set<Class<? extends Throwable>> parseExceptions = new HashSet(Arrays.asList(NullPointerException.class, ArrayIndexOutOfBoundsException.class, StringIndexOutOfBoundsException.class, IllegalStateException.class, ClassCastException.class));

    public ExceptionWrapper() {
        NON_JVM_PARSE_EXCEPTIONS.forEach((exceptionName) -> {
            try {
                Class<? extends Throwable> aClass = (Class<? extends Throwable>) Class.forName(exceptionName);
                if (Throwable.class.isAssignableFrom(aClass)) {
                    this.parseExceptions.add(aClass);
                }
            } catch (ClassNotFoundException var3) {
            }

        });
    }

    public CheckStylePluginException wrap(@Nullable String message, @NotNull Throwable error) {
        Throwable root = this.rootOrCheckStyleException(error);
        Optional var10000 = Optional.ofNullable(message);
        Objects.requireNonNull(root);
        String exMessage = (String)var10000.orElseGet(root::getMessage);
        return (CheckStylePluginException)(this.isParseException(root) ? new CheckStylePluginParseException(exMessage, root) : new CheckStylePluginException(exMessage, root));
    }

    private Throwable rootOrCheckStyleException(Throwable error) {
        Throwable root;
        for(root = error; root.getCause() != null && this.notBaseCheckstyleException(root); root = root.getCause()) {
        }

        return root;
    }

    private boolean notBaseCheckstyleException(Throwable root) {
        return !(root instanceof CheckstyleException) || root.getCause() instanceof CheckstyleException;
    }

    private boolean isParseException(Throwable throwable) {
        if (throwable instanceof CheckstyleException && throwable.getCause() != null) {
            Class<? extends Throwable> causeClass = throwable.getCause().getClass();
            Iterator var3 = this.parseExceptions.iterator();

            Class parseExceptionType;
            do {
                if (!var3.hasNext()) {
                    return this.isAntlrException(causeClass);
                }

                parseExceptionType = (Class)var3.next();
            } while(!parseExceptionType.isAssignableFrom(causeClass));

            return true;
        } else {
            return false;
        }
    }

    private boolean isAntlrException(Class<? extends Throwable> causeClass) {
        String causeClassPackage = causeClass.getPackage().getName();
        return causeClassPackage.startsWith("antlr.") || causeClassPackage.startsWith("org.antlr.");
    }
}
