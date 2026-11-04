/*
 * @test
 * @summary javax.security.auth.callback.CallbackHandler conformance: white-box structure + black-box behavior.
 * @run main CallbackHandlerTest
 */

import java.lang.reflect.*;
import java.util.Arrays;

public class CallbackHandlerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.auth.callback.CallbackHandler");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "handle"), "method handle");
        Method m = c.getDeclaredMethod("handle", javax.security.auth.callback.Callback[].class);
        check(m.getReturnType() == void.class, "handle returns void");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // A handler that fills in a name and a password it finds in the batch.
        final javax.security.auth.callback.NameCallback[] seenName = new javax.security.auth.callback.NameCallback[1];
        final javax.security.auth.callback.PasswordCallback[] seenPw = new javax.security.auth.callback.PasswordCallback[1];
        javax.security.auth.callback.CallbackHandler handler = callbacks -> {
            for (javax.security.auth.callback.Callback cb : callbacks) {
                if (cb instanceof javax.security.auth.callback.NameCallback) {
                    seenName[0] = (javax.security.auth.callback.NameCallback) cb;
                    seenName[0].setName("alice");
                } else if (cb instanceof javax.security.auth.callback.PasswordCallback) {
                    seenPw[0] = (javax.security.auth.callback.PasswordCallback) cb;
                    seenPw[0].setPassword("secret".toCharArray());
                } else {
                    throw new javax.security.auth.callback.UnsupportedCallbackException(cb, "Unrecognized");
                }
            }
        };

        javax.security.auth.callback.NameCallback nc = new javax.security.auth.callback.NameCallback("user: ");
        javax.security.auth.callback.PasswordCallback pc = new javax.security.auth.callback.PasswordCallback("pass: ", false);
        handler.handle(new javax.security.auth.callback.Callback[]{nc, pc});

        check(seenName[0] == nc, "handler received name callback");
        check("alice".equals(nc.getName()), "name set via handler");
        check(seenPw[0] == pc, "handler received password callback");
        check(Arrays.equals("secret".toCharArray(), pc.getPassword()), "password set via handler");

        // Unsupported callback type must throw the declared exception.
        javax.security.auth.callback.Callback unknown = new javax.security.auth.callback.Callback() { };
        boolean threw = false;
        try {
            handler.handle(new javax.security.auth.callback.Callback[]{unknown});
        } catch (javax.security.auth.callback.UnsupportedCallbackException expected) {
            threw = true;
            check(expected.getCallback() == unknown, "unsupported exception carries callback");
        }
        check(threw, "UnsupportedCallbackException thrown for unknown callback");

        // Empty array is accepted (nothing to do).
        boolean ok = false;
        try {
            handler.handle(new javax.security.auth.callback.Callback[0]);
            ok = true;
        } catch (Exception e) {
            ok = false;
        }
        check(ok, "empty callback array handled");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
