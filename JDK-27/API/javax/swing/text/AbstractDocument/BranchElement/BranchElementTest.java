/*
 * @test
 * @summary javax.swing.text.AbstractDocument.BranchElement conformance: child management under write lock, positionToElement, names/defaults, mutation guards (headless-safe).
 * @run main BranchElementTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import javax.swing.text.SimpleAttributeSet;

public class BranchElementTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    /** Exposes the protected constructors by holding the write lock during construction. */
    static class OpenDoc extends PlainDocument {
        Element makeBranch(Element parent, AttributeSet a) {
            writeLock();
            try { return ((AbstractDocument) this).new BranchElement(parent, a); }
            finally { writeUnlock(); }
        }
        Element makeLeaf(Element parent, AttributeSet a, int o0, int o1) {
            writeLock();
            try { return ((AbstractDocument) this).new LeafElement(parent, a, o0, o1); }
            finally { writeUnlock(); }
        }
        void locked(Runnable r) { writeLock(); try { r.run(); } finally { writeUnlock(); } }
        void branchReplace(Element branch, int i, int n, Element[] elems) {
            writeLock();
            try { ((AbstractDocument.BranchElement) branch).replace(i, n, elems); }
            finally { writeUnlock(); }
        }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.AbstractDocument$BranchElement", false, BranchElementTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.AbstractDocument$AbstractElement"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("positionToElement"), "method positionToElement");
        check(names.contains("replace"), "method replace");
        check(names.contains("toString"), "method toString");
        check(names.contains("getName"), "method getName");
        check(names.contains("getStartOffset"), "method getStartOffset");
        check(names.contains("getEndOffset"), "method getEndOffset");
        check(names.contains("getElement"), "method getElement");
        check(names.contains("children"), "method children");
        check(names.contains("getAllowsChildren"), "method getAllowsChildren");

        testBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testBehavior() throws Exception {
        OpenDoc d = new OpenDoc();
        d.insertString(0, "ab\ncd\nef", null);
        Element root = d.getDefaultRootElement();
        check(root instanceof AbstractDocument.BranchElement, "plain root is a BranchElement");
        check(root.getName().equals("paragraph"), "default branch name is paragraph");

        // a fresh branch with no children has degenerate bounds
        final AbstractDocument.AbstractElement childless = (AbstractDocument.AbstractElement) d.makeBranch(root, null);
        check(childless.getElementCount() == 0, "new branch has no children");
        check(!childless.isLeaf(), "branch is never a leaf");
        check(((AbstractDocument.AbstractElement) childless).getAllowsChildren(), "branch allows children");
        check(childless.getParentElement() == root, "parent recorded");
        check(childless.getDocument() == d, "document recorded");
        try {
            childless.getStartOffset();
            check(false, "childless branch getStartOffset should throw NPE");
        } catch (NullPointerException e) {
            check(true, "childless branch getStartOffset NPE");
        }

        // attach two leaf children under the write lock
        final Element branch = d.makeBranch(root, null);
        final Element line1 = d.makeLeaf(branch, SimpleAttributeSet.EMPTY, 0, 3);
        final Element line2 = d.makeLeaf(branch, SimpleAttributeSet.EMPTY, 3, 6);
        d.branchReplace(branch, 0, 0, new Element[] { line1, line2 });
        check(branch.getElementCount() == 2, "two children attached");
        check(branch.getElement(0) == line1 && branch.getElement(1) == line2, "children retrievable");
        check(branch.getStartOffset() == 0 && branch.getEndOffset() == 6, "branch spans children [0,6)");
        check(branch.getElementIndex(0) == 0 && branch.getElementIndex(2) == 0, "index in first child");
        check(branch.getElementIndex(3) == 1 && branch.getElementIndex(5) == 1, "index in second child");
        check(branch.getElementIndex(100) == 1, "index clamps to last child");
        check(((AbstractDocument.BranchElement) branch).positionToElement(1) == line1, "positionToElement first");
        check(((AbstractDocument.BranchElement) branch).positionToElement(3) == line2, "positionToElement boundary goes to next");
        // positions at/after the branch end, or negative, return null
        check(((AbstractDocument.BranchElement) branch).positionToElement(5) == line2, "positionToElement last position inside");
        check(((AbstractDocument.BranchElement) branch).positionToElement(6) == null, "positionToElement at end is null");
        check(((AbstractDocument.BranchElement) branch).positionToElement(100) == null, "positionToElement past end null");
        check(((AbstractDocument.BranchElement) branch).positionToElement(-1) == null, "positionToElement negative null");
        // the plain document root maps line positions to line elements
        check(((AbstractDocument.BranchElement) root).positionToElement(4).getStartOffset() == 3, "root positionToElement middle line");
        check(branch.toString().equals("BranchElement(paragraph) 0,6\n"), "toString format, got [" + branch.toString() + "]");

        // children() enumerates the attached children
        Enumeration<?> kids = ((AbstractDocument.AbstractElement) branch).children();
        check(kids.hasMoreElements() && kids.nextElement() == line1, "children first");
        check(kids.hasMoreElements() && kids.nextElement() == line2, "children second");
        check(!kids.hasMoreElements(), "children exhausted");

        // element attributes are mutable, but only under the write lock
        try {
            ((javax.swing.text.MutableAttributeSet) branch.getAttributes()).addAttribute("k", "v");
            check(false, "unlocked attribute mutation should fail");
        } catch (Throwable t) {
            check(t.getClass().getName().equals("javax.swing.text.StateInvariantError"),
                    "unlocked mutation throws StateInvariantError, got " + t);
        }
        d.locked(() -> ((javax.swing.text.MutableAttributeSet) branch.getAttributes()).addAttribute("k", "v"));
        check(branch.getAttributes().getAttributeCount() == 1, "locked mutation lands");
        check("v".equals(branch.getAttributes().getAttribute("k")), "attribute readable");

        // the name attribute overrides the default branch name
        SimpleAttributeSet named = new SimpleAttributeSet();
        named.addAttribute(AbstractDocument.ElementNameAttribute, "custom");
        Element custom = d.makeBranch(root, named);
        check(custom.getName().equals("custom"), "ElementNameAttribute overrides the name");
    }
}
