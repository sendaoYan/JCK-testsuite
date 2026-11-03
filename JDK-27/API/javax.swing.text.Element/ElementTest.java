/*
 * @test
 * @summary javax.swing.text.Element conformance: white-box structure + black-box behavior.
 * @run main ElementTest
 */

import java.lang.reflect.Modifier;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;

public class ElementTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.Element");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
        check(c.getDeclaredMethods().length == 10, "ten abstract methods");
    }

    static void testBlackBox() throws Exception {
        PlainDocument doc = new PlainDocument();
        doc.insertString(0, "hello world", null);

        Element root = doc.getDefaultRootElement();
        check(root != null, "root non-null");
        check(root.getDocument() == doc, "root getDocument identity");
        check(root.getParentElement() == null, "root has no parent");
        check(!root.isLeaf(), "root is not a leaf");
        check("paragraph".equals(root.getName()), "root element named paragraph");
        check(root.getAttributes() != null, "root attributes non-null");
        check(root.getAttributes().getAttributeCount() == 0, "root attributes empty");
        check(root.getElementCount() == 1, "one line element initially");

        Element line = root.getElement(0);
        check(line != null, "line element non-null");
        check(line.getParentElement() == root, "line parent is root");
        check(line.isLeaf(), "line is a leaf");
        check("content".equals(line.getName()), "line element named content");
        check(line.getStartOffset() == 0, "line start 0");
        check(line.getEndOffset() == 12, "line end 12 (trailing newline)");
        check(root.getElementIndex(0) == 0, "offset 0 maps to line 0");
        check(root.getElementIndex(10) == 0, "offset 10 maps to line 0");

        // Insert a newline: structure grows to two line elements.
        doc.insertString(11, "\nsecond", null);
        check(root.getElementCount() == 2, "two line elements after newline");
        Element line1 = root.getElement(1);
        check(line1.getStartOffset() == 12, "second line start 12");
        check(line1.getEndOffset() == 19, "second line end 19");
        check(root.getElementIndex(12) == 1, "offset 12 maps to line 1");
        check(line1.getParentElement() == root, "second line parent is root");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
