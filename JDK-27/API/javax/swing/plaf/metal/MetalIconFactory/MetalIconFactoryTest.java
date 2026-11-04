/*
 * @test
 * @summary javax.swing.plaf.metal.MetalIconFactory conformance: white-box structure + black-box behavior.
 * @run main MetalIconFactoryTest
 */

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.plaf.metal.MetalIconFactory;

public class MetalIconFactoryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Exception; }
    static void assertNoThrow(ThrowingRunnable r, String msg) {
        try { r.run(); }
        catch (Throwable t) { throw new AssertionError(msg + ": " + t, t); }
        passed++;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.metal.MetalIconFactory");
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

    static void testBlackBox() throws Exception {
        MetalIconFactory f = new MetalIconFactory();
        check(f instanceof Serializable, "factory Serializable");
        check(MetalIconFactory.DARK != MetalIconFactory.LIGHT, "DARK != LIGHT");

        Icon[] icons = new Icon[] {
            MetalIconFactory.getFileChooserDetailViewIcon(),
            MetalIconFactory.getFileChooserHomeFolderIcon(),
            MetalIconFactory.getFileChooserListViewIcon(),
            MetalIconFactory.getFileChooserNewFolderIcon(),
            MetalIconFactory.getFileChooserUpFolderIcon(),
            MetalIconFactory.getTreeFolderIcon(),
            MetalIconFactory.getTreeLeafIcon(),
            MetalIconFactory.getTreeControlIcon(true),
            MetalIconFactory.getTreeControlIcon(false),
            MetalIconFactory.getRadioButtonIcon(),
            MetalIconFactory.getCheckBoxIcon(),
            MetalIconFactory.getMenuArrowIcon(),
            MetalIconFactory.getMenuItemArrowIcon(),
            MetalIconFactory.getHorizontalSliderThumbIcon(),
            MetalIconFactory.getVerticalSliderThumbIcon(),
            MetalIconFactory.getInternalFrameCloseIcon(16),
            MetalIconFactory.getInternalFrameMinimizeIcon(16),
            MetalIconFactory.getInternalFrameMaximizeIcon(16),
            MetalIconFactory.getInternalFrameDefaultMenuIcon(),
        };
        check(icons.length == 19, "19 icon accessors");
        for (Icon icon : icons) {
            check(icon != null, "icon non-null");
            check(icon.getIconWidth() >= 0, "iconWidth non-negative");
            check(icon.getIconHeight() >= 0, "iconHeight non-negative");
        }

        // Icons whose paintIcon ignores the Component or only reads its
        // (possibly null, for a non-displayable JLabel) GraphicsConfiguration
        // are painted headless here. The remaining icons cast the Component to
        // a specific Swing type (JRadioButton/JCheckBox/JButton/JMenuItem/
        // JSlider/JInternalFrame) and are painted by their own per-class tests.
        Icon[] paintable = new Icon[] {
            MetalIconFactory.getFileChooserDetailViewIcon(),
            MetalIconFactory.getFileChooserHomeFolderIcon(),
            MetalIconFactory.getFileChooserListViewIcon(),
            MetalIconFactory.getFileChooserNewFolderIcon(),
            MetalIconFactory.getFileChooserUpFolderIcon(),
            MetalIconFactory.getTreeFolderIcon(),
            MetalIconFactory.getTreeLeafIcon(),
            MetalIconFactory.getTreeControlIcon(true),
            MetalIconFactory.getTreeControlIcon(false),
        };
        for (Icon icon : paintable) {
            final Icon ic = icon;
            assertNoThrow(() -> {
                BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
                ic.paintIcon(new JLabel(), img.createGraphics(), 0, 0);
            }, "paintIcon");
        }
    }
}
