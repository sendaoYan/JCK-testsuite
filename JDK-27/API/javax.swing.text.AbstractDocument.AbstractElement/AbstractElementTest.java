/*
 * @test
 * @summary javax.swing.text.AbstractDocument.AbstractElement conformance: white-box structure + black-box behavior.
 * @run main AbstractElementTest
 */

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.GapContent;
import javax.swing.text.PlainDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.tree.TreeNode;

public class AbstractElementTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // A concrete AbstractDocument that exposes the write lock and element
    // factories so a user can exercise MutableAttributeSet methods on elements.
    static class Doc extends AbstractDocument {
        Doc() { super(new GapContent()); }
        public Element getDefaultRootElement() { return root; }
        public Element getParagraphElement(int pos) { return root; }
        public void lock() { writeLock(); }
        public void unlock() { writeUnlock(); }
        public Element leaf(Element parent, AttributeSet a, int s, int e) { return createLeafElement(parent, a, s, e); }
        public Element branch(Element parent, AttributeSet a) { return createBranchElement(parent, a); }
        Element root = createBranchElement(null, null);
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.AbstractDocument$AbstractElement");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getInterfaces().length == 4, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.text.Element"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.swing.text.MutableAttributeSet"), "interface 1");
        check(c.getInterfaces()[2].getName().equals("java.io.Serializable"), "interface 2");
        check(c.getInterfaces()[3].getName().equals("javax.swing.tree.TreeNode"), "interface 3");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Reach an AbstractElement through a real document element tree.
        PlainDocument pd = new PlainDocument();
        pd.insertString(0, "ab\ncd", null);
        Element root = pd.getDefaultRootElement();
        Element leaf0 = root.getElement(0);

        check(leaf0.getDocument() == pd, "getDocument returns source document");
        check(root.getParentElement() == null, "root parent is null");
        check(leaf0.getParentElement() == root, "leaf parent is root");
        check(leaf0.getAttributes() != null, "getAttributes non-null");
        check("content".equals(leaf0.getName()), "leaf name content");
        check("paragraph".equals(root.getName()), "branch name paragraph");

        check(leaf0.getStartOffset() == 0 && leaf0.getEndOffset() == 3, "leaf offset range");
        check(leaf0.isLeaf(), "leaf isLeaf");
        check(leaf0.getElementCount() == 0, "leaf child count 0");
        check(root.getElementCount() == 2, "root child count 2");
        check(root.getElementIndex(0) == 0, "root element index 0");
        check(root.getElementIndex(3) == 1, "root element index 3");

        // TreeNode view
        TreeNode rootTn = (TreeNode) root;
        TreeNode leafTn = (TreeNode) leaf0;
        check(rootTn.getChildCount() == 2, "TreeNode child count");
        check(rootTn.getChildAt(0) == leaf0, "TreeNode childAt 0");
        check(leafTn.getParent() == root, "TreeNode parent");
        check(rootTn.getIndex(leafTn) == 0, "TreeNode index");
        check(rootTn.getAllowsChildren(), "branch allows children");
        check(!leafTn.getAllowsChildren(), "leaf disallows children");
        Enumeration<?> en = rootTn.children();
        int cnt = 0;
        while (en.hasMoreElements()) { en.nextElement(); cnt++; }
        check(cnt == 2, "children enumeration count 2");

        // MutableAttributeSet methods need the write lock.
        Doc d = new Doc();
        d.lock();
        try {
            Element e = d.leaf(d.root, null, 0, 1);
            AbstractDocument.AbstractElement ae = (AbstractDocument.AbstractElement) e;
            check(ae.getAttributeCount() == 0, "initial attribute count 0");
            check(!ae.isDefined("k"), "isDefined false before add");

            ae.addAttribute("k", "v");
            check(ae.getAttributeCount() == 1, "attribute count 1 after add");
            check(ae.isDefined("k"), "isDefined true after add");
            check("v".equals(ae.getAttribute("k")), "getAttribute returns value");
            check(ae.containsAttribute("k", "v"), "containsAttribute name/value");

            SimpleAttributeSet sas = new SimpleAttributeSet();
            sas.addAttribute("k", "v");
            check(ae.containsAttributes(sas), "containsAttributes set");
            check(ae.isEqual(ae.copyAttributes()), "copyAttributes isEqual");

            ae.removeAttribute("k");
            check(ae.getAttribute("k") == null, "removeAttribute clears value");

            ae.addAttributes(sas);
            check("v".equals(ae.getAttribute("k")), "addAttributes merges set");
            ae.removeAttributes(sas);
            check(ae.getAttribute("k") == null, "removeAttributes(AttributeSet)");

            ae.addAttribute("a", "1");
            ae.addAttribute("b", "2");
            Enumeration<?> names = new Vector<Object>(Arrays.asList("a", "b")).elements();
            ae.removeAttributes(names);
            check(ae.getAttributeCount() == 0, "removeAttributes(Enumeration) clears");

            SimpleAttributeSet rp = new SimpleAttributeSet();
            rp.addAttribute("rp", "yes");
            ae.setResolveParent(rp);
            check(ae.getResolveParent() == rp, "set/get resolve parent");
            ae.setResolveParent(null);
            // getResolveParent falls back to the parent element's attributes
            // when no explicit resolve parent is set.
            check(ae.getResolveParent() == ae.getParentElement(),
                  "resolve parent falls back to parent element attributes");

            // An element with no parent element returns null once reset.
            Element orphan = d.leaf(null, null, 0, 1);
            AbstractDocument.AbstractElement oae = (AbstractDocument.AbstractElement) orphan;
            oae.setResolveParent(null);
            check(oae.getResolveParent() == null, "resolve parent reset to null for parentless element");

            ae.addAttribute("z", "1");
            Enumeration<?> an = ae.getAttributeNames();
            check(an.hasMoreElements(), "getAttributeNames non-empty after add");
        } finally {
            d.unlock();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
