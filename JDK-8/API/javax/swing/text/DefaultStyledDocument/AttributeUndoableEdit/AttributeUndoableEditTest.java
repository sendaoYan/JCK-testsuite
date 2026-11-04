/*
 * @test
 * @summary javax.swing.text.DefaultStyledDocument.AttributeUndoableEdit conformance: attribute snapshot/undo/redo semantics under the write lock (headless-safe).
 * @run main AttributeUndoableEditTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.undo.AbstractUndoableEdit;

public class AttributeUndoableEditTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static class OpenDoc extends DefaultStyledDocument {
        Element makeBranch(Element parent, AttributeSet a) {
            writeLock();
            try { return ((AbstractDocument) this).new BranchElement(parent, a); }
            finally { writeUnlock(); }
        }
        void locked(Runnable r) { writeLock(); try { r.run(); } finally { writeUnlock(); } }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.DefaultStyledDocument$AttributeUndoableEdit", false, AttributeUndoableEditTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(c.getSuperclass().getName().equals("javax.swing.undo.AbstractUndoableEdit"), "superclass AbstractUndoableEdit");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("undo"), "method undo");
        check(names.contains("redo"), "method redo");
        check(names.contains("isSignificant"), "method isSignificant");

        testUndoRedo(false);
        testUndoRedo(true);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testUndoRedo(boolean replacing) throws Exception {
        OpenDoc d = new OpenDoc();
        d.insertString(0, "hello", null);
        Element root = d.getDefaultRootElement();
        final Element branch = d.makeBranch(root, null);
        // seed the element with one attribute
        SimpleAttributeSet seed = new SimpleAttributeSet();
        StyleConstants.setBold(seed, true);
        d.locked(() -> ((javax.swing.text.MutableAttributeSet) branch.getAttributes()).addAttributes(seed));
        check(StyleConstants.isBold(branch.getAttributes()), "seeded bold, replace=" + replacing);

        // the edit snapshots the current attributes and applies newAttributes on redo
        SimpleAttributeSet next = new SimpleAttributeSet();
        StyleConstants.setItalic(next, true);
        StyleConstants.setFontSize(next, 20);
        final DefaultStyledDocument.AttributeUndoableEdit edit =
                new DefaultStyledDocument.AttributeUndoableEdit(branch, next, replacing);
        check(edit.isSignificant(), "attribute edits are significant");
        check(edit.canUndo(), "fresh edit is in the done state");

        // the edit starts in the "done" state: undo first (restores the snapshot),
        // then redo to apply the recorded attributes like the document would
        d.locked(() -> {
            try { edit.undo(); } catch (Exception e) { throw new RuntimeException(e); }
        });
        check(StyleConstants.isBold(branch.getAttributes()), "initial undo restores the snapshot");
        d.locked(() -> {
            try { edit.redo(); } catch (Exception e) { throw new RuntimeException(e); }
        });
        check(StyleConstants.isItalic(branch.getAttributes()), "redo applied the new attributes");
        check(StyleConstants.getFontSize(branch.getAttributes()) == 20, "redo applied font size");
        if (replacing) {
            check(!StyleConstants.isBold(branch.getAttributes()), "replacing edit cleared the old value");
        } else {
            check(StyleConstants.isBold(branch.getAttributes()), "non-replacing edit kept the old value");
        }

        // undo restores the snapshotted attribute set
        d.locked(() -> {
            try { edit.undo(); } catch (Exception e) { throw new RuntimeException(e); }
        });
        check(StyleConstants.isBold(branch.getAttributes()), "undo restored bold");
        check(!StyleConstants.isItalic(branch.getAttributes()), "undo removed the new attributes");
        check(StyleConstants.getFontSize(branch.getAttributes()) != 20, "undo removed the new size");

        // redo re-applies, and the cycle is repeatable
        d.locked(() -> {
            try { edit.redo(); } catch (Exception e) { throw new RuntimeException(e); }
        });
        check(StyleConstants.isItalic(branch.getAttributes()), "second redo applies again");
        d.locked(() -> {
            try { edit.undo(); } catch (Exception e) { throw new RuntimeException(e); }
        });
        check(!StyleConstants.isItalic(branch.getAttributes()), "second undo reverts again");
    }
}
