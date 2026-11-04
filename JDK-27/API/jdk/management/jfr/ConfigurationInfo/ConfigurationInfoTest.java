/*
 * @test
 * @summary jdk.management.jfr.ConfigurationInfo conformance: white-box structure + black-box behavior.
 * @run main ConfigurationInfoTest
 */

import jdk.management.jfr.*;
import javax.management.*;
import java.lang.management.*;
import java.lang.reflect.*;
import java.util.*;
public class ConfigurationInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.management.jfr.ConfigurationInfo");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !c.isEnum(), "concrete");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(hasMethod(c, "getName"), "getName");
        check(hasMethod(c, "getProvider"), "getProvider");
        check(hasMethod(c, "getContents"), "getContents");
        check(hasMethod(c, "getSettings"), "getSettings");
        check(hasMethod(c, "getLabel"), "getLabel");
        check(hasMethod(c, "getDescription"), "getDescription");
        check(hasMethod(c, "from"), "from");

    }

    static void testBlackBox() throws Exception {
        if (!jdk.jfr.FlightRecorder.isAvailable()) { check(true, "JFR unavailable"); return; }
        FlightRecorderMXBean bean = ManagementFactory.getPlatformMXBean(FlightRecorderMXBean.class);
        List<ConfigurationInfo> list = bean.getConfigurations();
        check(list != null, "configurations list");
        if (!list.isEmpty()) {
            ConfigurationInfo ci = list.get(0);
            check(ci.getName() != null, "name");
            check(ci.getLabel() != null, "label");
            check(ci.getProvider() != null, "provider");
            check(ci.getContents() != null, "contents");
            check(ci.getSettings() != null, "settings");
            check(ci.toString() != null, "toString");
        } else { check(true, "no configurations available"); }

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
