/*
 * @test
 * @summary javax.swing.plaf.metal.MetalBorders conformance: factory types, nested border insets, UIResource marking (headless, Metal LaF).
 * @run main MetalBordersContractTest
 */

import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.metal.MetalBorders;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;
import javax.swing.table.JTableHeader;
import javax.swing.JTextField;

/**
 * MetalBorders is the border kit of the Metal look-and-feel. Locked: the five
 * public static factory methods' return types, the exact insets of every
 * publicly constructible nested border on its matching component, and the
 * UIResource marking that lets UIManager clear LaF-installed borders.
 */
public class MetalBordersContractTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean insetsEq(Insets in, int t, int l, int b, int r) {
        return in != null && in.top == t && in.left == l && in.bottom == b && in.right == r;
    }

    static void testFactories() {
        Border button = MetalBorders.getButtonBorder();
        check(button instanceof BorderUIResource.CompoundBorderUIResource,
                "getButtonBorder -> CompoundBorderUIResource");
        Border text = MetalBorders.getTextBorder();
        check(text instanceof BorderUIResource.CompoundBorderUIResource,
                "getTextBorder -> CompoundBorderUIResource");
        Border textField = MetalBorders.getTextFieldBorder();
        check(textField instanceof UIResource, "getTextFieldBorder is UIResource");
        Border toggle = MetalBorders.getToggleButtonBorder();
        check(toggle instanceof UIResource, "getToggleButtonBorder is UIResource");
        Border desktop = MetalBorders.getDesktopIconBorder();
        check(desktop instanceof UIResource, "getDesktopIconBorder is UIResource");
        // the factory vends a cached compound instance
        check(MetalBorders.getButtonBorder() == button, "factory returns a cached compound instance");
    }

    static void testFactoryInsets() {
        JButton b = new JButton();
        b.updateUI();
        check(insetsEq(MetalBorders.getButtonBorder().getBorderInsets(b), 5, 17, 5, 17),
                "button border insets on button (Metal border 3,3,3,3 + margin 2,14): "
                        + MetalBorders.getButtonBorder().getBorderInsets(b));
        JToggleButton tb = new JToggleButton();
        tb.updateUI();
        check(insetsEq(MetalBorders.getToggleButtonBorder().getBorderInsets(tb), 5, 17, 5, 17),
                "toggle border insets on toggle button");
        JTextField tf = new JTextField();
        tf.updateUI();
        check(insetsEq(MetalBorders.getTextFieldBorder().getBorderInsets(tf), 2, 2, 2, 2),
                "text field border insets (2,2,2,2)");
        // label-like component: the text border reads the component margin
        JButton host = new JButton();
        host.updateUI();
        check(insetsEq(MetalBorders.getTextBorder().getBorderInsets(host), 4, 16, 4, 16),
                "text border insets on button host: " + MetalBorders.getTextBorder().getBorderInsets(host));
    }

    static void testNestedBorderInsets() {
        JButton host = new JButton();
        host.updateUI();
        // host component is irrelevant for these geometry constants
        check(insetsEq(new MetalBorders.ButtonBorder().getBorderInsets(host), 3, 3, 3, 3),
                "MetalBorders.ButtonBorder (3,3,3,3)");
        check(insetsEq(new MetalBorders.Flush3DBorder().getBorderInsets(host), 2, 2, 2, 2),
                "Flush3DBorder (2,2,2,2)");
        // JDK-27 trimmed the internal-frame border by one pixel on every side
        check(insetsEq(new MetalBorders.InternalFrameBorder().getBorderInsets(new JInternalFrame()), 4, 4, 4, 4),
                "InternalFrameBorder (4,4,4,4) [JDK-27; JDK-8 was (5,5,5,5)]");
        check(insetsEq(new MetalBorders.MenuBarBorder().getBorderInsets(new JMenuBar()), 0, 0, 2, 0),
                "Metal MenuBarBorder (0,0,2,0)");
        check(insetsEq(new MetalBorders.ToolBarBorder().getBorderInsets(new javax.swing.JToolBar()), 1, 16, 3, 2),
                "ToolBarBorder (1,16,3,2) on horizontal tool bar");
        check(insetsEq(new MetalBorders.MenuItemBorder().getBorderInsets(new JMenuItem()), 2, 2, 2, 2),
                "MenuItemBorder (2,2,2,2)");
        check(insetsEq(new MetalBorders.PopupMenuBorder().getBorderInsets(new JPopupMenu()), 3, 1, 2, 1),
                "PopupMenuBorder (3,1,2,1)");
        check(insetsEq(new MetalBorders.RolloverButtonBorder().getBorderInsets(host), 3, 3, 3, 3),
                "RolloverButtonBorder (3,3,3,3)");
        check(insetsEq(new MetalBorders.ScrollPaneBorder().getBorderInsets(new JScrollPane()), 1, 1, 2, 2),
                "ScrollPaneBorder (1,1,2,2)");
        // JDK-27 made the table header border symmetric on the right edge
        check(insetsEq(new MetalBorders.TableHeaderBorder().getBorderInsets(new JTableHeader()), 2, 2, 2, 2),
                "TableHeaderBorder (2,2,2,2) [JDK-27; JDK-8 was (2,2,2,0)]");
        check(insetsEq(new MetalBorders.TextFieldBorder().getBorderInsets(new JTextField()), 2, 2, 2, 2),
                "Metal TextFieldBorder (2,2,2,2)");
        check(insetsEq(new MetalBorders.ToggleButtonBorder().getBorderInsets(new JToggleButton()), 3, 3, 3, 3),
                "Metal ToggleButtonBorder (3,3,3,3)");
        check(insetsEq(new MetalBorders.OptionDialogBorder().getBorderInsets(host), 3, 3, 3, 3),
                "OptionDialogBorder (3,3,3,3)");
        check(insetsEq(new MetalBorders.PaletteBorder().getBorderInsets(host), 1, 1, 1, 1),
                "PaletteBorder (1,1,1,1)");
    }

    static void testTypeShape() {
        check(new MetalBorders.ButtonBorder() instanceof UIResource, "ButtonBorder is UIResource");
        check(new MetalBorders.Flush3DBorder() instanceof UIResource, "Flush3DBorder is UIResource");
        check(new MetalBorders.TextFieldBorder() instanceof UIResource, "TextFieldBorder is UIResource");
        check(new MetalBorders.TextFieldBorder() instanceof MetalBorders.Flush3DBorder,
                "TextFieldBorder refines Flush3DBorder");
        check(new MetalBorders.ToggleButtonBorder() instanceof MetalBorders.ButtonBorder,
                "ToggleButtonBorder refines ButtonBorder");
        check(new MetalBorders.RolloverButtonBorder() instanceof MetalBorders.ButtonBorder,
                "RolloverButtonBorder refines ButtonBorder");
        check(new MetalBorders.TableHeaderBorder() instanceof UIResource == false,
                "TableHeaderBorder is not a UIResource");
        check(new MetalBorders.ToolBarBorder() instanceof javax.swing.SwingConstants,
                "ToolBarBorder knows the compass constants");
        // the metal compound borders wrap the plain Swing compound contract
        Border button = MetalBorders.getButtonBorder();
        check(button instanceof CompoundBorder, "factory border is a CompoundBorder");
        CompoundBorder cb = (CompoundBorder) button;
        check(cb.getOutsideBorder() != null && cb.getInsideBorder() != null,
                "compound has both outside and inside borders");
        int t = cb.getOutsideBorder().getBorderInsets(null).top
                + cb.getInsideBorder().getBorderInsets(null).top;
        check(cb.getBorderInsets(null).top == t, "compound insets sum over members");
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        try {
            testFactories();
            testFactoryInsets();
            testNestedBorderInsets();
            testTypeShape();
        } finally {
            MetalLookAndFeel.setCurrentTheme(new OceanTheme());
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
