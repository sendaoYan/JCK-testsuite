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

    interface TR { void run() throws Throwable; }

    static void ct(Class<? extends Throwable> expected, TR r, String msg) {
        try { r.run(); }
        catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but nothing was thrown");
    }

    // Records every argument; the three abstract methods are the whole contract surface.
    static class RecAuth extends AuthProvider {
        Subject lastSubject; CallbackHandler lastHandler;
        int loginCalls, logoutCalls, handlerCalls;
        RecAuth() { super("JCKAuth", 1.0d, "JCK behavioral provider"); }
        RecAuth(String n, String v, String i) { super(n, v, i); }
        public void login(Subject subject, CallbackHandler handler) throws LoginException {
            loginCalls++; lastSubject = subject; lastHandler = handler;
            if (handler == null) throw new LoginException("no handler");
        }
        public void logout() throws LoginException { logoutCalls++; }
        public void setCallbackHandler(CallbackHandler handler) { handlerCalls++; lastHandler = handler; }
    }

    static void testContractDeep() throws Exception {
        Class<?> c = AuthProvider.class;

        // ---- structure: JDK 9+ has TWO protected constructors ----
        Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 2, "exactly 2 declared constructors (JDK 8 had only String,double,String)");
        for (Constructor<?> k : ctors) {
            check(Modifier.isProtected(k.getModifiers()), "constructor " + java.util.Arrays.toString(k.getParameterTypes()) + " is protected");
        }
        Method m = c.getDeclaredMethod("login", Subject.class, CallbackHandler.class);
        check(Modifier.isPublic(m.getModifiers()) && Modifier.isAbstract(m.getModifiers()), "login public abstract");
        check(m.getExceptionTypes().length == 1 && m.getExceptionTypes()[0] == LoginException.class,
                "login declares LoginException");
        m = c.getDeclaredMethod("logout");
        check(Modifier.isPublic(m.getModifiers()) && Modifier.isAbstract(m.getModifiers()), "logout public abstract");
        check(m.getExceptionTypes()[0] == LoginException.class, "logout declares LoginException");
        m = c.getDeclaredMethod("setCallbackHandler", CallbackHandler.class);
        check(Modifier.isPublic(m.getModifiers()) && Modifier.isAbstract(m.getModifiers()),
                "setCallbackHandler public abstract");
        check(m.getExceptionTypes().length == 0, "setCallbackHandler declares no checked exceptions");

        // ---- behavior: argument pass-through into the recording subclass ----
        RecAuth ap = new RecAuth();
        check(ap instanceof Provider && ap instanceof java.util.Properties,
                "AuthProvider is a Provider and therefore a Hashtable/Properties");
        check("JCKAuth".equals(ap.getName()) && "1.0".equals(ap.getVersionStr()) && ap.getVersion() == 1.0d,
                "name / getVersionStr / deprecated getVersion from double ctor");
        Subject sub = new Subject();
        CallbackHandler h = (CallbackHandler) (cbs) -> { };
        ap.login(sub, h);
        check(ap.loginCalls == 1 && ap.lastSubject == sub && ap.lastHandler == h,
                "login receives exact Subject and CallbackHandler");
        ct(LoginException.class, () -> ap.login(sub, null),
                "LoginException thrown by the provider implementation propagates");
        ap.setCallbackHandler(h);
        check(ap.handlerCalls == 1 && ap.lastHandler == h, "setCallbackHandler stores the exact handler");
        ap.logout();
        check(ap.logoutCalls == 1, "logout reaches the implementation");

        // ---- JDK 9+ String-version constructor ----
        RecAuth ap3 = new RecAuth("JCKAuth3", "3.1.4", "string ctor");
        check("3.1.4".equals(ap3.getVersionStr()) && "3.1.4".equals(ap3.get("Provider.id version")),
                "String ctor keeps the version verbatim (not collapsed to a double)");

        // ---- inherited Provider map / Properties behavior ----
        check(ap.size() == 4, "fresh provider map holds exactly the 4 Provider.id entries");
        check("JCKAuth".equals(ap.get("Provider.id name"))
                && "JCKAuth".equals(ap.getName())
                && "JCK behavioral provider".equals(ap.get("Provider.id info"))
                && RecAuth.class.getName().equals(ap.get("Provider.id className")),
                "legacy keys \"Provider.id name/version/info/className\" pre-populated (space-separated)");
        check(ap.setProperty("jck.legacy", "LegacyVal") == null, "Properties.setProperty on new key returns null");
        check("LegacyVal".equals(ap.setProperty("jck.legacy", "LegacyVal2")), "Properties.setProperty returns previous value");
        check("LegacyVal2".equals(ap.getProperty("jck.legacy")), "Properties.getProperty inherited");
        check(Security.getProvider("JCKAuth") == null,
                "constructing an AuthProvider does NOT register it with Security");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        // ---- contract deepening: ctor matrix + recording subclass + Provider.id legacy keys ----
        testContractDeep();
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
