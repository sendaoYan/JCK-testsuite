/*
 * @test
 * @summary javax.swing.plaf.basic constants + small data contracts: slider/scrollbar constants, combo renderer/editor, icon factory, arrow button, title pane (headless).
 * @run main BasicConstantsContractTest
 */

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.plaf.basic.BasicIconFactory;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicSliderUI;

/**
 * Deterministic, LaF-independent data of the basic package: documented
 * scroll-direction constants, the combo-box renderer/editor beans, the basic
 * menu icon kit, the four-direction arrow button and the internal-frame title
 * pane constructor.
 */
public class BasicConstantsContractTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testSliderConstants() {
        check(BasicSliderUI.POSITIVE_SCROLL == +1, "POSITIVE_SCROLL = +1");
        check(BasicSliderUI.NEGATIVE_SCROLL == -1, "NEGATIVE_SCROLL = -1");
        check(BasicSliderUI.MIN_SCROLL == -2, "MIN_SCROLL = -2");
        check(BasicSliderUI.MAX_SCROLL == +2, "MAX_SCROLL = +2");
        // the pair is ordered by distance from zero
        check(BasicSliderUI.MIN_SCROLL < BasicSliderUI.NEGATIVE_SCROLL
                && BasicSliderUI.NEGATIVE_SCROLL < BasicSliderUI.POSITIVE_SCROLL
                && BasicSliderUI.POSITIVE_SCROLL < BasicSliderUI.MAX_SCROLL,
                "scroll constants totally ordered");
    }

    /**
     * BasicScrollBarUI's highlight modes are protected static finals; a subclass
     * in this test exposes them without reflection.
     */
    static class ProbeScrollBarUI extends BasicScrollBarUI {
        static int noHighlight() { return NO_HIGHLIGHT; }
        static int decreaseHighlight() { return DECREASE_HIGHLIGHT; }
        static int increaseHighlight() { return INCREASE_HIGHLIGHT; }
    }

    static void testScrollBarConstants() {
        check(ProbeScrollBarUI.noHighlight() == 0, "NO_HIGHLIGHT = 0");
        check(ProbeScrollBarUI.decreaseHighlight() == 1, "DECREASE_HIGHLIGHT = 1");
        check(ProbeScrollBarUI.increaseHighlight() == 2, "INCREASE_HIGHLIGHT = 2");
    }

    static void testComboBoxRenderer() {
        BasicComboBoxRenderer r = new BasicComboBoxRenderer();
        check(r instanceof UIResource == false, "BasicComboBoxRenderer itself is not a UIResource");
        check(r.isOpaque(), "renderer is opaque");
        check(r.getBorder() != null, "renderer installs a no-focus border");
        JList<String> list = new JList<String>(new String[] { "a", "abc" });
        Object back = r.getListCellRendererComponent(list, "abc", 1, true, true);
        check(back == r, "getListCellRendererComponent returns this");
        check("abc".equals(r.getText()), "renderer text set from value");
        // selected+focused flag combination without throwing
        r.getListCellRendererComponent(list, null, 0, false, false);
        check("".equals(r.getText()), "null value renders as empty text");

        BasicComboBoxRenderer.UIResource ru = new BasicComboBoxRenderer.UIResource();
        check(ru instanceof UIResource, "renderer UIResource variant is a UIResource");
        check(ru instanceof BasicComboBoxRenderer, "UIResource variant is still a renderer");
        check(ru instanceof java.io.Serializable, "renderer is serializable (JLabel base)");
    }

    static void testComboBoxEditor() {
        BasicComboBoxEditor e = new BasicComboBoxEditor();
        check(e.getEditorComponent() instanceof javax.swing.JTextField,
                "editor component is a JTextField");
        javax.swing.JTextField field = (javax.swing.JTextField) e.getEditorComponent();
        check(field.isEditable(), "editor field is editable");
        e.setItem("hello");
        check("hello".equals(field.getText()), "setItem reaches the field");
        check("hello".equals(e.getItem()), "getItem reads back");
        e.setItem(null);
        check("".equals(field.getText()), "null item clears the field");
        BasicComboBoxEditor.UIResource eu = new BasicComboBoxEditor.UIResource();
        check(eu instanceof UIResource, "editor UIResource variant is a UIResource");
        // action notification: pressing Enter fires exactly one ActionEvent
        final int[] fired = { 0 };
        e.addActionListener(ev -> fired[0]++);
        field.setActionCommand("cmd");
        field.postActionEvent();
        check(fired[0] == 1, "Enter in the editor field fires one action");
    }

    static void testBasicIconFactory() {
        javax.swing.Icon arrow = BasicIconFactory.getMenuArrowIcon();
        check(arrow != null && arrow.getIconWidth() == 4 && arrow.getIconHeight() == 8,
                "menu arrow icon 4x8");
        javax.swing.Icon check = BasicIconFactory.getMenuItemCheckIcon();
        check(check != null && check.getIconWidth() == 9 && check.getIconHeight() == 9,
                "menu item check icon 9x9");
        javax.swing.Icon itemArrow = BasicIconFactory.getMenuItemArrowIcon();
        check(itemArrow != null && itemArrow.getIconWidth() == 4 && itemArrow.getIconHeight() == 8,
                "menu item arrow icon 4x8");
        // shared singletons across calls
        check(BasicIconFactory.getMenuArrowIcon() == BasicIconFactory.getMenuArrowIcon(),
                "menu arrow icon is a shared instance");
    }

    static void testArrowButton() throws Exception {
        UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        int[] dirs = { SwingConstants.NORTH, SwingConstants.SOUTH, SwingConstants.EAST, SwingConstants.WEST };
        for (int dir : dirs) {
            BasicArrowButton b = new BasicArrowButton(dir);
            check(b.getDirection() == dir, "direction round-trip " + dir);
            check(b.getPreferredSize().width > 0 && b.getPreferredSize().height > 0,
                    "arrow preferred size non-null");
        }
        BasicArrowButton b = new BasicArrowButton(SwingConstants.NORTH);
        b.setDirection(SwingConstants.SOUTH);
        check(b.getDirection() == SwingConstants.SOUTH, "setDirection round-trip");

        // deterministic paint: an opaque 16x16 arrow button fills every pixel
        b = new BasicArrowButton(SwingConstants.EAST);
        b.setSize(16, 16);
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        b.paint(img.getGraphics());
        int drawn = 0;
        for (int y = 0; y < 16; y++)
            for (int x = 0; x < 16; x++)
                if ((img.getRGB(x, y) >>> 24) != 0) drawn++;
        check(drawn == 256, "arrow button paints all 256 pixels of a 16x16 tile");
    }

    static void testInternalFrameTitlePane() {
        JInternalFrame f = new JInternalFrame("title");
        BasicInternalFrameTitlePane pane = new BasicInternalFrameTitlePane(f);
        check(pane != null && pane.getComponentCount() > 0, "title pane built with sub-components");
        check(pane.getPreferredSize() != null, "title pane preferred size non-null");
        check(pane instanceof javax.swing.JComponent, "title pane is a JComponent");
        // it is a JComponent usable headless and its size does not explode
        java.awt.Dimension p = pane.getPreferredSize();
        check(p.width >= 0 && p.height > 0, "title pane preferred height positive");
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        testSliderConstants();
        testScrollBarConstants();
        testComboBoxRenderer();
        testComboBoxEditor();
        testBasicIconFactory();
        testArrowButton();
        testInternalFrameTitlePane();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
