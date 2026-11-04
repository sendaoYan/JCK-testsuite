/*
 * @test
 * @summary java.awt.image.LookupOp conformance: white-box structure + black-box contract.
 * @run main LookupOpTest
 */

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ByteLookupTable;
import java.awt.image.LookupOp;
import java.awt.image.LookupTable;

public class LookupOpTest {
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
        Class<?> c = Class.forName("java.awt.image.LookupOp");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.image.BufferedImageOp", "java.awt.image.RasterOp"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getTable"), "method getTable");
        check(hasMethod(c, "filter"), "method filter");
        check(hasMethod(c, "getBounds2D"), "method getBounds2D");
        check(hasMethod(c, "createCompatibleDestImage"), "method createCompatibleDestImage");
        check(hasMethod(c, "createCompatibleDestRaster"), "method createCompatibleDestRaster");
        check(hasMethod(c, "getPoint2D"), "method getPoint2D");
        check(hasMethod(c, "getRenderingHints"), "method getRenderingHints");
    }

    static ByteLookupTable invertTable() {
        byte[] data = new byte[256];
        for (int i = 0; i < 256; i++) data[i] = (byte) (255 - i);
        return new ByteLookupTable(0, data);
    }

    static void testConstruction() {
        LookupTable t = invertTable();
        LookupOp op = new LookupOp(t, null);
        check(op.getTable() == t, "getTable same reference");
        check(op.getRenderingHints() == null, "no hints -> null");
    }

    static void testFilter() {
        LookupOp op = new LookupOp(invertTable(), null);
        BufferedImage src = new BufferedImage(2, 2, BufferedImage.TYPE_BYTE_GRAY);
        src.getRaster().setSample(0, 0, 0, 10);
        src.getRaster().setSample(1, 0, 0, 255);
        BufferedImage dst = op.filter(src, null);
        check(dst.getRaster().getSample(0, 0, 0) == 245, "invert maps 10 -> 245");
        check(dst.getRaster().getSample(1, 0, 0) == 0, "invert maps 255 -> 0");
        check(dst.getWidth() == 2 && dst.getHeight() == 2, "dims unchanged");
        // Provided destination is used and returned.
        BufferedImage provided = new BufferedImage(2, 2, BufferedImage.TYPE_BYTE_GRAY);
        check(op.filter(src, provided) == provided, "filter returns provided dst");
        check(provided.getRaster().getSample(0, 0, 0) == 245, "provided dst filled");
        // Zero table maps everything to zero.
        LookupOp zero = new LookupOp(new ByteLookupTable(0, new byte[256]), null);
        BufferedImage z = zero.filter(src, null);
        check(z.getRaster().getSample(0, 0, 0) == 0 && z.getRaster().getSample(1, 0, 0) == 0,
              "zero table zeroes samples");
    }

    static void testGeometryAndErrors() {
        LookupOp op = new LookupOp(invertTable(), null);
        BufferedImage src = new BufferedImage(3, 4, BufferedImage.TYPE_BYTE_GRAY);
        check(op.getBounds2D(src).equals(new java.awt.Rectangle(0, 0, 3, 4)), "getBounds2D same rect");
        BufferedImage cd = op.createCompatibleDestImage(src, null);
        check(cd.getWidth() == 3 && cd.getHeight() == 4, "compatible dest same dims");
        check(cd.getType() == src.getType(), "compatible dest same type");
        Point2D p = op.getPoint2D(new Point2D.Double(1.5, 2.5), null);
        check(p.getX() == 1.5 && p.getY() == 2.5, "getPoint2D identity");

        boolean threw = false;
        try { op.filter(src, new BufferedImage(4, 4, BufferedImage.TYPE_BYTE_GRAY)); }
        catch (IllegalArgumentException e) { threw = true; }
        check(threw, "width mismatch -> IAE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConstruction();
        testFilter();
        testGeometryAndErrors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
