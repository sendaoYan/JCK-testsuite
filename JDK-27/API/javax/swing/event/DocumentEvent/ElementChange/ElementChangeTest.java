/*
 * @test
 * @summary javax.swing.event.DocumentEvent.ElementChange conformance: white-box structure + black-box behavior.
 * @run main ElementChangeTest
 */

import java.lang.reflect.Modifier;
import javax.swing.text.Element;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

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


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // Legacy shallow assertions preserved verbatim from the original JDK-8 test.
    static void testLegacyShallow() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.DocumentEvent$ElementChange", false, ElementChangeTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getElement"), "method getElement");
        check(names.contains("getIndex"), "method getIndex");
        check(names.contains("getChildrenRemoved"), "method getChildrenRemoved");
        check(names.contains("getChildrenAdded"), "method getChildrenAdded");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLegacyShallow();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
