/*
 * @test
 * @summary javax.swing.colorchooser.ColorChooserComponentFactory conformance: type shape
 *          via reflection plus behavioral contract (Metal panels, preview, EDT).
 * @run main ColorChooserComponentFactoryTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.ColorChooserComponentFactory;

public class ColorChooserComponentFactoryTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        shape();
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { behavior(); }});
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- white-box: type shape (retained from skeleton) ----
    static void shape() throws Exception {
        Class<?> c = Class.forName("javax.swing.colorchooser.ColorChooserComponentFactory",
                false, ColorChooserComponentFactoryTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "not abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "not final (class is public, non-final)");
        check(c.getSuperclass() != null
              && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        int privateCtors = 0;
        for (Constructor<?> k : c.getDeclaredConstructors())
            if (Modifier.isPrivate(k.getModifiers())) privateCtors++;
        check(privateCtors == 1, "sole constructor is private (static factory only)");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getDefaultChooserPanels"), "method getDefaultChooserPanels");
        check(names.contains("getPreviewPanel"), "method getPreviewPanel");
    }

    // ---- black-box: behavioral contract ----
    static void behavior() {
        try { UIManager.setLookAndFeel(new MetalLookAndFeel0()); }
        catch (Exception e) { throw new AssertionError("Metal LaF: " + e); }

        // default panels: 5 under Metal, all concrete AbstractColorChooserPanels
        AbstractColorChooserPanel[] panels = ColorChooserComponentFactory.getDefaultChooserPanels();
        check(panels != null, "getDefaultChooserPanels non-null");
        check(panels.length == 5, "Metal default panel count 5");
        StringBuilder names = new StringBuilder();
        for (AbstractColorChooserPanel p : panels) {
            check(p != null, "panel element non-null");
            check(p instanceof AbstractColorChooserPanel, "panel is AbstractColorChooserPanel");
            check(p.getDisplayName() != null, "panel has display name");
            check(p.getMnemonic() != 0, "panel mnemonic set");
            check(p.getDisplayedMnemonicIndex() >= 0, "panel displayed mnemonic index valid");
            names.append(p.getDisplayName()).append(';');
        }
        check(names.toString().equals("Swatches;HSV;HSL;RGB;CMYK;"),
              "Metal panel display names Swatches/HSV/HSL/RGB/CMYK");

        // preview panel: fresh JComponent each call, DefaultPreviewPanel type
        JComponent preview = ColorChooserComponentFactory.getPreviewPanel();
        check(preview != null, "getPreviewPanel non-null");
        check(preview instanceof JComponent, "preview is a JComponent");
        check("DefaultPreviewPanel".equals(preview.getClass().getSimpleName()),
              "preview concrete type DefaultPreviewPanel");
        check(ColorChooserComponentFactory.getPreviewPanel() != preview,
              "getPreviewPanel returns a fresh instance each call");

        // panels integrate with a JColorChooser
        javax.swing.JColorChooser cc = new javax.swing.JColorChooser();
        int before = cc.getChooserPanels().length;
        cc.addChooserPanel(panels[0]);
        check(cc.getChooserPanels().length == before + 1, "factory panel addable to chooser");
    }

    static class MetalLookAndFeel0 extends javax.swing.plaf.metal.MetalLookAndFeel {}
}
