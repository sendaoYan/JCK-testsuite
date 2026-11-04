/*
 * @test
 * @summary java.awt.Stroke conformance: interface reflection.
 * @run main StrokeTest
 */

public class StrokeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.awt.Stroke");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "createStrokedShape"), "method createStrokedShape");
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
