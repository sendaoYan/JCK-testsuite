/*
 * @test
 * @summary javax.swing.text.ElementIterator conformance: white-box structure + black-box behavior.
 * @run main ElementIteratorTest
 */

import java.lang.reflect.Modifier;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.ElementIterator;
import javax.swing.text.PlainDocument;

public class ElementIteratorTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.ElementIterator");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.lang.Cloneable"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        PlainDocument doc = new PlainDocument();
        doc.insertString(0, "hello\nworld", null);
        Element root = doc.getDefaultRootElement();
        check(root.getElementCount() == 2, "root has 2 leaf children");
        check(!root.isLeaf(), "root is branch");

        // Document constructor iterates from the default root element.
        ElementIterator it = new ElementIterator(doc);
        check(it.depth() == 0, "depth 0 before first");
        Element first = it.first();
        check(first == root, "first() == default root");
        check(it.current() == root, "current() after first() == root");
        check(it.depth() == 1, "depth 1 after first");

        Element leaf0 = it.next();
        check(leaf0 != null && leaf0.isLeaf(), "next leaf0 is leaf");
        check(leaf0.getStartOffset() == 0, "leaf0 start offset");
        check(leaf0.getEndOffset() == 6, "leaf0 end offset");
        check("content".equals(leaf0.getName()), "leaf0 name content");

        Element leaf1 = it.next();
        check(leaf1 != null && leaf1.isLeaf(), "next leaf1 is leaf");
        check(leaf1.getStartOffset() == 6, "leaf1 start offset");
        check(leaf1.getEndOffset() == 12, "leaf1 end offset");

        check(it.next() == null, "next after end == null");
        check(it.previous() == null, "previous at exhausted stack == null");
        check(it.current() == null, "current at exhausted stack == null");

        // previous() from the first child returns the root.
        ElementIterator it2 = new ElementIterator(root);
        it2.first();
        it2.next(); // now positioned on leaf0
        check(it2.previous() == root, "previous from first child == root");

        // next() called before first()/current() returns the root.
        ElementIterator it3 = new ElementIterator(root);
        check(it3.next() == root, "next before first == root");

        // Element constructor is equivalent for the same root.
        ElementIterator it4 = new ElementIterator(root);
        check(it4.first() == root, "Element ctor first == root");

        // clone() yields an independent iterator over the same tree.
        ElementIterator clone = (ElementIterator) it4.clone();
        check(clone != root && clone != it4, "clone is a distinct object");
        check(clone.first() == root, "clone first == root");
        Element cLeaf = clone.next();
        check(cLeaf != null && cLeaf.isLeaf() && cLeaf.getStartOffset() == 0, "clone iterates leaf0");
        check(clone.depth() == 1, "clone depth independent");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
