/*
 * @test
 * @summary javax.security.auth.login.AppConfigurationEntry conformance: constructor and getters.
 * @run main AppConfigurationEntryTest
 */

public class AppConfigurationEntryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.security.auth.login.AppConfigurationEntry e = new javax.security.auth.login.AppConfigurationEntry(
            "com.example.LoginModule", javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
            new java.util.HashMap<String, Object>());
        check("com.example.LoginModule".equals(e.getLoginModuleName()), "getLoginModuleName");
        check(e.getControlFlag() == javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, "getControlFlag");
        check(e.getOptions().isEmpty(), "getOptions empty");
        check("LoginModuleControlFlag: required".equals(javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag.REQUIRED.toString()), "control flag toString");
        check(javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag.REQUISITE != null, "REQUISITE constant");
        check(javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT != null, "SUFFICIENT constant");
        check(javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL != null, "OPTIONAL constant");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
