/*
 * @test
 * @summary javax.swing.plaf.metal.MetalIconFactory conformance: every icon factory non-null with exact dimensions; fixed-geometry paint (headless).
 * @run main MetalIconFactoryContractTest
 */

import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.UIManager;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalIconFactory;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;

/**
 * MetalIconFactory vends the icon kit of the Metal look-and-feel. Icon
 * dimensions are compile-time constants of the theme, so they are locked
 * exactly; painting is plain line/box geometry (no fonts) and is verified on
 * a BufferedImage.
 */
public class MetalIconFactoryContractTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void icon(String what, Icon icon, int w, int h) {
        check(icon != null, what + " non-null");
        check(icon.getIconWidth() == w, what + " width " + w + " (got " + icon.getIconWidth() + ")");
        check(icon.getIconHeight() == h, what + " height " + h + " (got " + icon.getIconHeight() + ")");
    }

    static void testDimensions() {
        // boolean pair used by getTreeControlIcon
        check(MetalIconFactory.DARK == false, "DARK = false");
        check(MetalIconFactory.LIGHT == true, "LIGHT = true");

        icon("radioButtonIcon", MetalIconFactory.getRadioButtonIcon(), 13, 13);
        icon("checkBoxIcon", MetalIconFactory.getCheckBoxIcon(), 13, 13);
        icon("treeFolderIcon", MetalIconFactory.getTreeFolderIcon(), 16, 18);
        icon("treeLeafIcon", MetalIconFactory.getTreeLeafIcon(), 16, 20);
        icon("treeComputerIcon", MetalIconFactory.getTreeComputerIcon(), 16, 16);
        icon("treeHardDriveIcon", MetalIconFactory.getTreeHardDriveIcon(), 16, 16);
        icon("treeFloppyDriveIcon", MetalIconFactory.getTreeFloppyDriveIcon(), 16, 16);
        icon("fileChooserDetailViewIcon", MetalIconFactory.getFileChooserDetailViewIcon(), 18, 18);
        icon("fileChooserHomeFolderIcon", MetalIconFactory.getFileChooserHomeFolderIcon(), 18, 18);
        icon("fileChooserListViewIcon", MetalIconFactory.getFileChooserListViewIcon(), 18, 18);
        icon("fileChooserNewFolderIcon", MetalIconFactory.getFileChooserNewFolderIcon(), 18, 18);
        icon("fileChooserUpFolderIcon", MetalIconFactory.getFileChooserUpFolderIcon(), 18, 18);
        icon("internalFrameDefaultMenuIcon", MetalIconFactory.getInternalFrameDefaultMenuIcon(), 16, 16);
        icon("menuArrowIcon", MetalIconFactory.getMenuArrowIcon(), 4, 8);
        icon("menuItemArrowIcon", MetalIconFactory.getMenuItemArrowIcon(), 4, 8);
        icon("checkBoxMenuItemIcon", MetalIconFactory.getCheckBoxMenuItemIcon(), 10, 10);
        icon("radioButtonMenuItemIcon", MetalIconFactory.getRadioButtonMenuItemIcon(), 10, 10);
    }

    static void testParametrizedIcons() {
        // internal-frame widget icons honour the requested size
        for (int size : new int[] { 12, 16, 20 }) {
            icon("internalFrameCloseIcon(" + size + ")",
                    MetalIconFactory.getInternalFrameCloseIcon(size), size, size);
            icon("internalFrameMaximizeIcon(" + size + ")",
                    MetalIconFactory.getInternalFrameMaximizeIcon(size), size, size);
            icon("internalFrameMinimizeIcon(" + size + ")",
                    MetalIconFactory.getInternalFrameMinimizeIcon(size), size, size);
            icon("internalFrameAltMaximizeIcon(" + size + ")",
                    MetalIconFactory.getInternalFrameAltMaximizeIcon(size), size, size);
        }
        icon("treeControlIcon(collapsed)", MetalIconFactory.getTreeControlIcon(false), 18, 18);
        icon("treeControlIcon(expanded)", MetalIconFactory.getTreeControlIcon(true), 18, 18);
    }

    static void testThemeDependentSliderIcons() {
        try {
            // Ocean theme: dedicated Ocean widget icons
            MetalLookAndFeel.setCurrentTheme(new OceanTheme());
            Icon oceanH = MetalIconFactory.getHorizontalSliderThumbIcon();
            Icon oceanV = MetalIconFactory.getVerticalSliderThumbIcon();
            icon("ocean horizontalSliderThumbIcon", oceanH, 15, 16);
            icon("ocean verticalSliderThumbIcon", oceanV, 16, 15);
            // Steel theme: the classic widget icon set
            MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
            Icon steelH = MetalIconFactory.getHorizontalSliderThumbIcon();
            Icon steelV = MetalIconFactory.getVerticalSliderThumbIcon();
            icon("steel horizontalSliderThumbIcon", steelH, 15, 16);
            icon("steel verticalSliderThumbIcon", steelV, 16, 15);
            check(oceanH != steelH, "slider thumb icon instance follows the theme");
        } finally {
            MetalLookAndFeel.setCurrentTheme(new OceanTheme());
        }
    }

    /** Painting is font-free geometry: the checkbox icon fills its whole tile. */
    static void testPaintGeometry() {
        Icon cb = MetalIconFactory.getCheckBoxIcon();
        BufferedImage img = new BufferedImage(cb.getIconWidth(), cb.getIconHeight(),
                BufferedImage.TYPE_INT_ARGB);
        cb.paintIcon(new JCheckBox(), img.getGraphics(), 0, 0);
        int drawn = 0;
        for (int y = 0; y < img.getHeight(); y++)
            for (int x = 0; x < img.getWidth(); x++)
                if ((img.getRGB(x, y) >>> 24) != 0) drawn++;
        check(drawn == cb.getIconWidth() * cb.getIconHeight(),
                "checkBoxIcon paints every pixel of its 13x13 tile");

        Icon close = MetalIconFactory.getInternalFrameCloseIcon(16);
        BufferedImage img2 = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        close.paintIcon(new javax.swing.JButton(), img2.getGraphics(), 0, 0);
        int drawn2 = 0;
        for (int y = 0; y < 16; y++)
            for (int x = 0; x < 16; x++)
                if ((img2.getRGB(x, y) >>> 24) != 0) drawn2++;
        check(drawn2 > 0, "close icon paints a visible glyph");

        Icon leaf = MetalIconFactory.getTreeLeafIcon();
        BufferedImage img3 = new BufferedImage(leaf.getIconWidth(), leaf.getIconHeight(),
                BufferedImage.TYPE_INT_ARGB);
        leaf.paintIcon(new JCheckBox(), img3.getGraphics(), 0, 0);
        int drawn3 = 0;
        for (int y = 0; y < img3.getHeight(); y++)
            for (int x = 0; x < img3.getWidth(); x++)
                if ((img3.getRGB(x, y) >>> 24) != 0) drawn3++;
        check(drawn3 > 0, "tree leaf icon paints a visible glyph");
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        testDimensions();
        testParametrizedIcons();
        testThemeDependentSliderIcons();
        testPaintGeometry();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
