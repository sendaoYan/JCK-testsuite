/*
 * @test
 * @summary javax.swing.event.DocumentListener conformance: white-box structure + black-box behavior.
 * @run main DocumentListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.PlainDocument;
import javax.swing.text.SimpleAttributeSet;
import java.util.HashSet;
import java.util.Set;

public class DocumentListenerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void checkMethod(Class<?> c, String name, Class<?>... params) {
        try {
            Method m = c.getMethod(name, params);
            check(m.getReturnType() == void.class, name + " returns void");
            check(Modifier.isAbstract(m.getModifiers()), name + " abstract");
        } catch (NoSuchMethodException e) {
            throw new AssertionError("missing method " + name);
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.DocumentListener");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.util.EventListener"), "interface 0");
        checkMethod(c, "insertUpdate", DocumentEvent.class);
        checkMethod(c, "removeUpdate", DocumentEvent.class);
        checkMethod(c, "changedUpdate", DocumentEvent.class);
        check(c.getDeclaredMethods().length == 3, "declared method count == 3");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final String[] fired = new String[1];
        final DocumentEvent[] last = new DocumentEvent[1];
        DocumentListener l = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { fired[0] = "insert"; last[0] = e; }
            public void removeUpdate(DocumentEvent e) { fired[0] = "remove"; last[0] = e; }
            public void changedUpdate(DocumentEvent e) { fired[0] = "change"; last[0] = e; }
        };

        // Real insert/remove events from PlainDocument.
        PlainDocument doc = new PlainDocument();
        doc.addDocumentListener(l);
        doc.insertString(0, "hello", null);
        check("insert".equals(fired[0]), "insertUpdate fired by insertString");
        check(last[0].getType() == DocumentEvent.EventType.INSERT, "insert event type");
        check(last[0].getOffset() == 0, "insert offset == 0");
        check(last[0].getLength() == 5, "insert length == 5");
        check(last[0].getDocument() == doc, "insert document round-trip");

        doc.remove(1, 3);
        check("remove".equals(fired[0]), "removeUpdate fired by remove");
        check(last[0].getType() == DocumentEvent.EventType.REMOVE, "remove event type");
        check(last[0].getOffset() == 1, "remove offset == 1");
        check(last[0].getLength() == 3, "remove length == 3");
        check(last[0].getDocument() == doc, "remove document round-trip");

        // Real changed event from DefaultStyledDocument attribute change.
        DefaultStyledDocument sd = new DefaultStyledDocument();
        sd.insertString(0, "abc", null);
        sd.addDocumentListener(l);
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        attrs.addAttribute("color", "red");
        sd.setCharacterAttributes(0, 1, attrs, false);
        check("change".equals(fired[0]), "changedUpdate fired by setCharacterAttributes");
        check(last[0].getType() == DocumentEvent.EventType.CHANGE, "change event type");
        check(last[0].getOffset() == 0, "change offset == 0");
        check(last[0].getLength() == 1, "change length == 1");
        check(last[0].getDocument() == sd, "change document round-trip");

        // EventType constants are distinct and stable.
        check(DocumentEvent.EventType.INSERT != DocumentEvent.EventType.REMOVE, "INSERT != REMOVE");
        check(DocumentEvent.EventType.INSERT != DocumentEvent.EventType.CHANGE, "INSERT != CHANGE");
        check(DocumentEvent.EventType.REMOVE != DocumentEvent.EventType.CHANGE, "REMOVE != CHANGE");
        check("INSERT".equals(DocumentEvent.EventType.INSERT.toString()), "INSERT toString");
        check("REMOVE".equals(DocumentEvent.EventType.REMOVE.toString()), "REMOVE toString");
        check("CHANGE".equals(DocumentEvent.EventType.CHANGE.toString()), "CHANGE toString");
    }


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // Legacy shallow assertions preserved verbatim from the original JDK-8 test.
    static void testLegacyShallow() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.DocumentListener", false, DocumentListenerTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "java.util.EventListener"), "implements java.util.EventListener");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("insertUpdate"), "method insertUpdate");
        check(names.contains("removeUpdate"), "method removeUpdate");
        check(names.contains("changedUpdate"), "method changedUpdate");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLegacyShallow();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
