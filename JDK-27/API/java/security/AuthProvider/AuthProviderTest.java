/*
 * @test
 * @summary java.security.AuthProvider conformance: white-box structure + black-box behavior.
 * @run main AuthProviderTest
 */

import java.lang.reflect.*;
import java.security.*;
import java.util.*;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;

public class AuthProviderTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.AuthProvider");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.security.Provider"), "superclass");
        checkMethods(c, new String[]{"login", "logout", "setCallbackHandler"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // AuthProvider is the abstract login-aware Provider; implement the three abstract methods
        class JckAuthProvider extends AuthProvider {
            volatile CallbackHandler seen;
            JckAuthProvider() { super("JCKAuth", 1.0d, "JCK behavioral provider"); }
            public void login(Subject s, CallbackHandler h) throws LoginException {
                if (h == null) throw new LoginException("no handler");
            }
            public void logout() throws LoginException { }
            public void setCallbackHandler(CallbackHandler h) { seen = h; }
        }
        JckAuthProvider ap = new JckAuthProvider();
        check(ap.getName().equals("JCKAuth"), "name");
        check(ap.getInfo().equals("JCK behavioral provider"), "info");
        check(ap.getVersionStr().equals("1.0"), "version");
        check(ap instanceof Provider, "hierarchy");
        // provider map operations still work
        ap.put("jck.service", "jck.JckImpl");
        check(ap.get("jck.service").equals("jck.JckImpl"), "put/get");
        check(ap.containsKey("jck.service"), "containsKey");
        check(ap.size() >= 1, "size");
        check(ap.toString().contains("JCKAuth"), "toString");
        check(ap.values().contains("jck.JckImpl"), "values");
        // login/logout behavior
        try {
            ap.login(new Subject(), null);
            check(false, "login null handler");
        } catch (LoginException le) {
            check("no handler".equals(le.getMessage()), "login exception");
        }
        ap.login(new Subject(), (CallbackHandler) (cbs) -> { });
        check(true, "login with handler ok");
        ap.logout();
        check(true, "logout ok");
        // setCallbackHandler stores the handler
        CallbackHandler h = (CallbackHandler) (cbs) -> { };
        ap.setCallbackHandler(h);
        check(ap.seen == h, "setCallbackHandler");
        // string constructor variant
        AuthProvider ap2 = new AuthProvider("JCK2", "2.0", "second") {
            public void login(Subject s, CallbackHandler c) throws LoginException { }
            public void logout() throws LoginException { }
            public void setCallbackHandler(CallbackHandler c) { }
        };
        check(ap2.getVersionStr().equals("2.0"), "string ctor version");
        check(!ap.getName().equals(ap2.getName()), "different names");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    static void checkInterfaces(Class<?> c, String[] names) {
        for (String n : names) check(hasInterface(c, n), "implements " + n);
    }
    static void checkMethods(Class<?> c, String[] names) {
        for (String n : names) check(hasMethod(c, n), "method " + n);
    }
    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static void checkEnumConstants(Class<?> c, String[] names) {
        Object[] cs = c.getEnumConstants();
        check(cs != null, "enum constants");
        check(cs.length == names.length, "enum constant count");
        for (String n : names) check(hasConst(cs, n), "constant " + n);
    }
    static boolean hasConst(Object[] cs, String name) {
        for (Object o : cs) if (((Enum<?>) o).name().equals(name)) return true;
        return false;
    }
}
