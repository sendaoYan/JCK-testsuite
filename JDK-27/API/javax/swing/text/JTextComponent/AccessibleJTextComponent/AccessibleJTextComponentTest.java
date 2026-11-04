/*
 * @test
 * @summary javax.swing.text.JTextComponent.AccessibleJTextComponent conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main AccessibleJTextComponentTest
 */

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleState;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.text.AttributeSet;
import javax.swing.text.JTextComponent;

public class AccessibleJTextComponentTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.JTextComponent$AccessibleJTextComponent");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent$AccessibleJComponent"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 6, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.accessibility.AccessibleText"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.swing.event.CaretListener"), "interface 1");
        check(c.getInterfaces()[2].getName().equals("javax.swing.event.DocumentListener"), "interface 2");
        check(c.getInterfaces()[3].getName().equals("javax.accessibility.AccessibleAction"), "interface 3");
        check(c.getInterfaces()[4].getName().equals("javax.accessibility.AccessibleEditableText"), "interface 4");
        check(c.getInterfaces()[5].getName().equals("javax.accessibility.AccessibleExtendedText"), "interface 5");
        check(c.getMethod("getCaretPosition") != null, "getCaretPosition present");
        check(c.getMethod("getSelectionStart") != null, "getSelectionStart present");
        check(c.getMethod("getSelectionEnd") != null, "getSelectionEnd present");
        check(c.getMethod("getCharCount") != null, "getCharCount present");
        check(c.getMethod("getTextRange", int.class, int.class) != null, "getTextRange present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        JFrame frame = new JFrame();
        try {
            JTextArea area = new JTextArea();
            frame.add(area);
            frame.pack();
            frame.setVisible(true);

            area.setText("Hello World"); // length 11
            AccessibleContext ctx = area.getAccessibleContext();
            check(ctx instanceof JTextComponent.AccessibleJTextComponent, "accessible context type");
            JTextComponent.AccessibleJTextComponent acc = (JTextComponent.AccessibleJTextComponent) ctx;

            check(acc.getAccessibleText() == acc, "getAccessibleText returns this");
            check(acc.getAccessibleEditableText() == acc, "getAccessibleEditableText returns this");
            check(acc.getAccessibleAction() == acc, "getAccessibleAction returns this");
            check(acc.getAccessibleRole() == AccessibleRole.TEXT, "role TEXT");
            check(acc.getAccessibleStateSet().contains(AccessibleState.EDITABLE), "editable state set");

            check(acc.getCharCount() == 11, "char count");
            check(acc.getIndexAtPoint(null) == -1, "getIndexAtPoint(null) -> -1");
            AttributeSet as = acc.getCharacterAttribute(0);
            check(as != null, "character attribute non-null");

            area.setCaretPosition(3);
            check(acc.getCaretPosition() == 3, "accessible caret position");
            area.select(1, 4);
            check(acc.getSelectionStart() == 1 && acc.getSelectionEnd() == 4, "accessible selection range");
            check("ell".equals(acc.getSelectedText()), "accessible selected text");
            check("Hello".equals(acc.getTextRange(0, 5)), "getTextRange(0,5)");

            acc.setTextContents("abc");
            check("abc".equals(area.getText()), "setTextContents updates component text");
            check(acc.getCharCount() == 3, "char count after setTextContents");

            check(acc.getAccessibleActionCount() > 0, "positive action count");
            check(acc.getAccessibleActionDescription(0) != null, "action description 0");
            check(acc.getAccessibleActionDescription(-1) == null, "action description -1 null");
        } finally {
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(AccessibleJTextComponentTest::testWhiteBox);
        runOnEDT(AccessibleJTextComponentTest::testBlackBox);
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
