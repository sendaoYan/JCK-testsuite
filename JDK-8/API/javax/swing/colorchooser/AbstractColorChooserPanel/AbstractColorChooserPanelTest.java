/*
 * @test
 * @summary javax.swing.colorchooser.AbstractColorChooserPanel conformance: type shape via
 *          reflection plus behavioral contract (install lifecycle, Metal LaF, EDT).
 * @run main AbstractColorChooserPanelTest
 */

import java.lang.reflect.Method;
import java.util.Set;
import java.lang.reflect.Modifier;
import javax.swing.JColorChooser;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.colorchooser.AbstractColorChooserPanel;

public class AbstractColorChooserPanelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static Throwable checkThrows(Class<? extends Throwable> ex, Runnable r) {
        try { r.run(); } catch (Throwable t) {
            if (!ex.isInstance(t)) throw new AssertionError("expected " + ex.getSimpleName() + " got " + t);
            passed++; return t; }
        throw new AssertionError("expected " + ex.getSimpleName() + " not thrown"); }

    public static void main(String[] args) throws Exception {
        shape();
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { behavior(); }});
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- white-box: type shape (retained from skeleton) ----
    static void shape() throws Exception {
        Class<?> c = Class.forName("javax.swing.colorchooser.AbstractColorChooserPanel",
                false, AbstractColorChooserPanelTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "not final");
        check(c.getSuperclass() != null
              && c.getSuperclass().getName().equals("javax.swing.JPanel"), "superclass JPanel");
        int abstractCount = 0;
        Set<String> abstractNames = new java.util.HashSet<String>();
        for (Method m : c.getDeclaredMethods())
            if (Modifier.isAbstract(m.getModifiers())) { abstractCount++; abstractNames.add(m.getName()); }
        check(abstractCount == 5, "five abstract methods");
        check(abstractNames.contains("updateChooser") && abstractNames.contains("buildChooser")
              && abstractNames.contains("getDisplayName")
              && abstractNames.contains("getSmallDisplayIcon")
              && abstractNames.contains("getLargeDisplayIcon"),
              "abstract method set");
    }

    // ---- black-box: behavioral contract ----
    static class ProbePanel extends AbstractColorChooserPanel {
        int buildCalls, updateCalls;
        String name() { return "probe"; }
        public void updateChooser() { updateCalls++; }
        protected void buildChooser() { buildCalls++; }
        public String getDisplayName() { return "probe"; }
        public javax.swing.Icon getSmallDisplayIcon() { return null; }
        public javax.swing.Icon getLargeDisplayIcon() { return null; }
    }

    static void behavior() {
        try { UIManager.setLookAndFeel(new MetalLookAndFeel0()); }
        catch (Exception e) { throw new AssertionError("Metal LaF: " + e); }

        ProbePanel p = new ProbePanel();

        // defaults before install
        check(p.getMnemonic() == 0, "default mnemonic 0");
        check(p.getDisplayedMnemonicIndex() == -1, "default displayed mnemonic index -1");
        check(p.getColorSelectionModel() == null, "no model before install");
        check(p.buildCalls == 0 && p.updateCalls == 0, "no lifecycle callbacks before install");
        check("probe".equals(p.getDisplayName()), "getDisplayName from subclass");
        check(p.getSmallDisplayIcon() == null && p.getLargeDisplayIcon() == null,
              "icons from subclass");
        check(p instanceof javax.swing.JPanel, "panel is a JPanel (usable as component)");

        // install lifecycle against a real chooser
        JColorChooser chooser = new JColorChooser();
        p.installChooserPanel(chooser);
        check(p.buildCalls == 1, "install invokes buildChooser exactly once");
        check(p.updateCalls == 1, "install invokes updateChooser exactly once");
        check(p.getColorSelectionModel() == chooser.getSelectionModel(),
              "model wired to enclosing chooser after install");

        // second install of the same panel instance is rejected
        checkThrows(RuntimeException.class, new Runnable() {
            public void run() { p.installChooserPanel(new JColorChooser()); }});

        // uninstall detaches the model
        p.uninstallChooserPanel(chooser);
        check(p.getColorSelectionModel() == null, "model detached after uninstall");

        // the panel sees color changes through the chooser's model while installed
        ProbePanel q = new ProbePanel();
        JColorChooser ch2 = new JColorChooser();
        q.installChooserPanel(ch2);
        ch2.getSelectionModel().setSelectedColor(java.awt.Color.MAGENTA);
        check(q.getColorSelectionModel().getSelectedColor().equals(java.awt.Color.MAGENTA),
              "selected color visible through panel model");
        q.uninstallChooserPanel(ch2);

        check(p.getAccessibleContext() != null, "accessible context non-null");
    }

    static class MetalLookAndFeel0 extends javax.swing.plaf.metal.MetalLookAndFeel {}
}
