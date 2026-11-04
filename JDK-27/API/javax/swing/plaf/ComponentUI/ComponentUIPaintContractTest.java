/*
 * @test
 * @summary javax.swing.plaf paint-layer conformance under Metal: opaque fills, gradient presence, border rules, icon glyphs - all on BufferedImage (headless, no fonts in the assertions).
 * @run main ComponentUIPaintContractTest
 */

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalBorders;
import javax.swing.plaf.metal.MetalIconFactory;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicBorders;

/**
 * Paint-level contracts that are deterministic without a screen and without
 * font metrics: ComponentUI.update() background fill, opaque JPanel full fill,
 * Metal button gradient (opaque everywhere, several distinct theme colors,
 * fill differs from border rows), fixed-geometry border rules and icons.
 */
public class ComponentUIPaintContractTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static int drawn(BufferedImage img) {
        int n = 0;
        for (int y = 0; y < img.getHeight(); y++)
            for (int x = 0; x < img.getWidth(); x++)
                if ((img.getRGB(x, y) >>> 24) != 0) n++;
        return n;
    }

    static void testUpdateFill() {
        JComponent c = new JPanel();
        c.setSize(20, 10);
        c.setBackground(Color.RED);
        c.setOpaque(true);
        ComponentUI bare = new ComponentUI() {};
        BufferedImage img = new BufferedImage(20, 10, BufferedImage.TYPE_INT_ARGB);
        bare.update(img.getGraphics(), c);
        boolean allRed = true;
        for (int y = 0; y < 10; y++)
            for (int x = 0; x < 20; x++)
                if (img.getRGB(x, y) != 0xFFFF0000) allRed = false;
        check(allRed, "ComponentUI.update fills an opaque component exactly");

        c.setOpaque(false);
        BufferedImage img2 = new BufferedImage(20, 10, BufferedImage.TYPE_INT_ARGB);
        bare.update(img2.getGraphics(), c);
        check(drawn(img2) == 0, "ComponentUI.update leaves a non-opaque component empty");
    }

    static void testPanelFullFill() {
        JPanel p = new JPanel();
        p.setOpaque(true);
        p.setBackground(new Color(0, 255, 0));
        p.setSize(50, 30);
        BufferedImage img = new BufferedImage(50, 30, BufferedImage.TYPE_INT_ARGB);
        p.paint(img.getGraphics());
        boolean allGreen = true;
        for (int y = 0; y < 30; y++)
            for (int x = 0; x < 50; x++)
                if (img.getRGB(x, y) != 0xFF00FF00) allGreen = false;
        check(allGreen, "opaque JPanel paints a solid background tile");
    }

    static void testMetalButtonPaint() {
        // empty button: no text, no icon -> no font rendering in the tile
        JButton b = new JButton("");
        b.setOpaque(true);
        b.setSize(120, 60);
        BufferedImage img = new BufferedImage(120, 60, BufferedImage.TYPE_INT_ARGB);
        b.paint(img.getGraphics());
        boolean allOpaque = true;
        Set<Integer> colors = new HashSet<Integer>();
        for (int y = 0; y < 60; y += 2)
            for (int x = 0; x < 120; x += 2) {
                int rgb = img.getRGB(x, y);
                if ((rgb & 0xFF000000) != 0xFF000000) allOpaque = false;
                colors.add(rgb);
            }
        check(allOpaque, "Metal button paints an opaque tile");
        check(colors.size() > 5, "Metal button shows its multi-stop gradient: "
                + colors.size() + " distinct colors");
        check(img.getRGB(60, 30) != img.getRGB(60, 1),
                "fill colour differs from the border row");
        check(img.getRGB(60, 30) != img.getRGB(60, 58),
                "top and bottom gradient stops differ");
    }

    static void testBorderPaintRules() {
        // Basic menu bar border: exactly its 2-pixel bottom rule
        BasicBorders.MenuBarBorder mb =
                new BasicBorders.MenuBarBorder(Color.BLACK, Color.WHITE) { };
        BufferedImage img = new BufferedImage(40, 20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        mb.paintBorder(new JMenuBar(), g, 0, 0, 40, 20);
        g.dispose();
        check(drawn(img) == 80, "Basic MenuBarBorder paints exactly 2 rows of 40");
        check((img.getRGB(3, 17) >>> 24) == 0, "no pixels above the rule");

        // Metal button border ring: some border pixels drawn, interior untouched
        javax.swing.border.Border metal = MetalBorders.getButtonBorder();
        BufferedImage img2 = new BufferedImage(30, 20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img2.createGraphics();
        metal.paintBorder(new JButton(), g2, 0, 0, 30, 20);
        g2.dispose();
        int n = drawn(img2);
        check(n > 0, "Metal button border paints pixels");
        check(n < 30 * 20, "Metal border leaves interior unpainted");
        check((img2.getRGB(15, 10) >>> 24) == 0, "interior pixel untouched");
        check((img2.getRGB(0, 10) >>> 24) != 0, "edge pixel painted");
    }

    static void testArrowButtonAndIcons() {
        // Basic arrow button: opaque tile fully painted
        BasicArrowButton ab = new BasicArrowButton(javax.swing.SwingConstants.EAST);
        ab.setSize(16, 16);
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        ab.paint(img.getGraphics());
        check(drawn(img) == 256, "BasicArrowButton paints an opaque 16x16 tile");

        // Metal checkbox icon: solid 13x13 tile (background + box outline)
        javax.swing.Icon cb = MetalIconFactory.getCheckBoxIcon();
        BufferedImage img2 = new BufferedImage(13, 13, BufferedImage.TYPE_INT_ARGB);
        cb.paintIcon(new JCheckBox(), img2.getGraphics(), 0, 0);
        check(drawn(img2) == 169, "MetalCheckBoxIcon fills its 13x13 tile");

        // Metal internal-frame close icon: opaque widget
        javax.swing.Icon close = MetalIconFactory.getInternalFrameCloseIcon(16);
        BufferedImage img3 = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        close.paintIcon(new JButton(), img3.getGraphics(), 0, 0);
        check(drawn(img3) > 0, "internal frame close icon visible");
    }

    static void testLayerPaint() {
        // an opaque JLayer paints its background through the default LayerUI
        LayerUIProbe<JPanel> ui = new LayerUIProbe<JPanel>();
        JLayer<JPanel> layer = new JLayer<JPanel>(new JPanel(), ui);
        layer.setBackground(Color.CYAN);
        layer.setOpaque(true);
        layer.setSize(30, 20);
        BufferedImage img = new BufferedImage(30, 20, BufferedImage.TYPE_INT_ARGB);
        layer.paint(img.getGraphics());
        check(drawn(img) > 0, "opaque JLayer paints through the default LayerUI");
    }

    static class LayerUIProbe<V extends java.awt.Component> extends javax.swing.plaf.LayerUI<V> { }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        MetalLookAndFeel.setCurrentTheme(new OceanTheme());
        try {
            testUpdateFill();
            SwingUtilities.invokeAndWait(() -> {
                testPanelFullFill();
                testMetalButtonPaint();
                testBorderPaintRules();
                testArrowButtonAndIcons();
                testLayerPaint();
            });
        } finally {
            MetalLookAndFeel.setCurrentTheme(new OceanTheme());
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
