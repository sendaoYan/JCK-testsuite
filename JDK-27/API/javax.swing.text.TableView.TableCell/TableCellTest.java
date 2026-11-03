/*
 * @test
 * @summary javax.swing.text.TableView.TableCell conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main TableCellTest
 */

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.text.Element;
import javax.swing.text.TableView;
import javax.swing.text.View;

public class TableCellTest {
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
        Class<?> c = Class.forName("javax.swing.text.TableView$TableCell");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.BoxView"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.text.TableView$GridCell"), "interface 0");
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
            TableView.TableCell cell = tv.new TableCell(root);

            check(cell.getAxis() == View.Y_AXIS, "cell tiles its content vertically");
            check(cell.getStartOffset() == 0 && cell.getEndOffset() == root.getEndOffset(), "offsets from element");

            // grid cell contract: single cell span by default
            check(cell.getColumnCount() == 1, "default column span == 1");
            check(cell.getRowCount() == 1, "default row span == 1");

            // grid location round-trips
            check(cell.getGridRow() == 0 && cell.getGridColumn() == 0, "default grid location (0,0)");
            cell.setGridLocation(2, 3);
            check(cell.getGridRow() == 2 && cell.getGridColumn() == 3, "setGridLocation round-trip");

            // empty cell spans are zero
            check(cell.getPreferredSpan(View.X_AXIS) == 0f && cell.getPreferredSpan(View.Y_AXIS) == 0f, "empty preferred spans zero");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(TableCellTest::testWhiteBox);
        runOnEDT(TableCellTest::testBlackBox);
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
