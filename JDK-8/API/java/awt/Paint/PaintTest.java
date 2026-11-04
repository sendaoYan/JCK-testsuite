/*
 * @test
 * @summary java.awt.Paint conformance: interface reflection.
 * @run main PaintTest
 */

public class PaintTest {
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
        Class<?> c = Class.forName("java.awt.Paint");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.Transparency"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "createContext"), "method createContext");
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testContract() {
        check(java.awt.Paint.class.isInterface(), "Paint is an interface");
        // Paint extends Transparency and declares createContext
        check(java.awt.Transparency.class.isAssignableFrom(java.awt.Paint.class), "Paint extends Transparency");
        boolean hasCreate = false;
        for (java.lang.reflect.Method m : java.awt.Paint.class.getMethods())
            if (m.getName().equals("createContext")) hasCreate = true;
        check(hasCreate, "Paint declares createContext");
        // implementations: Color, GradientPaint, LinearGradientPaint, RadialGradientPaint, TexturePaint
        java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(2, 2, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.geom.Rectangle2D ub = new java.awt.geom.Rectangle2D.Double(0, 0, 2, 2);
        java.awt.RenderingHints none = new java.awt.RenderingHints(null);
        java.awt.Paint[] paints = {
            java.awt.Color.red,
            new java.awt.GradientPaint(0f, 0f, java.awt.Color.red, 1f, 1f, java.awt.Color.blue),
            new java.awt.LinearGradientPaint(0f, 0f, 1f, 0f, new float[]{0f, 1f},
                    new java.awt.Color[]{java.awt.Color.red, java.awt.Color.blue}),
            new java.awt.RadialGradientPaint(new java.awt.geom.Point2D.Float(1f, 1f), 2f,
                    new float[]{0f, 1f}, new java.awt.Color[]{java.awt.Color.red, java.awt.Color.blue})};
        String[] pn = {"Color", "GradientPaint", "LinearGradientPaint", "RadialGradientPaint"};
        for (int i = 0; i < paints.length; i++) {
            check(paints[i].createContext(bi.getColorModel(), new java.awt.Rectangle(0, 0, 2, 2), ub,
                    new java.awt.geom.AffineTransform(), none) != null, pn[i] + " createContext headless");
            check(paints[i].getTransparency() == java.awt.Transparency.OPAQUE, pn[i] + " opaque transparency");
        }
        // translucent variant
        java.awt.Paint tr = new java.awt.Color(1, 2, 3, 100);
        check(tr.getTransparency() == java.awt.Transparency.TRANSLUCENT, "translucent Color paint");
    }

}
