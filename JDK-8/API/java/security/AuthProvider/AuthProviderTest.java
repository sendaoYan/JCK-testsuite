/*
 * @test
 * @summary java.security.AuthProvider conformance: abstract class hierarchy and key methods.
 * @run main AuthProviderTest
 */

public class AuthProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
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
    static class RecAuth extends java.security.AuthProvider {
        javax.security.auth.Subject lastSubject; javax.security.auth.callback.CallbackHandler lastHandler;
        int loginCalls, logoutCalls, handlerCalls;
        RecAuth() { super("JCKAuth", 1.0d, "JCK behavioral provider"); }
        public void login(javax.security.auth.Subject subject, javax.security.auth.callback.CallbackHandler handler)
                throws javax.security.auth.login.LoginException {
            loginCalls++; lastSubject = subject; lastHandler = handler;
            if (handler == null) throw new javax.security.auth.login.LoginException("no handler");
        }
        public void logout() throws javax.security.auth.login.LoginException { logoutCalls++; }
        public void setCallbackHandler(javax.security.auth.callback.CallbackHandler handler) {
            handlerCalls++; lastHandler = handler;
        }
    }

    static void testContractDeep() throws Exception {
        Class<?> c = java.security.AuthProvider.class;

        // ---- structure: protected (String,double,String) is the ONLY JDK-8 constructor ----
        java.lang.reflect.Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1, "exactly 1 declared constructor (JDK 9+ adds a String-version one)");
        check(java.lang.reflect.Modifier.isProtected(ctors[0].getModifiers()),
                "constructor is protected (subclassing only)");
        java.lang.reflect.Method m = c.getDeclaredMethod("login",
                javax.security.auth.Subject.class, javax.security.auth.callback.CallbackHandler.class);
        check(java.lang.reflect.Modifier.isPublic(m.getModifiers())
                && java.lang.reflect.Modifier.isAbstract(m.getModifiers()), "login public abstract");
        check(m.getExceptionTypes().length == 1
                && m.getExceptionTypes()[0] == javax.security.auth.login.LoginException.class,
                "login declares LoginException");
        m = c.getDeclaredMethod("logout");
        check(java.lang.reflect.Modifier.isPublic(m.getModifiers())
                && java.lang.reflect.Modifier.isAbstract(m.getModifiers()), "logout public abstract");
        check(m.getExceptionTypes()[0] == javax.security.auth.login.LoginException.class, "logout declares LoginException");
        m = c.getDeclaredMethod("setCallbackHandler", javax.security.auth.callback.CallbackHandler.class);
        check(java.lang.reflect.Modifier.isPublic(m.getModifiers())
                && java.lang.reflect.Modifier.isAbstract(m.getModifiers()), "setCallbackHandler public abstract");
        check(m.getExceptionTypes().length == 0, "setCallbackHandler declares no checked exceptions");

        // ---- behavior: argument pass-through into the recording subclass ----
        RecAuth ap = new RecAuth();
        check(ap instanceof java.security.Provider && ap instanceof java.util.Properties,
                "AuthProvider is a Provider and therefore a Hashtable/Properties");
        check("JCKAuth".equals(ap.getName()), "getName from ctor");
        check("JCK behavioral provider".equals(ap.getInfo()), "getInfo from ctor");
        check(ap.getVersion() == 1.0d, "double version (JDK 8 surface; JDK 9+ adds getVersionStr)");
        javax.security.auth.Subject sub = new javax.security.auth.Subject();
        javax.security.auth.callback.CallbackHandler h =
                (javax.security.auth.callback.CallbackHandler) (cbs) -> { };
        ap.login(sub, h);
        check(ap.loginCalls == 1 && ap.lastSubject == sub && ap.lastHandler == h,
                "login receives exact Subject and CallbackHandler");
        ct(javax.security.auth.login.LoginException.class, () -> ap.login(sub, null),
                "LoginException thrown by the provider implementation propagates");
        check(ap.loginCalls == 2, "failing login still reached the implementation");
        ap.setCallbackHandler(h);
        check(ap.handlerCalls == 1 && ap.lastHandler == h, "setCallbackHandler stores the exact handler");
        ap.logout();
        check(ap.logoutCalls == 1, "logout reaches the implementation");

        // ---- inherited Provider map / Properties behavior ----
        ap.put("jck.service", "jck.JckImpl");
        check("jck.JckImpl".equals(ap.get("jck.service")), "put/get on provider map");
        check(ap.containsKey("jck.service"), "containsKey");
        check(ap.setProperty("jck.legacy", "LegacyVal") == null, "Properties.setProperty on new key returns null");
        check("LegacyVal".equals(ap.setProperty("jck.legacy", "LegacyVal2")), "Properties.setProperty returns previous value");
        check("LegacyVal2".equals(ap.getProperty("jck.legacy")), "Properties.getProperty inherited");
        check("JCKAuth".equals(ap.get("Provider.id name"))
                && "1.0".equals(ap.get("Provider.id version"))
                && "JCK behavioral provider".equals(ap.get("Provider.id info"))
                && RecAuth.class.getName().equals(ap.get("Provider.id className")),
                "legacy keys \"Provider.id name/version/info/className\" pre-populated (space-separated)");
        check(ap.toString().contains("JCKAuth"), "toString mentions provider name");
        // not registered anywhere: Security knows nothing about it
        check(java.security.Security.getProvider("JCKAuth") == null,
                "constructing an AuthProvider does NOT register it with Security");
        RecAuth fresh = new RecAuth();
        check(fresh.size() == 4, "fresh provider map holds exactly the 4 Provider.id entries");
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.AuthProvider");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == Class.forName("java.security.Provider"), "extends java.security.Provider");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("login"), "declares login");
        check(ms.contains("logout"), "declares logout");
        check(ms.contains("setCallbackHandler"), "declares setCallbackHandler");

        // ---- contract deepening: recording subclass + protected ctor matrix + Provider inheritance ----
        testContractDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }
}
