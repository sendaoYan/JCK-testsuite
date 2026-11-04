/*
 * @test
 * @summary javax.swing.text.PasswordView conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main PasswordViewTest
 */

import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import javax.swing.JFrame;
import javax.swing.JPasswordField;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.PasswordView;
import javax.swing.text.Position;
import javax.swing.text.View;

public class PasswordViewTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.PasswordView");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.FieldView"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        JFrame frame = new JFrame();
        JPasswordField pf = new JPasswordField("secret");
        frame.add(pf);
        frame.pack();
        try {
            Document doc = pf.getDocument();
            Element leaf = doc.getDefaultRootElement().getElement(0);

            PasswordView pv = new PasswordView(leaf);
            check(pv.getElement() == leaf, "getElement returns ctor element");
            check(pv.getStartOffset() == leaf.getStartOffset() && pv.getEndOffset() == leaf.getEndOffset(), "offsets from element");

            // actual password field uses a PasswordView
            View real = pf.getUI().getRootView(pf).getView(0);
            check(real instanceof PasswordView, "JPasswordField root child is PasswordView");

            // echo-char based preferred span is positive
            float xspan = real.getPreferredSpan(View.X_AXIS);
            check(xspan > 0f, "echo-based X span positive");
            check(real.getPreferredSpan(View.Y_AXIS) > 0f, "Y span positive");
            check(real.getMinimumSpan(View.X_AXIS) == 0f, "X minimum == 0 (resizable)");
            check(real.getMaximumSpan(View.X_AXIS) == Integer.MAX_VALUE, "X maximum == MAX_VALUE (resizable)");

            // modelToView maps each position to an echo-char column
            Rectangle alloc = new Rectangle(0, 0, 120, 30);
            Shape s0 = real.modelToView(0, alloc, Position.Bias.Forward);
            Shape s3 = real.modelToView(3, alloc, Position.Bias.Forward);
            check(s0 != null && s3 != null, "modelToView non-null for password field");
            check(s0.getBounds().width == 1, "caret is one column wide");
            check(s3.getBounds().x > s0.getBounds().x, "x advances with position");

            // viewToModel inverts the echo-char mapping
            Position.Bias[] bias = new Position.Bias[1];
            int m0 = real.viewToModel((float) s0.getBounds().x, (float) s0.getBounds().y, alloc, bias);
            check(m0 == 0, "left edge maps to start offset");
            check(bias[0] == Position.Bias.Forward, "forward bias");
            int m3 = real.viewToModel((float) s3.getBounds().x, (float) s3.getBounds().y, alloc, bias);
            check(m3 == 3, "echo column maps back to its position");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(PasswordViewTest::testWhiteBox);
        runOnEDT(PasswordViewTest::testBlackBox);
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
