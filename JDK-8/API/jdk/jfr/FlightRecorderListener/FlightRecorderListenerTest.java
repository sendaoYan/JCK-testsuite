/*
 * @test
 * @summary jdk.jfr.FlightRecorderListener conformance: type shape via reflection (headless-safe).
 * @run main FlightRecorderListenerTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import jdk.jfr.*;
import java.util.*;

public class FlightRecorderListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.FlightRecorderListener", false, FlightRecorderListenerTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasMethod(c, "recorderInitialized"), "declares recorderInitialized");
        check(hasMethod(c, "recordingStateChanged"), "declares recordingStateChanged");
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
        // both methods are default implementations (javap-8 does not display the
        // default flag, but isDefault() reflects it; identical on JDK-27)
        boolean allDefault = true;
        for (Method m : c.getDeclaredMethods()) if (!m.isDefault()) allDefault = false;
        check(allDefault, "fl methods are default implementations");
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
