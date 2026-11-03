/*
 * @test
 * @summary javax.swing.text.DefaultStyledDocument.AttributeUndoableEdit conformance: white-box structure + black-box behavior.
 * @run main AttributeUndoableEditTest
 */

import java.lang.reflect.Modifier;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.GapContent;
import javax.swing.text.SimpleAttributeSet;

public class AttributeUndoableEditTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // Concrete document exposing write lock + leaf factory so attribute
    // mutations on elements can be driven through a user-visible path.
    static class Doc extends AbstractDocument {
        Doc() { super(new GapContent()); }
        public Element getDefaultRootElement() { return root; }
        public Element getParagraphElement(int pos) { return root; }
        public void lock() { writeLock(); }
        public void unlock() { writeUnlock(); }
        public Element leaf(Element parent, AttributeSet a, int s, int e) { return createLeafElement(parent, a, s, e); }
        Element root = createBranchElement(null, null);
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.DefaultStyledDocument$AttributeUndoableEdit");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.undo.AbstractUndoableEdit"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Doc d = new Doc();

        // Replacing edit: redo drops old attributes, undo restores them.
        d.lock();
        AbstractDocument.AbstractElement leaf = (AbstractDocument.AbstractElement) d.leaf(d.root, null, 0, 1);
        leaf.addAttribute("old", "1");
        d.unlock();

        SimpleAttributeSet nas = new SimpleAttributeSet();
        nas.addAttribute("new", "2");
        DefaultStyledDocument.AttributeUndoableEdit replace =
            new DefaultStyledDocument.AttributeUndoableEdit(leaf, nas, true);

        d.lock();
        replace.undo();
        d.unlock();
        check("1".equals(leaf.getAttribute("old")) && leaf.getAttribute("new") == null,
              "undo restores pre-edit attributes");

        d.lock();
        replace.redo();
        d.unlock();
        check("2".equals(leaf.getAttribute("new")) && leaf.getAttribute("old") == null,
              "replacing redo replaces all attributes");

        // Merging edit: redo keeps existing attributes and adds new ones.
        SimpleAttributeSet nas2 = new SimpleAttributeSet();
        nas2.addAttribute("extra", "3");
        DefaultStyledDocument.AttributeUndoableEdit merge =
            new DefaultStyledDocument.AttributeUndoableEdit(leaf, nas2, false);

        d.lock();
        merge.undo();
        d.unlock();
        check("2".equals(leaf.getAttribute("new")) && leaf.getAttribute("extra") == null,
              "merging undo keeps base attributes");

        d.lock();
        merge.redo();
        d.unlock();
        check("2".equals(leaf.getAttribute("new")) && "3".equals(leaf.getAttribute("extra")),
              "merging redo adds attributes without dropping existing");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
