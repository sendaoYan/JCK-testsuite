/*
 * @test
 * @summary jdk.jfr.EventType conformance: type shape via reflection (headless-safe).
 * @run main EventTypeTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import jdk.jfr.*;
import java.util.*;

public class EventTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.EventType", false, EventTypeTest.class.getClassLoader());
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
        check(hasMethod(c, "getCategoryNames"), "declares getCategoryNames");
        check(hasMethod(c, "getDescription"), "declares getDescription");
        check(hasMethod(c, "getEventType"), "declares getEventType");
        check(hasMethod(c, "getField"), "declares getField");
        check(hasMethod(c, "getFields"), "declares getFields");
        check(hasMethod(c, "getId"), "declares getId");
        testDeepMetadata();
        testDeepSettingDefaults();
        testDeepEnabledLifecycle();
        testDeepRegisteredState();
        testDeepRegistryMembership();
        testDeepDefaultCategory();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ================= deepening: contract coverage =================
    static void expectEx(String what, Class<? extends Throwable> type, String contains, Runnable r) {
        try { r.run(); check(false, what + ": expected " + type.getSimpleName()); }
        catch (Throwable t) {
            check(type.isInstance(t), what + ": expected " + type.getSimpleName() + " got " + t.getClass().getName() + " (" + t.getMessage() + ")");
            check(contains == null || String.valueOf(t.getMessage()).contains(contains), what + ": msg '" + t.getMessage() + "' lacks '" + contains + "'");
        }
    }
    static boolean typeRegistered(String name) {
        for (EventType t : FlightRecorder.getFlightRecorder().getEventTypes()) if (t.getName().equals(name)) return true;
        return false;
    }

    @Name("test.EventType.Rich") @Label("Rich") @Category({"rc1", "rc2"}) @Description("rd")
    static class RichEvent extends Event { @Label("id") long id; @Label("msg") String msg; }

    @Registered(false) @Name("test.EventType.Manual")
    static class ManualEvent extends Event { long x; }

    @Name("test.EventType.NoCat")
    static class NoCatEvent extends Event { long x; }

    @Name("test.EventType.Plain")
    static class PlainEvent extends Event { long x; }

    static void testDeepMetadata() throws Exception {
        EventType t = EventType.getEventType(RichEvent.class);
        check("test.EventType.Rich".equals(t.getName()), "et name");
        check("Rich".equals(t.getLabel()), "et label");
        check("rd".equals(t.getDescription()), "et description");
        check(t.getCategoryNames().size() == 2 && "rc1".equals(t.getCategoryNames().get(0)) && "rc2".equals(t.getCategoryNames().get(1)), "et categories order");
        check(t.getAnnotation(Name.class) != null && "test.EventType.Rich".equals(t.getAnnotation(Name.class).value()), "et @Name roundtrip");
        check(t.getAnnotation(Label.class) != null && "Rich".equals(t.getAnnotation(Label.class).value()), "et @Label roundtrip");
        check(t.getAnnotation(Description.class) != null && "rd".equals(t.getAnnotation(Description.class).value()), "et @Description roundtrip");
        check(t.getId() > 0, "et id > 0");
        boolean nameElem = false;
        for (AnnotationElement ae : t.getAnnotationElements()) {
            if ("jdk.jfr.Name".equals(ae.getTypeName()) && "test.EventType.Rich".equals(ae.getValue("value"))) nameElem = true;
        }
        check(nameElem, "et Name annotation element roundtrip");
        List<String> fn = new ArrayList<String>();
        for (ValueDescriptor vd : t.getFields()) fn.add(vd.getName());
        check(fn.size() == 6, "et 4 implicit + 2 user fields, got " + fn.size());
        check("startTime".equals(fn.get(0)) && "duration".equals(fn.get(1)) && "eventThread".equals(fn.get(2)) && "stackTrace".equals(fn.get(3)), "et implicit field order");
        check("id".equals(fn.get(4)) && "msg".equals(fn.get(5)), "et user field order");
        check(t.getField("id") != null && "id".equals(t.getField("id").getLabel()), "et getField label");
        check(t.getField("nope") == null, "et getField(missing) null");
        check(t.getAnnotation(Enabled.class) == null && t.getAnnotation(Threshold.class) == null, "et setting-derived annotations not via getAnnotation");
    }

    static void testDeepSettingDefaults() throws Exception {
        EventType t = EventType.getEventType(PlainEvent.class);
        Map<String, String> d = new HashMap<String, String>();
        for (SettingDescriptor sd : t.getSettingDescriptors()) d.put(sd.getName(), sd.getDefaultValue());
        check(d.size() == 3, "et plain event has exactly 3 settings, got " + d.size());
        check("true".equals(d.get("enabled")), "et default enabled true");
        check("0 ns".equals(d.get("threshold")), "et default threshold '0 ns'");
        check("true".equals(d.get("stackTrace")), "et default stackTrace true");
    }

    static void testDeepEnabledLifecycle() throws Exception {
        EventType t = EventType.getEventType(PlainEvent.class);
        check(!t.isEnabled(), "et isEnabled false with no active recording");
        try (Recording r = new Recording()) {
            r.enable("test.EventType.Plain").withThreshold(java.time.Duration.ZERO);
            r.start();
            check(t.isEnabled(), "et isEnabled true while enabled+running");
            r.stop();
        }
        check(!t.isEnabled(), "et isEnabled false again after recording stopped");
    }

    static void testDeepRegisteredState() throws Exception {
        check(!typeRegistered("test.EventType.Manual"), "et @Registered(false) not auto-registered");
        expectEx("et getEventType on @Registered(false)", IllegalStateException.class, "is not registered", () -> EventType.getEventType(ManualEvent.class));
        FlightRecorder.register(ManualEvent.class);
        check(typeRegistered("test.EventType.Manual"), "et @Registered(false) present after explicit register");
        FlightRecorder.unregister(ManualEvent.class);
        check(!typeRegistered("test.EventType.Manual"), "et absent again after unregister");
        expectEx("et getEventType after unregister", IllegalStateException.class, "is not registered", () -> EventType.getEventType(ManualEvent.class));
        FlightRecorder.register(ManualEvent.class);
        check(typeRegistered("test.EventType.Manual"), "et recover after re-register");
    }

    static void testDeepRegistryMembership() {
        List<EventType> all = FlightRecorder.getFlightRecorder().getEventTypes();
        Set<String> names = new HashSet<String>();
        for (EventType t : all) names.add(t.getName());
        check(names.contains("jdk.ThreadStart"), "et registry contains jdk.ThreadStart");
        check(names.contains("jdk.NativeMethodSample"), "et registry contains jdk.NativeMethodSample");
        // version-diff: jdk.ObjectAllocationSample exists on JDK-27 only
        check(!names.contains("jdk.ObjectAllocationSample"), "et jdk.ObjectAllocationSample absent on JDK-8");
    }

    static void testDeepDefaultCategory() throws Exception {
        EventType t = EventType.getEventType(NoCatEvent.class);
        check(t.getCategoryNames().size() == 1 && "Uncategorized".equals(t.getCategoryNames().get(0)), "et default category Uncategorized");
    }
}
