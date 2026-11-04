/*
 * @test
 * @summary javax.swing.plaf.basic Basic*UI delegates: createUI factories, install/uninstall idempotency, size contracts (headless, Metal LaF installed).
 * @run main BasicUIInstallTest
 */

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFormattedTextField;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JToolTip;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.TextUI;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicEditorPaneUI;
import javax.swing.plaf.basic.BasicFormattedTextFieldUI;
import javax.swing.plaf.basic.BasicLabelUI;
import javax.swing.plaf.basic.BasicListUI;
import javax.swing.plaf.basic.BasicMenuBarUI;
import javax.swing.plaf.basic.BasicMenuItemUI;
import javax.swing.plaf.basic.BasicOptionPaneUI;
import javax.swing.plaf.basic.BasicPanelUI;
import javax.swing.plaf.basic.BasicPasswordFieldUI;
import javax.swing.plaf.basic.BasicProgressBarUI;
import javax.swing.plaf.basic.BasicRadioButtonUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicSeparatorUI;
import javax.swing.plaf.basic.BasicSliderUI;
import javax.swing.plaf.basic.BasicSpinnerUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.plaf.basic.BasicTextAreaUI;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.plaf.basic.BasicTextPaneUI;
import javax.swing.plaf.basic.BasicToggleButtonUI;
import javax.swing.plaf.basic.BasicToolBarSeparatorUI;
import javax.swing.plaf.basic.BasicToolBarUI;
import javax.swing.plaf.basic.BasicToolTipUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.plaf.basic.BasicViewportUI;

/**
 * The BasicXxxUI delegates can be instantiated directly and driven against a
 * real component even though a Metal look-and-feel is installed (installUI
 * works on any matching component regardless of which LaF is current).
 * Locked: (a) every public static createUI factory returns a delegate of the
 * right family, (b) shared vs. per-component instances, (c) install/uninstall
 * round-trips keep size queries resolvable, (d) typing rules (installing on the
 * wrong component class fails fast), (e) the fixed JScrollBar maximum size.
 */
