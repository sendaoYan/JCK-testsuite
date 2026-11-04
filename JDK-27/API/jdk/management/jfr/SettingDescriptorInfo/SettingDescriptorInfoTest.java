/*
 * @test
 * @summary jdk.management.jfr.SettingDescriptorInfo conformance: white-box structure + black-box behavior.
 * @run main SettingDescriptorInfoTest
 */

import jdk.management.jfr.*;
import javax.management.*;
import java.lang.management.*;
import java.lang.reflect.*;
import java.util.*;
public class SettingDescriptorInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.management.jfr.SettingDescriptorInfo");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !c.isEnum(), "concrete");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(hasMethod(c, "getLabel"), "getLabel");
        check(hasMethod(c, "getName"), "getName");
        check(hasMethod(c, "getDescription"), "getDescription");
        check(hasMethod(c, "getTypeName"), "getTypeName");
        check(hasMethod(c, "getContentType"), "getContentType");
        check(hasMethod(c, "getDefaultValue"), "getDefaultValue");
        check(hasMethod(c, "from"), "from");

    }

    static void testBlackBox() throws Exception {
        if (!jdk.jfr.FlightRecorder.isAvailable()) { check(true, "JFR unavailable"); return; }
        FlightRecorderMXBean bean = ManagementFactory.getPlatformMXBean(FlightRecorderMXBean.class);
        List<EventTypeInfo> types = bean.getEventTypes();
        if (!types.isEmpty()) {
            List<SettingDescriptorInfo> descs = types.get(0).getSettingDescriptors();
            check(descs != null, "setting descriptors");
            if (!descs.isEmpty()) {
                SettingDescriptorInfo d = descs.get(0);
                check(d.getName() != null, "name");
                check(d.getLabel() != null, "label");
                check(d.getDescription() != null, "description");
                check(d.getTypeName() != null, "typeName");
                check(d.getContentType() != null, "contentType");
                check(d.toString() != null, "toString");
            } else { check(true, "no descriptors"); }
        } else { check(true, "no event types"); }

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
