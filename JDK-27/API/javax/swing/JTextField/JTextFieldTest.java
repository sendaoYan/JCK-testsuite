/*
 * @test
 * @summary javax.swing.JTextField conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main JTextFieldTest
 */

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Modifier;
import javax.swing.BoundedRangeModel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

public class JTextFieldTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Exception; }

    static void runOnEDT(ThrowingRunnable r) throws Exception {
        try {
            EventQueue.invokeAndWait(() -> {
                try { r.run(); }
                catch (Exception e) { throw new RuntimeException(e); }
            });
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable t = e.getCause();
            if (t instanceof RuntimeException) throw (RuntimeException) t;
            if (t instanceof Error) throw (Error) t;
            throw new RuntimeException(t);
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JTextField");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.JTextComponent"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.SwingConstants"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Constructor defaults.
        JTextField f0 = new JTextField();
        check("".equals(f0.getText()), "default text empty");
        check(f0.getColumns() == 0, "default columns 0");
        check(f0.isEditable(), "default editable");
        check(f0.getDocument() instanceof PlainDocument, "default document PlainDocument");
        check(f0.getHorizontalAlignment() == SwingConstants.LEADING, "default horizontal alignment LEADING");

        // Text + columns constructor variants.
        JTextField f1 = new JTextField("hello");
        check("hello".equals(f1.getText()), "JTextField(String) text");
        check(f1.getColumns() == 0, "JTextField(String) columns 0");
        JTextField f2 = new JTextField(20);
        check("".equals(f2.getText()), "JTextField(int) empty text");
        check(f2.getColumns() == 20, "JTextField(int) columns");
        JTextField f3 = new JTextField("hi", 10);
        check("hi".equals(f3.getText()), "JTextField(String,int) text");
        check(f3.getColumns() == 10, "JTextField(String,int) columns");

        // Document constructor honors supplied model.
        Document doc = new PlainDocument();
        JTextField f4 = new JTextField(doc, "abc", 5);
        check(f4.getDocument() == doc, "custom document preserved");
        check("abc".equals(f4.getText()), "document ctor text");
        check(f4.getColumns() == 5, "document ctor columns");

        // setText/getText + caret chain.
        check(f0.getCaretPosition() == 0, "caret starts at 0");
        f0.setText("world");
        check("world".equals(f0.getText()), "setText/getText");
        check(f0.getCaretPosition() >= 0 && f0.getCaretPosition() <= 5, "caret within bounds after setText");
        f0.setCaretPosition(3);
        check(f0.getCaretPosition() == 3, "setCaretPosition/getCaretPosition");
        f0.setText("");
        check(f0.getCaretPosition() == 0, "caret reset after empty setText");

        // Columns roundtrip and reset.
        f3.setColumns(7);
        check(f3.getColumns() == 7, "setColumns/getColumns");
        f3.setColumns(0);
        check(f3.getColumns() == 0, "setColumns(0)");

        // Editable roundtrip.
        f3.setEditable(false);
        check(!f3.isEditable(), "setEditable(false)");
        f3.setEditable(true);
        check(f3.isEditable(), "setEditable(true)");

        // setDocument roundtrip.
        Document d2 = new PlainDocument();
        f3.setDocument(d2);
        check(f3.getDocument() == d2, "setDocument/getDocument");

        // Horizontal alignment roundtrip + invalid value.
        f3.setHorizontalAlignment(SwingConstants.CENTER);
        check(f3.getHorizontalAlignment() == SwingConstants.CENTER, "setHorizontalAlignment(CENTER)");
        f3.setHorizontalAlignment(SwingConstants.RIGHT);
        check(f3.getHorizontalAlignment() == SwingConstants.RIGHT, "setHorizontalAlignment(RIGHT)");
        f3.setHorizontalAlignment(SwingConstants.LEFT);
        check(f3.getHorizontalAlignment() == SwingConstants.LEFT, "setHorizontalAlignment(LEFT)");
        f3.setHorizontalAlignment(SwingConstants.TRAILING);
        check(f3.getHorizontalAlignment() == SwingConstants.TRAILING, "setHorizontalAlignment(TRAILING)");
        try {
            f3.setHorizontalAlignment(-1);
            check(false, "setHorizontalAlignment(-1) should throw IAE");
        } catch (IllegalArgumentException expected) {
            check(true, "setHorizontalAlignment(-1) IAE");
        }

        // Action command delivered to action listeners via postActionEvent.
        final String[] cmd = new String[1];
        final int[] count = new int[1];
        JTextField fa = new JTextField("default-cmd");
        fa.setActionCommand("cmd");
        fa.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cmd[0] = e.getActionCommand();
                count[0]++;
            }
        });
        check(fa.getActionListeners().length == 1, "one action listener registered");
        fa.postActionEvent();
        check(count[0] == 1, "action listener fired once");
        check("cmd".equals(cmd[0]), "action command delivered");

        // Visibility model non-null and well-formed BoundedRangeModel.
        BoundedRangeModel vis = fa.getHorizontalVisibility();
        check(vis != null, "getHorizontalVisibility non-null");
        check(vis.getMinimum() <= vis.getMaximum(), "visibility model range valid");
        check(vis.getValue() >= vis.getMinimum() && vis.getValue() <= vis.getMaximum(), "visibility value in range");
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(JTextFieldTest::testWhiteBox);
        runOnEDT(JTextFieldTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
