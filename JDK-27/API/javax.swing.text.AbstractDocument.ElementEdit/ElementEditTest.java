/*
 * @test
 * @summary javax.swing.text.AbstractDocument.ElementEdit conformance: white-box structure + black-box behavior.
 * @run main ElementEditTest
 */

import java.lang.reflect.Modifier;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;

public class ElementEditTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.AbstractDocument$ElementEdit");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.undo.AbstractUndoableEdit"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.event.DocumentEvent$ElementChange"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        PlainDocument pd = new PlainDocument();
        AbstractDocument.BranchElement be = pd.new BranchElement(null, null);
        Element A = pd.new LeafElement(be, null, 0, 1);
        Element B = pd.new LeafElement(be, null, 1, 2);
        Element C = pd.new LeafElement(be, null, 2, 3);
        be.replace(0, 0, new Element[]{A, B});

        AbstractDocument.ElementEdit ee =
            new AbstractDocument.ElementEdit(be, 0, new Element[]{A}, new Element[]{C});

        check(ee.getElement() == be, "getElement returns branch");
        check(ee.getIndex() == 0, "getIndex");
        check(ee.getChildrenRemoved().length == 1 && ee.getChildrenRemoved()[0] == A, "getChildrenRemoved");
        check(ee.getChildrenAdded().length == 1 && ee.getChildrenAdded()[0] == C, "getChildrenAdded");

        // Apply the forward state (A replaced by C), then undo/redo.
        be.replace(0, 1, new Element[]{C});
        check(be.getElement(0) == C, "forward state applied");
        ee.undo();
        check(be.getElementCount() == 2, "undo restores element count");
        check(be.getElement(0) == A && be.getElement(1) == B, "undo restores removed children");
        ee.redo();
        check(be.getElementCount() == 2, "redo keeps element count");
        check(be.getElement(0) == C && be.getElement(1) == B, "redo reapplies added children");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
