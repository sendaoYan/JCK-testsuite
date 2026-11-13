/*
 * @test
 * @summary javax.swing.text.html.AccessibleHTML.TableElementInfo.TableAccessibleContext conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main TableAccessibleContextTest
 */

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Locale;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleTable;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.text.html.HTMLEditorKit;

public class TableAccessibleContextTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.AccessibleHTML$TableElementInfo$TableAccessibleContext");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.html.AccessibleHTML$HTMLAccessibleContext"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.accessibility.AccessibleTable"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        JFrame frame = new JFrame();
        JEditorPane editor = new JEditorPane();
        try {
            editor.setEditorKit(new HTMLEditorKit());
            editor.setLocale(Locale.US);
            editor.setText("<html><body><table border='1'><tr><td>a</td><td>b</td></tr><tr><td>c</td><td>d</td></tr></table></body></html>");
            frame.getContentPane().add(editor);
            frame.setSize(600, 400);
            frame.setVisible(true);
            editor.getUI().getRootView(editor).setSize(600, 400);

            HTMLEditorKit kit = (HTMLEditorKit) editor.getEditorKit();
            AccessibleContext root = kit.getAccessibleContext();
            AccessibleContext table = findContext(root, "TableAccessibleContext");
            check(table != null, "TableAccessibleContext reachable");
            check(Class.forName("javax.swing.text.html.AccessibleHTML$TableElementInfo$TableAccessibleContext").isInstance(table), "instanceof TableAccessibleContext");
            check(table instanceof AccessibleTable, "is AccessibleTable");

            check(table.getAccessibleRole() == AccessibleRole.TABLE, "role TABLE");
            // AccessibleHTML 用 getAccessibleRole().toString() 作为名称，而
            // AccessibleRole.toString() 返回的是本地化显示串（中文 Windows 上
            // 不是 "table"）。用同一 role 的显示串比较，仍是“名称即角色文本”。
            String roleText = AccessibleRole.TABLE.toString();
            check(roleText != null && !roleText.isEmpty(), "AccessibleRole.TABLE display text present");
            check(roleText.equals(table.getAccessibleName()), "name == role text");

            AccessibleTable t = (AccessibleTable) table;
            check(t.getAccessibleRowCount() == 2, "row count == 2");
            check(t.getAccessibleColumnCount() == 2, "column count == 2");
            check(table.getAccessibleChildrenCount() == 4, "children == rows * cols");

            check(t.getAccessibleAt(0, 0) != null, "cell(0,0) non-null");
            check(t.getAccessibleAt(1, 1) != null, "cell(1,1) non-null");
            check(t.getAccessibleAt(2, 2) == null, "cell(2,2) out of bounds null");

            check(t.getAccessibleRowExtentAt(0, 0) == 1, "row extent 1");
            check(t.getAccessibleColumnExtentAt(0, 0) == 1, "column extent 1");

            check(t.getAccessibleCaption() == null, "caption null");
            check(t.getAccessibleSummary() == null, "summary null");
            check(t.getAccessibleRowHeader() == null, "row header null without TH");
            check(t.getAccessibleColumnHeader() == null, "column header null");

            // no-op setters are callable
            t.setAccessibleCaption(null);
            t.setAccessibleSummary(null);
            t.setAccessibleRowHeader(null);
            t.setAccessibleColumnHeader(null);
            check(true, "table setters callable");

            check(t.isAccessibleSelected(0, 0) == false, "cell not selected");
            check(t.isAccessibleRowSelected(0) == false, "row not selected");
            check(t.isAccessibleColumnSelected(0) == false, "column not selected");
            check(t.getSelectedAccessibleRows().length == 0, "no selected rows");
            check(t.getSelectedAccessibleColumns().length == 0, "no selected columns");
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
        runOnEDT(TableAccessibleContextTest::testWhiteBox);
        runOnEDT(TableAccessibleContextTest::testBlackBox);
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
