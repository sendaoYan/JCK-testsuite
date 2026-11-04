/*
 * @test
 * @summary java.awt.CompositeContext conformance: interface reflection.
 * @run main CompositeContextTest
 */

public class CompositeContextTest {
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
        Class<?> c = Class.forName("java.awt.CompositeContext");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "dispose"), "method dispose");
        check(hasMethod(c, "compose"), "method compose");
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testContract() {
        check(java.awt.Composite.class.isInterface(), "Composite is an interface");
        check(java.awt.Composite.class.getDeclaredMethods().length >= 1, "declares createContext");
        // AlphaComposite is the flagship implementation: createContext contract
        java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(2, 2, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Composite ac = java.awt.AlphaComposite.SrcOver;
        java.awt.CompositeContext ctx = ac.createContext(bi.getColorModel(), bi.getColorModel(), null);
        check(ctx != null, "createContext returns CompositeContext");
        check(ctx instanceof java.awt.CompositeContext, "context implements CompositeContext");
        ctx.dispose();
        // CompositeContext interface methods
        boolean hasCompose = false;
        for (java.lang.reflect.Method m : java.awt.CompositeContext.class.getMethods())
            if (m.getName().equals("compose")) hasCompose = true;
        check(hasCompose, "CompositeContext declares compose");
        check(java.awt.CompositeContext.class.isInterface(), "CompositeContext is an interface");
        // compose actually blends pixels: SRC over transparent dst gives src color
        java.awt.image.BufferedImage src = new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.image.BufferedImage dst = new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        src.setRGB(0, 0, 0xFF804020);
        java.awt.CompositeContext c2 = java.awt.AlphaComposite.SrcOver.createContext(
                src.getColorModel(), dst.getColorModel(), null);
        c2.compose(src.getRaster(), dst.getRaster(), dst.getRaster());
        check(dst.getRGB(0, 0) == 0xFF804020, "SRC_OVER over transparent yields src pixel: "
              + Integer.toHexString(dst.getRGB(0, 0)));
        c2.dispose();
    }

}
