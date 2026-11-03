/*
 * @test
 * @summary javax.swing.text.TableView.TableRow conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main TableRowTest
 */

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.text.Element;
import javax.swing.text.TableView;
import javax.swing.text.View;

public class TableRowTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static final class TestTableView extends TableView {
        TestTableView(Element e) { super(e); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.TableView$TableRow");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.BoxView"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        JFrame frame = new JFrame();
        JTextArea ta = new JTextArea("hello world");
        frame.add(ta);
        frame.pack();
        try {
            Element root = ta.getDocument().getDefaultRootElement();
            TestTableView tv = new TestTableView(root);
            TableView.TableRow row = tv.new TableRow(root);

            check(row.getAxis() == View.X_AXIS, "row tiles cells horizontally");
            check(row.getStartOffset() == 0 && row.getEndOffset() == root.getEndOffset(), "offsets from element");

            // empty row: no columns contributed yet
            check(row.getMinimumSpan(View.X_AXIS) == 0f, "X minimum == 0 (empty columns)");
            check(row.getPreferredSpan(View.X_AXIS) == 0f, "X preferred == 0 (empty columns)");
            check(row.getMaximumSpan(View.X_AXIS) == Integer.MAX_VALUE, "X maximum unbounded");
            check(row.getMinimumSpan(View.Y_AXIS) == 0f, "Y minimum == 0 (empty)");
            check(row.getPreferredSpan(View.Y_AXIS) == 0f, "Y preferred == 0 (empty)");
            check(row.getMaximumSpan(View.Y_AXIS) == Integer.MAX_VALUE, "Y maximum unbounded (minor axis)");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(TableRowTest::testWhiteBox);
        runOnEDT(TableRowTest::testBlackBox);
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
