/*
 * @test
 * @summary jdk.management.jfr.EventTypeInfo conformance: value-object getters + CompositeData round-trip.
 * @run main EventTypeInfoTest
 */

import jdk.management.jfr.*;
import javax.management.*;
import javax.management.openmbean.CompositeData;
import java.lang.management.*;
import java.lang.reflect.*;
import java.util.*;

public class EventTypeInfoTest {
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
        Class<?> c = Class.forName("jdk.management.jfr.EventTypeInfo");
        check(c != null, "jdk.management.jfr.EventTypeInfo loads");
        check(c.getName().equals("jdk.management.jfr.EventTypeInfo"), "binary name is jdk.management.jfr.EventTypeInfo");
        check(Modifier.isPublic(c.getModifiers()), "jdk.management.jfr.EventTypeInfo is public");
        check(!c.isInterface(), "jdk.management.jfr.EventTypeInfo is not an interface");
        check(!Modifier.isAbstract(c.getModifiers()), "jdk.management.jfr.EventTypeInfo is concrete");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass is java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "from"), "declares from(CompositeData)");

        FlightRecorderMXBean bean = ManagementFactory.getPlatformMXBean(FlightRecorderMXBean.class);
        List<EventTypeInfo> evts = bean.getEventTypes();
        check(evts != null && evts.size() >= 100, "event types plentiful");
        EventTypeInfo fr = null;
        for (EventTypeInfo e : evts) if ("jdk.FileRead".equals(e.getName())) fr = e;
        check(fr != null, "jdk.FileRead present");
        check("File Read".equals(fr.getLabel()), "label");
        check(fr.getId() > 0, "id > 0");
        check(fr.getDescription() != null && !fr.getDescription().isEmpty(), "description non-empty");
        check(fr.getCategoryNames().contains("Java Application"), "category Java Application");
        check(fr.getSettingDescriptors() != null && fr.getSettingDescriptors().size() == 4,
              "JDK-27 FileRead has 4 settings incl. threshold (was " + fr.getSettingDescriptors().size() + ")");
        List<String> settingNames = new ArrayList<>();
        for (SettingDescriptorInfo s : fr.getSettingDescriptors()) settingNames.add(s.getName());
        check(settingNames.contains("threshold") && settingNames.contains("enabled") && settingNames.contains("stackTrace"),
              "settings include threshold/enabled/stackTrace");
        check(fr.toString() != null && fr.toString().contains("jdk.FileRead"), "toString mentions name");

        // version marker: JDK 16+ event exposed on 27, absent on 8
        boolean hasObjAllocSample = false;
        for (EventTypeInfo e : evts) if ("jdk.ObjectAllocationSample".equals(e.getName())) hasObjAllocSample = true;
        check(hasObjAllocSample, "jdk.ObjectAllocationSample present (JDK 16+)");

        // dual channel: EventTypes attribute row -> from(CompositeData) equivalence
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName on = new ObjectName(FlightRecorderMXBean.MXBEAN_NAME);
        CompositeData[] rows = (CompositeData[]) mbs.getAttribute(on, "EventTypes");
        check(rows != null && rows.length == evts.size(), "EventTypes attribute row count == getEventTypes().size()");
        CompositeData row = null;
        for (CompositeData r : rows) if ("jdk.FileRead".equals(r.get("name"))) row = r;
        check(row != null, "CompositeData row for jdk.FileRead");
        EventTypeInfo fromCd = EventTypeInfo.from(row);
        check(fromCd.getName().equals(fr.getName()), "from(): name equal");
        check(fromCd.getLabel().equals(fr.getLabel()), "from(): label equal");
        check(fromCd.getId() == fr.getId(), "from(): id equal");
        check(fromCd.getDescription().equals(fr.getDescription()), "from(): description equal");
        check(fromCd.getCategoryNames().equals(fr.getCategoryNames()), "from(): categories equal");
        check(fromCd.getSettingDescriptors().size() == fr.getSettingDescriptors().size(), "from(): settings size equal");
        check(fromCd.toString() != null, "from(): toString non-null");

        // equality shape: EventTypeInfo does not override equals/hashCode (Object identity semantics)
        check(!declares(c, "equals"), "equals not overridden (identity semantics)");
        check(!declares(c, "hashCode"), "hashCode not overridden");
        EventTypeInfo fr2 = null;
        for (EventTypeInfo e : bean.getEventTypes()) if ("jdk.FileRead".equals(e.getName())) fr2 = e;
        check(fr != fr2, "getEventTypes returns fresh instances");
        check(fr.equals(fr) && !fr.equals(fr2), "equals behaves as identity");
        testBaselineWhiteBox();
        testBaselineBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ==== 基线断言原样保留（tranche-8 遗产；深化只增不删） ====
    static boolean baselineHasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testBaselineWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.management.jfr.EventTypeInfo");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !c.isEnum(), "concrete");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(baselineHasMethod(c, "getLabel"), "getLabel");
        check(baselineHasMethod(c, "getCategoryNames"), "getCategoryNames");
        check(baselineHasMethod(c, "getId"), "getId");
        check(baselineHasMethod(c, "getName"), "getName");
        check(baselineHasMethod(c, "getDescription"), "getDescription");
        check(baselineHasMethod(c, "getSettingDescriptors"), "getSettingDescriptors");
        check(baselineHasMethod(c, "from"), "from");

    }

    static void testBaselineBlackBox() throws Exception {
        if (!jdk.jfr.FlightRecorder.isAvailable()) { check(true, "JFR unavailable"); return; }
        FlightRecorderMXBean bean = ManagementFactory.getPlatformMXBean(FlightRecorderMXBean.class);
        List<EventTypeInfo> types = bean.getEventTypes();
        check(types != null, "event types list");
        if (!types.isEmpty()) {
            EventTypeInfo t = types.get(0);
            check(t.getId() >= 0, "id");
            check(t.getName() != null, "name");
            check(t.getLabel() != null, "label");
            check(t.getDescription() != null, "description");
            check(t.getCategoryNames() != null, "category names");
            check(t.getSettingDescriptors() != null, "setting descriptors");
            check(t.toString() != null, "toString");
        } else { check(true, "no event types available"); }

    }

}
