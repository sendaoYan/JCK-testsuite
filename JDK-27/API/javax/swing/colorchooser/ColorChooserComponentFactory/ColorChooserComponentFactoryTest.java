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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
