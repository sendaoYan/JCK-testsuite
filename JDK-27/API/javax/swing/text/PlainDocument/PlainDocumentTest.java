/*
 * @test
 * @summary javax.swing.text.PlainDocument conformance: white-box structure + black-box behavior.
 * @run main PlainDocumentTest
 */

import java.lang.reflect.Modifier;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import javax.swing.text.StringContent;

public class PlainDocumentTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    interface ThrowingRunnable { void run() throws Exception; }
    static void expectThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        boolean thrown = false;
        try { r.run(); } catch (Throwable t) {
            thrown = true;
            check(expected.isAssignableFrom(t.getClass()),
                  msg + " expected " + expected.getSimpleName() + " but got " + t.getClass().getSimpleName());
        }
        check(thrown, msg + " should throw " + expected.getSimpleName());
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.PlainDocument");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.AbstractDocument"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Constants and default tab size.
        check("tabSize".equals(PlainDocument.tabSizeAttribute), "tabSizeAttribute");
        check("lineLimit".equals(PlainDocument.lineLimitAttribute), "lineLimitAttribute");

        PlainDocument doc = new PlainDocument();
        check(doc.getLength() == 0, "initial length 0");
        check(Integer.valueOf(8).equals(doc.getProperty(PlainDocument.tabSizeAttribute)),
              "default tab size 8");

        // text round-trips.
        doc.insertString(0, "hello world", null);
        check("hello world".equals(doc.getText(0, doc.getLength())), "getText round-trip");
        doc.insertString(5, " brave", null);
        check("hello brave world".equals(doc.getText(0, doc.getLength())), "mid insert");
        doc.remove(5, 6);
        check("hello world".equals(doc.getText(0, doc.getLength())), "remove round-trip");

        // line-based element structure.
        PlainDocument lines = new PlainDocument();
        lines.insertString(0, "one\ntwo\nthree", null);
        Element root = lines.getDefaultRootElement();
        check("paragraph".equals(root.getName()), "root name paragraph");
        check(root.getElementCount() == 3, "three lines");
        Element line0 = lines.getParagraphElement(0);
        Element line1 = lines.getParagraphElement(4);
        Element line2 = lines.getParagraphElement(8);
        check(line0 == root.getElement(0) && line1 == root.getElement(1) && line2 == root.getElement(2),
              "getParagraphElement maps to lines");
        check(line0.getStartOffset() == 0 && line0.getEndOffset() == 4, "line0 range");
        check(line1.getStartOffset() == 4 && line1.getEndOffset() == 8, "line1 range");
        check(line2.getStartOffset() == 8 && line2.getEndOffset() == lines.getLength() + 1, "line2 range");

        // newline filtering when the property is enabled.
        PlainDocument filtered = new PlainDocument();
        filtered.putProperty("filterNewlines", Boolean.TRUE);
        filtered.insertString(0, "a\nb", null);
        check("a b".equals(filtered.getText(0, filtered.getLength())),
              "filterNewlines replaces newline with space");

        // explicit content constructor.
        PlainDocument custom = new PlainDocument(new StringContent());
        custom.insertString(0, "custom", null);
        check("custom".equals(custom.getText(0, custom.getLength())), "StringContent-backed document");

        // insertString is a no-op for null/empty.
        PlainDocument empty = new PlainDocument();
        empty.insertString(0, null, null);
        empty.insertString(0, "", null);
        check(empty.getLength() == 0, "null/empty insert is no-op");

        // bounds.
        expectThrows(BadLocationException.class, () -> doc.insertString(doc.getLength() + 1, "x", null), "insert past end");
        expectThrows(BadLocationException.class, () -> doc.remove(0, doc.getLength() + 1), "remove past end");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
