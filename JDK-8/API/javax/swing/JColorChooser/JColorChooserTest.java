/*
 * @test
 * @summary javax.swing.JColorChooser conformance: type shape via reflection (headless-safe)
 *          plus behavioral contract (Metal LaF, EDT discipline, headless).
 * @run main JColorChooserTest
 */

import java.awt.Color;
import java.awt.HeadlessException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.colorchooser.DefaultColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicColorChooserUI;

public class JColorChooserTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static Throwable checkThrows(Class<? extends Throwable> ex, Runnable r) {
        try { r.run(); } catch (Throwable t) {
            if (!ex.isInstance(t)) throw new AssertionError("expected " + ex.getSimpleName() + " got " + t);
            passed++; return t; }
        throw new AssertionError("expected " + ex.getSimpleName() + " not thrown"); }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        shape();
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { behavior(); }});
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- white-box: type shape (retained from skeleton) ----
    static void shape() throws Exception {
        Class<?> c = Class.forName("javax.swing.JColorChooser", false, JColorChooserTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(hasInterface(c, "javax.accessibility.Accessible"), "implements javax.accessibility.Accessible");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getColor"), "method getColor");
        check(names.contains("setColor"), "method setColor");
    }

    // ---- black-box: behavioral contract ----
    static void behavior() {
        try { UIManager.setLookAndFeel(new MetalLookAndFeel0()); }
        catch (Exception e) { throw new AssertionError("Metal LaF: " + e); }

        // ctor matrix
        JColorChooser cc = new JColorChooser();
        check(cc.getColor().equals(Color.WHITE), "no-arg default color WHITE");
        check(cc.getSelectionModel() instanceof DefaultColorSelectionModel, "default model DefaultColorSelectionModel");
        JColorChooser red = new JColorChooser(Color.RED);
        check(red.getColor().equals(Color.RED), "ctor(Color.RED)");
        DefaultColorSelectionModel m = new DefaultColorSelectionModel(Color.GREEN);
        JColorChooser mc = new JColorChooser(m);
        check(mc.getSelectionModel() == m, "ctor(model) keeps model identity");
        check(mc.getColor().equals(Color.GREEN), "ctor(model) color follows model");

        // setColor variants
        cc.setColor(Color.BLUE);
        check(cc.getColor().equals(Color.BLUE), "setColor(Color)");
        cc.setColor(0xFF00FF);
        check(cc.getColor().getRed() == 0xFF && cc.getColor().getGreen() == 0
              && cc.getColor().getBlue() == 0xFF, "setColor(int) exact channels");
        cc.setColor(1, 2, 3);
        check(cc.getColor().getRed() == 1 && cc.getColor().getGreen() == 2
              && cc.getColor().getBlue() == 3, "setColor(r,g,b) exact channels");

        // change events: 1 for a different color, 0 for the same color
        final int[] fired = {0};
        cc.getSelectionModel().addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) { fired[0]++; }});
        cc.setColor(Color.RED);
        check(fired[0] == 1, "setColor different color fires exactly 1 event");
        cc.setColor(Color.RED);
        check(fired[0] == 1, "setColor same color fires 0 events");
        cc.setColor(Color.BLACK);
        check(fired[0] == 2, "second change fires exactly 1 event");

        // selection model swap: color follows the new model, events come from it
        DefaultColorSelectionModel m2 = new DefaultColorSelectionModel(Color.CYAN);
        cc.setSelectionModel(m2);
        check(cc.getSelectionModel() == m2, "setSelectionModel roundtrip");
        check(cc.getColor().equals(Color.CYAN), "color follows new model");
        final int[] fired2 = {0};
        m2.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) { fired2[0]++; }});
        cc.setColor(Color.DARK_GRAY);
        check(fired2[0] == 1, "setColor routes through the new model's listeners");

        // chooser panels: default count, add/remove roundtrip on a scratch chooser
        AbstractColorChooserPanel[] panels = cc.getChooserPanels();
        check(panels.length == 5, "Metal default panels == 5 (Swatches/HSV/HSL/RGB/CMYK)");
        check(panels.length > 0 && panels[0] instanceof AbstractColorChooserPanel, "panels typed");
        NopPanel extra = new NopPanel();
        cc.addChooserPanel(extra); // adding a fresh panel instance -> 6
        check(cc.getChooserPanels().length == 6, "addChooserPanel grows list");
        check(cc.removeChooserPanel(extra) == extra, "removeChooserPanel returns removed panel");
        check(cc.getChooserPanels().length == 5, "back to 5 after remove");

        // destructive cases each get a throwaway chooser so later UI plumbing is unaffected
        JColorChooser scratch = new JColorChooser();
        scratch.setChooserPanels(new AbstractColorChooserPanel[]{new NopPanel(), new NopPanel()});
        check(scratch.getChooserPanels().length == 2, "setChooserPanels replaces list");
        final JColorChooser clean = new JColorChooser();
        checkThrows(IllegalArgumentException.class, new Runnable() {
            public void run() { clean.removeChooserPanel(new NopPanel()); }});
        check(clean.getChooserPanels().length == 5, "failed remove leaves list unchanged");
        final JColorChooser dup = new JColorChooser();
        checkThrows(RuntimeException.class, new Runnable() {
            public void run() { dup.addChooserPanel(dup.getChooserPanels()[0]); }});

        // JDK-8 contract: getChooserPanels() returns the SAME internal array each
        // call (aliasing); JDK-27 returns a defensive copy instead
        check(cc.getChooserPanels() == cc.getChooserPanels(), "JDK-8: repeated getChooserPanels same array");

        // preview panel
        check(cc.getPreviewPanel() != null, "default preview panel non-null");
        JPanel pv = new JPanel();
        cc.setPreviewPanel(pv);
        check(cc.getPreviewPanel() == pv, "setPreviewPanel roundtrip");
        cc.setPreviewPanel(null);
        check(cc.getPreviewPanel() == null, "setPreviewPanel(null) -> null");

        // headless: dialog creation throws. Under a display showDialog is modal
        // and would block (the -Xcomp run timed out here), so gate on isHeadless().
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            checkThrows(HeadlessException.class, new Runnable() {
                public void run() { JColorChooser.showDialog(null, "t", Color.RED); }});
            checkThrows(HeadlessException.class, new Runnable() {
                public void run() {
                    new JColorChooser().createDialog(null, "t", true, new JColorChooser(), null, null); }});
        }

        // UI plumbing
        check(cc.getUIClassID().equals("ColorChooserUI"), "getUIClassID");
        check(cc.getUI() instanceof BasicColorChooserUI, "Metal LaF installs BasicColorChooserUI");
        cc.updateUI();
        check(cc.getUI() != null, "updateUI no-throw");
        check(cc.getAccessibleContext() != null, "accessible context non-null");
    }

    static class NopPanel extends AbstractColorChooserPanel {
        public void updateChooser() {}
        protected void buildChooser() {}
        public String getDisplayName() { return "nop"; }
        public javax.swing.Icon getSmallDisplayIcon() { return null; }
        public javax.swing.Icon getLargeDisplayIcon() { return null; }
    }

    static class MetalLookAndFeel0 extends javax.swing.plaf.metal.MetalLookAndFeel {}
}
