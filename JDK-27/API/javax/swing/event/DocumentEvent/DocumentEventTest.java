/*
 * @test
 * @summary javax.swing.event.DocumentEvent conformance: white-box structure + black-box behavior.
 * @run main DocumentEventTest
 */

import java.lang.reflect.Modifier;
import javax.swing.text.Document;
import javax.swing.text.Element;

public class DocumentEventTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.DocumentEvent");
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
        Document doc = new javax.swing.text.PlainDocument();
        javax.swing.event.DocumentEvent.EventType type = javax.swing.event.DocumentEvent.EventType.INSERT;

        javax.swing.event.DocumentEvent.ElementChange change = new javax.swing.event.DocumentEvent.ElementChange() {
            public Element getElement() { return doc.getDefaultRootElement(); }
            public int getIndex() { return 5; }
            public Element[] getChildrenRemoved() { return new Element[0]; }
            public Element[] getChildrenAdded() { return new Element[0]; }
        };

        javax.swing.event.DocumentEvent ev = new javax.swing.event.DocumentEvent() {
            public int getOffset() { return 12; }
            public int getLength() { return 4; }
            public Document getDocument() { return doc; }
            public javax.swing.event.DocumentEvent.EventType getType() { return type; }
            public javax.swing.event.DocumentEvent.ElementChange getChange(Element elem) {
                return (elem == doc.getDefaultRootElement()) ? change : null;
            }
        };

        check(ev.getOffset() == 12, "getOffset round-trip");
        check(ev.getLength() == 4, "getLength round-trip");
        check(ev.getDocument() == doc, "getDocument round-trip");
        check(ev.getType() == type, "getType round-trip");
        check(ev.getType() == javax.swing.event.DocumentEvent.EventType.INSERT, "type is INSERT");
        check(ev.getChange(doc.getDefaultRootElement()) == change, "getChange round-trip for matching element");
        check(ev.getChange(null) == null, "getChange returns null for non-matching element");
        check(ev.getChange(doc.getDefaultRootElement()).getIndex() == 5, "nested ElementChange getIndex");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
