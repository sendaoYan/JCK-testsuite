/*
 * @test
 * @summary jdk.jfr.SettingDescriptor conformance: type shape via reflection (headless-safe).
 * @run main SettingDescriptorTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import jdk.jfr.*;
import java.util.*;

public class SettingDescriptorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.SettingDescriptor", false, SettingDescriptorTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasMethod(c, "getAnnotation"), "declares getAnnotation");
        check(hasMethod(c, "getAnnotationElements"), "declares getAnnotationElements");
        check(hasMethod(c, "getContentType"), "declares getContentType");
        check(hasMethod(c, "getDefaultValue"), "declares getDefaultValue");
        check(hasMethod(c, "getDescription"), "declares getDescription");
        check(hasMethod(c, "getLabel"), "declares getLabel");
        check(hasMethod(c, "getName"), "declares getName");
        check(hasMethod(c, "getTypeId"), "declares getTypeId");
        testDeepBuiltInDescriptors();
        testDeepCustomDescriptor();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ================= deepening: contract coverage =================
    static class MyControl extends SettingControl {
        public String combine(Set<String> s) { return String.join("|", s); }
        public void setValue(String s) { }
        public String getValue() { return "none"; }
    }
    @Name("test.SettingDescriptor.SD")
    static class SDEvent extends Event {
        @SettingDefinition boolean flavor(MyControl c) { return true; }
    }

    static Map<String, SettingDescriptor> descriptorsOf(String eventName) throws Exception {
        Map<String, SettingDescriptor> m = new HashMap<String, SettingDescriptor>();
        for (SettingDescriptor sd : EventType.getEventType(SDEvent.class).getSettingDescriptors()) m.put(sd.getName(), sd);
        return m;
    }

    static void testDeepBuiltInDescriptors() throws Exception {
        EventType t = EventType.getEventType(SDEvent.class);
        Map<String, SettingDescriptor> m = new HashMap<String, SettingDescriptor>();
        for (SettingDescriptor sd : t.getSettingDescriptors()) m.put(sd.getName(), sd);
        check(m.size() == 4, "sd custom event exposes 3 built-in + 1 custom, got " + m.size());
        SettingDescriptor enabled = m.get("enabled");
        check("true".equals(enabled.getDefaultValue()), "sd enabled default 'true'");
        check("jdk.jfr.BooleanFlag".equals(enabled.getContentType()), "sd enabled contentType jdk.jfr.BooleanFlag");
        check(enabled.getLabel() != null, "sd enabled has label");
        check(enabled.getDescription() != null, "sd enabled has description");
        check(enabled.getTypeId() > 0, "sd enabled typeId > 0");
        SettingDescriptor threshold = m.get("threshold");
        check("0 ns".equals(threshold.getDefaultValue()), "sd threshold default '0 ns'");
        check("jdk.jfr.Timespan".equals(threshold.getContentType()), "sd threshold contentType jdk.jfr.Timespan");
        check("Threshold".equals(threshold.getLabel()), "sd threshold label 'Threshold'");
        check(threshold.getDescription() != null, "sd threshold has description");
        check(threshold.getTypeName() != null, "sd threshold typeName non-null");
        SettingDescriptor stackTrace = m.get("stackTrace");
        check("true".equals(stackTrace.getDefaultValue()), "sd stackTrace default 'true'");
        check("jdk.jfr.BooleanFlag".equals(stackTrace.getContentType()), "sd stackTrace contentType jdk.jfr.BooleanFlag");
        // setting-derived annotations (e.g. @Timespan) are NOT returned by getAnnotation
        check(threshold.getAnnotation(Timespan.class) == null, "sd threshold getAnnotation(Timespan) null");
        // names are well-formed identifiers
        for (SettingDescriptor sd : m.values()) {
            check(sd.getName() != null && !sd.getName().isEmpty(), "sd name non-empty: " + sd.getName());
            check(sd.getTypeName() != null, "sd typeName non-null");
        }
    }

    static void testDeepCustomDescriptor() throws Exception {
        Map<String, SettingDescriptor> m = descriptorsOf("test.SettingDescriptor.SD");
        SettingDescriptor flavor = m.get("flavor");
        check(flavor != null, "sd custom descriptor present");
        check("none".equals(flavor.getDefaultValue()), "sd custom default from fresh control getValue()");
        check(flavor.getTypeName().endsWith("MyControl"), "sd custom typeName is control class, got " + flavor.getTypeName());
        check(flavor.getLabel() == null, "sd custom label null");
        check(flavor.getDescription() == null, "sd custom description null");
        check(flavor.getContentType() == null, "sd custom contentType null (no @ContentType machinery for controls)");
        check(flavor.getAnnotationElements().isEmpty(), "sd custom annotationElements empty");
        check(flavor.getAnnotation(Label.class) == null, "sd custom getAnnotation(Label) null");
        check(flavor.getTypeId() > 0, "sd custom typeId > 0");
    }
}
