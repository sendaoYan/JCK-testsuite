/*
 * @test
 * @summary jdk.jfr.FlightRecorder conformance: type shape via reflection (headless-safe).
 * @run main FlightRecorderTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import jdk.jfr.*;
import java.util.*;

public class FlightRecorderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.FlightRecorder", false, FlightRecorderTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasMethod(c, "addListener"), "declares addListener");
        check(hasMethod(c, "addPeriodicEvent"), "declares addPeriodicEvent");
        check(hasMethod(c, "getEventTypes"), "declares getEventTypes");
        check(hasMethod(c, "getFlightRecorder"), "declares getFlightRecorder");
        check(hasMethod(c, "getRecordings"), "declares getRecordings");
        check(hasMethod(c, "isAvailable"), "declares isAvailable");
        check(hasMethod(c, "isInitialized"), "declares isInitialized");
        check(hasMethod(c, "register"), "declares register");
        testDeepSingletonAndAvail();
        testDeepRegisterMachine();
        testDeepRegistryNames();
        testDeepRecordingsLifecycle();
        testDeepSnapshot();
        testDeepListener();
        testDeepPeriodic();
        testDeepExceptionDecls();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ================= deepening: contract coverage =================
    static void expectEx(String what, Class<? extends Throwable> type, String contains, Runnable r) {
        try { r.run(); check(false, what + ": expected " + type.getSimpleName()); }
        catch (Throwable t) {
            check(type.isInstance(t), what + ": expected " + type.getSimpleName() + " got " + t.getClass().getName() + " (" + t.getMessage() + ")");
            check(contains == null || String.valueOf(t.getMessage()).contains(contains), what + ": msg '" + t.getMessage() + "' lacks '" + contains + "'");
        }
    }
    static boolean typeRegistered(String name) {
        for (EventType t : FlightRecorder.getFlightRecorder().getEventTypes()) if (t.getName().equals(name)) return true;
        return false;
    }

    @Name("test.FlightRecorder.FR")
    static class FREvent extends Event { long x; }

    static void testDeepSingletonAndAvail() {
        check(FlightRecorder.isAvailable(), "fr isAvailable on HotSpot");
        check(!FlightRecorder.isInitialized(), "fr not initialized before first getFlightRecorder()");
        FlightRecorder fr1 = FlightRecorder.getFlightRecorder();
        check(fr1 != null, "fr getFlightRecorder non-null");
        check(FlightRecorder.isInitialized(), "fr initialized after getFlightRecorder()");
        FlightRecorder fr2 = FlightRecorder.getFlightRecorder();
        check(fr1 == fr2, "fr singleton identity");
    }

    static void testDeepRegisterMachine() throws Exception {
        EventType.getEventType(FREvent.class);
        check(typeRegistered("test.FlightRecorder.FR"), "fr implicit registration on first metadata access");
        FlightRecorder.unregister(FREvent.class);
        check(!typeRegistered("test.FlightRecorder.FR"), "fr absent after unregister");
        expectEx("fr getEventType after unregister", IllegalStateException.class, "is not registered", () -> EventType.getEventType(FREvent.class));
        FlightRecorder.register(FREvent.class);
        check(typeRegistered("test.FlightRecorder.FR"), "fr present after re-register");
    }

    static void testDeepRegistryNames() {
        Set<String> names = new HashSet<String>();
        for (EventType t : FlightRecorder.getFlightRecorder().getEventTypes()) names.add(t.getName());
        check(names.contains("jdk.ThreadStart"), "fr registry contains jdk.ThreadStart");
        check(names.contains("jdk.NativeMethodSample"), "fr registry contains jdk.NativeMethodSample");
        // version-diff: jdk.ObjectAllocationSample is JDK-14+; absent on JDK-8
        check(!names.contains("jdk.ObjectAllocationSample"), "fr jdk.ObjectAllocationSample absent on JDK-8");
        boolean allWellFormed = true;
        for (String n : names) if (n == null || n.isEmpty()) allWellFormed = false;
        check(allWellFormed, "fr all registered names non-empty");
    }

    static void testDeepRecordingsLifecycle() {
        FlightRecorder fr = FlightRecorder.getFlightRecorder();
        Recording r = new Recording();
        check(fr.getRecordings().contains(r), "fr getRecordings contains live recording");
        r.close();
        check(!fr.getRecordings().contains(r), "fr getRecordings drops closed recording");
    }

    static void testDeepSnapshot() throws Exception {
        FlightRecorder fr = FlightRecorder.getFlightRecorder();
        Recording snap = fr.takeSnapshot();
        try {
            check(snap != null, "fr takeSnapshot non-null");
            check(snap.getState() == RecordingState.STOPPED, "fr snapshot state STOPPED");
            check(snap.getSize() >= 0, "fr snapshot size >= 0");
        } finally { snap.close(); }
    }

    static void testDeepListener() throws Exception {
        FlightRecorder fr = FlightRecorder.getFlightRecorder();
        final List<String> events = Collections.synchronizedList(new ArrayList<String>());
        final FlightRecorder[] seen = new FlightRecorder[1];
        FlightRecorderListener li = new FlightRecorderListener() {
            public void recorderInitialized(FlightRecorder f) { seen[0] = f; events.add("init"); }
            public void recordingStateChanged(Recording r) { events.add(String.valueOf(r.getState())); }
        };
        fr.addListener(li);
        check(events.contains("init"), "fr listener init callback fires immediately on already-initialized recorder");
        check(seen[0] == fr, "fr recorderInitialized passes the singleton");
        Recording r = new Recording();
        r.start();
        long deadline = System.nanoTime() + 2_000_000_000L;
        while (!events.contains("RUNNING") && System.nanoTime() < deadline) Thread.sleep(10);
        check(events.contains("RUNNING"), "fr listener saw RUNNING");
        r.stop();
        deadline = System.nanoTime() + 2_000_000_000L;
        while (!events.contains("STOPPED") && System.nanoTime() < deadline) Thread.sleep(10);
        check(events.contains("STOPPED"), "fr listener saw STOPPED");
        r.close();
        deadline = System.nanoTime() + 2_000_000_000L;
        while (!events.contains("CLOSED") && System.nanoTime() < deadline) Thread.sleep(10);
        check(events.contains("CLOSED"), "fr listener saw CLOSED");
        fr.removeListener(li);
        check(true, "fr removeListener returns normally");
    }

    static volatile int periodicCount = 0;
    static void testDeepPeriodic() throws Exception {
        Runnable per = new Runnable() { public void run() { periodicCount++; } };
        FlightRecorder.addPeriodicEvent(FREvent.class, per);
        try {
            try (Recording r = new Recording()) {
                r.enable("test.FlightRecorder.FR").with("period", "10 ms");
                r.start();
                long deadline = System.nanoTime() + 2_000_000_000L;
                while (periodicCount < 1 && System.nanoTime() < deadline) Thread.sleep(20);
                check(periodicCount >= 1, "fr periodic runnable executed while recording, count=" + periodicCount);
                r.stop();
            }
        } finally {
            check(FlightRecorder.removePeriodicEvent(per), "fr removePeriodicEvent true for registered runnable");
        }
        check(!FlightRecorder.removePeriodicEvent(per), "fr removePeriodicEvent false after removal");
    }

    static void testDeepExceptionDecls() throws Exception {
        // version-diff: JDK-8 declares SecurityException on these three methods; JDK-27 does not
        Class<?> c = Class.forName("jdk.jfr.FlightRecorder");
        boolean secGet = false, secAdd = false, secRemove = false;
        for (Method m : c.getDeclaredMethods()) {
            if (!m.getName().equals("getFlightRecorder") && !m.getName().equals("addPeriodicEvent") && !m.getName().equals("removePeriodicEvent")) continue;
            boolean has = false;
            for (Class<?> ex : m.getExceptionTypes()) if (ex == SecurityException.class) has = true;
            if (m.getName().equals("getFlightRecorder")) secGet = has;
            if (m.getName().equals("addPeriodicEvent")) secAdd = has;
            if (m.getName().equals("removePeriodicEvent")) secRemove = has;
        }
        check(secGet, "fr JDK-8 getFlightRecorder declares SecurityException");
        check(secAdd, "fr JDK-8 addPeriodicEvent declares SecurityException");
        check(secRemove, "fr JDK-8 removePeriodicEvent declares SecurityException");
    }
}
