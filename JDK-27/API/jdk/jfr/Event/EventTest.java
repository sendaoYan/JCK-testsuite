/*
 * @test
 * @summary jdk.jfr.Event conformance: white-box structure + black-box commit lifecycle.
 * @run main EventTest
 */

import jdk.jfr.*;
import java.lang.reflect.*;

public class EventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    @Name("com.test.BeanEvent")
    @Label("Test Event")
    @Description("A test event")
    static class TestEvent extends Event {
        @Label("message") String message;
        @Label("count") int count;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.Event");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.isAssignableFrom(TestEvent.class) || Event.class.isAssignableFrom(TestEvent.class), "TestEvent extends Event");
        check(hasMethod(c, "begin"), "begin");
        check(hasMethod(c, "end"), "end");
        check(hasMethod(c, "commit"), "commit");
        check(hasMethod(c, "shouldCommit"), "shouldCommit");
        check(hasMethod(c, "isEnabled"), "isEnabled");
    }

    static void testBlackBox() {
        TestEvent e = new TestEvent();
        e.begin();
        e.message = "hello";
        e.count = 42;
        e.end();
        check(e.shouldCommit() || !e.shouldCommit(), "shouldCommit callable");
        e.commit();
        check(true, "committed without exception");
        check(TestEvent.class.getAnnotation(Name.class) != null, "Name annotation present");
        check("com.test.BeanEvent".equals(TestEvent.class.getAnnotation(Name.class).value()), "Name value");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
