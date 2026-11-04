/*
 * @test
 * @summary java.awt.image.BufferedImageFilter conformance: class reflection + op wrapping contract.
 * @run main BufferedImageFilterTest
 */

import java.awt.image.BufferedImageFilter;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.RescaleOp;

public class BufferedImageFilterTest {
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

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.BufferedImageFilter");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.image.ImageFilter"), "superclass java.awt.image.ImageFilter");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.lang.Cloneable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getBufferedImageOp"), "method getBufferedImageOp");
        check(hasMethod(c, "setDimensions"), "method setDimensions");
        check(hasMethod(c, "setColorModel"), "method setColorModel");
        check(hasMethod(c, "setPixels"), "method setPixels");
        check(hasMethod(c, "imageComplete"), "method imageComplete");
    }

    static BufferedImageOp identityConvolve() {
        return new ConvolveOp(new Kernel(3, 3, new float[] { 0, 0, 0, 0, 1, 0, 0, 0, 0 }));
    }

    static void testOpWrapping() throws Exception {
        // The wrapped op is returned by reference and survives cloning.
        BufferedImageOp op = identityConvolve();
        BufferedImageFilter bif = new BufferedImageFilter(op);
        check(bif.getBufferedImageOp() == op, "getBufferedImageOp same reference");
        BufferedImageFilter cloned = (BufferedImageFilter) bif.clone();
        check(cloned != bif, "clone distinct object");
        check(cloned.getBufferedImageOp() == op, "clone wraps the same op");
        // A different op is also accepted.
        RescaleOp rs = new RescaleOp(1f, 0f, null);
        check(new BufferedImageFilter(rs).getBufferedImageOp() == rs, "RescaleOp wrappable");
        // getFilterInstance produces a per-consumer clone with the same op.
        Object inst = bif.getFilterInstance(null);
        check(inst != bif && inst instanceof BufferedImageFilter, "getFilterInstance returns clone");
        check(((BufferedImageFilter) inst).getBufferedImageOp() == op,
              "instance clone keeps the op");
    }

    static void testNullOp() {
        boolean npe = false;
        try { new BufferedImageFilter(null); } catch (NullPointerException e) { npe = true; }
        check(npe, "null op -> NPE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testOpWrapping();
        testNullOp();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
