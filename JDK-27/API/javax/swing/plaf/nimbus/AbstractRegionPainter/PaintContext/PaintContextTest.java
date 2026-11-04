/*
 * @test
 * @summary javax.swing.plaf.nimbus.AbstractRegionPainter.PaintContext conformance: white-box structure + black-box behavior.
 * @run main PaintContextTest
 */

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.lang.reflect.Modifier;
import javax.swing.JComponent;
import javax.swing.plaf.nimbus.AbstractRegionPainter;

public class PaintContextTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Painter extends AbstractRegionPainter {
        protected PaintContext getPaintContext() { return null; }
        protected void doPaint(Graphics2D g, JComponent c, int width, int height, Object[] extendedCacheKeys) {}
        static void blackBox() {
            PaintContext pc1 = new PaintContext(null, null, false);
            check(pc1 != null, "null insets/canvasSize constructor");
            PaintContext pc2 = new PaintContext(new Insets(1, 2, 3, 4), new Dimension(20, 10), true);
            check(pc2 != null, "inverted constructor");
            PaintContext pc3 = new PaintContext(new Insets(1, 2, 3, 4), new Dimension(20, 10), false, null, 1, 1);
            check(pc3 != null, "null cacheMode constructor");
            PaintContext pc4 = new PaintContext(new Insets(1, 2, 3, 4), new Dimension(20, 10), false, null, 2.5, 3.0);
            check(pc4 != null, "scale factor constructor");

            boolean iae1 = false;
            try { new PaintContext(new Insets(1, 2, 3, 4), new Dimension(20, 10), false, null, 0.5, 1); }
            catch (IllegalArgumentException e) { iae1 = true; }
            check(iae1, "maxH < 1 throws IAE");

            boolean iae2 = false;
            try { new PaintContext(new Insets(1, 2, 3, 4), new Dimension(20, 10), false, null, 1, 0.5); }
            catch (IllegalArgumentException e) { iae2 = true; }
            check(iae2, "maxV < 1 throws IAE");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        Painter.blackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.nimbus.AbstractRegionPainter$PaintContext");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == false, "public modifier");
        check(Modifier.isProtected(mods) == true, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }
}
