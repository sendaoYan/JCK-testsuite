/*
 * @test
 * @summary jdk.management.jfr.RecordingInfo conformance: value-object getters + CompositeData round-trip.
 * @run main RecordingInfoTest
 */

import jdk.management.jfr.*;
import javax.management.*;
import javax.management.openmbean.CompositeData;
import java.lang.management.*;
import java.lang.reflect.*;
import java.util.*;

public class RecordingInfoTest {
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
        Class<?> c = Class.forName("jdk.management.jfr.RecordingInfo");
        check(c != null, "jdk.management.jfr.RecordingInfo loads");
        check(c.getName().equals("jdk.management.jfr.RecordingInfo"), "binary name is jdk.management.jfr.RecordingInfo");
        check(Modifier.isPublic(c.getModifiers()), "jdk.management.jfr.RecordingInfo is public");
        check(!c.isInterface(), "jdk.management.jfr.RecordingInfo is not an interface");
        check(!Modifier.isAbstract(c.getModifiers()), "jdk.management.jfr.RecordingInfo is concrete");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass is java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "from"), "declares from(CompositeData)");

        FlightRecorderMXBean bean = ManagementFactory.getPlatformMXBean(FlightRecorderMXBean.class);
        check(bean != null, "bean present");
        long id = bean.newRecording();
        try {
            bean.setPredefinedConfiguration(id, "default");
            // NEW state getters
            RecordingInfo ri = null;
            for (RecordingInfo x : bean.getRecordings()) if (x.getId() == id) ri = x;
            check(ri != null, "RecordingInfo listed via getRecordings()");
            check(ri.getId() == id, "getId");
            check("NEW".equals(ri.getState()), "state NEW before start (was " + ri.getState() + ")");
            check(ri.getName() != null && !ri.getName().isEmpty(), "name non-null");
            check(ri.getSettings() != null && ri.getSettings().size() > 1, "settings populated from configuration");
            check(ri.getDestination() == null, "destination null by default");
            check(ri.getDumpOnExit() == false, "dumpOnExit false by default");
            check(ri.getMaxAge() == 0, "maxAge 0 by default");
            check(ri.getMaxSize() == 0, "maxSize 0 by default");
            check(ri.getDuration() == 0, "duration 0 by default");
            check(ri.getSize() >= 0, "size >= 0");
            check(ri.isToDisk() == true, "toDisk true by default");
            check(ri.getStartTime() == 0, "startTime 0 before start");
            check(ri.getStopTime() == 0, "stopTime 0 before start");
            check(ri.toString() != null && ri.toString().contains(String.valueOf(id)), "toString mentions id");

            // state transitions mirrored in RecordingInfo
            bean.startRecording(id);
            for (RecordingInfo x : bean.getRecordings()) if (x.getId() == id) ri = x;
            check("RUNNING".equals(ri.getState()), "state RUNNING while recording");
            check(ri.getStartTime() > 0, "startTime set after start");
            bean.stopRecording(id);
            for (RecordingInfo x : bean.getRecordings()) if (x.getId() == id) ri = x;
            check("STOPPED".equals(ri.getState()), "state STOPPED after stop");
            check(ri.getStopTime() >= ri.getStartTime(), "stopTime >= startTime");

            // dual channel: MBeanServer Recordings attribute row -> from(CompositeData) equivalence
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName on = new ObjectName(FlightRecorderMXBean.MXBEAN_NAME);
            CompositeData[] rows = (CompositeData[]) mbs.getAttribute(on, "Recordings");
            CompositeData row = null;
            for (CompositeData r : rows) if (((Number) r.get("id")).longValue() == id) row = r;
            check(row != null, "CompositeData row found via MBeanServer");
            RecordingInfo fromCd = RecordingInfo.from(row);
            check(fromCd.getId() == ri.getId(), "from(): id equal");
            check(fromCd.getState().equals(ri.getState()), "from(): state equal");
            check(fromCd.getName().equals(ri.getName()), "from(): name equal");
            check(fromCd.getDestination() == null ? ri.getDestination() == null : fromCd.getDestination().equals(ri.getDestination()), "from(): destination equal");
            check(fromCd.getDumpOnExit() == ri.getDumpOnExit(), "from(): dumpOnExit equal");
            check(fromCd.getMaxAge() == ri.getMaxAge(), "from(): maxAge equal");
            check(fromCd.getMaxSize() == ri.getMaxSize(), "from(): maxSize equal");
            check(fromCd.getDuration() == ri.getDuration(), "from(): duration equal");
            check(fromCd.getSize() == ri.getSize(), "from(): size equal");
            check(fromCd.isToDisk() == ri.isToDisk(), "from(): toDisk equal");
            check(fromCd.getStartTime() == ri.getStartTime(), "from(): startTime equal");
            check(fromCd.getStopTime() == ri.getStopTime(), "from(): stopTime equal");
            // Verified quirk (identical on JDK-8 and JDK-27, see openjdk RecordingInfo(CompositeData)):
            // when settings arrive as MXBean TabularData, from() rebuilds the map using the TabularData
            // index name ("key") as the map key and the row key as the value, so every row overwrites
            // the same entry: the round-tripped map collapses to a single "key" -> <some settings key>.
            Map<String, String> fromSettings = fromCd.getSettings();
            check(fromSettings != null && !fromSettings.isEmpty(), "from(): settings non-empty (collapsed)");
            check(fromSettings.size() == 1 && fromSettings.containsKey("key"),
                  "from(): settings collapsed to single entry keyed by TabularData index name 'key'");
            check(ri.getSettings().containsKey(fromSettings.get("key")),
                  "from(): collapsed value is one of the direct-channel settings keys");
            check(fromCd.toString() != null, "from(): toString non-null");
        } finally {
            bean.closeRecording(id);
        }
        boolean gone = true;
        for (RecordingInfo x : bean.getRecordings()) if (x.getId() == id) gone = false;
        check(gone, "recording info removed after closeRecording");
        testBaselineWhiteBox();
        testBaselineBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ==== 基线断言原样保留（tranche-8 遗产；深化只增不删） ====
    static boolean baselineHasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testBaselineWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.management.jfr.RecordingInfo");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !c.isEnum(), "concrete");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(baselineHasMethod(c, "getName"), "getName");
        check(baselineHasMethod(c, "getId"), "getId");
        check(baselineHasMethod(c, "getState"), "getState");
        check(baselineHasMethod(c, "getStartTime"), "getStartTime");
        check(baselineHasMethod(c, "getStopTime"), "getStopTime");
        check(baselineHasMethod(c, "getMaxAge"), "getMaxAge");
        check(baselineHasMethod(c, "getMaxSize"), "getMaxSize");
        check(baselineHasMethod(c, "getSettings"), "getSettings");
        check(baselineHasMethod(c, "getDestination"), "getDestination");
        check(baselineHasMethod(c, "from"), "from");

    }

    static void testBaselineBlackBox() throws Exception {
        if (!jdk.jfr.FlightRecorder.isAvailable()) { check(true, "JFR unavailable"); return; }
        FlightRecorderMXBean bean = ManagementFactory.getPlatformMXBean(FlightRecorderMXBean.class);
        long id = bean.newRecording();
        RecordingInfo ri = null;
        for (RecordingInfo r : bean.getRecordings()) { if (r.getId() == id) { ri = r; break; } }
        check(ri != null, "recording found");
        check(ri.getId() == id, "id");
        check(ri.getName() != null, "name");
        check(ri.getState() != null, "state");
        check(ri.getSettings() != null, "settings");
        check(ri.toString() != null, "toString");
        bean.closeRecording(id);

    }

}
