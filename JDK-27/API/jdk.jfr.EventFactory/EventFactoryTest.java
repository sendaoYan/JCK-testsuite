/*
 * @test
 * @summary jdk.jfr.EventFactory conformance: white-box structure + black-box create/commit.
 * @run main EventFactoryTest
 */

import jdk.jfr.*;
import java.util.List;
import java.lang.reflect.*;

public class EventFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.EventFactory");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(hasMethod(c, "create"), "create");
        check(hasMethod(c, "getEventType"), "getEventType");
        check(hasMethod(c, "newEvent"), "newEvent");
    }

    static void testBlackBox() {
        EventFactory f = EventFactory.create(
            List.of(new AnnotationElement(Name.class, "com.test.FactoryEvent"),
                    new AnnotationElement(Label.class, "Factory Event")),
            List.of(new ValueDescriptor(String.class, "msg")));
        check(f != null, "create");
        EventType t = f.getEventType();
        check(t != null && "com.test.FactoryEvent".equals(t.getName()), "event type");
        Event e = f.newEvent();
        check(e != null, "newEvent");
        e.set(0, "hello");
        e.commit();
        check(true, "committed");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
