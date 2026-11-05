/*
 * @test
 * @summary jdk.management.jfr.ConfigurationInfo conformance: reflection shape + value getters + CompositeData round-trip.
 * @run main ConfigurationInfoTest
 */

import jdk.management.jfr.*;
import java.lang.management.ManagementFactory;
import java.lang.reflect.*;
import java.util.*;

import javax.management.*;
import javax.management.openmbean.*;

public class ConfigurationInfoTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

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
        // ---------- original shape checks (kept verbatim) ----------
        Class<?> c = Class.forName("jdk.management.jfr.ConfigurationInfo");
        check(c != null, "jdk.management.jfr.ConfigurationInfo loads");
        check(c.getName().equals("jdk.management.jfr.ConfigurationInfo"), "binary name is jdk.management.jfr.ConfigurationInfo");
        check(Modifier.isPublic(c.getModifiers()), "jdk.management.jfr.ConfigurationInfo is public");
        check(!c.isInterface(), "jdk.management.jfr.ConfigurationInfo is not an interface");
        check(!Modifier.isAbstract(c.getModifiers()), "jdk.management.jfr.ConfigurationInfo is concrete");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass is java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "from"), "declares from(CompositeData)");

        // ---------- value getters over shipped .jfc configurations ----------
        FlightRecorderMXBean bean = ManagementFactory.getPlatformMXBean(FlightRecorderMXBean.class);
        List<ConfigurationInfo> confs = bean.getConfigurations();
        check(confs.size() >= 2, "getConfigurations size (" + confs.size() + ")");
        ConfigurationInfo deflt = null, profile = null;
        for (ConfigurationInfo cf : confs) {
            if ("default".equals(cf.getName())) deflt = cf;
            if ("profile".equals(cf.getName())) profile = cf;
        }
        check(deflt != null, "default configuration present");
        check(profile != null, "profile configuration present");
        check("Continuous".equals(deflt.getLabel()), "default label Continuous");
        check("Profiling".equals(profile.getLabel()), "profile label Profiling");
        check(deflt.getProvider() != null && !deflt.getProvider().isEmpty(), "default provider non-empty");
        check(deflt.getDescription() != null && !deflt.getDescription().isEmpty(), "default description non-empty");
        check(deflt.getContents() != null && !deflt.getContents().isEmpty(), "default contents (jfc XML) non-empty");
        check(deflt.getContents().startsWith("<"), "default contents looks like XML");
        check(deflt.getSettings() != null && deflt.getSettings().size() > 100,
              "default settings map large (" + deflt.getSettings().size() + ")");
        boolean hashKeys = false;
        for (String k : deflt.getSettings().keySet()) if (k.contains("#")) { hashKeys = true; break; }
        check(hashKeys, "settings keys use the event#setting form");
        check(profile.getSettings().size() > 100, "profile settings map large");
        check(!deflt.getSettings().equals(profile.getSettings()), "default and profile settings differ");
        check(deflt.toString() != null && deflt.toString().contains("default"), "toString mentions name");

        // identity semantics
        check(!declares(c, "equals"), "equals not overridden");
        check(!declares(c, "hashCode"), "hashCode not overridden");

        // ---------- dual channel: MXBean row -> from() equivalence ----------
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName on = new ObjectName(FlightRecorderMXBean.MXBEAN_NAME);
        CompositeData[] rows = (CompositeData[]) mbs.getAttribute(on, "Configurations");
        CompositeData row = null;
        for (CompositeData r : rows) if ("default".equals(r.get("name"))) row = r;
        check(row != null, "CompositeData row for default");
        check(new TreeSet<String>(row.getCompositeType().keySet()).equals(
                  new TreeSet<String>(Arrays.asList("contents", "description", "label", "name", "provider", "settings"))),
              "row item names");
        ConfigurationInfo fromCd = ConfigurationInfo.from(row);
        check("default".equals(fromCd.getName()), "from(): name");
        check(fromCd.getLabel().equals(deflt.getLabel()), "from(): label equal");
        check(fromCd.getProvider().equals(deflt.getProvider()), "from(): provider equal");
        check(fromCd.getDescription().equals(deflt.getDescription()), "from(): description equal");
        check(fromCd.getContents().equals(deflt.getContents()), "from(): contents equal");
        check(fromCd.getSettings().equals(deflt.getSettings()), "from(): settings map fully round-trips");
        check(fromCd.toString() != null, "from(): toString non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
