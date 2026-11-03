/*
 * @test
 * @summary com.sun.security.auth.login.ConfigFile conformance: white-box structure + black-box behavior.
 * @run main ConfigFileTest
 */

import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import javax.security.auth.login.AppConfigurationEntry;

public class ConfigFileTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }
    static Class<?> load(String fqcn) throws Exception { return Class.forName(fqcn); }

    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.security.auth.login.ConfigFile");
        check(!c.isInterface(), "not an interface");
        check(c.getSuperclass().getName().equals("javax.security.auth.login.Configuration"), "extends Configuration");
        Set<String> m = methodNames(c);
        check(m.contains("getAppConfigurationEntry"), "getAppConfigurationEntry");
        check(m.contains("refresh"), "refresh");
    }

    static void testBlackBox() throws Exception {
        com.sun.security.auth.login.ConfigFile cf = new com.sun.security.auth.login.ConfigFile();
        // getType() is inherited from Configuration and returns null.
        check(cf.getType() == null, "getType inherited returns null");
        check(cf.getAppConfigurationEntry("nonexistent.jck.app") == null, "unknown app -> null entries");
        cf.refresh();
        check(cf.getAppConfigurationEntry("nonexistent.jck.app") == null, "after refresh unknown app still null");

        // Build an isolated JAAS config file and read it through ConfigFile(URI).
        Path dir = Files.createTempDirectory("jck-config");
        Path cfg = dir.resolve("jaas.conf");
        String jaas =
            "jckTestApp {\n" +
            "  com.sun.security.auth.module.UnixLoginModule required debug=true;\n" +
            "};\n";
        try {
            Files.write(cfg, jaas.getBytes("UTF-8"));
            com.sun.security.auth.login.ConfigFile cf2 = new com.sun.security.auth.login.ConfigFile(cfg.toUri());
            AppConfigurationEntry[] entries = cf2.getAppConfigurationEntry("jckTestApp");
            check(entries != null, "known app returns entries");
            check(entries.length == 1, "single entry");
            check(entries[0].getLoginModuleName().equals("com.sun.security.auth.module.UnixLoginModule"), "module name");
            check(entries[0].getControlFlag() == AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, "required flag");
            check("true".equals(entries[0].getOptions().get("debug")), "option debug=true");
            check(cf2.getAppConfigurationEntry("other.app") == null, "other app null");
            cf2.refresh();
            check(cf2.getAppConfigurationEntry("jckTestApp") != null, "known app still resolves after refresh");
        } finally {
            Files.deleteIfExists(cfg);
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
