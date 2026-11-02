/*
 * @test
 * @summary jdk.jfr.EventFactory conformance: create factory, new event and commit.
 * @run main EventFactoryTest
 */

import jdk.jfr.*;
import java.util.List;

public class EventFactoryTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
