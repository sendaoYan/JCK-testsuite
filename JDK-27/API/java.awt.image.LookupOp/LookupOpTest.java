/*
 * @test
 * @summary java.awt.image.LookupOp conformance: white-box structure + black-box behavior.
 * @run main LookupOpTest
 */

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ByteLookupTable;
import java.awt.image.LookupOp;
import java.awt.image.LookupTable;
import java.awt.image.RasterOp;
import java.lang.reflect.*;

public class LookupOpTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.LookupOp", false, LookupOpTest.class.getClassLoader());
        check(c != null, "type loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(BufferedImageOp.class.isAssignableFrom(c), "implements BufferedImageOp");
        check(RasterOp.class.isAssignableFrom(c), "implements RasterOp");
        check(hasMethod(c, "getTable"), "getTable()");
        check(hasMethod(c, "filter", BufferedImage.class, BufferedImage.class), "filter(BufferedImage,BufferedImage)");
        check(hasMethod(c, "getBounds2D", BufferedImage.class), "getBounds2D");
    }

    static void testBlackBox() {
        byte[][] table = new byte[3][256];
        for (int i = 0; i < 256; i++) {
            table[0][i] = (byte) Math.min(i, 127);
            table[1][i] = (byte) Math.min(i, 127);
            table[2][i] = (byte) Math.min(i, 127);
        }
        ByteLookupTable lut = new ByteLookupTable(0, table);
        LookupOp op = new LookupOp(lut, null);
        check(op.getTable() == lut, "getTable returns table");
        check(op.getRenderingHints() == null, "null hints");

        BufferedImage src = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        src.setRGB(0, 0, 0xFFC86432); // (200, 100, 50)
        BufferedImage dst = op.filter(src, null);
        int p = dst.getRGB(0, 0);
        check(((p >> 16) & 0xFF) == 127, "200 -> min(200,127)=127 red");
        check(((p >> 8) & 0xFF) == 100, "100 -> 100 green");
        check((p & 0xFF) == 50, "50 -> 50 blue");

        // identity lookup keeps values below 128 unchanged
        BufferedImage src2 = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        src2.setRGB(0, 0, 0xFF010203);
        BufferedImage dst2 = op.filter(src2, null);
        check(dst2.getRGB(0, 0) == 0xFF010203, "identity for sub-128 values");

        Rectangle2D b = op.getBounds2D(src);
        check(b.getWidth() == 2 && b.getHeight() == 2, "getBounds2D");
        Point2D pt = op.getPoint2D(new Point2D.Float(1, 1), null);
        check(pt.getX() == 1.0 && pt.getY() == 1.0, "getPoint2D identity");
        BufferedImage compat = op.createCompatibleDestImage(src, src.getColorModel());
        check(compat.getWidth() == 2 && compat.getHeight() == 2, "compatible dest dims");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
