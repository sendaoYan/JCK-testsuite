/*
 * @test
 * @summary javax.sql.rowset.serial.SerialJavaObject conformance: white-box structure + black-box behavior.
 * @run main SerialJavaObjectTest
 */

public class SerialJavaObjectTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // A serializable value object with a couple of public fields.
    public static class Point implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        public int x;
        public String label;
        public Point(int x, String label) { this.x = x; this.label = label; }
        public boolean equals(Object o) {
            if (!(o instanceof Point)) return false;
            Point p = (Point) o;
            return x == p.x && java.util.Objects.equals(label, p.label);
        }
        public int hashCode() { return 31 * x + (label == null ? 0 : label.hashCode()); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sql.rowset.serial.SerialJavaObject");
        check(c != null, "loads");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 2, "2 direct interface(s)");
        check(ifs[0].getName().equals("java.io.Serializable"), "interface 0");
        check(ifs[1].getName().equals("java.lang.Cloneable"), "interface 1");
        check(c.getDeclaredMethods().length == 9, "declared method count 9");
        check(c.getDeclaredConstructors().length == 1, "constructor count 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // A Serializable object round-trips through getObject() as the same value.
        Point p = new Point(3, "origin");
        javax.sql.rowset.serial.SerialJavaObject sj = new javax.sql.rowset.serial.SerialJavaObject(p);
        check(p.equals(sj.getObject()), "getObject equals original serializable object");
        check(sj.getObject() == p, "getObject returns the same stored reference (implementation detail)");

        // getFields() exposes the public fields of the wrapped object's class.
        java.lang.reflect.Field[] fields = sj.getFields();
        check(fields != null, "getFields non-null");
        java.util.Set<String> names = new java.util.TreeSet<String>();
        for (java.lang.reflect.Field f : fields) names.add(f.getName());
        check(names.contains("x") && names.contains("label"), "getFields exposes public fields x/label");

        // equals/hashCode delegate to the wrapped object.
        javax.sql.rowset.serial.SerialJavaObject sj2 = new javax.sql.rowset.serial.SerialJavaObject(new Point(3, "origin"));
        javax.sql.rowset.serial.SerialJavaObject sj3 = new javax.sql.rowset.serial.SerialJavaObject(new Point(4, "other"));
        check(sj.equals(sj2), "equals same value");
        check(sj.hashCode() == sj2.hashCode(), "hashCode same value");
        check(!sj.equals(sj3), "not equals different value");
        check(!sj.equals(null), "not equals null");

        javax.sql.rowset.serial.SerialJavaObject clone = (javax.sql.rowset.serial.SerialJavaObject) sj.clone();
        check(clone != sj, "clone is a distinct instance");
        check(sj.equals(clone), "clone equals original");

        // A non-Serializable object is accepted (a warning is recorded, no exception).
        Object plain = new Object();
        javax.sql.rowset.serial.SerialJavaObject sjn = new javax.sql.rowset.serial.SerialJavaObject(plain);
        check(sjn.getObject() == plain, "non-Serializable object is accepted and returned");

        // null object dereferences at construction time (NPE, per implementation).
        try {
            new javax.sql.rowset.serial.SerialJavaObject(null);
            check(false, "null object should throw");
        } catch (NullPointerException expected) {
            check(true, "null object throws NullPointerException");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
