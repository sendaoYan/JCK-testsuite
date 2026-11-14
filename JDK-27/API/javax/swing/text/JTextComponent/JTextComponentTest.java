/*
 * @test
 * @summary javax.swing.text.JTextComponent conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main JTextComponentTest
 */

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

public class JTextComponentTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean throwsIAE(ThrowingRunnable r) {
        try { r.run(); return false; }
        catch (Throwable t) { return t instanceof IllegalArgumentException; }
    }
    static boolean throwsBadLocation(ThrowingRunnable r) {
        try { r.run(); return false; }
        catch (Throwable t) { return t instanceof BadLocationException; }
    }
    static String textAt(JTextArea a, int offs, int len) {
        try { return a.getText(offs, len); }
        catch (BadLocationException e) { throw new RuntimeException(e); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.JTextComponent");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.Scrollable"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.accessibility.Accessible"), "interface 1");
        check(c.getMethod("setText", String.class) != null, "setText present");
        check(c.getMethod("getText") != null, "getText present");
        check(c.getMethod("select", int.class, int.class) != null, "select present");
        check(c.getMethod("getSelectedText") != null, "getSelectedText present");
        check(c.getMethod("setDocument", javax.swing.text.Document.class) != null, "setDocument present");
        check(c.getMethod("copy") != null && c.getMethod("cut") != null && c.getMethod("paste") != null, "copy/cut/paste present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        JFrame frame = new JFrame();
        try {
            JTextArea area = new JTextArea();
            frame.add(area);
            frame.pack();
            frame.setVisible(true);

            // text round trip
            check("".equals(area.getText()), "default text empty");
            area.setText("Hello World");
            check("Hello World".equals(area.getText()), "setText/getText round trip");
            area.setText(null);
            check("".equals(area.getText()), "setText(null) clears text");
            area.setText("Hello World");

            // getText(offs,len) + BadLocationException
            check("Hello".equals(textAt(area, 0, 5)), "getText(0,5)");
            check(throwsBadLocation(() -> area.getText(-1, 5)), "getText(-1,5) -> BLE");
            check(throwsBadLocation(() -> area.getText(0, -1)), "getText(0,-1) -> BLE");
            check(throwsBadLocation(() -> area.getText(0, 100)), "getText(0,100) -> BLE");

            // caret position
            area.setCaretPosition(5);
            check(area.getCaretPosition() == 5, "setCaretPosition round trip");
            area.moveCaretPosition(8);
            check(area.getCaretPosition() == 8, "moveCaretPosition");
            check(throwsIAE(() -> area.setCaretPosition(100)), "setCaretPosition(100) -> IAE");
            check(throwsIAE(() -> area.setCaretPosition(-1)), "setCaretPosition(-1) -> IAE");
            check(throwsIAE(() -> area.moveCaretPosition(-1)), "moveCaretPosition(-1) -> IAE");

            // selection
            area.select(0, 5);
            check(area.getSelectionStart() == 0 && area.getSelectionEnd() == 5, "select(0,5)");
            check("Hello".equals(area.getSelectedText()), "getSelectedText");
            area.setSelectionStart(2);
            area.setSelectionEnd(7);
            check(area.getSelectionStart() == 2 && area.getSelectionEnd() == 7, "setSelectionStart/End");
            check("llo W".equals(area.getSelectedText()), "getSelectedText after setSelection");
            area.selectAll();
            check(area.getSelectionStart() == 0 && area.getSelectionEnd() == area.getText().length(), "selectAll");

            // editable
            check(area.isEditable(), "default editable true");
            area.setEditable(false);
            check(!area.isEditable(), "setEditable(false)");

            // document replace
            check(area.getDocument() != null, "document non-null");
            PlainDocument pd = new PlainDocument();
            area.setDocument(pd);
            check(area.getDocument() == pd, "setDocument round trip");

            // caret / highlighter / keymap / actions
            check(area.getCaret() != null, "caret non-null");
            check(area.getHighlighter() != null, "highlighter non-null");
            check(area.getKeymap() != null, "keymap non-null");
            check(area.getActions() != null && area.getActions().length > 0, "actions non-empty");

            // focus accelerator
            check(area.getFocusAccelerator() == '\0', "default focus accelerator NUL");
            area.setFocusAccelerator('x');
            check(area.getFocusAccelerator() == 'X', "setFocusAccelerator round trip (uppercased)");

            // drag enabled
            check(area.getDragEnabled() == false, "default dragEnabled false");
            area.setDragEnabled(true);
            check(area.getDragEnabled(), "setDragEnabled(true)");

            // 剪贴板是进程外的全局独占资源：并行跑用例（-conc:16）时其他用例会短暂
            // 占用它，Toolkit 的复制/剪切/粘贴动作把失败吞成鸣笛（cut 失败时选区
            // 原样留在文本里），所以往返断言要重试到剪贴板空出来。
            JTextArea a1 = new JTextArea("ClipboardText");
            JTextArea a2 = new JTextArea("");
            boolean copied = false;
            for (int i = 0; i < 40 && !copied; i++) {
                a1.select(0, 9); // "Clipboard"
                a1.copy();
                a2.setText("");
                a2.paste();
                copied = "Clipboard".equals(a2.getText());
                if (!copied) sleepQuietly();
            }
            check("ClipboardText".equals(a1.getText()), "copy leaves source text");
            check(copied, "copy->paste round trip");

            // cut -> paste round trip
            JTextArea a3 = new JTextArea("ClipboardText");
            JTextArea a4 = new JTextArea("");
            boolean cutPasted = false;
            for (int i = 0; i < 40 && !cutPasted; i++) {
                a3.setText("ClipboardText");
                a3.select(0, 9);
                a3.cut();
                if (!"Text".equals(a3.getText())) { sleepQuietly(); continue; }
                a4.setText("");
                a4.paste();
                cutPasted = "Clipboard".equals(a4.getText());
                if (!cutPasted) sleepQuietly();
            }
            check("Text".equals(a3.getText()), "cut removes selection");
            check(cutPasted, "cut->paste round trip");
        } finally {
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(JTextComponentTest::testWhiteBox);
        runOnEDT(JTextComponentTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }
    static void sleepQuietly() {
        try { Thread.sleep(100); }
        catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
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
