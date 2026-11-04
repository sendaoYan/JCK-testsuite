/*
 * @test
 * @summary jdk.jfr.FlightRecorder conformance: white-box structure + black-box availability/types.
 * @run main FlightRecorderTest
 */

import jdk.jfr.*;
import java.lang.reflect.*;

public class FlightRecorderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.FlightRecorder");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(hasMethod(c, "getFlightRecorder"), "getFlightRecorder");
        check(hasMethod(c, "isAvailable"), "isAvailable");
        check(hasMethod(c, "isInitialized"), "isInitialized");
        check(hasMethod(c, "getEventTypes"), "getEventTypes");
        check(hasMethod(c, "getRecordings"), "getRecordings");
    }

    static void testBlackBox() {
        boolean avail = FlightRecorder.isAvailable();
        check(avail, "isAvailable");
        if (avail) {
            FlightRecorder fr = FlightRecorder.getFlightRecorder();
            check(fr != null, "getFlightRecorder");
            check(FlightRecorder.isInitialized(), "isInitialized");
            java.util.List<EventType> types = fr.getEventTypes();
            check(types != null && types.size() > 0, "event types non-empty");
            check(fr.getRecordings() != null, "recordings list");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
