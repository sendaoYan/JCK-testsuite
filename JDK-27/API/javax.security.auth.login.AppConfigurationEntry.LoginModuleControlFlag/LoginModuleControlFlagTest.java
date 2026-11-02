/*
 * @test
 * @summary javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag conformance: login module control flag constants.
 * @run main LoginModuleControlFlagTest
 */

import javax.security.auth.login.AppConfigurationEntry.*;
import javax.security.auth.login.*;

public class LoginModuleControlFlagTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        check(LoginModuleControlFlag.REQUIRED != null && LoginModuleControlFlag.REQUISITE != null && LoginModuleControlFlag.SUFFICIENT != null && LoginModuleControlFlag.OPTIONAL != null, "four flags non-null");
        check(LoginModuleControlFlag.REQUIRED.toString().contains("required"), "REQUIRED toString");
        check(LoginModuleControlFlag.OPTIONAL.toString().contains("optional"), "OPTIONAL toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
