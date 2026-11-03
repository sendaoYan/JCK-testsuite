/*
 * @test
 * @summary java.lang.Record conformance: white-box structure + black-box behavior.
 * @run main RecordTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class RecordTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    record Point(int x, int y) {}

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Record");
        check(Modifier.isAbstract(c.getModifiers()), "Record is abstract");
        check(!c.isInterface(), "Record is not an interface");
        check(c.getSuperclass() == Object.class, "Record extends Object");
        check(Modifier.isProtected(c.getDeclaredConstructor().getModifiers()), "protected ctor");
        boolean hasEq = false, hasHash = false, hasStr = false;
        for (Method m : c.getDeclaredMethods()) {
            if (Modifier.isAbstract(m.getModifiers())) {
                if (m.getName().equals("equals")) hasEq = true;
                if (m.getName().equals("hashCode")) hasHash = true;
                if (m.getName().equals("toString")) hasStr = true;
            }
        }
        check(hasEq && hasHash && hasStr, "abstract equals/hashCode/toString");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testRecordBehavior() {
        Point p = new Point(1, 2);
        check(p instanceof Record, "record instance instanceof Record");
        check(p.x() == 1 && p.y() == 2, "record accessors");
        check(p.equals(new Point(1, 2)), "record value equality");
        check(!p.equals(new Point(1, 3)), "record inequality");
        check(p.hashCode() == new Point(1, 2).hashCode(), "record hashCode");
        check("Point[x=1, y=2]".equals(p.toString()), "record toString");
        check(p.getClass().isRecord(), "isRecord true");
        check(Point.class.getRecordComponents().length == 2, "record components");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testRecordBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
