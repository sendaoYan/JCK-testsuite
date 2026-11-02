/*
 * @test
 * @summary jdk.jfr.EventType conformance: from a custom event class with fields and settings.
 * @run main EventTypeTest
 */

import jdk.jfr.*;

public class EventTypeTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

@Name("com.test.ETEvent")
@Label("ET")
@Category("Test")
static class ETEvent extends Event {
    @Label("message") String message;
}
    public static void main(String[] args) throws Exception {
        EventType t = EventType.getEventType(ETEvent.class);
        check(t != null, "getEventType");
        check("com.test.ETEvent".equals(t.getName()), "name");
        check("ET".equals(t.getLabel()), "label");
        check(t.getFields() != null && t.getFields().size() >= 1, "fields");
        ValueDescriptor f = t.getField("message");
        check(f != null && "message".equals(f.getName()), "field message");
        check(t.getCategoryNames() != null, "categories");
        check(t.getAnnotationElements() != null, "annotation elements");
        check(t.getSettingDescriptors() != null, "setting descriptors");
        check(t.isEnabled() == false || t.isEnabled() == true, "isEnabled callable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
