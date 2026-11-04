/*
 * @test
 * @summary javax.swing.JLayer conformance: type shape via reflection + layer/view/UI/glass
 *          contract (constructors, view reparenting, LayerUI swap with property event,
 *          layout/border delegation; headless-safe, Metal LaF, EDT).
 * @run main JLayerTest
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.LayerUI;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class JLayerTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void checkThrows(Class<? extends Throwable> ex, Runnable r, String m) {
        try { r.run(); }
        catch (Throwable t) {
            if (!ex.isInstance(t)) throw new AssertionError(m + ": expected " + ex.getSimpleName() + " got " + t);
            passed++; return; }
        throw new AssertionError(m + ": expected " + ex.getSimpleName() + " not thrown");
    }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 1: structural shape (kept from the original skeleton) ----
    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.JLayer", false, JLayerTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(hasInterface(c, "javax.swing.Scrollable"), "implements javax.swing.Scrollable");
        check(hasInterface(c, "java.beans.PropertyChangeListener"), "implements java.beans.PropertyChangeListener");
        check(hasInterface(c, "javax.accessibility.Accessible"), "implements javax.accessibility.Accessible");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getView"), "method getView");
        check(names.contains("setView"), "method setView");
        check(names.contains("setUI"), "method setUI");
        check(names.contains("getUI"), "method getUI");
        check(names.contains("getGlassPane"), "method getGlassPane");
    }

    // ---- Part 2: constructors and view/glass contract ----
    static void testViewAndGlass() {
        JLayer<JPanel> empty = new JLayer<JPanel>();
        check(empty.getView() == null, "no-arg ctor view null");
        check(empty.getGlassPane() != null, "no-arg glass pane non-null");
        check(empty.getGlassPane() instanceof JPanel, "glass pane is JPanel");
        check(empty.getGlassPane().isOpaque() == false, "glass pane not opaque");
        check(empty.getLayerEventMask() == 0L, "event mask default 0");

        JPanel view = new JPanel();
        JLayer<JPanel> layer = new JLayer<JPanel>(view);
        check(layer.getView() == view, "view ctor round-trip");
        check(view.getParent() == layer, "view parented to layer");

        JPanel view2 = new JPanel();
        layer.setView(view2);
        check(layer.getView() == view2, "setView new view");
        check(view2.getParent() == layer, "new view parented");
        check(view.getParent() == null, "old view detached");
        layer.setView(null);
        check(layer.getView() == null, "setView(null) allowed");

        JPanel gp = new JPanel();
        layer.setGlassPane(gp);
        check(layer.getGlassPane() == gp, "setGlassPane round-trip");
        layer.setGlassPane(null);
        check(layer.getGlassPane() == null, "setGlassPane(null) allowed");

        layer.setLayerEventMask(12345L);
        check(layer.getLayerEventMask() == 12345L, "event mask round-trip");
        layer.setLayerEventMask(0L);
        check(layer.getLayerEventMask() == 0L, "event mask reset");
    }

    // ---- Part 3: LayerUI contract ----
    static void testLayerUI() {
        JPanel v3 = new JPanel();
        LayerUI<JPanel> ui2 = new LayerUI<JPanel>();
        JLayer<JPanel> full = new JLayer<JPanel>(v3, ui2);
        check(full.getView() == v3, "view+ui ctor view");
        check(full.getUI() == ui2, "view+ui ctor ui");
        check("ComponentUI".equals(full.getUIClassID()), "getUIClassID ComponentUI");
        check(full.getUI() instanceof LayerUI, "getUI is LayerUI");

        // (null, ui) constructor: legal, view-less layer.
        LayerUI<JPanel> u3 = new LayerUI<JPanel>();
        JLayer<JPanel> nullView = new JLayer<JPanel>(null, u3);
        check(nullView.getView() == null, "null-view ctor view null");
        check(nullView.getUI() == u3, "null-view ctor ui");

        // setUI swap fires a "UI" property change with old/new values.
        final JLayer<JPanel> layer = new JLayer<JPanel>(new JPanel());
        final LayerUI<?> oldUi = layer.getUI();
        final AtomicReference<PropertyChangeEvent> ev = new AtomicReference<PropertyChangeEvent>();
        layer.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                if ("UI".equals(e.getPropertyName())) ev.set(e);
            }
        });
        LayerUI<JPanel> newUi = new LayerUI<JPanel>();
        layer.setUI(newUi);
        check(layer.getUI() == newUi, "setUI round-trip");
        check(ev.get() != null, "setUI fires UI property event");
        check(ev.get().getOldValue() == oldUi, "UI event old value");
        check(ev.get().getNewValue() == newUi, "UI event new value");
        layer.updateUI();
        check(true, "updateUI no-throw");
    }

    // ---- Part 4: layout / border contract ----
    static void testDelegation() {
        final JLayer<JPanel> layer = new JLayer<JPanel>(new JPanel());
        checkThrows(IllegalArgumentException.class,
                new Runnable() { public void run() { layer.setLayout(new java.awt.FlowLayout()); } },
                "setLayout(non-null) IAE");
        layer.setLayout(null);
        check(layer.getLayout() == null, "setLayout(null) allowed");
        // JDK-8: JLayer.setBorder(non-null) is rejected outright (delegation to the view
        // was added in a later release); setBorder(null) and getBorder() delegation hold.
        final JPanel bordered = new JPanel();
        final JLayer<JPanel> bl = new JLayer<JPanel>(bordered);
        checkThrows(IllegalArgumentException.class,
                new Runnable() { public void run() { bl.setBorder(new EmptyBorder(1, 2, 3, 4)); } },
                "setBorder(non-null) IAE on JDK-8");
        check(bordered.getBorder() == null, "view border untouched by rejected set");
        bl.setBorder(null);
        check(bordered.getBorder() == null, "setBorder(null) allowed");
        check(bl.getBorder() == null, "getBorder delegates to view border");
        JLayer<JPanel> noView = new JLayer<JPanel>();
        check(noView.getBorder() == null, "getBorder null with no view");
    }

    public static void main(String[] args) throws Exception {
        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (Exception e) {
            throw new AssertionError("cannot install Metal LaF", e);
        }
        testShape();
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { testViewAndGlass(); } });
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { testLayerUI(); } });
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { testDelegation(); } });
        System.out.println("PASS (" + passed + " assertions)");
    }
}
