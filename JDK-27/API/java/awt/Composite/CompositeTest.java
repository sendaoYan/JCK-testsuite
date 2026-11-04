/*
 * @test
 * @summary java.awt.Composite conformance: white-box structure + black-box behavior.
 * @run main CompositeTest
 */

import java.lang.reflect.*;

public class CompositeTest {
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
        Class<?> c = Class.forName("java.awt.Composite", false, CompositeTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.Composite");
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
        java.awt.Composite impl = (cm, dm, hints) -> null;
        check(impl.createContext(null, null, null) == null, "createContext returns null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
