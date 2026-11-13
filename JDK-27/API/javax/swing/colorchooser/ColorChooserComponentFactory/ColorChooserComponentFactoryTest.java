/*
 * @test
 * @summary javax.swing.colorchooser.ColorChooserComponentFactory conformance: white-box structure + black-box behavior.
 * @run main ColorChooserComponentFactoryTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.swing.JComponent;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.ColorChooserComponentFactory;

public class ColorChooserComponentFactoryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.colorchooser.ColorChooserComponentFactory");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");

        Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1 && Modifier.isPrivate(ctors[0].getModifiers()), "private sole constructor");
        check(c.getMethod("getDefaultChooserPanels") != null, "static getDefaultChooserPanels");
        check(c.getMethod("getPreviewPanel") != null, "static getPreviewPanel");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        AbstractColorChooserPanel[] panels = ColorChooserComponentFactory.getDefaultChooserPanels();
        check(panels != null, "getDefaultChooserPanels non-null");
        check(panels.length >= 1, "at least one default panel");
        for (int i = 0; i < panels.length; i++) {
            check(panels[i] != null, "panel element non-null");
            check(panels[i] instanceof AbstractColorChooserPanel, "panel is an AbstractColorChooserPanel");
            check(panels[i].getDisplayName() != null && panels[i].getDisplayName().length() > 0,
                    "panel has a display name");
        }

        JComponent preview = ColorChooserComponentFactory.getPreviewPanel();
        check(preview != null, "getPreviewPanel non-null");
        check(preview instanceof javax.swing.JComponent, "preview is a JComponent");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // Appended contract: panel names/mnemonics, fresh preview instances.
    static void testContract() throws Exception {
        try { javax.swing.UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel()); }
        catch (Exception e) { throw new AssertionError("Metal LaF: " + e); }
        javax.swing.colorchooser.AbstractColorChooserPanel[] panels =
                javax.swing.colorchooser.ColorChooserComponentFactory.getDefaultChooserPanels();
        check(panels.length == 5, "Metal default panel count 5");
        // 面板显示名来自 UIManager 资源键，随默认 locale 本地化（中文 Windows
        // 上不是 Swatches/HSV/...）。用实现所用的同一组资源键取期望值，并额外
        // 校验名称互不相同、非空，保持“五个可区分的面板”这一契约。
        String[] nameKeys = {
            "ColorChooser.swatchesNameText", "ColorChooser.hsvNameText",
            "ColorChooser.hslNameText", "ColorChooser.rgbNameText",
            "ColorChooser.cmykNameText",
        };
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (int i = 0; i < panels.length; i++) {
            javax.swing.colorchooser.AbstractColorChooserPanel p = panels[i];
            check(p.getMnemonic() != 0, "panel mnemonic set");
            check(p.getDisplayedMnemonicIndex() >= 0, "panel displayed mnemonic index valid");
            String expected = javax.swing.UIManager.getString(nameKeys[i], p.getLocale());
            check(expected != null && expected.equals(p.getDisplayName()),
                  "panel " + i + " display name matches " + nameKeys[i] + " [" + p.getDisplayName() + "]");
            names.add(p.getDisplayName());
        }
        check(names.size() == panels.length, "panel display names are distinct");
        javax.swing.JComponent preview =
                javax.swing.colorchooser.ColorChooserComponentFactory.getPreviewPanel();
        check("DefaultPreviewPanel".equals(preview.getClass().getSimpleName()),
              "preview concrete type DefaultPreviewPanel");
        check(javax.swing.colorchooser.ColorChooserComponentFactory.getPreviewPanel() != preview,
              "getPreviewPanel returns a fresh instance each call");
    }
}
