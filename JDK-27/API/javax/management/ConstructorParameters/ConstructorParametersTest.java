/*
 * @test
 * @summary javax.management.ConstructorParameters conformance: white-box structure + annotation behavior.
 *          Deepened: Target exactly CONSTRUCTOR, Documented, since 9 (not
 *          present on JDK 8), and the REAL MXBean closure - a CompositeData
 *          returned by an MXBean proxy is reconstructed through the
 *          ConstructorParameters-annotated constructor (Point wrap/round-trip),
 *          plus ConstructorParameters taking precedence over
 *          java.beans.ConstructorProperties when both are present.
 * @run main ConstructorParametersTest
 */

import java.lang.reflect.*;
import java.lang.annotation.*;
import java.util.*;
import javax.management.*;

public class ConstructorParametersTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.ConstructorParameters");
        check(c.isAnnotation(), "is annotation");
        check(java.lang.annotation.Annotation.class.isAssignableFrom(c), "extends Annotation");
        check(hasMethod(c, "value"), "has value()");
        Method value = c.getMethod("value");
        check(value.getReturnType() == String[].class, "value() returns String[]");
        String[] def = (String[]) value.getDefaultValue();
        check(def == null, "value() has no default");
        check(RetentionPolicy.RUNTIME.equals(c.getAnnotation(Retention.class).value()), "runtime retention");
        Target t = c.getAnnotation(Target.class);
        check(t != null && t.value().length == 1 && t.value()[0] == ElementType.CONSTRUCTOR,
                "@Target is exactly {ElementType.CONSTRUCTOR}");
        check(c.isAnnotationPresent(Documented.class), "@Documented present");
        // constructor-only target: the annotation is NOT applicable to methods
        check(!Sample.class.getMethod("describe").isAnnotationPresent(ConstructorParameters.class),
                "not applicable to methods");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        Constructor<?> ctor = Sample.class.getConstructor(String.class, int.class);
        ConstructorParameters cp = ctor.getAnnotation(ConstructorParameters.class);
        check(cp != null, "annotation present on constructor");
        check(cp.value().length == 2, "two parameter names");
        check("name".equals(cp.value()[0]) && "count".equals(cp.value()[1]), "parameter names");
    }

    public static class Sample {
        @ConstructorParameters({"name", "count"})
        public Sample(String name, int count) { }
        public String describe() { return ""; }
    }

    // ---- deepened: MXBean CompositeData -> object reconstruction ------------

    public static class Point {
        private final int x, y;
        @ConstructorParameters({"x", "y"})
        public Point(int x, int y) { this.x = x; this.y = y; }
        public int getX() { return x; }
        public int getY() { return y; }
        public boolean equals(Object o) {
            if (!(o instanceof Point)) return false;
            Point p = (Point) o;
            return p.x == x && p.y == y;
        }
        public int hashCode() { return 31 * x + y; }
    }

    // both annotations present with DIFFERENT orders: only the
    // javax.management.ConstructorParameters order may be used for reconstruction
    public static class Both {
        private final int x, y;
        @ConstructorParameters({"x", "y"})
        @java.beans.ConstructorProperties({"y", "x"})
        public Both(int x, int y) { this.x = x; this.y = y; }
        public int getX() { return x; }
        public int getY() { return y; }
        public boolean equals(Object o) {
            if (!(o instanceof Both)) return false;
            Both b = (Both) o;
            return b.x == x && b.y == y;
        }
        public int hashCode() { return 31 * x + y; }
    }

    public interface PointMXBean {
        Point wrap(int x, int y);
        Both both(int x, int y);
    }

    public static class PointImpl implements PointMXBean {
        public Point wrap(int x, int y) { return new Point(x, y); }
        public Both both(int x, int y) { return new Both(x, y); }
    }

    static void testMxBeanRoundTrip() throws Exception {
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        ObjectName on = new ObjectName("jck.b:type=Point");
        mbs.registerMBean(new PointImpl(), on);
        // raw access sees the CompositeData wire form
        Object raw = mbs.invoke(on, "wrap", new Object[]{3, 4}, new String[]{"int", "int"});
        check(raw instanceof javax.management.openmbean.CompositeData,
                "raw MXBean invoke returns CompositeData");
        javax.management.openmbean.CompositeData cd = (javax.management.openmbean.CompositeData) raw;
        check(Integer.valueOf(3).equals(cd.get("x")) && Integer.valueOf(4).equals(cd.get("y")),
                "CompositeData carries x=3, y=4");
        // the proxy reconstructs a Point through the @ConstructorParameters ctor
        PointMXBean proxy = JMX.newMXBeanProxy(mbs, on, PointMXBean.class);
        Point p = proxy.wrap(3, 4);
        check(p != null && p.getX() == 3 && p.getY() == 4,
                "proxy reconstructs Point(3,4) via @ConstructorParameters");
        check(new Point(3, 4).equals(p), "reconstructed Point equals a local Point(3,4)");
        Both b = proxy.both(3, 4);
        check(b != null && b.getX() == 3 && b.getY() == 4,
                "ConstructorParameters wins over ConstructorProperties");
        // getMBeanInfo still introspects the MXBean normally
        check(mbs.getMBeanInfo(on).getOperations().length >= 2, "MXBean MBeanInfo has both operations");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testMxBeanRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
