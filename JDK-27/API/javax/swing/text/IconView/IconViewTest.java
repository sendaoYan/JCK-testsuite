/*
 * @test
 * @summary javax.swing.text.IconView conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main IconViewTest
 */

import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.Position;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.View;

public class IconViewTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean throwsBLE(ThrowingRunnable r) {
        try { r.run(); return false; }
        catch (BadLocationException e) { return true; }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.IconView");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.View"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        JFrame frame = new JFrame();
        JTextPane tp = new JTextPane();
        frame.add(tp);
        frame.pack();
        try {
            StyledDocument doc = tp.getStyledDocument();
            ImageIcon icon = new ImageIcon(new BufferedImage(16, 12, BufferedImage.TYPE_INT_ARGB));
            Style iconStyle = tp.addStyle("icon", null);
            StyleConstants.setIcon(iconStyle, icon);
            doc.insertString(0, "I", iconStyle);

            Element para = doc.getDefaultRootElement().getElement(0);
            Element iconElem = para.getElement(0);
            check("icon".equals(iconElem.getName()), "icon element name");

            IconView iv = new IconView(iconElem);
            check(iv.getElement() == iconElem, "getElement returns ctor element");
            check(iv.getStartOffset() == 0 && iv.getEndOffset() == 1, "icon occupies one character");
            check(iv.getDocument() == doc, "getDocument from element");

            // preferred spans come from the icon geometry
            check(iv.getPreferredSpan(View.X_AXIS) == 16f, "X preferred span == icon width");
            check(iv.getPreferredSpan(View.Y_AXIS) == 12f, "Y preferred span == icon height");
            check(iv.getAlignment(View.Y_AXIS) == 1.0f, "icon aligned to bottom");
            check(iv.getAlignment(View.X_AXIS) == 0.5f, "default X alignment");

            // modelToView maps start/end to left/right edges with zero width
            Rectangle alloc = new Rectangle(5, 6, 40, 20);
            Shape s0 = iv.modelToView(0, alloc, Position.Bias.Forward);
            Shape s1 = iv.modelToView(1, alloc, Position.Bias.Forward);
            check(s0.getBounds().x == alloc.x && s0.getBounds().width == 0, "start maps to left edge, zero width");
            check(s1.getBounds().x == alloc.x + alloc.width && s1.getBounds().width == 0, "end maps to right edge, zero width");
            check(throwsBLE(() -> iv.modelToView(2, alloc, Position.Bias.Forward)), "modelToView out of range -> BadLocationException");

            // viewToModel chooses start for left half, end for right half
            Position.Bias[] bias = new Position.Bias[1];
            check(iv.viewToModel(alloc.x - 1f, 0f, alloc, bias) == 0, "left of midpoint -> start offset");
            check(bias[0] == Position.Bias.Forward, "forward bias for left half");
            check(iv.viewToModel(alloc.x + alloc.width + 1f, 0f, alloc, bias) == 1, "right of midpoint -> end offset");
            check(bias[0] == Position.Bias.Backward, "backward bias for right half");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(IconViewTest::testWhiteBox);
        runOnEDT(IconViewTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }
    interface ThrowingRunnable { void run() throws Exception; }
    static void runOnEDT(ThrowingRunnable r) throws Exception {
        try {
            EventQueue.invokeAndWait(() -> {
                try { r.run(); }
                catch (Exception e) { throw new RuntimeException(e); }
            });
        } catch (InvocationTargetException ite) {
            Throwable t = ite.getCause();
            if (t instanceof RuntimeException) throw (RuntimeException) t;
            if (t instanceof Error) throw (Error) t;
            throw new RuntimeException(t);
        }
    }
}
