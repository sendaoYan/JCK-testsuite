/*
 * @test
 * @summary jdk.management.jfr.EventTypeInfo conformance: reflection shape + value getters + CompositeData channels.
 * @run main EventTypeInfoTest
 */

import jdk.management.jfr.*;
import java.lang.management.ManagementFactory;
import java.lang.reflect.*;
import java.util.*;

import javax.management.*;
import javax.management.openmbean.*;

public class EventTypeInfoTest {
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
        Class<?> c = Class.forName("jdk.management.jfr.EventTypeInfo");
        check(c != null, "jdk.management.jfr.EventTypeInfo loads");
        check(c.getName().equals("jdk.management.jfr.EventTypeInfo"), "binary name is jdk.management.jfr.EventTypeInfo");
        check(Modifier.isPublic(c.getModifiers()), "jdk.management.jfr.EventTypeInfo is public");
        check(!c.isInterface(), "jdk.management.jfr.EventTypeInfo is not an interface");
        check(!Modifier.isAbstract(c.getModifiers()), "jdk.management.jfr.EventTypeInfo is concrete");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass is java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");

        // ---------- value getters over the real registry ----------
        FlightRecorderMXBean bean = ManagementFactory.getPlatformMXBean(FlightRecorderMXBean.class);
        List<EventTypeInfo> types = bean.getEventTypes();
        check(types.size() > 100, "getEventTypes registry size (" + types.size() + ")");
        EventTypeInfo fr = null;
        boolean sawObjectAllocationSample = false;
        for (EventTypeInfo t : types) {
            if ("jdk.FileRead".equals(t.getName())) fr = t;
            if ("jdk.ObjectAllocationSample".equals(t.getName())) sawObjectAllocationSample = true;
        }
        check(fr != null, "jdk.FileRead present");
        check(!sawObjectAllocationSample, "jdk.ObjectAllocationSample absent on JDK-8");
        check(fr.getLabel() != null && !fr.getLabel().isEmpty(), "label non-empty");
        check(fr.getDescription() != null && !fr.getDescription().isEmpty(), "description non-empty");
        check(fr.getId() > 0, "id positive");
        check(fr.getCategoryNames() != null && fr.getCategoryNames().contains("Java Application"),
              "FileRead categorized under Java Application, got " + fr.getCategoryNames());
        check(fr.toString() != null && fr.toString().contains("jdk.FileRead"), "toString mentions name");

        // settings detail (JDK-8 FileRead: exactly 3, ordered enabled/threshold/stackTrace)
        List<SettingDescriptorInfo> sds = fr.getSettingDescriptors();
        check(sds.size() == 3, "FileRead settings count 3");
        SettingDescriptorInfo enabled = null, threshold = null, stackTrace = null;
        List<String> order = new ArrayList<String>();
        for (SettingDescriptorInfo s : sds) {
            order.add(s.getName());
            if ("enabled".equals(s.getName())) enabled = s;
            if ("threshold".equals(s.getName())) threshold = s;
            if ("stackTrace".equals(s.getName())) stackTrace = s;
        }
        check(order.equals(Arrays.asList("enabled", "threshold", "stackTrace")), "settings order");
        check(enabled != null && "Enabled".equals(enabled.getLabel()) && "false".equals(enabled.getDefaultValue())
              && "jdk.settings.Enabled".equals(enabled.getTypeName()) && "jdk.jfr.BooleanFlag".equals(enabled.getContentType()),
              "enabled setting full value tuple");
        check(threshold != null && "Threshold".equals(threshold.getLabel()) && "0 ns".equals(threshold.getDefaultValue())
              && "jdk.settings.Threshold".equals(threshold.getTypeName()) && "jdk.jfr.Timespan".equals(threshold.getContentType()),
              "threshold setting full value tuple");
        check(stackTrace != null && "Stack Trace".equals(stackTrace.getLabel()) && "false".equals(stackTrace.getDefaultValue())
              && "jdk.settings.StackTrace".equals(stackTrace.getTypeName()) && "jdk.jfr.BooleanFlag".equals(stackTrace.getContentType()),
              "stackTrace setting full value tuple");

        // identity semantics (equals/hashCode not overridden)
        check(!declares(c, "equals"), "equals not overridden");
        check(!declares(c, "hashCode"), "hashCode not overridden");
        EventTypeInfo fr2 = null;
        for (EventTypeInfo t : bean.getEventTypes()) if ("jdk.FileRead".equals(t.getName())) fr2 = t;
        check(fr != fr2, "getEventTypes returns fresh instances");
        check(fr.equals(fr) && !fr.equals(fr2), "equals behaves as identity");

        // ---------- MXBean channel row shape ----------
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName on = new ObjectName(FlightRecorderMXBean.MXBEAN_NAME);
        CompositeData[] rows = (CompositeData[]) mbs.getAttribute(on, "EventTypes");
        CompositeData row = null;
        for (CompositeData r : rows) if ("jdk.FileRead".equals(r.get("name"))) row = r;
        check(row != null, "CompositeData row for jdk.FileRead");
        check(new TreeSet<String>(row.getCompositeType().keySet()).equals(
                  new TreeSet<String>(Arrays.asList("categoryNames", "description", "id", "label", "name", "settingDescriptors"))),
              "row item names");
        check(row.getCompositeType().getType("id") == SimpleType.LONG, "row id item is LONG");
        CompositeData[] nested = (CompositeData[]) row.get("settingDescriptors");
        check(nested != null && nested.length == 3, "nested settingDescriptors length 3");

        // ---------- verified JDK-8 negative: from() reads item "settings", MXBean emits "settingDescriptors" ----------
        try {
            EventTypeInfo.from(row);
            check(false, "from(mbeanRow) should throw InvalidKeyException on JDK-8");
        } catch (IllegalArgumentException e) {
            Throwable r = e;
            check(r instanceof InvalidKeyException || (r.getCause() instanceof InvalidKeyException),
                  "from(mbeanRow) -> InvalidKeyException, got " + r);
            String msg = String.valueOf(r.getMessage()) + String.valueOf(r.getCause() != null ? r.getCause().getMessage() : "");
            check(msg.contains("settings"), "InvalidKeyException mentions item 'settings'");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
