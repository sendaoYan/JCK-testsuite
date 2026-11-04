/*
 * @test
 * @summary javax.security.auth.login.LoginContext conformance: white-box structure + black-box behavior.
 * @compile TestLoginModule.java
 * @run main LoginContextTest
 */

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;

public class LoginContextTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Exception; }

    static void expect(Class<? extends Throwable> type, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            check(type.isInstance(t), msg + " (got " + t.getClass().getName() + ")");
            return;
        }
        check(false, msg + " (no exception)");
    }

    static boolean hasMethod(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.auth.login.LoginContext");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(hasMethod(c, "login") && hasMethod(c, "logout") && hasMethod(c, "getSubject"), "methods present");
        c.getConstructor(String.class);
        c.getConstructor(String.class, Subject.class);
        c.getConstructor(String.class, CallbackHandler.class);
        c.getConstructor(String.class, Subject.class, CallbackHandler.class);
        c.getConstructor(String.class, Subject.class, CallbackHandler.class, Configuration.class);
        check(true, "all 5 constructors present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // (a) no configuration entry: constructor throws LoginException immediately
        expect(LoginException.class, () -> new LoginContext("__nonexistent_jaas_config__"),
                "unknown config name throws LoginException at construction");

        Configuration original = Configuration.getConfiguration();
        Configuration custom = new Configuration() {
            public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
                if ("app".equals(name)) {
                    return new AppConfigurationEntry[] {
                        new AppConfigurationEntry("TestLoginModule",
                            LoginModuleControlFlag.REQUIRED, new HashMap<String, Object>())
                    };
                }
                if ("badmodule".equals(name)) {
                    return new AppConfigurationEntry[] {
                        new AppConfigurationEntry("com.example.NoSuchModule",
                            LoginModuleControlFlag.REQUIRED, new HashMap<String, Object>())
                    };
                }
                return null;
            }
        };

        Configuration.setConfiguration(custom);
        try {
            // (b) successful login populates the subject
            LoginContext lc = new LoginContext("app");
            check(lc.getSubject() == null, "getSubject null before login (subject not provided)");

            expect(LoginException.class, lc::logout, "logout before login throws LoginException");

            lc.login();
            Subject subject = lc.getSubject();
            check(subject != null, "getSubject non-null after successful login");
            check(TestLoginModule.lastCreated != null, "login module instantiated");
            check(TestLoginModule.lastCreated.loggedIn, "module.login invoked");
            check(TestLoginModule.lastCreated.committed, "module.commit invoked");
            check(!TestLoginModule.lastCreated.aborted, "module.abort not invoked on success");
            check(!TestLoginModule.lastCreated.loggedOut, "module.logout not invoked yet");
            check(TestLoginModule.lastCreated.initialized, "module.initialize invoked");

            boolean hasPrincipal = false;
            for (Principal p : subject.getPrincipals()) if (p.getName().equals("p")) hasPrincipal = true;
            check(hasPrincipal, "subject contains principal added by commit");
            check(subject.getPublicCredentials().contains("credential"), "subject contains public credential added by commit");

            // (c) logout invokes module.logout and cleans the subject
            lc.logout();
            check(TestLoginModule.lastCreated.loggedOut, "module.logout invoked");
            check(subject.getPrincipals().isEmpty(), "principals cleared after logout");
            check(subject.getPublicCredentials().isEmpty(), "public credentials cleared after logout");
            check(lc.getSubject() == subject, "getSubject still returns subject after logout");

            // (d) login twice in a row on the same context keeps a single subject
            lc.login();
            check(lc.getSubject() == subject, "subject identity stable across login calls");

            // (e) Subject.doAs propagates the authenticated subject
            Subject propagated = Subject.doAs(subject, new PrivilegedAction<Subject>() {
                public Subject run() {
                    return Subject.current();
                }
            });
            check(propagated == subject, "Subject.doAs propagates the login subject");

            // (f) module-not-found during login is a LoginException
            LoginContext bad = new LoginContext("badmodule");
            expect(LoginException.class, bad::login, "unknown login module class throws LoginException");

            // (g) getSubject returns null for a failed/never-attempted login
            check(bad.getSubject() == null, "getSubject null when login failed/never attempted");
        } finally {
            Configuration.setConfiguration(original);
        }
        check(Configuration.getConfiguration() == original, "original configuration restored");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
