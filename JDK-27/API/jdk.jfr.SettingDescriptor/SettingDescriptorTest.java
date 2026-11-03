/*
 * @test
 * @summary jdk.jfr.SettingDescriptor conformance: white-box structure + black-box from event type.
 * @run main SettingDescriptorTest
 */

import java.lang.reflect.*;
import jdk.jfr.*;
import java.util.List;

public class SettingDescriptorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    @Name("com.test.SDEvent")
    static class SDEvent extends Event { }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.SettingDescriptor");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(hasMethod(c, "getName"), "getName");
        check(hasMethod(c, "getTypeName"), "getTypeName");
        check(hasMethod(c, "getDefaultValue"), "getDefaultValue");
        check(hasMethod(c, "getLabel"), "getLabel");
    }

    static void testBlackBox() {
        EventType t = EventType.getEventType(SDEvent.class);
        check(t != null, "event type");
        List<SettingDescriptor> ds = t.getSettingDescriptors();
        check(ds != null && !ds.isEmpty(), "has setting descriptors");
        boolean sawEnabled = false;
        for (SettingDescriptor d : ds) {
            check(d.getName() != null, "name non-null");
            check(d.getTypeName() != null, "typeName non-null");
            if ("enabled".equals(d.getName())) sawEnabled = true;
        }
        check(sawEnabled, "has 'enabled' setting");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
