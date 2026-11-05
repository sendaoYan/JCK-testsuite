/*
 * @test
 * @summary jdk.jfr.FlightRecorderPermission conformance: white-box structure + black-box permission semantics.
 * @run main FlightRecorderPermissionTest
 */

import java.lang.reflect.*;
import jdk.jfr.FlightRecorderPermission;
import java.security.BasicPermission;

public class FlightRecorderPermissionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.FlightRecorderPermission");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() == java.security.BasicPermission.class, "extends BasicPermission");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() {
        FlightRecorderPermission p = new FlightRecorderPermission("accessFlightRecorder");
        check("accessFlightRecorder".equals(p.getName()), "getName");
        check(p.getActions().length() == 0, "empty actions");
        check(p.implies(new FlightRecorderPermission("accessFlightRecorder")), "implies same");
        check(!p.implies(new FlightRecorderPermission("registerEvent")), "not implies other");
        check(p.equals(new FlightRecorderPermission("accessFlightRecorder")), "equals");
        check(p.hashCode() == new FlightRecorderPermission("accessFlightRecorder").hashCode(), "hashCode consistent");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
        expectEx("fp null name", NullPointerException.class, "name", () -> new FlightRecorderPermission(null));
        expectEx("fp bogus name", IllegalArgumentException.class, "name: bogus", () -> new FlightRecorderPermission("bogus"));
        // version-diff: JDK-27 deprecates this class for removal (JDK-8 does not)
        Deprecated dep = FlightRecorderPermission.class.getAnnotation(Deprecated.class);
        check(dep != null, "fp deprecated on JDK-27");
        check(dep != null && dep.forRemoval(), "fp deprecated forRemoval=true on JDK-27");
        // single public constructor taking String
        int ctors = 0;
        for (Constructor<?> ctor : FlightRecorderPermission.class.getConstructors()) {
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
