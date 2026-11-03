/*
 * @test
 * @summary javax.swing.event.DocumentEvent.ElementChange conformance: white-box structure + black-box behavior.
 * @run main ElementChangeTest
 */

import java.lang.reflect.Modifier;
import javax.swing.text.Element;

public class ElementChangeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.DocumentEvent$ElementChange");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() {
        Element elem = new javax.swing.text.PlainDocument().getDefaultRootElement();
        Element[] removed = { elem };
        Element[] added = { elem, elem };

        javax.swing.event.DocumentEvent.ElementChange change = new javax.swing.event.DocumentEvent.ElementChange() {
            public Element getElement() { return elem; }
            public int getIndex() { return 2; }
            public Element[] getChildrenRemoved() { return removed; }
            public Element[] getChildrenAdded() { return added; }
        };

        check(change.getElement() == elem, "getElement round-trip");
        check(change.getIndex() == 2, "getIndex round-trip");
        check(change.getChildrenRemoved() == removed, "getChildrenRemoved round-trip");
        check(change.getChildrenAdded() == added, "getChildrenAdded round-trip");
        check(change.getChildrenRemoved().length == 1, "removed array length");
        check(change.getChildrenAdded().length == 2, "added array length");
        check(change.getChildrenRemoved()[0] == elem, "removed element identity");
        check(change.getChildrenAdded()[1] == elem, "added element identity");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
