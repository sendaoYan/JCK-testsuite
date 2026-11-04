/*
 * @test
 * @summary javax.swing.text.AbstractDocument.Content conformance: interface shape + shared Content contract umbrella over GapContent and StringContent (headless-safe).
 * @run main ContentTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.GapContent;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.text.StringContent;

public class ContentTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    interface Throwing { Object run() throws Exception; }
    static void expectBLE(Throwing r, String m) throws Exception {
        try {
            Object o = r.run();
            throw new AssertionError(m + ": expected BadLocationException but returned " + o);
        } catch (BadLocationException e) {
            check(true, m + " throws BadLocationException");
        }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.AbstractDocument$Content", false, ContentTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("createPosition"), "method createPosition");
        check(names.contains("length"), "method length");
        check(names.contains("insertString"), "method insertString");
        check(names.contains("remove"), "method remove");
        check(names.contains("getString"), "method getString");
        check(names.contains("getChars"), "method getChars");
        check(hasInterface(Class.forName("javax.swing.text.GapContent"), "javax.swing.text.AbstractDocument$Content"),
                "GapContent implements Content");
        check(hasInterface(Class.forName("javax.swing.text.StringContent"), "javax.swing.text.AbstractDocument$Content"),
                "StringContent implements Content");

        exercise(new GapContent(), "gap");
        exercise(new StringContent(), "string");
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void exercise(AbstractDocument.Content content, String tag) throws Exception {
        // both implementations start with exactly the implicit trailing newline
        check(content.length() == 1, tag + " initial length 1");
        check("\n".equals(content.getString(0, 1)), tag + " initial newline only");

        content.insertString(0, "hello");
        check(content.length() == 6, tag + " length after insert");
        check("hello".equals(content.getString(0, 5)), tag + " user content");
        check("\n".equals(content.getString(5, 1)), tag + " newline still last");

        content.insertString(2, "XX");
        check("heXXllo".equals(content.getString(0, 7)), tag + " middle insert");

        // positions track edits
        Position p = content.createPosition(2);
        check(p.getOffset() == 2, tag + " position created");
        content.remove(2, 2);
        check(p.getOffset() == 2, tag + " position after remove");
        check("hello".equals(content.getString(0, 5)), tag + " middle remove");

        // segment extraction
        Segment seg = new Segment();
        content.getChars(1, 3, seg);
        check(seg.count == 3 && "ell".equals(seg.toString()), tag + " getChars");

        // out-of-range access is refused uniformly at the end of the buffer
        expectBLE(() -> content.getString(0, content.length() + 1), tag + " getString past end");
        expectBLE(() -> { content.getChars(0, content.length() + 1, seg); return null; }, tag + " getChars past end");
        // the trailing newline cannot be removed
        expectBLE(() -> content.remove(content.length() - 1, 1), tag + " remove trailing newline");
        expectBLE(() -> content.remove(0, content.length()), tag + " remove everything");

        // insert returns an undoable edit that restores the previous content
        javax.swing.undo.UndoableEdit edit = content.insertString(0, "Z");
        check(edit != null && edit.canUndo(), tag + " insert edit canUndo");
        edit.undo();
        check("hello".equals(content.getString(0, 5)), tag + " insert undone");
        edit.redo();
        check("Zhello".equals(content.getString(0, 6)), tag + " insert redone");
    }
}