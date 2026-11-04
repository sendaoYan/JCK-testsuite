/*
 * @test
 * @summary javax.swing.text.html.AccessibleHTML conformance (package-private):
 *          shape, JEditorPane accessibility bridge and direct reflective
 *          construction (EDT-driven, headless-safe; reflection needs
 *          --add-opens on JDK-9+).
 * @run main/othervm --add-opens=java.desktop/javax.swing.text.html=ALL-UNNAMED AccessibleHTMLTest
 */

import java.awt.EventQueue;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import javax.swing.JEditorPane;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.text.html.HTMLEditorKit;

public class AccessibleHTMLTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static final String ACC_HTML =
            "<html><body><p>acc</p><a href='http://x/'>link</a></body></html>";

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new MetalLookAndFeel());

        // ---- shape ----
        Class<?> c = Class.forName("javax.swing.text.html.AccessibleHTML", false,
                AccessibleHTMLTest.class.getClassLoader());
        check(c.getModifiers() == 0, "package-private class");
        boolean impl = false;
        for (Class<?> i : c.getInterfaces()) if (i == Accessible.class) impl = true;
        check(impl, "implements javax.accessibility.Accessible");
        String[] nested = { "HTMLAccessibleContext", "IconElementInfo", "TableElementInfo", "TextElementInfo" };
        for (String n : nested) {
            check(Class.forName("javax.swing.text.html.AccessibleHTML$" + n, false,
                    AccessibleHTMLTest.class.getClassLoader()) != null, "nested " + n + " exists");
        }

        // ---- JEditorPane accessibility bridge (public path, on the EDT) ----
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                try {
                    JEditorPane ep = new JEditorPane();
                    ep.setEditorKit(new HTMLEditorKit());
                    ep.setContentType("text/html");
                    ep.setText(ACC_HTML);
                    ep.setSize(100, 100);
                    AccessibleContext ac = ep.getAccessibleContext();
                    check(ac != null, "editor pane accessible context non-null");
                    check("javax.swing.JEditorPane$AccessibleJEditorPaneHTML".equals(ac.getClass().getName()),
                          "html content installs the HTML accessible context: " + ac.getClass().getName());
                    check(ac.getAccessibleRole() == javax.accessibility.AccessibleRole.TEXT,
                          "root role is text");
                    check(ac.getAccessibleChildrenCount() == 5,
                          "root exposes five accessible children: " + ac.getAccessibleChildrenCount());
                    check(ac.getAccessibleName() == null, "accessible name defaults to null");
                    AccessibleStateSet states = ac.getAccessibleStateSet();
                    check(states.contains(AccessibleState.ENABLED), "state enabled");
                    check(states.contains(AccessibleState.FOCUSABLE), "state focusable");
                    check(states.contains(AccessibleState.VISIBLE), "state visible");
                    check(states.contains(AccessibleState.OPAQUE), "state opaque");
                    check(states.contains(AccessibleState.EDITABLE), "state editable");
                    check(states.contains(AccessibleState.MULTI_LINE), "state multi line");
                    check(ac.getAccessibleText() != null, "text accessible provided");
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            }
        });

        // ---- direct reflective construction ----
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                try {
                    Class<?> cls = Class.forName("javax.swing.text.html.AccessibleHTML");
                    Constructor<?> ctor = cls.getConstructor(JEditorPane.class);
                    ctor.setAccessible(true);
                    JEditorPane ep = new JEditorPane();
                    ep.setEditorKit(new HTMLEditorKit());
                    ep.setContentType("text/html");
                    ep.setText("<html><body><p>x</p><p>y</p></body></html>");
                    Object ah = ctor.newInstance(ep);
                    Method gac = cls.getMethod("getAccessibleContext");
                    gac.setAccessible(true);
                    Object ctx = gac.invoke(ah);
                    check(ctx != null, "reflective getAccessibleContext non-null");
                    check("javax.swing.text.html.AccessibleHTML$RootHTMLAccessibleContext"
                          .equals(ctx.getClass().getName()),
                          "root context class: " + ctx.getClass().getName());
                    AccessibleContext rac = (AccessibleContext) ctx;
                    check(rac.getAccessibleRole() == javax.accessibility.AccessibleRole.TEXT,
                          "reflective root role is text");
                    check(rac.getAccessibleChildrenCount() == 5,
                          "reflective root children: " + rac.getAccessibleChildrenCount());
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            }
        });

        System.out.println("PASS (" + passed + " assertions)");
    }
}
