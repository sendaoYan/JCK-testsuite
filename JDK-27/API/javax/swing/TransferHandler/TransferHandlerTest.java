/*
 * @test
 * @summary javax.swing.TransferHandler conformance: type shape + clipboard/drag contract (headless-safe).
 * @run main TransferHandlerTest
 */

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.Point;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class TransferHandlerTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static Throwable checkThrows(Class<? extends Throwable> ex, Runnable r) {
        try { r.run(); } catch (Throwable t) {
            if (!ex.isInstance(t)) throw new AssertionError("expected " + ex.getSimpleName() + " got " + t);
            passed++; return t; }
        throw new AssertionError("expected " + ex.getSimpleName() + " not thrown");
    }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.TransferHandler", false, TransferHandlerTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "java.io.Serializable"), "implements java.io.Serializable");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getCutAction"), "method getCutAction");
        check(names.contains("getCopyAction"), "method getCopyAction");
        check(names.contains("getPasteAction"), "method getPasteAction");
        check(names.contains("setDragImage"), "method setDragImage");
        check(names.contains("getDragImage"), "method getDragImage");
        check(names.contains("setDragImageOffset"), "method setDragImageOffset");
        check(names.contains("getDragImageOffset"), "method getDragImageOffset");
    }

    static void testConstantsAndActions() {
        check(TransferHandler.NONE == 0, "NONE == 0");
        check(TransferHandler.COPY == 1, "COPY == 1");
        check(TransferHandler.MOVE == 2, "MOVE == 2");
        check(TransferHandler.COPY_OR_MOVE == 3, "COPY_OR_MOVE == 3");
        Action copy = TransferHandler.getCopyAction();
        Action cut = TransferHandler.getCutAction();
        Action paste = TransferHandler.getPasteAction();
        check(copy != null && cut != null && paste != null, "built-in actions nonnull");
        check(copy == TransferHandler.getCopyAction(), "getCopyAction returns shared instance");
        check(cut == TransferHandler.getCutAction(), "getCutAction returns shared instance");
        check(paste == TransferHandler.getPasteAction(), "getPasteAction returns shared instance");
        check(copy instanceof Action && cut instanceof Action && paste instanceof Action,
                "built-ins are Actions");
    }

    static void testPropertyHandler() {
        new TransferHandler(null); // no validation in constructor
        TransferHandler th = new TransferHandler("text");
        check(th.toString() != null && !th.toString().isEmpty(), "toString nonempty");
        JLabel label = new JLabel("hello");
        check(th.getSourceActions(label) == TransferHandler.COPY,
                "property handler advertises COPY");
        check(!th.canImport(label, new DataFlavor[]{DataFlavor.stringFlavor}),
                "canImport(JComponent, flavors) default false");
        check(th.getDragImage() == null, "dragImage default null");
        java.awt.Point p = new Point(3, 4);
        th.setDragImageOffset(p);
        check(p.equals(th.getDragImageOffset()), "dragImageOffset roundtrip");
        checkThrows(NullPointerException.class, () -> th.importData(label, null));
    }

    static class Defaulting extends TransferHandler {
        @Override public boolean canImport(TransferSupport support) { return super.canImport(support); }
        @Override public boolean importData(TransferSupport support) { return super.importData(support); }
    }

    static void testDefaultMethods() {
        Defaulting d = new Defaulting();
        JLabel label = new JLabel("x");
        TransferHandler.TransferSupport s = new TransferHandler.TransferSupport(label,
                new StringSelection("t"));
        check(!d.canImport(s), "canImport(TransferSupport) default false");
        check(!d.importData(s), "importData(TransferSupport) default false");
        check(d.getVisualRepresentation(null) == null,
                "getVisualRepresentation(null) default null");
        d.exportAsDrag(label, null, TransferHandler.COPY); // headless: no throw
    }

    static class Recording extends TransferHandler {
        boolean created;
        @Override protected Transferable createTransferable(JComponent c) {
            created = true;
            return new StringSelection("payload");
        }
        @Override public int getSourceActions(JComponent c) { return COPY_OR_MOVE; }
    }

    static void testExportToClipboard() {
        JLabel label = new JLabel("hello");
        Clipboard cb = new Clipboard("test");
        // Action not in source actions: silently ignored, nothing exported
        new TransferHandler("text").exportToClipboard(label, cb, TransferHandler.NONE);
        check(cb.getContents(null) == null, "NONE action exports nothing");
        Recording rec = new Recording();
        rec.exportToClipboard(label, cb, TransferHandler.COPY);
        check(rec.created, "exportToClipboard invokes createTransferable");
        Transferable contents = cb.getContents(null);
        check(contents != null, "clipboard holds exported contents");
        try {
            check(contents.isDataFlavorSupported(DataFlavor.stringFlavor),
                    "exported contents expose string flavor");
        } catch (Exception e) {
            throw new AssertionError("flavor probe failed", e);
        }
        check(rec.getSourceActions(label) == TransferHandler.COPY_OR_MOVE,
                "getSourceActions unaffected by export");
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        testShape();
        testConstantsAndActions();
        testPropertyHandler();
        testDefaultMethods();
        testExportToClipboard();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
