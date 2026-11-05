/*
 * @test
 * @summary jdk.jfr.FlightRecorderPermission conformance: type shape via reflection (headless-safe).
 * @run main FlightRecorderPermissionTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import jdk.jfr.FlightRecorderPermission;
import java.security.BasicPermission;

public class FlightRecorderPermissionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.FlightRecorderPermission", false, FlightRecorderPermissionTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.security.BasicPermission"), "superclass java.security.BasicPermission");
        testDeepConstruction();
        testDeepPermissionSemantics();
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

    static void testDeepConstruction() {
        FlightRecorderPermission access = new FlightRecorderPermission("accessFlightRecorder");
        check("accessFlightRecorder".equals(access.getName()), "fp name roundtrip accessFlightRecorder");
        FlightRecorderPermission reg = new FlightRecorderPermission("registerEvent");
        check("registerEvent".equals(reg.getName()), "fp name roundtrip registerEvent");
        expectEx("fp null name", NullPointerException.class, null, () -> new FlightRecorderPermission(null));
        expectEx("fp bogus name", IllegalArgumentException.class, "name: bogus", () -> new FlightRecorderPermission("bogus"));
        // JDK-8: not deprecated (JDK-27 marks it @Deprecated(forRemoval=true))
        check(FlightRecorderPermission.class.getAnnotation(Deprecated.class) == null, "fp not deprecated on JDK-8");
        // single public constructor taking String
        int ctors = 0;
        for (java.lang.reflect.Constructor<?> ctor : FlightRecorderPermission.class.getConstructors()) {
            if (ctor.getParameterTypes().length == 1 && ctor.getParameterTypes()[0] == String.class) ctors++;
        }
        check(ctors == 1, "fp exactly one public String constructor");
    }

    static void testDeepPermissionSemantics() {
        FlightRecorderPermission p = new FlightRecorderPermission("accessFlightRecorder");
        check(p.implies(new FlightRecorderPermission("accessFlightRecorder")), "fp implies same-name");
        check(!p.implies(new FlightRecorderPermission("registerEvent")), "fp not implies other-name");
        check(!p.implies(new BasicPermission("accessFlightRecorder") {
            private static final long serialVersionUID = 1L;
        }), "fp not implies same-name different-type permission");
        check(p.getActions().isEmpty(), "fp actions empty");
        check(p.equals(new FlightRecorderPermission("accessFlightRecorder")), "fp equals same-name");
        check(!p.equals(new FlightRecorderPermission("registerEvent")), "fp not equals other-name");
        check(!p.equals("accessFlightRecorder"), "fp not equals foreign type");
        check(p.hashCode() == new FlightRecorderPermission("accessFlightRecorder").hashCode(), "fp hashCode consistent");
        check(p instanceof java.io.Serializable, "fp Serializable via BasicPermission");
        check(p.newPermissionCollection() != null, "fp newPermissionCollection non-null");
        check(p.toString() != null && p.toString().contains("accessFlightRecorder"), "fp toString mentions target name");
    }
}
