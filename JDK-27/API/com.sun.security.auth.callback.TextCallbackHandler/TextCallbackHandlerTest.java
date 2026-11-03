/*
 * @test
 * @summary com.sun.security.auth.callback.TextCallbackHandler conformance: white-box structure + black-box behavior.
 * @run main TextCallbackHandlerTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.security.auth.callback.*;

public class TextCallbackHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }
    static Set<String> interfaces(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static Class<?> load(String fqcn) throws Exception { return Class.forName(fqcn); }

    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.security.auth.callback.TextCallbackHandler");
        check(!c.isInterface(), "not an interface");
        check(interfaces(c).contains("javax.security.auth.callback.CallbackHandler"), "implements CallbackHandler");
        check(methodNames(c).contains("handle"), "handle method");
    }

    static void testBlackBox() throws Exception {
        com.sun.security.auth.callback.TextCallbackHandler h = new com.sun.security.auth.callback.TextCallbackHandler();
        check(h instanceof CallbackHandler, "instanceof CallbackHandler");

        // An empty callback array is a valid no-op.
        h.handle(new Callback[0]);

        // TextOutputCallback only writes to the standard error stream and never
        // blocks waiting for console input, so it is safe to exercise here.
        h.handle(new Callback[]{
            new TextOutputCallback(TextOutputCallback.INFORMATION, "jck-info"),
            new TextOutputCallback(TextOutputCallback.WARNING, "jck-warning"),
            new TextOutputCallback(TextOutputCallback.ERROR, "jck-error"),
        });

        // A fresh handler can process another batch (no shared mutable state).
        com.sun.security.auth.callback.TextCallbackHandler h2 = new com.sun.security.auth.callback.TextCallbackHandler();
        h2.handle(new Callback[]{ new TextOutputCallback(TextOutputCallback.INFORMATION, "jck-again") });

        // Unrecognized callback types must be rejected.
        boolean unsupported = false;
        try {
            h.handle(new Callback[]{ new LanguageCallback() });
        } catch (UnsupportedCallbackException e) {
            unsupported = true;
        }
        check(unsupported, "LanguageCallback -> UnsupportedCallbackException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
