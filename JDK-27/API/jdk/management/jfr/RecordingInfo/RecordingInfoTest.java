/*
 * @test
 * @summary jdk.management.jfr.RecordingInfo conformance: white-box structure + black-box behavior.
 * @run main RecordingInfoTest
 */

import jdk.management.jfr.*;
import javax.management.*;
import java.lang.management.*;
import java.lang.reflect.*;
import java.util.*;
public class RecordingInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.management.jfr.RecordingInfo");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !c.isEnum(), "concrete");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(hasMethod(c, "getName"), "getName");
        check(hasMethod(c, "getId"), "getId");
        check(hasMethod(c, "getState"), "getState");
        check(hasMethod(c, "getStartTime"), "getStartTime");
        check(hasMethod(c, "getStopTime"), "getStopTime");
        check(hasMethod(c, "getMaxAge"), "getMaxAge");
        check(hasMethod(c, "getMaxSize"), "getMaxSize");
        check(hasMethod(c, "getSettings"), "getSettings");
        check(hasMethod(c, "getDestination"), "getDestination");
        check(hasMethod(c, "from"), "from");

    }

    static void testBlackBox() throws Exception {
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
