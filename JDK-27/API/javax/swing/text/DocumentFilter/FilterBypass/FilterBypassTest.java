/*
 * @test
 * @summary javax.swing.text.DocumentFilter.FilterBypass conformance: white-box structure + black-box behavior.
 * @run main FilterBypassTest
 */

import java.lang.reflect.Modifier;
import javax.swing.JTextArea;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class FilterBypassTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.DocumentFilter$FilterBypass");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        JTextArea area = new JTextArea("abcdef");
        AbstractDocument doc = (AbstractDocument) area.getDocument();
        final DocumentFilter.FilterBypass[] captured = new DocumentFilter.FilterBypass[1];
        doc.setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offs, int len, String text, AttributeSet attrs) throws BadLocationException {
                captured[0] = fb;
                super.replace(fb, offs, len, text, attrs);
            }
        });

        doc.replace(0, 2, "XY", null);
        DocumentFilter.FilterBypass bypass = captured[0];
        check(bypass != null, "bypass captured from filter");
        check(bypass.getDocument() == doc, "getDocument returns the mutated document");
        check(doc.getText(0, doc.getLength()).equals("XYcdef"), "replace applied");

        bypass.insertString(0, "P", null);
        check(doc.getText(0, doc.getLength()).equals("PXYcdef"), "bypass insertString");

        bypass.remove(0, 1);
        check(doc.getText(0, doc.getLength()).equals("XYcdef"), "bypass remove");

        bypass.replace(0, 2, "Q", null);
        check(doc.getText(0, doc.getLength()).equals("Qcdef"), "bypass replace");

        doc.setDocumentFilter(null);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
