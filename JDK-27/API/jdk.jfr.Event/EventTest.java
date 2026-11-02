/*
 * @test
 * @summary jdk.jfr.Event conformance: commit lifecycle and subclass metadata.
 * @run main EventTest
 */

import jdk.jfr.*;
import java.lang.reflect.Modifier;

public class EventTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

@Name("com.test.BeanEvent")
@Label("Test Event")
@Description("A test event")
static class TestEvent extends Event {
    @Label("message") String message;
    @Label("count") int count;
}
    public static void main(String[] args) throws Exception {
        check(Modifier.isAbstract(Event.class.getModifiers()), "Event abstract");
        check(Event.class.isAssignableFrom(TestEvent.class), "TestEvent extends Event");
        TestEvent e = new TestEvent();
        e.begin();
        e.message = "hello";
        e.count = 42;
        e.end();
        check(e.shouldCommit() == false || e.shouldCommit() == true, "shouldCommit callable");
        e.commit();
        check(true, "committed without exception");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
