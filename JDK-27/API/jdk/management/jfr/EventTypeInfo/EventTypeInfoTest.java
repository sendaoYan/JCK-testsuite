/*
 * @test
 * @summary jdk.management.jfr.EventTypeInfo conformance: white-box structure + black-box behavior.
 * @run main EventTypeInfoTest
 */

import jdk.management.jfr.*;
import javax.management.*;
import java.lang.management.*;
import java.lang.reflect.*;
import java.util.*;
public class EventTypeInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.management.jfr.EventTypeInfo");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !c.isEnum(), "concrete");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(hasMethod(c, "getLabel"), "getLabel");
        check(hasMethod(c, "getCategoryNames"), "getCategoryNames");
        check(hasMethod(c, "getId"), "getId");
        check(hasMethod(c, "getName"), "getName");
        check(hasMethod(c, "getDescription"), "getDescription");
        check(hasMethod(c, "getSettingDescriptors"), "getSettingDescriptors");
        check(hasMethod(c, "from"), "from");

    }

    static void testBlackBox() throws Exception {
        if (!jdk.jfr.FlightRecorder.isAvailable()) { check(true, "JFR unavailable"); return; }
        FlightRecorderMXBean bean = ManagementFactory.getPlatformMXBean(FlightRecorderMXBean.class);
        List<EventTypeInfo> types = bean.getEventTypes();
        check(types != null, "event types list");
        if (!types.isEmpty()) {
            EventTypeInfo t = types.get(0);
            check(t.getId() >= 0, "id");
            check(t.getName() != null, "name");
            check(t.getLabel() != null, "label");
            check(t.getDescription() != null, "description");
            check(t.getCategoryNames() != null, "category names");
            check(t.getSettingDescriptors() != null, "setting descriptors");
            check(t.toString() != null, "toString");
        } else { check(true, "no event types available"); }

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
