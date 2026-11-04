/*
 * @test
 * @summary javax.swing.text.AbstractDocument.LeafElement conformance: span/childless semantics, names/defaults, attribute mutation guards (headless-safe).
 * @run main LeafElementTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import javax.swing.text.SimpleAttributeSet;

public class LeafElementTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    /** Constructs LeafElements under the write lock like the document internals do. */
    static class OpenDoc extends PlainDocument {
        Element makeLeaf(Element parent, AttributeSet a, int o0, int o1) {
            writeLock();
            try { return ((AbstractDocument) this).new LeafElement(parent, a, o0, o1); }
            finally { writeUnlock(); }
        }
        void locked(Runnable r) { writeLock(); try { r.run(); } finally { writeUnlock(); } }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.AbstractDocument$LeafElement", false, LeafElementTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.AbstractDocument$AbstractElement"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("toString"), "method toString");
        check(names.contains("getStartOffset"), "method getStartOffset");
        check(names.contains("getEndOffset"), "method getEndOffset");
        check(names.contains("getName"), "method getName");
        check(names.contains("getElementIndex"), "method getElementIndex");
        check(names.contains("getElement"), "method getElement");
        check(names.contains("getElementCount"), "method getElementCount");

        testBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testBehavior() throws Exception {
        OpenDoc d = new OpenDoc();
        d.insertString(0, "hello world", null);
        Element root = d.getDefaultRootElement();
        Element docLeaf = root.getElement(0);
        check(docLeaf instanceof AbstractDocument.LeafElement, "plain line is a LeafElement");
        check(docLeaf.getName().equals("content"), "default leaf name is content");

        // a standalone leaf spans exactly the given offsets
        final Element leaf = d.makeLeaf(root, SimpleAttributeSet.EMPTY, 2, 5);
        check(leaf.getStartOffset() == 2 && leaf.getEndOffset() == 5, "leaf spans [2,5)");
        check(leaf.isLeaf(), "leaf reports isLeaf");
        check(leaf.getElementCount() == 0, "leaf has no children");
        check(leaf.getElement(0) == null, "leaf getElement null");
        check(leaf.getElementIndex(0) == -1, "leaf getElementIndex -1");
        check(leaf.getParentElement() == root, "parent recorded");
        check(leaf.getDocument() == d, "document recorded");
        check(!((AbstractDocument.AbstractElement) leaf).getAllowsChildren(), "leaf does not allow children");
        check(((AbstractDocument.AbstractElement) leaf).children() == null, "leaf children() is null");
        check(leaf.toString().equals("LeafElement(content) 2,5\n"), "toString format, got [" + leaf.toString() + "]");

        // attributes are mutable only under the write lock
        try {
            ((javax.swing.text.MutableAttributeSet) leaf.getAttributes()).addAttribute("k", "v");
            check(false, "unlocked attribute mutation should fail");
        } catch (Throwable t) {
            check(t.getClass().getName().equals("javax.swing.text.StateInvariantError"),
                    "unlocked mutation throws StateInvariantError, got " + t);
        }
        d.locked(() -> ((javax.swing.text.MutableAttributeSet) leaf.getAttributes()).addAttribute("k", "v"));
        check(leaf.getAttributes().getAttributeCount() == 1, "locked mutation lands");
        check("v".equals(leaf.getAttributes().getAttribute("k")), "attribute readable");

        // the name attribute overrides the default leaf name
        SimpleAttributeSet named = new SimpleAttributeSet();
        named.addAttribute(AbstractDocument.ElementNameAttribute, "custom");
        Element custom = d.makeLeaf(root, named, 0, 2);
        check(custom.getName().equals("custom"), "ElementNameAttribute overrides the name");

        // a null attribute set defaults to an empty one
        Element noAttrs = d.makeLeaf(root, null, 0, 1);
        check(noAttrs.getAttributes() != null && noAttrs.getAttributes().getAttributeCount() == 0, "null attrs tolerated");
    }
}
