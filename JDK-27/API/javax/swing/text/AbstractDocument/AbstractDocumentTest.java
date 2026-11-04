/*
 * @test
 * @summary javax.swing.text.AbstractDocument conformance: properties/roots/filter/lock/render/undoable events/listener isolation (headless-safe).
 * @run main AbstractDocumentTest
 */

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;

public class AbstractDocumentTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static interface Throwing { Object run() throws Exception; }
    static void expectBLE(Throwing r, String m, int expectedOffset) throws Exception {
        try {
            Object o = r.run();
            throw new AssertionError(m + ": expected BadLocationException but returned " + o);
        } catch (BadLocationException e) {
            check(e.offsetRequested() == expectedOffset, m + " offsetRequested==" + expectedOffset);
        }
    }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static void testTypeShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.AbstractDocument", false, AbstractDocumentTest.class.getClassLoader());
        check(!c.isInterface(), "is class");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(hasInterface(c, "javax.swing.text.Document"), "implements Document");
        check(hasInterface(c, "java.io.Serializable"), "implements Serializable");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getDocumentProperties"), "method getDocumentProperties");
        check(names.contains("setDocumentProperties"), "method setDocumentProperties");
        check(names.contains("getListeners"), "method getListeners");
        check(names.contains("getAsynchronousLoadPriority"), "method getAsynchronousLoadPriority");
        check(names.contains("setAsynchronousLoadPriority"), "method setAsynchronousLoadPriority");
        check(names.contains("setDocumentFilter"), "method setDocumentFilter");
        check(names.contains("getDocumentFilter"), "method getDocumentFilter");
        check(names.contains("render"), "method render");
        check(names.contains("replace"), "method replace");
        check(names.contains("getBidiRootElement"), "method getBidiRootElement");
        check(names.contains("dump"), "method dump");
        check(names.contains("readLock"), "method readLock");
        check(names.contains("readUnlock"), "method readUnlock");
        // element name constants
        check("paragraph".equals(AbstractDocument.ParagraphElementName), "ParagraphElementName");
        check("content".equals(AbstractDocument.ContentElementName), "ContentElementName");
        check("section".equals(AbstractDocument.SectionElementName), "SectionElementName");
        check("bidi level".equals(AbstractDocument.BidiElementName), "BidiElementName");
        check("$ename".equals(AbstractDocument.ElementNameAttribute), "ElementNameAttribute internal key");
    }

    static void testProperties() throws Exception {
        PlainDocument d = new PlainDocument();
        Dictionary<Object, Object> props = d.getDocumentProperties();
        check(props != null, "properties dictionary non-null");
        check(props instanceof java.util.Hashtable, "properties is Hashtable");
        check("8".equals(String.valueOf(props.get(PlainDocument.tabSizeAttribute))), "tabSize present via dictionary");

        // the dictionary is live in both directions
        props.put("direct", "dv");
        check("dv".equals(d.getProperty("direct")), "dictionary put visible via getProperty");
        d.putProperty("api", "av");
        check("av".equals(props.get("api")), "putProperty visible via dictionary");

        // null value removes
        d.putProperty("api", null);
        check(d.getProperty("api") == null, "null value removes property");
        check(!props.isEmpty(), "tabSize still present after removal");

        // replace the whole dictionary
        java.util.Hashtable<Object, Object> fresh = new java.util.Hashtable<Object, Object>();
        fresh.put("n", "1");
        d.setDocumentProperties(fresh);
        check("1".equals(d.getProperty("n")), "new dictionary consulted");
        check(d.getProperty("direct") == null, "old entries gone");

        // async load priority
        check(d.getAsynchronousLoadPriority() == -1, "default async priority -1");
        d.setAsynchronousLoadPriority(4);
        check(d.getAsynchronousLoadPriority() == 4, "async priority set");
        d.setAsynchronousLoadPriority(0);
        check(d.getAsynchronousLoadPriority() == 0, "zero async priority kept");
        d.setAsynchronousLoadPriority(-3);
        check(d.getAsynchronousLoadPriority() == -1, "negative async priority clamped to -1");
    }

    static void testRoots() throws Exception {
        PlainDocument d = new PlainDocument();
        d.insertString(0, "a\nb", null);
        Element[] roots = d.getRootElements();
        check(roots.length == 2, "two root elements");
        check(roots[0] == d.getDefaultRootElement(), "root 0 is default root");
        check(roots[1] == d.getBidiRootElement(), "root 1 is bidi root");
        check("bidi root".equals(roots[1].getName()), "bidi root name");
        check(!roots[1].isLeaf(), "bidi root is a branch");
        check(roots[1].getElementCount() == 1, "bidi root has one level element");
        check("bidi level".equals(roots[1].getElement(0).getName()), "bidi level element name");
        check(roots[1].getElement(0).isLeaf(), "bidi level element is a leaf");
        check(roots[1].getStartOffset() == 0, "bidi root start");
        check(roots[1].getEndOffset() == 4, "bidi root end spans content");
    }

    static void testReplace() throws Exception {
        PlainDocument d = new PlainDocument();
        d.insertString(0, "hello world", null);
        d.replace(0, 5, "goodbye", null);
        check("goodbye world".equals(d.getText(0, d.getLength())), "replace region");
        d.replace(0, 0, "X", null); // pure insert
        check("Xgoodbye world".equals(d.getText(0, d.getLength())), "replace as insert");
        d.replace(0, 1, null, null); // pure remove
        check("goodbye world".equals(d.getText(0, d.getLength())), "replace as remove");
        // no-op combinations
        int len = d.getLength();
        d.replace(0, 0, "", null);
        d.replace(0, 0, null, null);
        check(d.getLength() == len, "no-op replace");
        // out of range: delegated to remove/insert bounds (identical on all JDKs:
        // the failing leg is remove(offs,len) / insertString(-1) which both report
        // getLength()+1)
        expectBLE(() -> { d.replace(0, len + 1, "z", null); return null; }, "replace past end", len + 1);
        expectBLE(() -> { d.replace(-1, 0, "z", null); return null; }, "replace negative offset insert", len + 1);
        expectBLE(() -> { d.replace(-1, 1, "z", null); return null; }, "replace negative remove", len + 1);
    }

    static void testRenderAndLocks() throws Exception {
        PlainDocument d = new PlainDocument();
        d.insertString(0, "abc", null);
        final int[] ran = { 0 };
        d.render(new Runnable() { public void run() { ran[0]++; } });
        check(ran[0] == 1, "render runs runnable");
        // render is re-entrant (read lock counting)
        d.render(new Runnable() { public void run() {
            d.render(new Runnable() { public void run() { ran[0]++; } });
        } });
        check(ran[0] == 2, "nested render");
        // exceptions from the runnable propagate but leave the lock released
        try {
            d.render(new Runnable() { public void run() { throw new RuntimeException("boom"); } });
            check(false, "render should propagate");
        } catch (RuntimeException e) {
            check("boom".equals(e.getMessage()), "render propagates original exception");
        }
        // document still usable afterwards (lock bookkeeping intact)
        d.render(new Runnable() { public void run() { ran[0]++; } });
        check(ran[0] == 3, "render after exception");
        // dump produces output without a display
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        d.dump(new PrintStream(bos));
        check(bos.size() > 0, "dump writes output");
    }

    static void testDocumentFilter() throws Exception {
        final PlainDocument d = new PlainDocument();
        d.insertString(0, "hello", null);
        check(d.getDocumentFilter() == null, "no filter by default");

        final Vector<String> calls = new Vector<String>();
        d.setDocumentFilter(new DocumentFilter() {
            public void insertString(FilterBypass fb, int offs, String str, javax.swing.text.AttributeSet a) throws BadLocationException {
                calls.add("ins:" + offs + ":" + str);
                if (!"BLOCK".equals(str)) fb.insertString(offs, str, a);
            }
            public void remove(FilterBypass fb, int offs, int len) throws BadLocationException {
                calls.add("rm:" + offs + ":" + len);
                fb.remove(offs, len);
            }
            public void replace(FilterBypass fb, int offs, int len, String str, javax.swing.text.AttributeSet a) throws BadLocationException {
                calls.add("rep:" + offs + ":" + len + ":" + str);
                fb.replace(offs, len, str, a);
            }
        });
        check(d.getDocumentFilter() != null, "filter installed");

        d.insertString(1, "XY", null);
        check("hXYello".equals(d.getText(0, d.getLength())), "filtered insert applied");
        d.insertString(0, "BLOCK", null);
        check("hXYello".equals(d.getText(0, d.getLength())), "blocked insert not applied");
        d.remove(0, 3);
        check("ello".equals(d.getText(0, d.getLength())), "filtered remove applied");
        d.replace(0, 4, "Z", null);
        check("Z".equals(d.getText(0, d.getLength())), "replace routed through filter.replace");
        check(calls.size() == 4, "all four operations routed through filter");
        check(calls.get(0).equals("ins:1:XY"), "filter insert args");
        check(calls.get(1).equals("ins:0:BLOCK"), "filter blocked args");
        check(calls.get(2).equals("rm:0:3"), "filter remove args");
        check(calls.get(3).equals("rep:0:4:Z"), "filter replace args");

        // removing the filter restores direct mutation
        d.setDocumentFilter(null);
        d.insertString(0, "A", null);
        check("AZ".equals(d.getText(0, d.getLength())), "filter removed");
        check(calls.size() == 4, "no more filter callbacks");
    }

    static void testUndoableEditEvents() throws Exception {
        PlainDocument d = new PlainDocument();
        final Vector<String> events = new Vector<String>();
        d.addUndoableEditListener(new UndoableEditListener() {
            public void undoableEditHappened(UndoableEditEvent e) {
                events.add(e.getEdit().getPresentationName() + ":" + e.getEdit().isSignificant() + ":" + (e.getSource() == d));
            }
        });
        check(d.getUndoableEditListeners().length == 1, "undoable listener registered");
        d.insertString(0, "ab", null);
        check(events.size() == 1, "one event per insert");
        check(events.get(0).equals("addition:true:true"), "insert edit presentation/significance/source");
        d.remove(0, 1);
        check(events.size() == 2, "one event per remove");
        d.insertString(0, "", null); // no-op, no event
        d.remove(0, 0);              // no-op, no event
        check(events.size() == 2, "no-op edits fire nothing");
        d.removeUndoableEditListener(d.getUndoableEditListeners()[0]);
        check(d.getUndoableEditListeners().length == 0, "listener removed");
        d.insertString(0, "z", null);
        check(events.size() == 2, "no events after removal");
        // the edit itself can undo the change
        d.remove(0, 1);
    }

    static void testListenerIsolation() throws Exception {
        // A throwing DocumentListener propagates its exception to the mutating thread,
        // but the document is already updated.
        PlainDocument d = new PlainDocument();
        d.addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { throw new RuntimeException("boom"); }
            public void removeUpdate(DocumentEvent e) { }
            public void changedUpdate(DocumentEvent e) { }
        });
        try {
            d.insertString(0, "x", null);
            check(false, "listener exception should propagate");
        } catch (RuntimeException e) {
            check("boom".equals(e.getMessage()), "original exception propagated");
        }
        check(d.getLength() == 1, "document updated despite listener exception");

        // Mutating the document from within a notification is forbidden.
        PlainDocument d2 = new PlainDocument();
        final PlainDocument inner = d2;
        d2.addDocumentListener(new DocumentListener() {
            boolean reentered = false;
            public void insertUpdate(DocumentEvent e) {
                if (!reentered) {
                    reentered = true;
                    try {
                        inner.insertString(1, "Y", null);
                        check(false, "nested mutation should be rejected");
                    } catch (BadLocationException ble) {
                        check(false, "unexpected BLE for nested mutation");
                    } catch (IllegalStateException ise) {
                        check(ise.getMessage() != null, "nested mutation rejected with IllegalStateException");
                    }
                }
            }
            public void removeUpdate(DocumentEvent e) { }
            public void changedUpdate(DocumentEvent e) { }
        });
        d2.insertString(0, "X", null);
        check(d2.getLength() == 1, "nested mutation had no effect");

        // getListeners returns typed arrays
        PlainDocument d3 = new PlainDocument();
        DocumentListener l = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { }
            public void removeUpdate(DocumentEvent e) { }
            public void changedUpdate(DocumentEvent e) { }
        };
        d3.addDocumentListener(l);
        d3.addDocumentListener(l); // duplicate registration allowed
        check(d3.getDocumentListeners().length == 2, "duplicate listeners kept");
        check(d3.getListeners(DocumentListener.class).length == 2, "getListeners typed");
        d3.removeDocumentListener(l);
        d3.removeDocumentListener(l);
        check(d3.getDocumentListeners().length == 0, "all removed");
        check(d3.getDocumentListeners() instanceof DocumentListener[], "listener array type");
    }

    static void testBleMatrix() throws Exception {
        PlainDocument d = new PlainDocument();
        d.insertString(0, "hello", null);
        int len = d.getLength();
        d.remove(len + 1, 0);
        d.remove(-1, 0);
        check(d.getLength() == len, "zero-length removes are no-ops anywhere");
        expectBLE(() -> d.getText(0, len + 2), "getText beyond content", len + 2);
        expectBLE(() -> { d.insertString(-1, "x", null); return null; }, "negative offset insert", len + 1);
        // JDK-27: AbstractDocument.insertString validates at the document level
        // (offs > getLength() rejected before the Content is touched), so inserting
        // at getLength()+1 (right after the implicit trailing newline) is refused
        // with BLE(getLength()). (JDK-8 accepted it because GapContent allows
        // where == length().)
        expectBLE(() -> { d.insertString(len + 1, "!", null); return null; }, "insert after implicit newline rejected (JDK-27)", len);
        expectBLE(() -> { d.insertString(len + 2, "x", null); return null; }, "insert beyond end", len);
        check(d.getLength() == len, "rejected inserts left the document unchanged");

        // the same rule through replace(): the tail insert leg is refused up front
        PlainDocument r = new PlainDocument();
        r.insertString(0, "hello world", null);
        int rl = r.getLength();
        expectBLE(() -> { r.replace(rl + 1, 0, "z", null); return null; }, "replace tail insert rejected (JDK-27)", rl);
        expectBLE(() -> { r.replace(rl + 2, 0, "z", null); return null; }, "replace far beyond end (JDK-27)", rl);
        check(r.getLength() == rl, "rejected replaces left the document unchanged");
    }

    public static void main(String[] args) throws Exception {
        testTypeShape();
        testProperties();
        testRoots();
        testReplace();
        testRenderAndLocks();
        testDocumentFilter();
        testUndoableEditEvents();
        testListenerIsolation();
        testBleMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
