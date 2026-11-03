/*
 * @test
 * @summary javax.security.auth.spi.LoginModule conformance: white-box structure + black-box behavior.
 * @run main LoginModuleTest
 */

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

public class LoginModuleTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static void checkMethods(Class<?> c, String[] ns) { for (String n : ns) check(hasMethod(c, n), "method " + n); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.auth.spi.LoginModule");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkMethods(c, new String[]{"initialize","login","commit","abort","logout"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final Subject subject = new Subject();
        final Map<String, Object> shared = Map.of("k", "v");
        final Map<String, Object> options = Map.of("debug", "true");

        final List<String> calls = new ArrayList<>();
        LoginModule lm = new LoginModule() {
            public void initialize(Subject s, CallbackHandler cb, Map<String, ?> sh, Map<String, ?> op) {
                calls.add("initialize");
                check(s == subject, "initialize receives subject");
                check(cb == null, "initialize receives null callback handler");
                check(sh == shared, "initialize receives shared state");
                check(op == options, "initialize receives options");
            }
            public boolean login() throws LoginException { calls.add("login"); return true; }
            public boolean commit() throws LoginException { calls.add("commit"); return true; }
            public boolean abort() throws LoginException { calls.add("abort"); return false; }
            public boolean logout() throws LoginException { calls.add("logout"); return true; }
        };

        lm.initialize(subject, null, shared, options);
        check(lm.login(), "login returns true");
        check(lm.commit(), "commit returns true");
        check(!lm.abort(), "abort returns false");
        check(lm.logout(), "logout returns true");

        check(calls.size() == 5, "all five methods invoked");
        check("initialize".equals(calls.get(0)), "initialize invoked first");
        check("login".equals(calls.get(1)), "login invoked second");
        check("commit".equals(calls.get(2)), "commit invoked third");
        check("abort".equals(calls.get(3)), "abort invoked fourth");
        check("logout".equals(calls.get(4)), "logout invoked fifth");

        // a login module may throw LoginException (checked) from the phase methods
        LoginModule throwing = new LoginModule() {
            public void initialize(Subject s, CallbackHandler cb, Map<String, ?> sh, Map<String, ?> op) { }
            public boolean login() throws LoginException { throw new LoginException("bad"); }
            public boolean commit() throws LoginException { return false; }
            public boolean abort() throws LoginException { return false; }
            public boolean logout() throws LoginException { return false; }
        };
        boolean le = false;
        try { throwing.login(); } catch (LoginException e) { le = true; }
        check(le, "login propagates LoginException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
