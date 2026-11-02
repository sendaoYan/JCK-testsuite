/*
 * @test
 * @summary javax.security.auth.login.LoginContext conformance: login context rejects unknown config.
 * @run main LoginContextTest
 */

import javax.security.auth.login.*;
import java.lang.reflect.*;

public class LoginContextTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.security.auth.login.LoginContext");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public class");
        check(hasMethod(c, "login") && hasMethod(c, "logout") && hasMethod(c, "getSubject"), "methods present");
        boolean le = false;
        try { new LoginContext("__nonexistent_jaas_config__"); } catch (LoginException e) { le = true; }
        check(le, "unknown config throws LoginException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
