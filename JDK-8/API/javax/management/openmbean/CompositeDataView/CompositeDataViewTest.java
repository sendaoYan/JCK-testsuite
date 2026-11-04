/*
 * @test
 * @summary javax.management.openmbean.CompositeDataView conformance: interface reflection.
 * @run main CompositeDataViewTest
 */

public class CompositeDataViewTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.CompositeDataView");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "toCompositeData"), "declares toCompositeData");
        testDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened: interface lock / custom impl / MXBean closed loop ----
    public static class Point implements javax.management.openmbean.CompositeDataView {
        private final int x;
        private final int y;
        public Point(int x, int y) { this.x = x; this.y = y; }
        public int getX() { return x; }
        public int getY() { return y; }
        public javax.management.openmbean.CompositeData toCompositeData(
                javax.management.openmbean.CompositeType ct) {
            try {
                return new javax.management.openmbean.CompositeDataSupport(ct,
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
        check(c.getMethods().length == 1, "single method interface");
        check(c.getMethod("toCompositeData",
                javax.management.openmbean.CompositeType.class).getReturnType()
                == javax.management.openmbean.CompositeData.class, "toCompositeData signature");
        check(!java.io.Serializable.class.isAssignableFrom(c), "does not extend Serializable");
        check(c.getInterfaces().length == 0, "no super-interfaces");

        // custom class implementation returns a controlled CompositeData
        javax.management.openmbean.CompositeType ct = new javax.management.openmbean.CompositeType(
                "CT", "d", new String[]{"x", "y"}, new String[]{"xd", "yd"},
                new javax.management.openmbean.OpenType[]{
                        javax.management.openmbean.SimpleType.INTEGER,
                        javax.management.openmbean.SimpleType.INTEGER});
        Point p = new Point(3, 4);
        javax.management.openmbean.CompositeData cd = p.toCompositeData(ct);
        check(cd.get("x").equals(3) && cd.get("y").equals(4), "custom impl supplies values");
        check(cd.getCompositeType().equals(ct), "custom impl uses requested type");
        // second call with a different type object still works
        javax.management.openmbean.CompositeData cd2 = p.toCompositeData(
                new javax.management.openmbean.CompositeType("CT", "d",
                        new String[]{"x", "y"}, new String[]{"xd", "yd"},
                        new javax.management.openmbean.OpenType[]{
                                javax.management.openmbean.SimpleType.INTEGER,
                                javax.management.openmbean.SimpleType.INTEGER}));
        check(cd2.get("x").equals(3), "second toCompositeData call");

        // closed loop: an MXBean getter returning a CompositeDataView type is served as CompositeData
        javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        javax.management.ObjectName on = new javax.management.ObjectName("jck.cdview:type=Point");
        try {
            server.registerMBean(new PointBean(), on);
            Object res = server.getAttribute(on, "Point");
            check(res instanceof javax.management.openmbean.CompositeData,
                    "MXBean attribute converted to CompositeData");
            javax.management.openmbean.CompositeData rcd = (javax.management.openmbean.CompositeData) res;
            check(rcd.get("x").equals(3), "closed loop x");
            check(rcd.get("y").equals(4), "closed loop y");
            check(rcd.getCompositeType().getTypeName().equals(Point.class.getName()),
                    "computed CompositeType typeName is the class name");
            check(rcd.getCompositeType().getType("x") == javax.management.openmbean.SimpleType.INTEGER,
                    "computed item type from getter");
            check(rcd.getCompositeType().keySet().contains("y"), "computed items from getters");
        } finally {
            if (server.isRegistered(on)) server.unregisterMBean(on);
        }
    }
}
