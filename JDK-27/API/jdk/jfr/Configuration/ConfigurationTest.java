/*
 * @test
 * @summary jdk.jfr.Configuration conformance: white-box structure + black-box built-in configs.
 * @run main ConfigurationTest
 */

import jdk.jfr.*;
import java.util.List;
import java.lang.reflect.*;

public class ConfigurationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.Configuration");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(hasMethod(c, "getConfigurations"), "getConfigurations");
        check(hasMethod(c, "getConfiguration"), "getConfiguration");
        check(hasMethod(c, "getName"), "getName");
        check(hasMethod(c, "getSettings"), "getSettings");
        check(hasMethod(c, "getContents"), "getContents");
    }

    static void testBlackBox() throws Exception {
        List<Configuration> cs = Configuration.getConfigurations();
        check(cs != null && cs.size() > 0, "has configurations");
        Configuration c = Configuration.getConfiguration("default");
        check(c != null, "default configuration");
        check("default".equals(c.getName()), "name");
        check(c.getSettings() != null && !c.getSettings().isEmpty(), "settings");
        check(c.getContents() != null, "contents");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
