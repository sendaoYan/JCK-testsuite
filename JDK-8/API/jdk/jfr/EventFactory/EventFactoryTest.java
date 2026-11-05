/*
 * @test
 * @summary jdk.jfr.EventFactory conformance: type shape via reflection (headless-safe).
 * @run main EventFactoryTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import jdk.jfr.*;
import jdk.jfr.consumer.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;

public class EventFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.EventFactory", false, EventFactoryTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasMethod(c, "create"), "declares create");
        check(hasMethod(c, "getEventType"), "declares getEventType");
        check(hasMethod(c, "newEvent"), "declares newEvent");
        check(hasMethod(c, "register"), "declares register");
        check(hasMethod(c, "unregister"), "declares unregister");
        testDeepFactoryLoop();
        testDeepNegatives();
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

    static void testDeepFactoryLoop() throws Exception {
        List<AnnotationElement> anns = new ArrayList<AnnotationElement>();
        anns.add(new AnnotationElement(Name.class, "test.EventFactory.FEv"));
        anns.add(new AnnotationElement(Label.class, "FE"));
        anns.add(new AnnotationElement(Description.class, "fd"));
        anns.add(new AnnotationElement(Category.class, new String[]{"fc"}));
        List<ValueDescriptor> fields = new ArrayList<ValueDescriptor>();
        fields.add(new ValueDescriptor(long.class, "num", Collections.singletonList(new AnnotationElement(Label.class, "Number"))));
        fields.add(new ValueDescriptor(String.class, "s", Collections.singletonList(new AnnotationElement(Label.class, "Str"))));
        fields.add(new ValueDescriptor(int.class, "i", Collections.singletonList(new AnnotationElement(Label.class, "Int"))));
        fields.add(new ValueDescriptor(boolean.class, "b", Collections.singletonList(new AnnotationElement(Label.class, "Bool"))));
        fields.add(new ValueDescriptor(double.class, "d", Collections.singletonList(new AnnotationElement(Label.class, "Dbl"))));
        fields.add(new ValueDescriptor(short.class, "h", Collections.singletonList(new AnnotationElement(Label.class, "Short"))));
        fields.add(new ValueDescriptor(byte.class, "y", Collections.singletonList(new AnnotationElement(Label.class, "Byte"))));
        fields.add(new ValueDescriptor(char.class, "c", Collections.singletonList(new AnnotationElement(Label.class, "Char"))));
        EventFactory f = EventFactory.create(anns, fields);
        EventType t = f.getEventType();
        check("test.EventFactory.FEv".equals(t.getName()), "fac name");
        check("FE".equals(t.getLabel()), "fac label");
        check("fd".equals(t.getDescription()), "fac description");
        check(t.getCategoryNames().size() == 1 && "fc".equals(t.getCategoryNames().get(0)), "fac category");
        check(t.getId() > 0, "fac id > 0");
        List<String> fn = new ArrayList<String>();
        for (ValueDescriptor vd : t.getFields()) fn.add(vd.getName());
        check(fn.size() == 12, "fac 4 implicit + 8 user fields, got " + fn.size());
        check("startTime".equals(fn.get(0)) && "duration".equals(fn.get(1)) && "eventThread".equals(fn.get(2)) && "stackTrace".equals(fn.get(3)), "fac implicit order");
        check(t.getField("num") != null && "Number".equals(t.getField("num").getLabel()), "fac field label roundtrip");
        check(t.getField("c") != null && "Char".equals(t.getField("c").getLabel()), "fac char field label");
        check(t.getField("nope") == null, "fac getField(missing) null");
        // annotation roundtrip through EventType
        boolean labelFound = false;
        for (AnnotationElement ae : t.getAnnotationElements()) {
            if ("jdk.jfr.Label".equals(ae.getTypeName()) && "FE".equals(ae.getValue("value"))) labelFound = true;
        }
        check(labelFound, "fac Label annotation element roundtrip");
        Set<String> settingNames = new HashSet<String>();
        for (SettingDescriptor sd : t.getSettingDescriptors()) settingNames.add(sd.getName());
        check(settingNames.contains("enabled") && settingNames.contains("threshold") && settingNames.contains("stackTrace"), "fac default settings present");
        // register / unregister state machine
        check(typeRegistered("test.EventFactory.FEv"), "fac event type registered immediately after create()");
        f.register();
        check(typeRegistered("test.EventFactory.FEv"), "fac registered after register()");
        // full record-read loop with the dynamic event
        Path tmp = Files.createTempFile("fac-loop", ".jfr");
        try {
            try (Recording r = new Recording()) {
                r.enable("test.EventFactory.FEv").withThreshold(Duration.ZERO);
                r.start();
                Event e = f.newEvent();
                check(e != null, "fac newEvent non-null");
                e.set(0, 42L);
                e.set(1, "str0");
                e.set(2, -7);
                e.set(3, true);
                e.set(4, 3.5);
                e.set(5, (short) 300);
                e.set(6, (byte) -2);
                e.set(7, 'Z');
                e.commit();
                check(true, "fac event committed");
                r.stop();
                r.dump(tmp);
            }
            List<RecordedEvent> mine = new ArrayList<RecordedEvent>();
            for (RecordedEvent re : RecordingFile.readAllEvents(tmp)) {
                if (re.getEventType().getName().equals("test.EventFactory.FEv")) mine.add(re);
            }
            check(mine.size() == 1, "fac loop 1 event, got " + mine.size());
            RecordedEvent re = mine.get(0);
            check(re.getLong("num") == 42L, "fac num roundtrip");
            check("str0".equals(re.getString("s")), "fac s roundtrip");
            check(re.getInt("i") == -7, "fac i roundtrip");
            check(re.getBoolean("b") == true, "fac b roundtrip");
            check(re.getDouble("d") == 3.5, "fac d roundtrip");
            check(re.getShort("h") == (short) 300, "fac h roundtrip");
            check(re.getByte("y") == (byte) -2, "fac y roundtrip");
            check(re.getChar("c") == 'Z', "fac c roundtrip");
            check(re.getValue("num") instanceof Long, "fac getValue boxed long");
            check(re.hasField("num") && !re.hasField("nope"), "fac hasField");
        } finally { Files.deleteIfExists(tmp); }
        f.unregister();
        check(!typeRegistered("test.EventFactory.FEv"), "fac unregistered after unregister()");
        // unregistered factory events are silently dropped by a recording
        Path tmp2 = Files.createTempFile("fac-unreg", ".jfr");
        try {
            try (Recording r = new Recording()) {
                r.enable("test.EventFactory.FEv").withThreshold(Duration.ZERO);
                r.start();
                Event e = f.newEvent();
                e.set(0, 1L);
                e.commit();
                check(true, "fac commit while unregistered does not throw");
                r.stop();
                r.dump(tmp2);
            }
            int dropped = 0;
            for (RecordedEvent re : RecordingFile.readAllEvents(tmp2)) {
                if (re.getEventType().getName().equals("test.EventFactory.FEv")) dropped++;
            }
            check(dropped == 0, "fac unregistered events not recorded, got " + dropped);
        } finally { Files.deleteIfExists(tmp2); }
    }

    static void testDeepNegatives() {
        List<ValueDescriptor> fields = new ArrayList<ValueDescriptor>();
        fields.add(new ValueDescriptor(long.class, "num"));
        expectEx("create(null, fields)", NullPointerException.class, null, () -> EventFactory.create(null, fields));
        List<AnnotationElement> anns = new ArrayList<AnnotationElement>();
        anns.add(new AnnotationElement(Name.class, "test.EventFactory.Neg"));
        expectEx("create(anns, null)", NullPointerException.class, null, () -> EventFactory.create(anns, null));
        EventFactory f = EventFactory.create(anns, fields);
        Event e = f.newEvent();
        expectEx("fac set(-1)", IndexOutOfBoundsException.class, "Index must between 0 and", () -> e.set(-1, 1L));
        expectEx("fac set(9)", IndexOutOfBoundsException.class, "Index must between 0 and", () -> e.set(9, 1L));
        f.register();
        f.register();
        check(true, "double register tolerated");
        f.unregister();
        f.unregister();
        check(true, "double unregister tolerated");
    }
}
