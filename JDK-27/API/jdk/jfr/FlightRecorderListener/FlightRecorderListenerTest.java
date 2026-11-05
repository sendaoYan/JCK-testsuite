/*
 * @test
 * @summary jdk.jfr.FlightRecorderListener conformance: white-box structure + black-box default methods.
 * @run main FlightRecorderListenerTest
 */

import java.lang.reflect.*;
import jdk.jfr.*;
import java.util.*;

public class FlightRecorderListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.FlightRecorderListener");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "isInterface");
        check(hasMethod(c, "recorderInitialized"), "recorderInitialized");
        check(hasMethod(c, "recordingStateChanged"), "recordingStateChanged");
    }

    static void testBlackBox() {
        FlightRecorderListener l = new FlightRecorderListener() {
            public void recordingStateChanged(Recording r) { check(r == null, "default method dispatchable"); }
        };
        l.recorderInitialized(null);
        l.recordingStateChanged(null);
        check(true, "default methods called without exception");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepInterfaceShape();
        testDeepCallbackDelivery();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ================= deepening: contract coverage =================
    static void testDeepInterfaceShape() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.FlightRecorderListener");
        int methods = 0;
        for (Method m : c.getDeclaredMethods()) {
            methods++;
            check(m.getParameterTypes().length == 1, "fl method has exactly one parameter: " + m.getName());
        }
        check(methods == 2, "fl exactly 2 methods");
        // version-diff: on JDK-27 both methods are default implementations (JDK-8: abstract)
        boolean allDefault = true;
        for (Method m : c.getDeclaredMethods()) if (!m.isDefault()) allDefault = false;
        check(allDefault, "fl JDK-27 methods are default implementations");
        check(c.getDeclaredClasses().length == 0, "fl no member types");
    }

    static void testDeepCallbackDelivery() throws Exception {
        FlightRecorder fr = FlightRecorder.getFlightRecorder();
        final List<String> events = Collections.synchronizedList(new ArrayList<String>());
        final FlightRecorder[] seenRecorder = new FlightRecorder[1];
        final List<Recording> seenRecordings = Collections.synchronizedList(new ArrayList<Recording>());
        FlightRecorderListener li = new FlightRecorderListener() {
            public void recorderInitialized(FlightRecorder f) { seenRecorder[0] = f; events.add("init"); }
            public void recordingStateChanged(Recording r) { seenRecordings.add(r); events.add(String.valueOf(r.getState())); }
        };
        fr.addListener(li);
        // recorder already initialized -> callback fires synchronously during addListener
        check(events.contains("init"), "fl init callback delivered immediately");
        check(seenRecorder[0] == fr, "fl recorderInitialized receives the singleton instance");
        Recording r = new Recording();
        r.start();
        long deadline = System.nanoTime() + 2_000_000_000L;
        while (!events.contains("RUNNING") && System.nanoTime() < deadline) Thread.sleep(10);
        check(events.contains("RUNNING"), "fl recordingStateChanged saw RUNNING");
        r.stop();
        deadline = System.nanoTime() + 2_000_000_000L;
        while (!events.contains("STOPPED") && System.nanoTime() < deadline) Thread.sleep(10);
        check(events.contains("STOPPED"), "fl recordingStateChanged saw STOPPED");
        r.close();
        deadline = System.nanoTime() + 2_000_000_000L;
        while (!events.contains("CLOSED") && System.nanoTime() < deadline) Thread.sleep(10);
        check(events.contains("CLOSED"), "fl recordingStateChanged saw CLOSED");
        check(seenRecordings.contains(r), "fl recordingStateChanged receives our recording instance");
        fr.removeListener(li);
        check(true, "fl removeListener returns normally");
        fr.removeListener(li);
        check(true, "fl removeListener of unregistered listener tolerated");
    }
}
