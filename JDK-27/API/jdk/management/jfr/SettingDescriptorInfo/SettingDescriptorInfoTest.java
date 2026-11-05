/*
 * @test
 * @summary jdk.management.jfr.SettingDescriptorInfo conformance: value-object getters + CompositeData round-trip.
 * @run main SettingDescriptorInfoTest
 */

import jdk.management.jfr.*;
import javax.management.*;
import javax.management.openmbean.CompositeData;
import java.lang.management.*;
import java.lang.reflect.*;
import java.util.*;

public class SettingDescriptorInfoTest {
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
        Class<?> c = Class.forName("jdk.management.jfr.SettingDescriptorInfo");
        check(c != null, "jdk.management.jfr.SettingDescriptorInfo loads");
        check(c.getName().equals("jdk.management.jfr.SettingDescriptorInfo"), "binary name is jdk.management.jfr.SettingDescriptorInfo");
        check(Modifier.isPublic(c.getModifiers()), "jdk.management.jfr.SettingDescriptorInfo is public");
        check(!c.isInterface(), "jdk.management.jfr.SettingDescriptorInfo is not an interface");
        check(!Modifier.isAbstract(c.getModifiers()), "jdk.management.jfr.SettingDescriptorInfo is concrete");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass is java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "from"), "declares from(CompositeData)");

        FlightRecorderMXBean bean = ManagementFactory.getPlatformMXBean(FlightRecorderMXBean.class);
        SettingDescriptorInfo threshold = null, enabled = null;
        for (EventTypeInfo e : bean.getEventTypes()) {
            if (!"jdk.FileRead".equals(e.getName())) continue;
            for (SettingDescriptorInfo s : e.getSettingDescriptors()) {
                if ("threshold".equals(s.getName())) threshold = s;
                if ("enabled".equals(s.getName())) enabled = s;
            }
        }
        check(threshold != null, "threshold setting present (JDK-27 FileRead)");
        check("Threshold".equals(threshold.getLabel()), "threshold label");
        check("0 ns".equals(threshold.getDefaultValue()), "threshold default 0 ns");
        check("jdk.settings.Threshold".equals(threshold.getTypeName()), "threshold typeName");
        check("jdk.jfr.Timespan".equals(threshold.getContentType()), "threshold contentType jdk.jfr.Timespan");
        check(threshold.getDescription() != null && !threshold.getDescription().isEmpty(), "threshold description non-empty");
        check(threshold.toString() != null && threshold.toString().contains("threshold"), "threshold toString mentions name");
        check(enabled != null, "enabled setting present");
        check("Enabled".equals(enabled.getLabel()), "enabled label");
        check("false".equals(enabled.getDefaultValue()), "enabled default false");
        check("jdk.settings.Enabled".equals(enabled.getTypeName()), "enabled typeName");
        check("jdk.jfr.BooleanFlag".equals(enabled.getContentType()), "enabled contentType jdk.jfr.BooleanFlag");

        // identity semantics: equals/hashCode not overridden
        check(!declares(c, "equals"), "equals not overridden");
        check(!declares(c, "hashCode"), "hashCode not overridden");
        check(threshold.equals(threshold), "equals reflexive");
        check(!threshold.equals(enabled), "distinct settings not equal");

        // dual channel: nested setting CompositeData from the EventTypes attribute -> from() equivalence
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName on = new ObjectName(FlightRecorderMXBean.MXBEAN_NAME);
        CompositeData[] rows = (CompositeData[]) mbs.getAttribute(on, "EventTypes");
        CompositeData fileReadRow = null;
        for (CompositeData r : rows) if ("jdk.FileRead".equals(r.get("name"))) fileReadRow = r;
        check(fileReadRow != null, "CompositeData row for jdk.FileRead");
        CompositeData[] settingRows = (CompositeData[]) fileReadRow.get("settingDescriptors");
        check(settingRows != null && settingRows.length == 4, "nested setting rows == 4 on JDK-27");
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
        testBaselineWhiteBox();
        testBaselineBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ==== 基线断言原样保留（tranche-8 遗产；深化只增不删） ====
    static boolean baselineHasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testBaselineWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.management.jfr.SettingDescriptorInfo");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !c.isEnum(), "concrete");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(baselineHasMethod(c, "getLabel"), "getLabel");
        check(baselineHasMethod(c, "getName"), "getName");
        check(baselineHasMethod(c, "getDescription"), "getDescription");
        check(baselineHasMethod(c, "getTypeName"), "getTypeName");
        check(baselineHasMethod(c, "getContentType"), "getContentType");
        check(baselineHasMethod(c, "getDefaultValue"), "getDefaultValue");
        check(baselineHasMethod(c, "from"), "from");

    }

    static void testBaselineBlackBox() throws Exception {
        if (!jdk.jfr.FlightRecorder.isAvailable()) { check(true, "JFR unavailable"); return; }
        FlightRecorderMXBean bean = ManagementFactory.getPlatformMXBean(FlightRecorderMXBean.class);
        List<EventTypeInfo> types = bean.getEventTypes();
        if (!types.isEmpty()) {
            List<SettingDescriptorInfo> descs = types.get(0).getSettingDescriptors();
            check(descs != null, "setting descriptors");
            if (!descs.isEmpty()) {
                SettingDescriptorInfo d = descs.get(0);
                check(d.getName() != null, "name");
                check(d.getLabel() != null, "label");
                check(d.getDescription() != null, "description");
                check(d.getTypeName() != null, "typeName");
                check(d.getContentType() != null, "contentType");
                check(d.toString() != null, "toString");
            } else { check(true, "no descriptors"); }
        } else { check(true, "no event types"); }

    }

}
