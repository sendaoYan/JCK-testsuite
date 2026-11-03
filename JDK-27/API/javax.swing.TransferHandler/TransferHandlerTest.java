/*
 * @test
 * @summary javax.swing.TransferHandler conformance: white-box structure + black-box behavior.
 * @run main TransferHandlerTest
 */

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.image.BufferedImage;
import java.lang.reflect.Modifier;

import javax.swing.JPanel;
import javax.swing.TransferHandler;

public class TransferHandlerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.TransferHandler");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.io.Serializable"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        check(TransferHandler.NONE == 0 && TransferHandler.COPY == 1 && TransferHandler.MOVE == 2
                && TransferHandler.COPY_OR_MOVE == 3 && TransferHandler.LINK == (1 << 30), "action constants");

        TransferHandler h = new TransferHandler("text");
        check(TransferHandler.getCutAction() != null, "cut action");
        check(TransferHandler.getCopyAction() != null, "copy action");
        check(TransferHandler.getPasteAction() != null, "paste action");
        check(h.getSourceActions(new JPanel()) == TransferHandler.NONE, "source actions default NONE");
        check(h.getDragImage() == null, "drag image null");

        BufferedImage img = new BufferedImage(5, 5, BufferedImage.TYPE_INT_ARGB);
        h.setDragImage(img);
        check(h.getDragImage() == img, "setDragImage");
        h.setDragImageOffset(new Point(1, 2));
        check(h.getDragImageOffset().equals(new Point(1, 2)), "drag image offset");

        check(!h.canImport(new JPanel(), new DataFlavor[] { DataFlavor.stringFlavor }), "canImport default false");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
