/*
 * @test
 * @summary javax.swing.text.DocumentFilter conformance: white-box structure + black-box behavior.
 * @run main DocumentFilterTest
 */

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTextArea;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class DocumentFilterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.DocumentFilter");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        JTextArea area = new JTextArea();
        area.setText("abcdef");
        AbstractDocument doc = (AbstractDocument) area.getDocument();
        check(doc.getDocumentFilter() == null, "no filter initially");

        final List<String> ops = new ArrayList<>();
        final DocumentFilter.FilterBypass[] captured = new DocumentFilter.FilterBypass[1];
        DocumentFilter f = new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offs, String str, AttributeSet a) throws BadLocationException {
                captured[0] = fb;
                ops.add("insert@" + offs + ":" + str);
                super.insertString(fb, offs, str.toUpperCase(), a);
            }
            @Override
            public void remove(FilterBypass fb, int offs, int len) throws BadLocationException {
                captured[0] = fb;
                ops.add("remove@" + offs + ":" + len);
                super.remove(fb, offs, len);
            }
            @Override
            public void replace(FilterBypass fb, int offs, int len, String text, AttributeSet attrs) throws BadLocationException {
                captured[0] = fb;
                ops.add("replace@" + offs + ":" + len + ":" + text);
                super.replace(fb, offs, len, text, attrs);
            }
        };
        doc.setDocumentFilter(f);
        check(doc.getDocumentFilter() == f, "filter set");

        doc.insertString(0, "xy", null);
        check(doc.getText(0, doc.getLength()).equals("XYabcdef"), "insertString uppercased by filter");
        check(captured[0].getDocument() == doc, "bypass getDocument returns the document");

        doc.remove(0, 2);
        check(doc.getText(0, doc.getLength()).equals("abcdef"), "remove through filter");

        doc.replace(0, 2, "mn", null);
        check(doc.getText(0, doc.getLength()).equals("mncdef"), "replace through filter");

        check(ops.size() == 3, "three filter ops recorded");
        check(ops.get(0).equals("insert@0:xy"), "insert op recorded");
        check(ops.get(1).equals("remove@0:2"), "remove op recorded");
        check(ops.get(2).equals("replace@0:2:mn"), "replace op recorded");

        // FilterBypass direct calls do not re-enter the filter
        DocumentFilter.FilterBypass fb = captured[0];
        fb.insertString(0, "P", null);
        check(doc.getText(0, doc.getLength()).equals("Pmncdef"), "bypass insertString");
        fb.remove(0, 1);
        check(doc.getText(0, doc.getLength()).equals("mncdef"), "bypass remove");
        fb.replace(0, 2, "Q", null);
        check(doc.getText(0, doc.getLength()).equals("Qcdef"), "bypass replace");
        check(ops.size() == 3, "bypass calls do not re-enter filter");

        doc.setDocumentFilter(null);
        check(doc.getDocumentFilter() == null, "filter unset");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
