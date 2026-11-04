/*
 * @test
 * @summary javax.swing.text.PlainDocument conformance: line-map element structure, paragraph lookup, split/merge on insert/remove, filterNewlines, tabSize/lineLimit properties (headless-safe).
 * @run main PlainDocumentTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import javax.swing.text.StringContent;

public class PlainDocumentTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static void testTypeShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.PlainDocument", false, PlainDocumentTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(!c.isEnum() && !c.isAnnotation(), "not enum/annotation");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.AbstractDocument"), "superclass AbstractDocument");
        check(c.getInterfaces().length == 0, "no direct interfaces (inherits Document/Serializable from AbstractDocument)");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("insertString"), "method insertString");
        check(names.contains("getDefaultRootElement"), "method getDefaultRootElement");
        check(names.contains("getParagraphElement"), "method getParagraphElement");
        // property-name constants
        check("tabSize".equals(PlainDocument.tabSizeAttribute), "tabSizeAttribute value");
        check("lineLimit".equals(PlainDocument.lineLimitAttribute), "lineLimitAttribute value");
    }

    static void testDefaults() {
        PlainDocument d = new PlainDocument();
        check(d.getLength() == 0, "empty document length");
        check(d.getDefaultRootElement().getName().equals("paragraph"), "default root named paragraph");
        check(d.getDefaultRootElement().getStartOffset() == 0, "root start 0");
        check(d.getDefaultRootElement().getEndOffset() == 1, "empty root spans the implicit newline [0,1)");
        check(d.getDefaultRootElement().getElementCount() == 1, "empty root has one line");
        Element line = d.getDefaultRootElement().getElement(0);
        check(line.getName().equals("content"), "line element named content");
        check(line.getStartOffset() == 0 && line.getEndOffset() == 1, "empty line [0,1)");
        check(line.isLeaf(), "line is leaf");
        check(d.getProperty(PlainDocument.tabSizeAttribute).equals(Integer.valueOf(8)), "default tabSize 8");
        check(d.getProperty(PlainDocument.lineLimitAttribute) == null, "default lineLimit null");

        PlainDocument withContent = new PlainDocument(new StringContent());
        check(withContent.getLength() == 0, "StringContent-backed document works");
        check(withContent.getDefaultRootElement().getElementCount() == 1, "StringContent root has one line");
    }

    static void testLineMap() throws Exception {
        PlainDocument d = new PlainDocument();
        d.insertString(0, "ab\ncd\nef", null);
        Element root = d.getDefaultRootElement();
        check(!root.isLeaf(), "root is branch");
        check(root.getStartOffset() == 0 && root.getEndOffset() == 9, "root [0,9)");
        check(root.getElementCount() == 3, "three line elements");
        int[][] expect = { {0, 3}, {3, 6}, {6, 9} };
        for (int i = 0; i < 3; i++) {
            Element line = root.getElement(i);
            check(line.getName().equals("content"), "line " + i + " named content");
            check(line.getStartOffset() == expect[i][0] && line.getEndOffset() == expect[i][1],
                    "line " + i + " spans [" + expect[i][0] + "," + expect[i][1] + ")");
            check(line.isLeaf(), "line " + i + " is leaf");
            check(line.getParentElement() == root, "line " + i + " parent is root");
            check(line.getDocument() == d, "line " + i + " documents back");
            check(line.getElementCount() == 0, "line " + i + " has no children");
            check(line.getElementIndex(0) == -1, "line " + i + " getElementIndex -1");
            check(line.getElement(0) == null, "line " + i + " getElement null");
            check(line.getAttributes().getAttributeCount() == 0, "line " + i + " has empty attributes");
        }
        // index lookup maps positions to the containing line
        check(root.getElementIndex(0) == 0, "index(0)");
        check(root.getElementIndex(2) == 0, "index(2)");
        check(root.getElementIndex(3) == 1, "index(3) first char of second line");
        check(root.getElementIndex(5) == 1, "index(5) newline of second line");
        check(root.getElementIndex(8) == 2, "index(8)");
        check(root.getElementIndex(100) == 2, "index(100) clamps to last");
        check(root.getElementIndex(-1) == 0, "index(-1) clamps to first");
        // getParagraphElement delegates to the line map
        check(d.getParagraphElement(2) == root.getElement(0), "paragraph(2) first line");
        check(d.getParagraphElement(3) == root.getElement(1), "paragraph(3) second line");
        check(d.getParagraphElement(6) == root.getElement(2), "paragraph(6) third line");
        check(d.getParagraphElement(100) == root.getElement(2), "paragraph(100) clamps");
        check(d.getParagraphElement(-1) == root.getElement(0), "paragraph(-1) clamps");
        check(d.getParagraphElement(0).getName().equals("content"), "paragraph element is a line");

        // root attributes are empty and root has no parent
        check(root.getParentElement() == null, "root has no parent");
        check(root.getAttributes() != null, "root attributes non-null");
        check(root.getDocument() == d, "root documents back");
    }

    static void testSplitAndMerge() throws Exception {
        PlainDocument d = new PlainDocument();
        d.insertString(0, "ab\ncd\nef", null);
        // inserting a newline in the middle of line 0 splits it
        d.insertString(1, "X\n", null); // "aX\nb\ncd\nef"
        Element root = d.getDefaultRootElement();
        check(root.getElementCount() == 4, "split produces four lines");
        check(root.getElement(0).getStartOffset() == 0 && root.getElement(0).getEndOffset() == 3, "split line 0 [0,3)");
        check(root.getElement(1).getStartOffset() == 3 && root.getElement(1).getEndOffset() == 5, "split line 1 [3,5)");
        // removing the inserted text merges the lines back
        d.remove(1, 2);
        root = d.getDefaultRootElement();
        check(root.getElementCount() == 3, "merge back to three lines");
        check(root.getElement(0).getStartOffset() == 0 && root.getElement(0).getEndOffset() == 3, "merged line 0 restored");
        // removing just a newline joins two lines into one element
        d.remove(2, 1); // "abcd\nef"
        root = d.getDefaultRootElement();
        check(root.getElementCount() == 2, "newline removal joins lines");
        check(root.getElement(0).getStartOffset() == 0 && root.getElement(0).getEndOffset() == 5, "joined line [0,5)");
        check(root.getElement(1).getStartOffset() == 5 && root.getElement(1).getEndOffset() == 8, "tail line [5,8)");
        // backspace-style removal from the start of a line also merges
        PlainDocument e = new PlainDocument();
        e.insertString(0, "one\ntwo", null);
        e.remove(0, 4); // "two"
        check(e.getDefaultRootElement().getElementCount() == 1, "front removal collapses to one line");
        check(e.getText(0, e.getLength()).equals("two"), "content after front removal");
    }

    static void testFilterNewlines() throws Exception {
        PlainDocument d = new PlainDocument();
        d.putProperty("filterNewlines", Boolean.TRUE);
        d.insertString(0, "a\nb", null);
        check(d.getText(0, d.getLength()).equals("a b"), "filterNewlines rewrites newlines to spaces");
        check(d.getDefaultRootElement().getElementCount() == 1, "rewritten text stays one line");
        d.putProperty("filterNewlines", Boolean.FALSE);
        d.insertString(0, "z\nz", null);
        check(d.getDefaultRootElement().getElementCount() == 2, "false value disables filtering");
        // a fresh document without the property is unfiltered
        PlainDocument e = new PlainDocument();
        e.insertString(0, "a\nb", null);
        check(e.getDefaultRootElement().getElementCount() == 2, "no property means unfiltered");
    }

    static void testProperties() throws Exception {
        PlainDocument d = new PlainDocument();
        d.putProperty(PlainDocument.tabSizeAttribute, Integer.valueOf(4));
        check(d.getProperty(PlainDocument.tabSizeAttribute).equals(Integer.valueOf(4)), "tabSize settable");
        d.putProperty(PlainDocument.lineLimitAttribute, Integer.valueOf(3));
        check(d.getProperty(PlainDocument.lineLimitAttribute).equals(Integer.valueOf(3)), "lineLimit settable");
        // the model itself does not enforce lineLimit: 10 chars go in whole
        d.insertString(0, "0123456789", null);
        check(d.getLength() == 10, "lineLimit not enforced at model level");
        check(d.getDefaultRootElement().getElementCount() == 1, "single line stored");
        // attributes on insert are not stored on plain line elements
        javax.swing.text.SimpleAttributeSet attrs = new javax.swing.text.SimpleAttributeSet();
        javax.swing.text.StyleConstants.setBold(attrs, true);
        d.insertString(0, "x", attrs);
        check(d.getDefaultRootElement().getElement(0).getAttributes().getAttributeCount() == 0,
                "plain document ignores insert attributes on line elements");
    }

    public static void main(String[] args) throws Exception {
        testTypeShape();
        testDefaults();
        testLineMap();
        testSplitAndMerge();
        testFilterNewlines();
        testProperties();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