public class BasicUIInstallTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface Sup { JComponent make(); }

    static ComponentUI create(Sup sup, Class<?> factory) throws Exception {
        JComponent c = sup.make();
        return (ComponentUI) factory.getMethod("createUI", JComponent.class).invoke(null, c);
    }

    static void testCreateUIFactories() throws Exception {
        Object[][] cases = {
                { (Sup) () -> new JButton(), BasicButtonUI.class, "ButtonUI" },
                { (Sup) () -> new JToggleButton(), BasicToggleButtonUI.class, "ToggleButtonUI" },
                { (Sup) () -> new JRadioButton(), BasicRadioButtonUI.class, "RadioButtonUI" },
                { (Sup) () -> new JCheckBox(), BasicButtonUI.class, "CheckBox falls back to Button family" },
                { (Sup) () -> new JLabel("x"), BasicLabelUI.class, "LabelUI" },
                { (Sup) () -> new JList<String>(), BasicListUI.class, "ListUI" },
                { (Sup) () -> new JMenuBar(), BasicMenuBarUI.class, "MenuBarUI" },
                { (Sup) () -> new JMenuItem(), BasicMenuItemUI.class, "MenuItemUI" },
                { (Sup) () -> new JOptionPane(), BasicOptionPaneUI.class, "OptionPaneUI" },
                { (Sup) () -> new JPanel(), BasicPanelUI.class, "PanelUI" },
                { (Sup) () -> new JPasswordField(), BasicPasswordFieldUI.class, "PasswordFieldUI" },
                { (Sup) () -> new JProgressBar(), BasicProgressBarUI.class, "ProgressBarUI" },
                { (Sup) () -> new JScrollBar(), BasicScrollBarUI.class, "ScrollBarUI" },
                { (Sup) () -> new JSeparator(), BasicSeparatorUI.class, "SeparatorUI" },
                { (Sup) () -> new JSlider(), BasicSliderUI.class, "SliderUI" },
                { (Sup) () -> new JSpinner(), BasicSpinnerUI.class, "SpinnerUI" },
                { (Sup) () -> new JTabbedPane(), BasicTabbedPaneUI.class, "TabbedPaneUI" },
                { (Sup) () -> new JTextField(), BasicTextFieldUI.class, "TextFieldUI" },
                { (Sup) () -> new JTextArea(), BasicTextAreaUI.class, "TextAreaUI" },
                { (Sup) () -> new JTextPane(), BasicTextPaneUI.class, "TextPaneUI" },
                { (Sup) () -> new JEditorPane(), BasicEditorPaneUI.class, "EditorPaneUI" },
                { (Sup) () -> new JFormattedTextField(), BasicFormattedTextFieldUI.class, "FormattedTextFieldUI" },
                { (Sup) () -> new JToolBar(), BasicToolBarUI.class, "ToolBarUI" },
                { (Sup) () -> { JToolBar t = new JToolBar(); javax.swing.JToolBar.Separator sep = new javax.swing.JToolBar.Separator(); t.add(sep); return sep; }, BasicToolBarSeparatorUI.class, "ToolBarSeparatorUI" },
                { (Sup) () -> new JToolTip(), BasicToolTipUI.class, "ToolTipUI" },
                { (Sup) () -> new JTree(), BasicTreeUI.class, "TreeUI" },
                { (Sup) () -> new JViewport(), BasicViewportUI.class, "ViewportUI" },
        };
        for (Object[] row : cases) {
            Sup sup = (Sup) row[0];
            Class<?> factory = (Class<?>) row[1];
            String label = (String) row[2];
            ComponentUI ui = create(sup, factory);
            check(ui != null, label + ": createUI non-null");
            check(factory.isInstance(ui), label + ": createUI returns " + factory.getSimpleName());
            check(ui instanceof ComponentUI, label + ": createUI returns a ComponentUI");
        }
    }

    static void testSharedVsPerComponentInstances() throws Exception {
        // stateless delegates share one instance across createUI calls
        check(BasicButtonUI.createUI(new JButton()) == BasicButtonUI.createUI(new JButton()),
                "BasicButtonUI.createUI shares a stateless instance");
        check(BasicLabelUI.createUI(new JLabel()) == BasicLabelUI.createUI(new JLabel()),
                "BasicLabelUI.createUI shares a stateless instance");
        check(BasicPanelUI.createUI(new JPanel()) == BasicPanelUI.createUI(new JPanel()),
                "BasicPanelUI.createUI shares a stateless instance");
        // stateful delegates mint a fresh instance per component
        check(BasicTextFieldUI.createUI(new JTextField()) != BasicTextFieldUI.createUI(new JTextField()),
                "BasicTextFieldUI.createUI is per-component");
        check(BasicScrollBarUI.createUI(new JScrollBar()) != BasicScrollBarUI.createUI(new JScrollBar()),
                "BasicScrollBarUI.createUI is per-component");
        check(BasicSliderUI.createUI(new JSlider()) != BasicSliderUI.createUI(new JSlider()),
                "BasicSliderUI.createUI is per-component");
    }

    static void testInstallUninstallRoundTrip() {
        // BasicButtonUI on a real button
        JButton b = new JButton();
        BasicButtonUI ui = new BasicButtonUI();
        ui.installUI(b);
        Dimension pref = ui.getPreferredSize(b);
        check(pref != null && pref.width > 0 && pref.height > 0, "button preferred size after install");
        check(ui.getMinimumSize(b) != null, "button minimum size after install");
        check(ui.getMaximumSize(b) != null, "button maximum size after install");
        check(ui.getDefaultTextIconGap(b) >= 0, "defaultTextIconGap non-negative");
        ui.uninstallUI(b);
        ui.installUI(b);
        check(ui.getPreferredSize(b) != null, "re-install after uninstall keeps sizes resolvable");
        b.updateUI();
        check(b.getUI() instanceof javax.swing.plaf.ButtonUI,
                "component recovers a proper delegate via updateUI");

        // installUI on a component of the wrong kind fails fast with ClassCastException
        try {
            new BasicButtonUI().installUI(new JLabel("x"));
            check(false, "BasicButtonUI.installUI on JLabel must throw CCE");
        } catch (ClassCastException expected) {
            check(true, "BasicButtonUI.installUI on JLabel throws CCE");
        }

        // BasicLabelUI round-trip
        JLabel l = new JLabel("x");
        BasicLabelUI lui = new BasicLabelUI();
        lui.installUI(l);
        check(lui.getPreferredSize(l) != null, "label preferred size after install");
        lui.uninstallUI(l);
        lui.installUI(l);
        check(lui.getPreferredSize(l) != null, "label re-install keeps sizes resolvable");

        // BasicTextFieldUI round-trip (TextUI family)
        JTextField tf = new JTextField("abc");
        BasicTextFieldUI tui = new BasicTextFieldUI();
        tui.installUI(tf);
        check(tui instanceof TextUI, "BasicTextFieldUI is a TextUI");
        check(tui.getPreferredSize(tf) != null, "text preferred size after install");
        tui.uninstallUI(tf);
        tui.installUI(tf);
        check(tui.getPreferredSize(tf) != null, "text re-install keeps sizes resolvable");

        // BasicScrollBarUI: fixed maximum size, layout manager contract
        JScrollBar sb = new JScrollBar();
        BasicScrollBarUI sui = new BasicScrollBarUI();
        sui.installUI(sb);
        Dimension max = sui.getMaximumSize(sb);
        check(max.width == Integer.MAX_VALUE && max.height == Integer.MAX_VALUE,
                "scrollbar maximum size is Integer.MAX_VALUE pair");
        Dimension spref = sui.getPreferredSize(sb);
        check(spref != null && spref.width > 0 && spref.height > 0, "scrollbar preferred size");
        check(sui.preferredLayoutSize(sb) != null, "preferredLayoutSize as LayoutManager");
        check(sui.minimumLayoutSize(sb) != null, "minimumLayoutSize as LayoutManager");
        check(sui.getSupportsAbsolutePositioning(), "Metal/basic scrollbar supports absolute positioning");
        sb.setSize(20, 100);
        sui.layoutContainer(sb);
        check(true, "layoutContainer with fixed size does not throw");
        sui.uninstallUI(sb);
        sui.installUI(sb);
        check(sui.getPreferredSize(sb) != null, "scrollbar re-install keeps sizes resolvable");

        // BasicSliderUI: constructed with its slider, install/uninstall round-trip
        JSlider sl = new JSlider();
        BasicSliderUI slui = new BasicSliderUI(sl);
        slui.installUI(sl);
        check(slui.getPreferredSize(sl) != null, "slider preferred size after install");
        check(slui.getPreferredHorizontalSize() != null, "slider preferred horizontal size");
        check(slui.getPreferredVerticalSize() != null, "slider preferred vertical size");
        check(slui.getMinimumHorizontalSize() != null, "slider minimum horizontal size");
        check(slui.getMinimumVerticalSize() != null, "slider minimum vertical size");
        check(slui.valueForXPosition(0) >= sl.getMinimum(), "valueForXPosition bounded");
        slui.uninstallUI(sl);
        slui.installUI(sl);
        check(slui.getPreferredSize(sl) != null, "slider re-install keeps sizes resolvable");
    }

    static void testInternalFrameUI() throws Exception {
        // BasicInternalFrameUI installs on a real internal frame headless
        JInternalFrame f = new JInternalFrame("t");
        javax.swing.plaf.basic.BasicInternalFrameUI fui =
                new javax.swing.plaf.basic.BasicInternalFrameUI(f);
        fui.installUI(f);
        check(true, "BasicInternalFrameUI.installUI no-throw on a real frame");
        fui.uninstallUI(f);
        check(true, "BasicInternalFrameUI.uninstallUI no-throw");
        fui.installUI(f);
        check(true, "BasicInternalFrameUI re-install no-throw");
        f.updateUI();
        check(f.getUI() instanceof javax.swing.plaf.InternalFrameUI,
                "frame recovers a proper delegate via updateUI");

        // BasicDesktopIconUI on the frame's desktop icon
        javax.swing.plaf.basic.BasicDesktopIconUI diui =
                new javax.swing.plaf.basic.BasicDesktopIconUI();
        diui.installUI(f.getDesktopIcon());
        check(diui.getPreferredSize(f.getDesktopIcon()) != null,
                "desktop icon preferred size after install");
        diui.uninstallUI(f.getDesktopIcon());
        diui.installUI(f.getDesktopIcon());
        check(diui.getPreferredSize(f.getDesktopIcon()) != null,
                "desktop icon re-install resolvable");
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        try {
            testCreateUIFactories();
            testSharedVsPerComponentInstances();
            SwingUtilities.invokeAndWait(() -> testInstallUninstallRoundTrip());
            SwingUtilities.invokeAndWait(() -> {
                try {
                    testInternalFrameUI();
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
            });
        } finally {
            javax.swing.plaf.metal.MetalLookAndFeel.setCurrentTheme(
                    new javax.swing.plaf.metal.OceanTheme());
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
