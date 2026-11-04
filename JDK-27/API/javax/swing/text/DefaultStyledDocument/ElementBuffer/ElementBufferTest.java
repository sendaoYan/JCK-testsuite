/*
 * @test
 * @summary javax.swing.text.DefaultStyledDocument.ElementBuffer conformance: white-box structure + black-box behavior.
 * @run main ElementBufferTest
 */

import java.lang.reflect.Modifier;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;

public class ElementBufferTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // Expose the protected write lock and buffer so an ElementBuffer's
    // structure-cloning API can be exercised under the required lock.
    static class DSD extends DefaultStyledDocument {
        public void lock() { writeLock(); }
        public void unlock() { writeUnlock(); }
        public ElementBuffer buffer() { return buffer; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.DefaultStyledDocument$ElementBuffer");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.io.Serializable"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        DSD dsd = new DSD();
        dsd.insertString(0, "abc\ndef", null);
        Element root = dsd.getDefaultRootElement();

        // A fresh ElementBuffer round-trips the root it is given.
        DefaultStyledDocument.ElementBuffer eb = dsd.new ElementBuffer(root);
        check(eb.getRootElement() == root, "fresh ElementBuffer getRootElement");

        // The document's own buffer exposes the same root.
        check(dsd.buffer() != null, "document buffer non-null");
        check(dsd.buffer().getRootElement() == root, "document buffer root matches");

        // clone mirrors the element structure (needs the write lock).
        dsd.lock();
        Element cloned;
        try {
            cloned = eb.clone(null, root);
        } finally {
            dsd.unlock();
        }
        check(cloned != null, "clone non-null");
        check(cloned != root, "clone is a distinct element");
        check(cloned.getElementCount() == root.getElementCount(), "clone child count preserved");
        check(cloned.getStartOffset() == root.getStartOffset()
              && cloned.getEndOffset() == root.getEndOffset(), "clone offset range preserved");
        check(((javax.swing.text.AttributeSet) cloned).getAttributeCount()
              == ((javax.swing.text.AttributeSet) root).getAttributeCount(),
              "clone attribute count preserved");

        // clone a paragraph branch as well.
        Element para = dsd.getParagraphElement(0);
        dsd.lock();
        Element pclone;
        try {
            pclone = eb.clone(null, para);
        } finally {
            dsd.unlock();
        }
        check("paragraph".equals(pclone.getName()), "paragraph clone name");
        check(pclone.getElementCount() == para.getElementCount(), "paragraph clone child count");
        check(pclone.getStartOffset() == para.getStartOffset()
              && pclone.getEndOffset() == para.getEndOffset(), "paragraph clone range");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
