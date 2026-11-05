/*
 * @test
 * @summary jdk.management.jfr.RecordingInfo conformance: reflection shape + lifecycle getters + CompositeData channels.
 * @run main RecordingInfoTest
 */

import jdk.management.jfr.*;
import java.lang.management.ManagementFactory;
import java.lang.reflect.*;
import java.util.*;

import javax.management.*;
import javax.management.openmbean.*;

public class RecordingInfoTest {
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
        Class<?> c = Class.forName("jdk.management.jfr.RecordingInfo");
        check(c != null, "jdk.management.jfr.RecordingInfo loads");
        check(c.getName().equals("jdk.management.jfr.RecordingInfo"), "binary name is jdk.management.jfr.RecordingInfo");
        check(Modifier.isPublic(c.getModifiers()), "jdk.management.jfr.RecordingInfo is public");
        check(!c.isInterface(), "jdk.management.jfr.RecordingInfo is not an interface");
        check(!Modifier.isAbstract(c.getModifiers()), "jdk.management.jfr.RecordingInfo is concrete");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass is java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");

        // ---------- lifecycle getters across NEW -> RUNNING -> STOPPED ----------
        FlightRecorderMXBean bean = ManagementFactory.getPlatformMXBean(FlightRecorderMXBean.class);
        long id = bean.newRecording();
        try {
            bean.setPredefinedConfiguration(id, "default");
            RecordingInfo ri = null;
            for (RecordingInfo r : bean.getRecordings()) if (r.getId() == id) ri = r;
            check(ri != null, "listed via getRecordings()");
            check(ri.getId() == id, "getId");
            check("NEW".equals(ri.getState()), "state NEW before start");
            check(ri.getName() != null && !ri.getName().isEmpty(), "default name non-empty");
            check(ri.getSettings() != null && ri.getSettings().size() > 100, "settings populated from configuration (" + ri.getSettings().size() + ")");
            check(ri.getDestination() == null, "destination null by default");
            check(ri.getDumpOnExit() == false, "dumpOnExit false by default");
            check(ri.getMaxAge() == 0, "maxAge 0 default");
            check(ri.getMaxSize() == 0, "maxSize 0 default");
            check(ri.getDuration() == 0, "duration 0 default");
            check(ri.getSize() >= 0, "size >= 0");
            check(ri.isToDisk() == true, "toDisk true by default");
            check(ri.getStartTime() == 0, "startTime 0 before start");
            check(ri.getStopTime() == 0, "stopTime 0 before start");
            check(ri.toString() != null && ri.toString().contains(String.valueOf(id)), "toString mentions id");

            bean.startRecording(id);
            for (RecordingInfo r : bean.getRecordings()) if (r.getId() == id) ri = r;
            check("RUNNING".equals(ri.getState()), "state RUNNING while started");
            check(ri.getStartTime() > 0, "startTime set after start");
            bean.stopRecording(id);
            for (RecordingInfo r : bean.getRecordings()) if (r.getId() == id) ri = r;
            check("STOPPED".equals(ri.getState()), "state STOPPED after stop");
            check(ri.getStopTime() >= ri.getStartTime(), "stopTime >= startTime");
            check(ri.getSize() >= 0, "size >= 0 when stopped");

            // ---------- MXBean channel row shape ----------
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName on = new ObjectName(FlightRecorderMXBean.MXBEAN_NAME);
            CompositeData[] rows = (CompositeData[]) mbs.getAttribute(on, "Recordings");
            CompositeData row = null;
            for (CompositeData r : rows) if (((Number) r.get("id")).longValue() == id) row = r;
            check(row != null, "CompositeData row via MBeanServer");
            java.util.Set<String> keys = new TreeSet<String>(row.getCompositeType().keySet());
            check(keys.equals(new TreeSet<String>(Arrays.asList("destination", "dumpOnExit", "duration", "id", "maxAge", "maxSize",
                     "name", "settings", "size", "startTime", "state", "stopTime", "toDisk"))),
                  "row item names (13 incl. toDisk)");
            check(row.getCompositeType().getType("id") == SimpleType.LONG, "row id item is LONG");

            // ---------- verified JDK-8 negative: from() cannot consume its own MXBean row ----------
            // RecordingInfo.from() narrows cd.get("id") with (int) while the MXBean emits a Long:
            // ClassCastException every time, on every JDK-8 build carrying the backport.
            try {
                RecordingInfo.from(row);
                check(false, "from(mbeanRow) should throw ClassCastException on JDK-8");
            } catch (ClassCastException e) {
                check(e.getMessage() != null && e.getMessage().contains("Long") && e.getMessage().contains("Integer"),
                      "from(mbeanRow) CCE Long->Integer, got: " + e.getMessage());
            }

            // ---------- verified positive: hand-built legacy wire shape (Integer id, disk key) ----------
            CompositeType kvType = new CompositeType("kv", "kv",
                new String[]{"key", "value"}, new String[]{"key", "value"},
                new OpenType[]{SimpleType.STRING, SimpleType.STRING});
            TabularType settingsType = new TabularType("settings", "settings", kvType, new String[]{"key"});
            TabularData td = new TabularDataSupport(settingsType);
            td.put(new CompositeDataSupport(kvType, new String[]{"key", "value"}, new Object[]{"a#enabled", "true"}));
            td.put(new CompositeDataSupport(kvType, new String[]{"key", "value"}, new Object[]{"b#threshold", "1 s"}));
            CompositeType legacy = new CompositeType("RecordingInfo", "RecordingInfo",
                new String[]{"id", "name", "state", "dumpOnExit", "size", "disk", "maxAge", "maxSize",
                             "startTime", "stopTime", "destination", "duration", "settings"},
                new String[]{"id", "name", "state", "dumpOnExit", "size", "disk", "maxAge", "maxSize",
                             "startTime", "stopTime", "destination", "duration", "settings"},
                new OpenType[]{SimpleType.INTEGER, SimpleType.STRING, SimpleType.STRING, SimpleType.BOOLEAN,
                    SimpleType.LONG, SimpleType.BOOLEAN, SimpleType.LONG, SimpleType.LONG, SimpleType.LONG,
                    SimpleType.LONG, SimpleType.STRING, SimpleType.LONG, settingsType});
            CompositeData cd = new CompositeDataSupport(legacy,
                new String[]{"id", "name", "state", "dumpOnExit", "size", "disk", "maxAge", "maxSize",
                             "startTime", "stopTime", "destination", "duration", "settings"},
                new Object[]{Integer.valueOf(7), "probe", "STOPPED", Boolean.FALSE, Long.valueOf(42),
                    Boolean.TRUE, Long.valueOf(0), Long.valueOf(0), Long.valueOf(11), Long.valueOf(22),
                    null, Long.valueOf(0), td});
            RecordingInfo fromCd = RecordingInfo.from(cd);
            check(fromCd.getId() == 7, "from(legacy): id");
            check("probe".equals(fromCd.getName()), "from(legacy): name");
            check("STOPPED".equals(fromCd.getState()), "from(legacy): state");
            check(fromCd.getDumpOnExit() == false, "from(legacy): dumpOnExit");
            check(fromCd.getSize() == 42, "from(legacy): size");
            check(fromCd.isToDisk() == true, "from(legacy): toDisk via legacy 'disk' item");
            check(fromCd.getStartTime() == 11 && fromCd.getStopTime() == 22, "from(legacy): times");
            check(fromCd.getDestination() == null, "from(legacy): destination null");
            check(fromCd.getDuration() == 0, "from(legacy): duration");
            // verified quirk (identical to JDK-27): TabularData settings collapse to a single entry
            // keyed by the TabularData index name, holding the last row's key
            Map<String, String> fs = fromCd.getSettings();
            check(fs.size() == 1 && "b#threshold".equals(fs.get("key")),
                  "from(legacy): settings collapse to {key=last row key}, got " + fs);
        } finally {
            bean.closeRecording(id);
        }
        boolean gone = true;
        for (RecordingInfo r : bean.getRecordings()) if (r.getId() == id) gone = false;
        check(gone, "removed after closeRecording");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
