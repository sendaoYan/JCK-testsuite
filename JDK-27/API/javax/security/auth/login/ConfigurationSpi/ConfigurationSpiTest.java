/*
 * @test
 * @summary javax.security.auth.login.ConfigurationSpi conformance: white-box structure + black-box behavior.
 * @run main ConfigurationSpiTest
 */

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.ConfigurationSpi;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import java.util.HashMap;

public class ConfigurationSpiTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.auth.login.ConfigurationSpi");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getConstructors().length > 0, "public constructor");
        check(hasMethod(c, "engineGetAppConfigurationEntry"), "engineGetAppConfigurationEntry present");
        check(hasMethod(c, "engineRefresh"), "engineRefresh present");
        // both engine methods are protected
        check(java.lang.reflect.Modifier.isProtected(c.getDeclaredMethod("engineGetAppConfigurationEntry", String.class).getModifiers()),
                "engineGetAppConfigurationEntry is protected");
        check(java.lang.reflect.Modifier.isProtected(c.getDeclaredMethod("engineRefresh").getModifiers()),
                "engineRefresh is protected");
    }

    // SPI impl exposes protected engine methods for black-box observation
    static class TestSpi extends ConfigurationSpi {
        int refreshCount;
        String lastName;
        public AppConfigurationEntry[] getEntry(String name) { return engineGetAppConfigurationEntry(name); }
        public void doRefresh() { engineRefresh(); }
        protected AppConfigurationEntry[] engineGetAppConfigurationEntry(String name) {
            lastName = name;
            if ("app".equals(name)) {
                return new AppConfigurationEntry[] {
                    new AppConfigurationEntry("com.example.Mod",
                        LoginModuleControlFlag.REQUIRED, new HashMap<String, Object>())
                };
            }
            return null;
        }
        protected void engineRefresh() { refreshCount++; }
    }

    // does not override engineRefresh: exercises the default no-op implementation
    static class NoOverrideSpi extends ConfigurationSpi {
        public AppConfigurationEntry[] getEntry(String name) { return engineGetAppConfigurationEntry(name); }
        public void doRefresh() { engineRefresh(); }
        protected AppConfigurationEntry[] engineGetAppConfigurationEntry(String name) { return null; }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        TestSpi spi = new TestSpi();
        check(spi instanceof ConfigurationSpi, "subclass instanceof ConfigurationSpi");

        AppConfigurationEntry[] entries = spi.getEntry("app");
        check(entries != null && entries.length == 1, "engineGetAppConfigurationEntry returns entry");
        check("com.example.Mod".equals(entries[0].getLoginModuleName()), "entry module name");
        check(entries[0].getControlFlag() == LoginModuleControlFlag.REQUIRED, "entry control flag");
        check("app".equals(spi.lastName), "name forwarded to engine method");

        check(spi.getEntry("missing") == null, "unknown name returns null");
        check("missing".equals(spi.lastName), "unknown name forwarded");

        // default engineRefresh is a no-op; our override counts invocations
        check(spi.refreshCount == 0, "refresh not yet called");
        spi.doRefresh();
        check(spi.refreshCount == 1, "engineRefresh invoked once");

        // default (non-overridden) engineRefresh is a no-op and never throws
        NoOverrideSpi base = new NoOverrideSpi();
        base.doRefresh();
        check(true, "default engineRefresh no-op callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
