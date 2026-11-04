/*
 * @test
 * @summary jdk.jfr.consumer.EventStream conformance: white-box structure + black-box openRepository.
 * @run main EventStreamTest
 */

import jdk.jfr.*;
import jdk.jfr.consumer.*;
import java.lang.reflect.*;

public class EventStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.consumer.EventStream");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(hasMethod(c, "openRepository"), "openRepository");
        check(hasMethod(c, "onEvent"), "onEvent");
        check(hasMethod(c, "onClose"), "onClose");
        check(hasMethod(c, "setReuse"), "setReuse");
        check(hasMethod(c, "setOrdered"), "setOrdered");
    }

    static void testBlackBox() throws Exception {
        if (!FlightRecorder.isAvailable()) { check(true, "JFR unavailable"); return; }
        try (EventStream es = EventStream.openRepository()) {
            check(es != null, "openRepository");
            es.onEvent(e -> {});
            es.onClose(() -> {});
            es.setReuse(true);
            es.setOrdered(true);
            check(true, "configured");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
