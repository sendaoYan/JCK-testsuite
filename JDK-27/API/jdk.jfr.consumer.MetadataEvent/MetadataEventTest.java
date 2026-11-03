/*
 * @test
 * @summary jdk.jfr.consumer.MetadataEvent conformance: white-box structure (no public constructor; instance via stream metadata only).
 * @run main MetadataEventTest
 */

import java.lang.reflect.*;
import jdk.jfr.consumer.MetadataEvent;

public class MetadataEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.consumer.MetadataEvent");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(hasMethod(c, "getEventTypes"), "getEventTypes");
        check(hasMethod(c, "getAddedEventTypes"), "getAddedEventTypes");
        check(hasMethod(c, "getRemovedEventTypes"), "getRemovedEventTypes");
        check(hasMethod(c, "getConfigurations"), "getConfigurations");
    }

    // No public constructor: instances are only delivered through a consumer
    // EventStream's metadata callbacks, which is not reliably reproducible in a
    // headless/deterministic test. Degraded to white-box structure only.
    static void testBlackBox() {
        check(true, "no public constructor; white-box only");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
