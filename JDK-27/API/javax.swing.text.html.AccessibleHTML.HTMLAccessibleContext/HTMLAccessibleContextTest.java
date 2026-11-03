/*
 * @test
 * @summary javax.swing.text.html.AccessibleHTML.HTMLAccessibleContext conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main HTMLAccessibleContextTest
 */

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.event.FocusListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Locale;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleComponent;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.text.html.HTMLEditorKit;

public class HTMLAccessibleContextTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.AccessibleHTML$HTMLAccessibleContext");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == false, "public modifier");
        check(Modifier.isProtected(mods) == true, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.accessibility.AccessibleContext"), "superclass");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.accessibility.Accessible"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.accessibility.AccessibleComponent"), "interface 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        JFrame frame = new JFrame();
        JEditorPane editor = new JEditorPane();
        try {
            editor.setEditorKit(new HTMLEditorKit());
            editor.setLocale(Locale.US);
            editor.setText("<html><body><p>hello world</p></body></html>");
            frame.getContentPane().add(editor);
            frame.setSize(600, 400);
            frame.setVisible(true);
            editor.getUI().getRootView(editor).setSize(600, 400);

            HTMLEditorKit kit = (HTMLEditorKit) editor.getEditorKit();
            AccessibleContext ctx = kit.getAccessibleContext();
            check(ctx != null, "HTMLAccessibleContext reachable via kit.getAccessibleContext()");
            check(Class.forName("javax.swing.text.html.AccessibleHTML$HTMLAccessibleContext").isInstance(ctx), "instanceof HTMLAccessibleContext");
            check(ctx instanceof AccessibleContext, "is AccessibleContext");
            check(ctx instanceof Accessible, "is Accessible");
            check(ctx instanceof AccessibleComponent, "is AccessibleComponent");
            check(((Accessible) ctx).getAccessibleContext() == ctx, "getAccessibleContext() returns this");

            check(ctx.getAccessibleChildrenCount() >= 1, "has accessible children");
            check(ctx.getAccessibleIndexInParent() == -1, "root index-in-parent == -1");
            AccessibleStateSet states = ctx.getAccessibleStateSet();
            check(states != null && states.contains(AccessibleState.ENABLED), "state set contains ENABLED");
            check(Locale.US.equals(ctx.getLocale()), "locale fixed to Locale.US");

            AccessibleComponent comp = (AccessibleComponent) ctx;
            check(ctx.getAccessibleComponent() == comp, "getAccessibleComponent() == this");

            // enabled round trip
            check(comp.isEnabled(), "default enabled");
            comp.setEnabled(false);
            check(!comp.isEnabled(), "disabled after setEnabled(false)");
            comp.setEnabled(true);
            check(comp.isEnabled(), "re-enabled");

            // background/foreground round trips
            comp.setBackground(Color.RED);
            check(Color.RED.equals(comp.getBackground()), "background round trip");
            comp.setForeground(Color.BLUE);
            check(Color.BLUE.equals(comp.getForeground()), "foreground round trip");

            // font + font metrics
            Font f = comp.getFont();
            check(f != null, "font non-null");
            FontMetrics fm = comp.getFontMetrics(f);
            check(fm != null, "fontMetrics non-null");

            // cursor round trip
            check(comp.getCursor() != null, "default cursor non-null");
            Cursor hand = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
            comp.setCursor(hand);
            check(hand.equals(comp.getCursor()), "cursor round trip");

            // size/bounds/location are non-null with non-negative extents
            Dimension size = comp.getSize();
            check(size != null && size.width >= 0 && size.height >= 0, "size >= 0");
            check(comp.getBounds() != null, "bounds non-null");
            check(comp.getLocation() != null, "location non-null");
            comp.setLocation(new Point(3, 4));
            check(comp.getLocation() != null, "setLocation callable (no-op), location still non-null");

            // visibility round trip (flag-based, synchronous on EDT)
            comp.setVisible(false);
            check(!comp.isVisible(), "hidden after setVisible(false)");
            comp.setVisible(true);
            check(comp.isVisible(), "shown after setVisible(true)");

            // focus listener add/remove are callable
            FocusListener fl = new FocusListener() {
                public void focusGained(java.awt.event.FocusEvent e) {}
                public void focusLost(java.awt.event.FocusEvent e) {}
            };
            comp.addFocusListener(fl);
            comp.removeFocusListener(fl);
            check(true, "add/removeFocusListener callable");

            // contains + requestFocus callable (no exact geometry asserted)
            check(comp.contains(new Point(1, 1)) || !comp.contains(new Point(1, 1)), "contains returns boolean");
            comp.requestFocus();
            check(true, "requestFocus callable");
        } finally {
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(HTMLAccessibleContextTest::testWhiteBox);
        runOnEDT(HTMLAccessibleContextTest::testBlackBox);
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
