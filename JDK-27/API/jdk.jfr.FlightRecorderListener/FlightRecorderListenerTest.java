/*
 * @test
 * @summary jdk.jfr.FlightRecorderListener conformance: white-box structure + black-box default methods.
 * @run main FlightRecorderListenerTest
 */

import java.lang.reflect.*;
import jdk.jfr.*;

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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
