/*
 * @test
 * @summary javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag conformance: control flag constants and toString.
 * @run main LoginModuleControlFlagTest
 */

public class LoginModuleControlFlagTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag f =
            javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag.REQUIRED;
        check("LoginModuleControlFlag: required".equals(f.toString()), "REQUIRED toString");
        check("LoginModuleControlFlag: requisite".equals(javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag.REQUISITE.toString()), "REQUISITE toString");
        check("LoginModuleControlFlag: sufficient".equals(javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT.toString()), "SUFFICIENT toString");
        check("LoginModuleControlFlag: optional".equals(javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL.toString()), "OPTIONAL toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
