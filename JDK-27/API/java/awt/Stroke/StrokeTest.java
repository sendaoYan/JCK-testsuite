/*
 * @test
 * @summary java.awt.Stroke conformance: white-box structure + black-box behavior.
 * @run main StrokeTest
 */

import java.lang.reflect.*;

public class StrokeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.Stroke", false, StrokeTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.Stroke");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(c.isInterface(), "is interface");
    }

    static void testBlackBox() throws Exception {
        java.awt.Stroke impl = s -> null;
        check(impl.createStrokedShape(null) == null, "createStrokedShape null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testContract() {
        check(java.awt.Stroke.class.isInterface(), "Stroke is an interface");
        java.awt.Stroke[] strokes = {new java.awt.BasicStroke(), new java.awt.BasicStroke(5f)};
        for (java.awt.Stroke s : strokes) {
            java.awt.Shape out = s.createStrokedShape(new java.awt.geom.Line2D.Double(0, 0, 100, 0));
            check(out != null && out.getBounds2D().getWidth() > 0, "createStrokedShape returns non-empty shape");
        }
        // stroked bounds grow with width: width 1 vs 5
        java.awt.geom.Line2D.Double line = new java.awt.geom.Line2D.Double(50, 50, 150, 50);
        double w1 = new java.awt.BasicStroke(1f).createStrokedShape(line).getBounds2D().getHeight();
        double w5 = new java.awt.BasicStroke(5f).createStrokedShape(line).getBounds2D().getHeight();
        check(w5 > w1 && w1 > 0, "stroke height grows with width: " + w1 + " -> " + w5);
    }

}
