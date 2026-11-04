/*
 * @test
 * @summary javax.swing.JTextArea conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main JTextAreaTest
 */

import java.awt.EventQueue;
import java.lang.reflect.Modifier;
import javax.swing.JTextArea;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

public class JTextAreaTest {
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
        Class<?> c = Class.forName("javax.swing.JTextArea");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.JTextComponent"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Default construction.
        JTextArea a0 = new JTextArea();
        check("".equals(a0.getText()), "default text empty");
        check(a0.getRows() == 0, "default rows 0");
        check(a0.getColumns() == 0, "default columns 0");
        check(a0.getDocument() instanceof PlainDocument, "default document PlainDocument");
        check("TextAreaUI".equals(a0.getUIClassID()), "getUIClassID TextAreaUI");

        // Text constructor.
        JTextArea a1 = new JTextArea("hello");
        check("hello".equals(a1.getText()), "JTextArea(String) text");

        // rows/columns constructor + roundtrip.
        JTextArea a2 = new JTextArea(5, 40);
        check(a2.getRows() == 5, "JTextArea(int,int) rows");
        check(a2.getColumns() == 40, "JTextArea(int,int) columns");
        a2.setRows(7);
        check(a2.getRows() == 7, "setRows/getRows");
        a2.setColumns(33);
        check(a2.getColumns() == 33, "setColumns/getColumns");

        // text + rows + columns constructor.
        JTextArea a3 = new JTextArea("txt", 6, 20);
        check("txt".equals(a3.getText()), "JTextArea(String,int,int) text");
        check(a3.getRows() == 6, "rows");
        check(a3.getColumns() == 20, "columns");

        // Document constructor roundtrip.
        Document d = new PlainDocument();
        JTextArea a4 = new JTextArea(d);
        check(a4.getDocument() == d, "JTextArea(Document) document preserved");
        Document d2 = new PlainDocument();
        JTextArea a5 = new JTextArea(d2, "doc", 3, 9);
        check(a5.getDocument() == d2, "JTextArea(Document,String,int,int) document preserved");
        check("doc".equals(a5.getText()), "document ctor text");
        check(a5.getRows() == 3 && a5.getColumns() == 9, "document ctor rows/cols");

        // append / insert / replaceRange chain.
        JTextArea a = new JTextArea();
        a.append("abc");
        check("abc".equals(a.getText()), "append");
        a.insert("XY", 1);
        check("aXYbc".equals(a.getText()), "insert");
        a.replaceRange("ZZ", 1, 3);
        check("aZZbc".equals(a.getText()), "replaceRange");
        a.replaceRange("", 0, 2);
        check("Zbc".equals(a.getText()), "replaceRange delete");

        // Line/wrap properties.
        a.setLineWrap(true);
        check(a.getLineWrap(), "setLineWrap(true)");
        a.setWrapStyleWord(true);
        check(a.getWrapStyleWord(), "setWrapStyleWord(true)");
        a.setLineWrap(false);
        check(!a.getLineWrap(), "setLineWrap(false)");
        a.setWrapStyleWord(false);
        check(!a.getWrapStyleWord(), "setWrapStyleWord(false)");

        // Tab size roundtrip.
        a.setTabSize(8);
        check(a.getTabSize() == 8, "setTabSize/getTabSize");

        // Line/offset queries over multi-line text.
        JTextArea lines = new JTextArea("aa\nbb\ncc");
        check(lines.getLineCount() == 3, "getLineCount 3");
        check(lines.getLineOfOffset(0) == 0, "getLineOfOffset(0)");
        check(lines.getLineOfOffset(3) == 1, "getLineOfOffset(3)");
        check(lines.getLineOfOffset(8) == 2, "getLineOfOffset(8)");
        check(lines.getLineStartOffset(0) == 0, "getLineStartOffset(0)");
        check(lines.getLineStartOffset(1) == 3, "getLineStartOffset(1)");
        check(lines.getLineStartOffset(2) == 6, "getLineStartOffset(2)");
        check(lines.getLineEndOffset(0) == 3, "getLineEndOffset(0)");
        check(lines.getLineEndOffset(1) == 6, "getLineEndOffset(1)");
        check(lines.getLineEndOffset(2) == 8, "getLineEndOffset(2)");

        // Caret position chain.
        lines.setCaretPosition(4);
        check(lines.getCaretPosition() == 4, "setCaretPosition/getCaretPosition");
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(JTextAreaTest::testWhiteBox);
        runOnEDT(JTextAreaTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
