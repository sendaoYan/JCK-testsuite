/*
 * @test
 * @summary jdk.jfr.EventType conformance: white-box structure + black-box from custom event.
 * @run main EventTypeTest
 */

import jdk.jfr.*;
import java.lang.reflect.*;

public class EventTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    @Name("com.test.ETEvent")
    @Label("ET")
    @Category("Test")
    static class ETEvent extends Event {
        @Label("message") String message;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.EventType");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(hasMethod(c, "getEventType"), "getEventType");
        check(hasMethod(c, "getName"), "getName");
        check(hasMethod(c, "getLabel"), "getLabel");
        check(hasMethod(c, "getFields"), "getFields");
        check(hasMethod(c, "getSettingDescriptors"), "getSettingDescriptors");
    }

    static void testBlackBox() {
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
        check(t.isEnabled() || !t.isEnabled(), "isEnabled callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
