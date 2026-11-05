/*
 * @test
 * @summary jdk.jfr.SettingDescriptor conformance: white-box structure + black-box from event type.
 * @run main SettingDescriptorTest
 */

import java.lang.reflect.*;
import jdk.jfr.*;
import java.util.List;
import java.util.*;

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
    static class SDCustomEvent extends Event {
        @SettingDefinition boolean flavor(MyControl c) { return true; }
    }

    static void testDeepBuiltInDescriptors() throws Exception {
        EventType t = EventType.getEventType(SDCustomEvent.class);
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
        EventType t = EventType.getEventType(SDCustomEvent.class);
        SettingDescriptor flavor = null;
        for (SettingDescriptor sd : t.getSettingDescriptors()) if ("flavor".equals(sd.getName())) flavor = sd;
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
