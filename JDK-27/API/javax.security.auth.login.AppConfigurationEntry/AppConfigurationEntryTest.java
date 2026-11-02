/*
 * @test
 * @summary javax.security.auth.login.AppConfigurationEntry conformance: app configuration entry accessors.
 * @run main AppConfigurationEntryTest
 */

import javax.security.auth.login.*;
import java.util.HashMap;

public class AppConfigurationEntryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        HashMap<String, Object> opts = new HashMap<>();
        opts.put("k", "v");
        AppConfigurationEntry e = new AppConfigurationEntry("com.example.Module", AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, opts);
        check("com.example.Module".equals(e.getLoginModuleName()), "getLoginModuleName");
        check(e.getControlFlag() == AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, "getControlFlag");
        check(e.getOptions().get("k").equals("v"), "getOptions");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
