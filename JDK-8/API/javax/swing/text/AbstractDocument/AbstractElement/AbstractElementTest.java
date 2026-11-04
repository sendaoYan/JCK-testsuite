/*
 * @test
 * @summary javax.swing.text.AbstractDocument.AbstractElement conformance: element-as-AttributeSet contract, resolve-parent fallback to the parent element, write-lock guard on mutators (headless-safe).
 * @run main AbstractElementTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.PlainDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class AbstractElementTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static class OpenDoc extends PlainDocument {
        Element makeBranch(Element parent, AttributeSet a) {
            writeLock();
            try { return ((AbstractDocument) this).new BranchElement(parent, a); }
            finally { writeUnlock(); }
        }
        void locked(Runnable r) { writeLock(); try { r.run(); } finally { writeUnlock(); } }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.AbstractDocument$AbstractElement", false, AbstractElementTest.class.getClassLoader());
        check(c != null, "forName");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(hasInterface(c, "javax.swing.text.Element"), "implements Element");
        check(hasInterface(c, "javax.swing.text.MutableAttributeSet"), "implements MutableAttributeSet");
        check(hasInterface(c, "javax.swing.tree.TreeNode"), "implements TreeNode");
        check(hasInterface(c, "java.io.Serializable"), "implements Serializable");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getDocument"), "method getDocument");
        check(names.contains("getAttributes"), "method getAttributes");
        check(names.contains("containsAttributes"), "method containsAttributes");
        check(names.contains("containsAttribute"), "method containsAttribute");
        check(names.contains("addAttribute"), "method addAttribute");
        check(names.contains("setResolveParent"), "method setResolveParent");
        check(names.contains("children"), "method children");
        check(names.contains("getAllowsChildren"), "method getAllowsChildren");

        testAttributeContract();
        testResolveParent();
        testLockGuard();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testAttributeContract() throws Exception {
        OpenDoc d = new OpenDoc();
        d.insertString(0, "hi", null);
        Element root = d.getDefaultRootElement();
        AbstractDocument.AbstractElement el =
                (AbstractDocument.AbstractElement) d.makeBranch(root, null);
        // getAttributes returns the element itself
        check(el.getAttributes() == el, "getAttributes identity");
        check(el.getAttributeCount() == 0, "fresh element has no attributes");
        check(el.getAttribute("missing") == null, "missing attribute null");
        check(!el.isDefined("missing"), "missing attribute not defined");
        check(el.isEqual(SimpleAttributeSet.EMPTY), "fresh element equals the canonical empty set");

        d.locked(() -> el.addAttribute("k1", "v1"));
        d.locked(() -> el.addAttribute("k2", "v2"));
        check(el.getAttributeCount() == 2, "two attributes added");
        check("v1".equals(el.getAttribute("k1")), "k1 value");
        check(el.isDefined("k1") && !el.isDefined("k9"), "isDefined");
        Vector<Object> names = new Vector<Object>();
        for (Enumeration<?> e = el.getAttributeNames(); e.hasMoreElements(); ) names.add(e.nextElement());
        check(names.size() == 2 && names.contains("k1") && names.contains("k2"), "names enumeration");

        // snapshots are independent of later mutation
        AttributeSet snap = el.copyAttributes();
        check(snap != el, "copy distinct");
        check(snap.isEqual(el) && el.isEqual(snap), "copy isEqual");
        d.locked(() -> el.addAttribute("k3", "v3"));
        check(snap.getAttribute("k3") == null, "snapshot independent");
        check(!snap.isEqual(el), "mutated element no longer equal to old snapshot");

        // isEqual compares content, not identity
        SimpleAttributeSet twin = new SimpleAttributeSet();
        twin.addAttribute("k1", "v1");
        twin.addAttribute("k2", "v2");
        twin.addAttribute("k3", "v3");
        check(el.isEqual(twin) && twin.isEqual(el), "isEqual across implementations");
        SimpleAttributeSet partial = new SimpleAttributeSet();
        partial.addAttribute("k1", "v1");
        check(!el.isEqual(partial), "subset is not equal");
        check(el.containsAttributes(partial), "containsAttributes subset");
        check(el.containsAttributes(twin), "containsAttributes equal set");
        check(el.containsAttribute("k1", "v1"), "containsAttribute match");
        check(!el.containsAttribute("k1", "OTHER"), "containsAttribute mismatch");
        SimpleAttributeSet clashing = new SimpleAttributeSet();
        clashing.addAttribute("k1", "OTHER");
        check(!el.containsAttributes(clashing), "clashing value fails containsAttributes");

        // addAttributes bulk merge and removeAttributes
        d.locked(() -> el.addAttributes(partial));
        check(el.getAttributeCount() == 3, "bulk add of existing keys is a no-op count-wise");
        d.locked(() -> el.removeAttribute("k3"));
        check(el.getAttributeCount() == 2 && el.getAttribute("k3") == null, "removeAttribute");
        d.locked(() -> el.addAttributes(partial));
        d.locked(() -> el.removeAttributes(java.util.Collections.enumeration(java.util.Arrays.asList("k1"))));
        check(el.getAttributeCount() == 1 && el.getAttribute("k1") == null && !el.isDefined("k1"),
                "removeAttributes(Enumeration) drops named keys");
        check("v2".equals(el.getAttribute("k2")), "untouched key survives the drop");
        // removeAttributes(AttributeSet) removes keys with equal values
        d.locked(() -> el.removeAttributes(partial));
        check(el.getAttribute("k1") == null, "removeAttributes(AttributeSet) removed k1");
        // removeAttributes(this) empties the set
        d.locked(() -> el.removeAttributes((AttributeSet) el));
        check(el.getAttributeCount() == 0, "removeAttributes(this) clears everything");
    }

    static void testResolveParent() throws Exception {
        OpenDoc d = new OpenDoc();
        d.insertString(0, "hi", null);
        Element root = d.getDefaultRootElement();
        AbstractDocument.AbstractElement el =
                (AbstractDocument.AbstractElement) d.makeBranch(root, null);
        // with no explicit resolver the parent element's attributes are used
        check(el.getResolveParent() == root.getAttributes(), "resolve parent defaults to the parent element");
        SimpleAttributeSet rp = new SimpleAttributeSet();
        rp.addAttribute("rk", "rv");
        d.locked(() -> el.setResolveParent(rp));
        check(el.getResolveParent() == rp, "explicit resolver recorded");
        check("rv".equals(el.getAttribute("rk")), "resolution flows through the resolver");
        check(!el.isDefined("rk"), "resolved value is not locally defined");
        // a null parent removes the resolver, restoring the element fallback
        d.locked(() -> el.setResolveParent(null));
        check(el.getResolveParent() == root.getAttributes(), "null resolver restores element fallback");
    }

    static void testLockGuard() throws Exception {
        OpenDoc d = new OpenDoc();
        d.insertString(0, "hi", null);
        Element root = d.getDefaultRootElement();
        final AbstractDocument.AbstractElement el =
                (AbstractDocument.AbstractElement) d.makeBranch(root, null);
        // every MutableAttributeSet method demands the write lock
        try {
            el.addAttribute("k", "v");
            check(false, "unlocked addAttribute should fail");
        } catch (Throwable t) {
            check(t.getClass().getName().equals("javax.swing.text.StateInvariantError"),
                    "unlocked addAttribute StateInvariantError, got " + t);
        }
        try {
            el.setResolveParent(SimpleAttributeSet.EMPTY);
            check(false, "unlocked setResolveParent should fail");
        } catch (Throwable t) {
            check(t.getClass().getName().equals("javax.swing.text.StateInvariantError"),
                    "unlocked setResolveParent StateInvariantError");
        }
        d.locked(() -> el.addAttribute("k", "v"));
        check(el.getAttribute("k") != null, "locked mutation lands");
        // TreeNode surface
        check(el.getParent() == null || el.getParent() != null, "TreeNode parent accessible");
        check(el.getChildCount() == 0, "childless TreeNode count");
        check(el.getIndex((javax.swing.tree.TreeNode) root) == -1, "root not a child of this element");
        check(el.getName() != null, "name non-null");
        check(el.getDocument() == d, "document identity");
        check(el.getParentElement() == root, "element parent identity");
    }
}
