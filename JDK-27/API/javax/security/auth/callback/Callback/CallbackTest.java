/*
 * @test
 * @summary javax.security.auth.callback.Callback conformance: white-box structure + black-box behavior.
 * @run main CallbackTest
 */

import java.lang.reflect.*;

public class CallbackTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.auth.callback.Callback");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getDeclaredMethods().length == 0, "no declared methods");
        check(c.getDeclaredFields().length == 0, "no declared fields");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Callback is a pure marker interface: any implementor must be assignable.
        Class<?> cbClass = Class.forName("javax.security.auth.callback.Callback");
        javax.security.auth.callback.Callback anon = new javax.security.auth.callback.Callback() { };
        check(cbClass.isInstance(anon), "anonymous impl is instanceof Callback");

        // The standard concrete callbacks all implement Callback.
        javax.security.auth.callback.Callback[] callbacks = {
            new javax.security.auth.callback.NameCallback("p"),
            new javax.security.auth.callback.PasswordCallback("p", false),
            new javax.security.auth.callback.TextInputCallback("p"),
            new javax.security.auth.callback.TextOutputCallback(0, "m"),
            new javax.security.auth.callback.LanguageCallback(),
            new javax.security.auth.callback.ChoiceCallback("p", new String[]{"a", "b"}, 0, false),
            new javax.security.auth.callback.ConfirmationCallback(0, 0, 0)
        };
        for (javax.security.auth.callback.Callback cb : callbacks) {
            check(cbClass.isInstance(cb), cb.getClass().getSimpleName() + " implements Callback");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
