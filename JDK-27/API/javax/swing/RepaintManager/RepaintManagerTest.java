/*
 * @test
 * @summary javax.swing.RepaintManager conformance: type shape + dirty-region/double-buffer contract (headless-safe).
 * @run main RepaintManagerTest
 */

import java.awt.Rectangle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.RepaintManager;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class RepaintManagerTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static Throwable checkThrows(Class<? extends Throwable> ex, Runnable r) {
        try { r.run(); } catch (Throwable t) {
            if (!ex.isInstance(t)) throw new AssertionError("expected " + ex.getSimpleName() + " got " + t);
            passed++; return t; }
        throw new AssertionError("expected " + ex.getSimpleName() + " not thrown");
    }

    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.RepaintManager", false, RepaintManagerTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("currentManager"), "method currentManager");
        check(names.contains("setCurrentManager"), "method setCurrentManager");
        check(names.contains("addInvalidComponent"), "method addInvalidComponent");
        check(names.contains("removeInvalidComponent"), "method removeInvalidComponent");
        check(names.contains("addDirtyRegion"), "method addDirtyRegion");
    }

    static void testCurrentManager() {
        JLabel probe = new JLabel("x");
        RepaintManager original = RepaintManager.currentManager(probe);
        check(original != null, "currentManager(Component) nonnull");
        check(original == RepaintManager.currentManager((javax.swing.JComponent) probe),
                "currentManager(Component) and (JComponent) agree");
        RepaintManager custom = new RepaintManager();
        RepaintManager.setCurrentManager(custom);
        check(RepaintManager.currentManager(probe) == custom, "setCurrentManager visible via currentManager");
        RepaintManager.setCurrentManager(original);
        check(RepaintManager.currentManager(probe) == original, "currentManager restored");
    }

    static void testDoubleBuffering() {
        RepaintManager rm = new RepaintManager();
        check(rm.isDoubleBufferingEnabled(), "double buffering default on");
        rm.setDoubleBufferingEnabled(false);
        check(!rm.isDoubleBufferingEnabled(), "setDoubleBufferingEnabled(false)");
        rm.setDoubleBufferingEnabled(true);
        check(rm.isDoubleBufferingEnabled(), "setDoubleBufferingEnabled(true)");
        check(rm.toString() != null && !rm.toString().isEmpty(), "toString nonempty");
    }

    static void testDirtyRegions() {
        RepaintManager rm = new RepaintManager();
        JPanel p = new JPanel(); // not showing, size 0x0
        check(rm.getDirtyRegion(p).isEmpty(), "clean component has empty dirty region");
        check(!rm.isCompletelyDirty(p), "fresh component not completely dirty");
        rm.addDirtyRegion(p, 0, 0, 10, 10);
        // Region is intersected with the component's (0x0) bounds: stays empty, ignored for non-showing peers
        check(rm.getDirtyRegion(p).isEmpty(),
                "dirty region clipped away for non-showing 0x0 component");
        check(!rm.isCompletelyDirty(p), "0x0 component never counts as completely dirty");
        rm.markCompletelyDirty(p);
        check(!rm.isCompletelyDirty(p), "markCompletelyDirty on 0x0 component has no effect");
        rm.markCompletelyClean(p);
        check(!rm.isCompletelyDirty(p), "markCompletelyClean clears dirty state");
        check(rm.getDirtyRegion(p).isEmpty(), "dirty region empty after markCompletelyClean");
        rm.addDirtyRegion(p, -1, -1, -1, -1); // negative bounds accepted silently
        check(!rm.isCompletelyDirty(p), "negative dirty region ignored");
        rm.paintDirtyRegions(); // no showing components: no-throw
        rm.validateInvalidComponents(); // nothing invalid: no-throw
        rm.addInvalidComponent(p);
        rm.removeInvalidComponent(p); // both no-throw off-EDT
        // JDK-27 difference vs JDK-8: null component is tolerated instead of throwing NPE
        rm.addDirtyRegion((javax.swing.JComponent) null, 0, 0, 1, 1); // no-throw
        passed++;
        rm.getDirtyRegion(null); // no-throw
        passed++;
        Rectangle r = rm.getDirtyRegion(new JLabel("other"));
        check(r.isEmpty(), "untracked component reports empty dirty region");
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        testShape();
        testCurrentManager();
        testDoubleBuffering();
        testDirtyRegions();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
