/*
 * @test
 * @summary jdk.management.jfr.SettingDescriptorInfo conformance: reflection shape + value getters + CompositeData round-trip.
 * @run main SettingDescriptorInfoTest
 */

import jdk.management.jfr.*;
import java.lang.management.ManagementFactory;
import java.lang.reflect.*;
import java.util.*;

import javax.management.*;
import javax.management.openmbean.*;

public class SettingDescriptorInfoTest {
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
        Class<?> c = Class.forName("jdk.management.jfr.SettingDescriptorInfo");
        check(c != null, "jdk.management.jfr.SettingDescriptorInfo loads");
        check(c.getName().equals("jdk.management.jfr.SettingDescriptorInfo"), "binary name is jdk.management.jfr.SettingDescriptorInfo");
        check(Modifier.isPublic(c.getModifiers()), "jdk.management.jfr.SettingDescriptorInfo is public");
        check(!c.isInterface(), "jdk.management.jfr.SettingDescriptorInfo is not an interface");
        check(!Modifier.isAbstract(c.getModifiers()), "jdk.management.jfr.SettingDescriptorInfo is concrete");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass is java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "from"), "declares from(CompositeData)");

        // ---------- value getters from the live FileRead settings ----------
        FlightRecorderMXBean bean = ManagementFactory.getPlatformMXBean(FlightRecorderMXBean.class);
        SettingDescriptorInfo enabled = null, threshold = null, stackTrace = null;
        for (EventTypeInfo e : bean.getEventTypes()) {
            if (!"jdk.FileRead".equals(e.getName())) continue;
            for (SettingDescriptorInfo s : e.getSettingDescriptors()) {
                if ("enabled".equals(s.getName())) enabled = s;
                if ("threshold".equals(s.getName())) threshold = s;
                if ("stackTrace".equals(s.getName())) stackTrace = s;
            }
        }
        check(enabled != null, "enabled setting present");
        check("Enabled".equals(enabled.getLabel()), "enabled label");
        check("false".equals(enabled.getDefaultValue()), "enabled default false");
        check("jdk.settings.Enabled".equals(enabled.getTypeName()), "enabled typeName");
        check("jdk.jfr.BooleanFlag".equals(enabled.getContentType()), "enabled contentType");
        check(enabled.getDescription() != null && !enabled.getDescription().isEmpty(), "enabled description non-empty");
        check(threshold != null, "threshold setting present");
        check("Threshold".equals(threshold.getLabel()), "threshold label");
        check("0 ns".equals(threshold.getDefaultValue()), "threshold default 0 ns");
        check("jdk.settings.Threshold".equals(threshold.getTypeName()), "threshold typeName");
        check("jdk.jfr.Timespan".equals(threshold.getContentType()), "threshold contentType");
        check(stackTrace != null, "stackTrace setting present (JDK-8 third FileRead setting)");
        check("Stack Trace".equals(stackTrace.getLabel()), "stackTrace label");
        check("false".equals(stackTrace.getDefaultValue()), "stackTrace default false");
        check("jdk.settings.StackTrace".equals(stackTrace.getTypeName()), "stackTrace typeName");
        check("jdk.jfr.BooleanFlag".equals(stackTrace.getContentType()), "stackTrace contentType");
        // verified toString shape: name="enabled" label="Enabled" ... defaultValue="false"
        check(enabled.toString() != null && enabled.toString().contains("name=\"enabled\"")
              && enabled.toString().contains("defaultValue=\"false\""), "toString shape");

        // identity semantics
        check(!declares(c, "equals"), "equals not overridden");
        check(!declares(c, "hashCode"), "hashCode not overridden");
        check(enabled.equals(enabled) && !enabled.equals(threshold), "identity equality");

        // ---------- dual channel: nested setting row -> from() equivalence ----------
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName on = new ObjectName(FlightRecorderMXBean.MXBEAN_NAME);
        CompositeData[] rows = (CompositeData[]) mbs.getAttribute(on, "EventTypes");
        CompositeData fileReadRow = null;
        for (CompositeData r : rows) if ("jdk.FileRead".equals(r.get("name"))) fileReadRow = r;
        check(fileReadRow != null, "CompositeData row for jdk.FileRead");
        CompositeData[] settingRows = (CompositeData[]) fileReadRow.get("settingDescriptors");
        check(settingRows != null && settingRows.length == 3, "nested setting rows == 3 on JDK-8");
        check(new TreeSet<String>(settingRows[0].getCompositeType().keySet()).equals(
                  new TreeSet<String>(Arrays.asList("contentType", "defaultValue", "description", "label", "name", "typeName"))),
              "nested row item names");
        CompositeData thresholdRow = null;
        for (CompositeData r : settingRows) if ("threshold".equals(r.get("name"))) thresholdRow = r;
        check(thresholdRow != null, "nested threshold row");
        SettingDescriptorInfo fromCd = SettingDescriptorInfo.from(thresholdRow);
        check(fromCd.getName().equals(threshold.getName()), "from(): name equal");
        check(fromCd.getLabel().equals(threshold.getLabel()), "from(): label equal");
        check(fromCd.getDefaultValue().equals(threshold.getDefaultValue()), "from(): defaultValue equal");
        check(fromCd.getTypeName().equals(threshold.getTypeName()), "from(): typeName equal");
        check(fromCd.getContentType().equals(threshold.getContentType()), "from(): contentType equal");
        check(fromCd.getDescription().equals(threshold.getDescription()), "from(): description equal");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
