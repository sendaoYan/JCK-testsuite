/*
 * @test
 * @summary javax.swing.text.html.AccessibleHTML.IconElementInfo.IconAccessibleContext conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main IconAccessibleContextTest
 */

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Locale;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleIcon;
import javax.accessibility.AccessibleRole;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.text.html.HTMLEditorKit;

public class IconAccessibleContextTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.AccessibleHTML$IconElementInfo$IconAccessibleContext");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == false, "public modifier");
        check(Modifier.isProtected(mods) == true, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.html.AccessibleHTML$HTMLAccessibleContext"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.accessibility.AccessibleIcon"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        JFrame frame = new JFrame();
        JEditorPane editor = new JEditorPane();
        try {
            editor.setEditorKit(new HTMLEditorKit());
            editor.setLocale(Locale.US);
            editor.setText("<html><body><img alt='hello' width='100' height='50' src='missing.png'></body></html>");
            frame.getContentPane().add(editor);
            frame.setSize(600, 400);
            frame.setVisible(true);
            editor.getUI().getRootView(editor).setSize(600, 400);

            HTMLEditorKit kit = (HTMLEditorKit) editor.getEditorKit();
            AccessibleContext root = kit.getAccessibleContext();
            AccessibleContext icon = findContext(root, "IconAccessibleContext");
            check(icon != null, "IconAccessibleContext reachable as accessible child");
            check(Class.forName("javax.swing.text.html.AccessibleHTML$IconElementInfo$IconAccessibleContext").isInstance(icon), "instanceof IconAccessibleContext");
            check(icon instanceof AccessibleIcon, "is AccessibleIcon");

            check(icon.getAccessibleRole() == AccessibleRole.ICON, "role ICON");
            check("hello".equals(icon.getAccessibleName()), "accessibleName == alt text");

            AccessibleIcon ai = (AccessibleIcon) icon;
            check("hello".equals(ai.getAccessibleIconDescription()), "icon description == alt text");
            check(ai.getAccessibleIconWidth() == 100, "icon width from WIDTH attribute");
            check(ai.getAccessibleIconHeight() == 50, "icon height from HEIGHT attribute");

            // getAccessibleIcon() is a class-level method (not on the interface)
            Method m = icon.getClass().getMethod("getAccessibleIcon");
            AccessibleIcon[] icons = (AccessibleIcon[]) m.invoke(icon);
            check(icons.length == 1, "getAccessibleIcon length 1");
            check(icons[0] == icon, "getAccessibleIcon()[0] == this");

            // setAccessibleIconDescription is a documented no-op entry point
            ai.setAccessibleIconDescription("other");
            check("hello".equals(ai.getAccessibleIconDescription()), "description unchanged after set");
        } finally {
            frame.dispose();
        }
    }

    static AccessibleContext findContext(AccessibleContext c, String suffix) {
        if (c.getClass().getName().endsWith(suffix)) return c;
        int n = c.getAccessibleChildrenCount();
        for (int i = 0; i < n; i++) {
            Accessible a = c.getAccessibleChild(i);
            if (a != null && a.getAccessibleContext() != null) {
                AccessibleContext r = findContext(a.getAccessibleContext(), suffix);
                if (r != null) return r;
            }
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(IconAccessibleContextTest::testWhiteBox);
        runOnEDT(IconAccessibleContextTest::testBlackBox);
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
