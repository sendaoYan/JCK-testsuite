/*
 * @test
 * @summary javax.management.openmbean.CompositeDataView conformance: white-box structure + light black-box behavior.
 * @run main CompositeDataViewTest
 */

import java.lang.reflect.Modifier;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeDataView;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

public class CompositeDataViewTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean has(java.lang.reflect.Method[] mm, String n) {
        for (java.lang.reflect.Method m : mm) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.CompositeDataView");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        java.lang.reflect.Method[] mm = c.getMethods();
        check(has(mm, "toCompositeData"), "has toCompositeData");
        check(mm.length == 1, "single method");
    }

    // ---- Part 2: black-box (light behavior via anonymous implementation) ----
    static void testBlackBox() throws Exception {
        final CompositeType ct = new CompositeType("CT", "d",
            new String[]{"name"}, new String[]{"nd"}, new OpenType[]{SimpleType.STRING});
        final CompositeData expected = new CompositeDataSupport(ct, new String[]{"name"}, new Object[]{"n"});

        CompositeDataView view = new CompositeDataView() {
            public CompositeData toCompositeData(CompositeType type) {
                check(type.equals(ct), "type passed to toCompositeData");
                return expected;
            }
        };
        check(view.toCompositeData(ct).equals(expected), "toCompositeData returns value");
    }

    // ---- deepened: custom class impl + MXBean closed loop through a local MBeanServer ----
    public static class Point implements CompositeDataView {
        private final int x;
        private final int y;
        public Point(int x, int y) { this.x = x; this.y = y; }
        public int getX() { return x; }
        public int getY() { return y; }
        public CompositeData toCompositeData(CompositeType ct) {
            try {
                return new CompositeDataSupport(ct,
                        new String[]{"x", "y"}, new Object[]{Integer.valueOf(x), Integer.valueOf(y)});
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public interface PointMXBean { Point getPoint(); }

    public static class PointBean implements PointMXBean {
        public Point getPoint() { return new Point(3, 4); }
    }

    static void testDeep() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.CompositeDataView");
        check(!java.io.Serializable.class.isAssignableFrom(c), "does not extend Serializable");
        check(c.getInterfaces().length == 0, "no super-interfaces");
        check(c.getMethod("toCompositeData", CompositeType.class).getReturnType() == CompositeData.class,
                "toCompositeData signature");

        // custom class implementation returns a controlled CompositeData
        CompositeType ct = new CompositeType("CT", "d", new String[]{"x", "y"}, new String[]{"xd", "yd"},
                new OpenType[]{SimpleType.INTEGER, SimpleType.INTEGER});
        Point p = new Point(3, 4);
        CompositeData cd = p.toCompositeData(ct);
        check(cd.get("x").equals(3) && cd.get("y").equals(4), "custom impl supplies values");
        check(cd.getCompositeType().equals(ct), "custom impl uses requested type");

        // closed loop: MXBean getter returning a CompositeDataView type is served as CompositeData
        javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        javax.management.ObjectName on = new javax.management.ObjectName("jck.cdview:type=Point");
        try {
            server.registerMBean(new PointBean(), on);
            Object res = server.getAttribute(on, "Point");
            check(res instanceof CompositeData, "MXBean attribute converted to CompositeData");
            CompositeData rcd = (CompositeData) res;
            check(rcd.get("x").equals(3), "closed loop x");
            check(rcd.get("y").equals(4), "closed loop y");
            check(rcd.getCompositeType().getTypeName().equals(Point.class.getName()),
                    "computed CompositeType typeName is the class name");
            check(rcd.getCompositeType().getType("x") == SimpleType.INTEGER, "computed item type from getter");
            check(rcd.getCompositeType().keySet().contains("y"), "computed items from getters");
        } finally {
            if (server.isRegistered(on)) server.unregisterMBean(on);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
