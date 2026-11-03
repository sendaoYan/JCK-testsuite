/*
 * @test
 * @summary javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag conformance: white-box structure + black-box behavior.
 * @run main LoginModuleControlFlagTest
 */

import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;

public class LoginModuleControlFlagTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.auth.login.AppConfigurationEntry$LoginModuleControlFlag");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public class");
        check(!c.isInterface() && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(!c.isEnum(), "not an enum (four static final instances, no values/valueOf)");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        c.getMethod("toString");
        check(true, "toString method present");
        check(c.getDeclaredConstructors().length > 0, "private constructor present");
        for (java.lang.reflect.Constructor<?> k : c.getDeclaredConstructors())
            check(java.lang.reflect.Modifier.isPrivate(k.getModifiers()), "constructor is private");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        check(LoginModuleControlFlag.REQUIRED != null, "REQUIRED non-null");
        check(LoginModuleControlFlag.REQUISITE != null, "REQUISITE non-null");
        check(LoginModuleControlFlag.SUFFICIENT != null, "SUFFICIENT non-null");
        check(LoginModuleControlFlag.OPTIONAL != null, "OPTIONAL non-null");

        // distinct identities
        check(LoginModuleControlFlag.REQUIRED != LoginModuleControlFlag.REQUISITE, "REQUIRED != REQUISITE");
        check(LoginModuleControlFlag.REQUIRED != LoginModuleControlFlag.SUFFICIENT, "REQUIRED != SUFFICIENT");
        check(LoginModuleControlFlag.REQUIRED != LoginModuleControlFlag.OPTIONAL, "REQUIRED != OPTIONAL");
        check(LoginModuleControlFlag.REQUISITE != LoginModuleControlFlag.SUFFICIENT, "REQUISITE != SUFFICIENT");
        check(LoginModuleControlFlag.REQUISITE != LoginModuleControlFlag.OPTIONAL, "REQUISITE != OPTIONAL");
        check(LoginModuleControlFlag.SUFFICIENT != LoginModuleControlFlag.OPTIONAL, "SUFFICIENT != OPTIONAL");

        // stable, singleton-like field identity
        check(LoginModuleControlFlag.REQUIRED == LoginModuleControlFlag.REQUIRED, "REQUIRED identity stable");

        check(LoginModuleControlFlag.REQUIRED.toString().contains("required"), "REQUIRED toString");
        check(LoginModuleControlFlag.REQUISITE.toString().contains("requisite"), "REQUISITE toString");
        check(LoginModuleControlFlag.SUFFICIENT.toString().contains("sufficient"), "SUFFICIENT toString");
        check(LoginModuleControlFlag.OPTIONAL.toString().contains("optional"), "OPTIONAL toString");
        check(LoginModuleControlFlag.REQUIRED.toString().startsWith("LoginModuleControlFlag"),
                "toString prefix LoginModuleControlFlag");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
