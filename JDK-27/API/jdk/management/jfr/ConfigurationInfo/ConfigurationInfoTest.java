/*
 * @test
 * @summary jdk.management.jfr.ConfigurationInfo conformance: value-object getters + CompositeData round-trip.
 * @run main ConfigurationInfoTest
 */

import jdk.management.jfr.*;
import javax.management.*;
import javax.management.openmbean.CompositeData;
import java.lang.management.*;
import java.lang.reflect.*;
import java.util.*;

public class ConfigurationInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static java.util.Set<String> set(String... v) {
        return new java.util.HashSet<String>(java.util.Arrays.asList(v));
    }
    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.management.jfr.ConfigurationInfo");
        check(c != null, "jdk.management.jfr.ConfigurationInfo loads");
        check(c.getName().equals("jdk.management.jfr.ConfigurationInfo"), "binary name is jdk.management.jfr.ConfigurationInfo");
        check(Modifier.isPublic(c.getModifiers()), "jdk.management.jfr.ConfigurationInfo is public");
        check(!c.isInterface(), "jdk.management.jfr.ConfigurationInfo is not an interface");
        check(!Modifier.isAbstract(c.getModifiers()), "jdk.management.jfr.ConfigurationInfo is concrete");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass is java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "from"), "declares from(CompositeData)");

        FlightRecorderMXBean bean = ManagementFactory.getPlatformMXBean(FlightRecorderMXBean.class);
        List<ConfigurationInfo> cfgs = bean.getConfigurations();
        check(cfgs != null && cfgs.size() >= 2, "at least default + profile");
        ConfigurationInfo def = null, profile = null;
        for (ConfigurationInfo ci : cfgs) {
            if ("default".equals(ci.getName())) def = ci;
            if ("profile".equals(ci.getName())) profile = ci;
        }
        check(def != null, "default present");
        check(profile != null, "profile present");
        check("Oracle".equals(def.getProvider()), "provider Oracle");
        check(def.getLabel() != null && !def.getLabel().isEmpty(), "label non-empty");
        check(def.getDescription() != null && !def.getDescription().isEmpty(), "description non-empty");
        check(def.getContents() != null && !def.getContents().isEmpty(), "contents non-empty");
        check(def.getSettings() != null && def.getSettings().size() > 1, "settings non-trivial");
        check(def.toString() != null && def.toString().contains("default"), "toString mentions name");
        // the two configs differ in settings
        check(!def.getSettings().equals(profile.getSettings()), "default and profile differ in settings");
        // settings keys reference event settings (name#setting form)
        boolean keyed = false;
        for (String k : def.getSettings().keySet()) if (k.contains("#")) keyed = true;
        check(keyed, "settings keys use <event>#<setting> form");

        // dual channel: Configurations attribute row -> from(CompositeData) equivalence
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName on = new ObjectName(FlightRecorderMXBean.MXBEAN_NAME);
        CompositeData[] rows = (CompositeData[]) mbs.getAttribute(on, "Configurations");
        check(rows != null && rows.length == cfgs.size(), "Configurations attribute row count equals");
        CompositeData row = null;
        for (CompositeData r : rows) if ("default".equals(r.get("name"))) row = r;
        check(row != null, "CompositeData row for default");
        ConfigurationInfo fromCd = ConfigurationInfo.from(row);
        check(fromCd.getName().equals(def.getName()), "from(): name equal");
        check(fromCd.getProvider().equals(def.getProvider()), "from(): provider equal");
        check(fromCd.getLabel().equals(def.getLabel()), "from(): label equal");
        check(fromCd.getDescription().equals(def.getDescription()), "from(): description equal");
        check(fromCd.getContents().equals(def.getContents()), "from(): contents equal");
        check(fromCd.getSettings().equals(def.getSettings()), "from(): settings equal");
        check(fromCd.toString() != null, "from(): toString non-null");
        testBaselineWhiteBox();
        testBaselineBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ==== 基线断言原样保留（tranche-8 遗产；深化只增不删） ====
    static boolean baselineHasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testBaselineWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.management.jfr.ConfigurationInfo");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !c.isEnum(), "concrete");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(baselineHasMethod(c, "getName"), "getName");
        check(baselineHasMethod(c, "getProvider"), "getProvider");
        check(baselineHasMethod(c, "getContents"), "getContents");
        check(baselineHasMethod(c, "getSettings"), "getSettings");
        check(baselineHasMethod(c, "getLabel"), "getLabel");
        check(baselineHasMethod(c, "getDescription"), "getDescription");
        check(baselineHasMethod(c, "from"), "from");

    }

    static void testBaselineBlackBox() throws Exception {
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

}
