/*
 * @test
 * @summary javax.swing.text.html.AccessibleHTML.TableElementInfo.TableAccessibleContext.AccessibleHeadersTable conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main AccessibleHeadersTableTest
 */

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Locale;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleTable;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.text.html.HTMLEditorKit;

public class AccessibleHeadersTableTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.AccessibleHTML$TableElementInfo$TableAccessibleContext$AccessibleHeadersTable");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == false, "public modifier");
        check(Modifier.isProtected(mods) == true, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
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
            editor.setText("<html><body><table border='1'><tr><th>r1</th><td>a</td></tr><tr><th>r2</th><td>b</td></tr></table></body></html>");
            frame.getContentPane().add(editor);
            frame.setSize(600, 400);
            frame.setVisible(true);
            editor.getUI().getRootView(editor).setSize(600, 400);

            HTMLEditorKit kit = (HTMLEditorKit) editor.getEditorKit();
            AccessibleContext root = kit.getAccessibleContext();
            AccessibleContext table = findContext(root, "TableAccessibleContext");
            check(table != null, "TableAccessibleContext reachable");
            AccessibleTable t = (AccessibleTable) table;

            // validating the main table loads the TH cells into the row-header table
            check(t.getAccessibleRowCount() == 2, "main table row count == 2");
            AccessibleTable headers = t.getAccessibleRowHeader();
            check(headers != null, "row header table non-null after validation");
            check(Class.forName("javax.swing.text.html.AccessibleHTML$TableElementInfo$TableAccessibleContext$AccessibleHeadersTable").isInstance(headers), "instanceof AccessibleHeadersTable");

            // rowCount/columnCount are never accumulated by addHeader()
            check(headers.getAccessibleRowCount() == 0, "header row count == 0");
            check(headers.getAccessibleColumnCount() == 0, "header column count == 0");

            // header cells are TableCellElementInfo, not Accessible -> null
            check(headers.getAccessibleAt(0, 0) == null, "header cell(0,0) not exposed as Accessible");

            check(headers.getAccessibleRowExtentAt(0, 0) == 1, "header row extent(0,0) == 1");
            check(headers.getAccessibleRowExtentAt(5, 5) == 0, "header row extent(5,5) == 0");
            check(headers.getAccessibleColumnExtentAt(0, 0) == 1, "header column extent(0,0) == 1");

            check(headers.getAccessibleCaption() == null, "header caption null");
            check(headers.getAccessibleSummary() == null, "header summary null");
            check(headers.getAccessibleRowHeader() == null, "header row header null");
            check(headers.getAccessibleColumnHeader() == null, "header column header null");

            // no-op setters callable
            headers.setAccessibleCaption(null);
            headers.setAccessibleSummary(null);
            headers.setAccessibleRowHeader(null);
            headers.setAccessibleColumnHeader(null);
            check(true, "header setters callable");

            check(headers.isAccessibleSelected(0, 0) == false, "header cell not selected");
            check(headers.isAccessibleRowSelected(0) == false, "header row not selected");
            check(headers.isAccessibleColumnSelected(0) == false, "header column not selected");
            check(headers.getSelectedAccessibleRows().length == 0, "no selected header rows");
            check(headers.getSelectedAccessibleColumns().length == 0, "no selected header columns");
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
        runOnEDT(AccessibleHeadersTableTest::testWhiteBox);
        runOnEDT(AccessibleHeadersTableTest::testBlackBox);
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
