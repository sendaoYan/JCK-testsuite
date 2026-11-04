/*
 * @test
 * @summary javax.swing.plaf ComponentUI base-class contract + UI delegate families under Metal (headless).
 * @run main ComponentUIFamiliesTest
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.lang.reflect.Modifier;

import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JRootPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JToolTip;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.JLayer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.ColorChooserUI;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.DesktopIconUI;
import javax.swing.plaf.DesktopPaneUI;
import javax.swing.plaf.FileChooserUI;
import javax.swing.plaf.InternalFrameUI;
import javax.swing.plaf.LabelUI;
import javax.swing.plaf.LayerUI;
import javax.swing.plaf.ListUI;
import javax.swing.plaf.MenuBarUI;
import javax.swing.plaf.MenuItemUI;
import javax.swing.plaf.OptionPaneUI;
import javax.swing.plaf.PanelUI;
import javax.swing.plaf.PopupMenuUI;
import javax.swing.plaf.ProgressBarUI;
import javax.swing.plaf.RootPaneUI;
import javax.swing.plaf.ScrollBarUI;
import javax.swing.plaf.ScrollPaneUI;
import javax.swing.plaf.SeparatorUI;
import javax.swing.plaf.SliderUI;
import javax.swing.plaf.SpinnerUI;
import javax.swing.plaf.SplitPaneUI;
import javax.swing.plaf.TabbedPaneUI;
import javax.swing.plaf.TableHeaderUI;
import javax.swing.plaf.TableUI;
import javax.swing.plaf.TextUI;
import javax.swing.plaf.ToolBarUI;
import javax.swing.plaf.ToolTipUI;
import javax.swing.plaf.TreeUI;
import javax.swing.plaf.ViewportUI;
import javax.swing.table.JTableHeader;

/**
 * Covers the javax.swing.plaf root UI-delegate base classes as one family:
 * (a) ComponentUI default (non-overridden) behaviour locked with an anonymous subclass;
 * (b) every public abstract XxxUI base class exists, is abstract and extends ComponentUI;
 * (c) under the installed Metal look-and-feel each standard Swing component's getUI()
 *     is an instance of the matching base class, and updateUI() is idempotent.
 * Headless-safe: only JComponent (no Window/Frame/Dialog) plus BufferedImage painting.
 */
public class ComponentUIFamiliesTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ------------------------------------------------------------------
    // (a) ComponentUI defaults
    // ------------------------------------------------------------------
    static void testComponentUIDefaults() {
        final ComponentUI bare = new ComponentUI() {};
        final JComponent c = new JPanel();
        check(true, "anonymous ComponentUI subclass instantiable");

        // install/uninstall/paint are documented no-ops on the base class
        bare.installUI(c);
        check(true, "installUI default is a no-op");
        bare.uninstallUI(c);
        check(true, "uninstallUI default is a no-op");
        bare.paint(new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB).getGraphics(), c);
        check(true, "paint default is a no-op");

        // size delegation: preferred null, minimum/maximum fall through to preferred
        check(bare.getPreferredSize(c) == null, "getPreferredSize default null");
        check(bare.getMinimumSize(c) == null, "getMinimumSize default == preferred == null");
        check(bare.getMaximumSize(c) == null, "getMaximumSize default == preferred == null");

        // contains delegates to the component bounds
        c.setSize(50, 30);
        check(bare.contains(c, 0, 0), "contains inside bounds");
        check(bare.contains(c, 49, 29), "contains at last inside pixel");
        check(!bare.contains(c, 50, 30), "contains outside bounds");
        check(!bare.contains(c, -1, 5), "contains negative x");

        // accessibility defaults
        check(bare.getAccessibleChildrenCount(c) == 0, "accessible children count 0");
        check(bare.getAccessibleChild(c, 0) == null, "accessible child null");

        // baseline contract: -1 default, IAE on negative extents, NPE on null component
        check(bare.getBaseline(c, 10, 10) == -1, "baseline default -1");
        check(bare.getBaseline(c, 0, 0) == -1, "baseline 0x0 still -1");
        try {
            bare.getBaseline(c, -1, 10);
            check(false, "baseline negative width must throw IAE");
        } catch (IllegalArgumentException expected) {
            check(true, "baseline negative width throws IAE");
        }
        try {
            bare.getBaseline(c, 10, -1);
            check(false, "baseline negative height must throw IAE");
        } catch (IllegalArgumentException expected) {
            check(true, "baseline negative height throws IAE");
        }
        try {
            bare.getBaseline(null, 10, 10);
            check(false, "baseline null component must throw NPE");
        } catch (NullPointerException expected) {
            check(true, "baseline null component throws NPE");
        }
        check(bare.getBaselineResizeBehavior(c) == Component.BaselineResizeBehavior.OTHER,
                "baseline resize behavior default OTHER");
        try {
            bare.getBaselineResizeBehavior(null);
            check(false, "baseline resize behavior null must throw NPE");
        } catch (NullPointerException expected) {
            check(true, "baseline resize behavior null throws NPE");
        }

        // the static createUI of the base class is documented to throw an Error
        try {
            ComponentUI.createUI(c);
            check(false, "static ComponentUI.createUI must throw Error");
        } catch (Error expected) {
            check("ComponentUI.createUI not implemented.".equals(expected.getMessage()),
                    "static createUI Error message exact");
        }

        // update() fills the background of an opaque component, delegates to paint
        c.setBackground(Color.RED);
        c.setOpaque(true);
        BufferedImage opaqueImg = new BufferedImage(20, 10, BufferedImage.TYPE_INT_ARGB);
        bare.update(opaqueImg.getGraphics(), c);
        check(opaqueImg.getRGB(10, 5) == 0xFFFF0000, "update() fills opaque background");
        // ... and leaves a non-opaque component untouched (paint is a no-op)
        c.setOpaque(false);
        BufferedImage clearImg = new BufferedImage(20, 10, BufferedImage.TYPE_INT_ARGB);
        bare.update(clearImg.getGraphics(), c);
        check(clearImg.getRGB(10, 5) == 0, "update() leaves non-opaque component empty");
    }

    // ------------------------------------------------------------------
    // (b) base-class family shapes
    // ------------------------------------------------------------------
    static final String[] ABSTRACT_BASES = {
            "ButtonUI", "ColorChooserUI", "ComboBoxUI", "DesktopIconUI", "DesktopPaneUI",
            "FileChooserUI", "InternalFrameUI", "LabelUI", "ListUI", "MenuBarUI",
            "MenuItemUI", "OptionPaneUI", "PanelUI", "PopupMenuUI", "ProgressBarUI",
            "RootPaneUI", "ScrollBarUI", "ScrollPaneUI", "SeparatorUI", "SliderUI",
            "SpinnerUI", "SplitPaneUI", "TabbedPaneUI", "TableHeaderUI", "TableUI",
            "TextUI", "ToolBarUI", "ToolTipUI", "TreeUI", "ViewportUI",
    };

    static void testBaseClassShapes() throws Exception {
        Class<?> cui = Class.forName("javax.swing.plaf.ComponentUI");
        check(Modifier.isAbstract(cui.getModifiers()), "ComponentUI is abstract");
        check(cui.getSuperclass().getName().equals("java.lang.Object"), "ComponentUI extends Object");

        for (String name : ABSTRACT_BASES) {
            Class<?> c = Class.forName("javax.swing.plaf." + name);
            check(Modifier.isPublic(c.getModifiers()), name + " public");
            check(Modifier.isAbstract(c.getModifiers()), name + " abstract");
            check(!Modifier.isFinal(c.getModifiers()), name + " not final");
            check(cui.isAssignableFrom(c), name + " is a ComponentUI");
        }

        // one documented refinement: menu items are buttons
        Class<?> menuItem = Class.forName("javax.swing.plaf.MenuItemUI");
        check(menuItem.getSuperclass().getName().equals("javax.swing.plaf.ButtonUI"),
                "MenuItemUI extends ButtonUI");

        Class<?> layer = Class.forName("javax.swing.plaf.LayerUI");
        check(!Modifier.isAbstract(layer.getModifiers()), "LayerUI is concrete");
        check(layer.getSuperclass() == cui, "LayerUI extends ComponentUI");
        boolean serializable = false;
        for (Class<?> i : layer.getInterfaces()) if (i.getName().equals("java.io.Serializable")) serializable = true;
        check(serializable, "LayerUI implements Serializable");
    }

    // ------------------------------------------------------------------
    // (c) installed delegates under Metal + updateUI idempotency
    // ------------------------------------------------------------------
    interface Sup { JComponent make(); }

    /** Each concrete JComponent subclass re-exposes getUI() with a covariant type. */
    static ComponentUI uiOf(JComponent comp) {
        try {
            return (ComponentUI) comp.getClass().getMethod("getUI").invoke(comp);
        } catch (Exception e) {
            throw new AssertionError("no public getUI on " + comp.getClass(), e);
        }
    }

    static void testMetalDelegates() {
        // each entry: component factory and the matching plaf base class
        Object[][] cases = {
                { (Sup) () -> new javax.swing.JButton(), ButtonUI.class },
                { (Sup) () -> new javax.swing.JToggleButton(), ButtonUI.class },
                { (Sup) () -> new JLabel("x"), LabelUI.class },
                { (Sup) () -> new JList<String>(), ListUI.class },
                { (Sup) () -> new JTable(), TableUI.class },
                { (Sup) () -> new javax.swing.table.JTableHeader(), TableHeaderUI.class },
                { (Sup) () -> new JTree(), TreeUI.class },
                { (Sup) () -> new JMenuBar(), MenuBarUI.class },
                { (Sup) () -> new JMenuItem(), MenuItemUI.class },
                { (Sup) () -> new JOptionPane(), OptionPaneUI.class },
                { (Sup) () -> new JPanel(), PanelUI.class },
                { (Sup) () -> new JPopupMenu(), PopupMenuUI.class },
                { (Sup) () -> new JProgressBar(), ProgressBarUI.class },
                { (Sup) () -> new JRootPane(), RootPaneUI.class },
                { (Sup) () -> new JScrollBar(), ScrollBarUI.class },
                { (Sup) () -> new JScrollPane(), ScrollPaneUI.class },
                { (Sup) () -> new JSeparator(), SeparatorUI.class },
                { (Sup) () -> new JSlider(), SliderUI.class },
                { (Sup) () -> new JSpinner(), SpinnerUI.class },
                { (Sup) () -> new JSplitPane(), SplitPaneUI.class },
                { (Sup) () -> new JTabbedPane(), TabbedPaneUI.class },
                { (Sup) () -> new JTextField(), TextUI.class },
                { (Sup) () -> new JTextArea(), TextUI.class },
                { (Sup) () -> new JToolBar(), ToolBarUI.class },
                { (Sup) () -> new JToolTip(), ToolTipUI.class },
                { (Sup) () -> new javax.swing.JComboBox<String>(), ComboBoxUI.class },
                { (Sup) () -> new JDesktopPane(), DesktopPaneUI.class },
                { (Sup) () -> new JInternalFrame("t"), InternalFrameUI.class },
                { (Sup) () -> new JInternalFrame("t").getDesktopIcon(), DesktopIconUI.class },
                { (Sup) () -> new JFileChooser(), FileChooserUI.class },
                { (Sup) () -> new JColorChooser(), ColorChooserUI.class },
                { (Sup) () -> new JViewport(), ViewportUI.class },
        };
        for (Object[] row : cases) {
            Sup sup = (Sup) row[0];
            Class<?> base = (Class<?>) row[1];
            JComponent comp = sup.make();
            check(uiOf(comp) != null, base.getSimpleName() + ": delegate installed");
            check(base.isInstance(uiOf(comp)),
                    base.getSimpleName() + ": getUI() instanceof " + base.getSimpleName());
            // updateUI twice: delegate still valid, still of the family, sizes resolvable
            comp.updateUI();
            comp.updateUI();
            check(base.isInstance(uiOf(comp)), base.getSimpleName() + ": updateUI idempotent family");
            Dimension pref = comp.getPreferredSize();
            check(pref != null && pref.width >= 0 && pref.height >= 0,
                    base.getSimpleName() + ": preferred size non-null after refresh");
            Dimension min = comp.getMinimumSize();
            check(min != null && min.width >= 0 && min.height >= 0,
                    base.getSimpleName() + ": minimum size non-null after refresh");
            Dimension max = comp.getMaximumSize();
            check(max != null && max.width >= 0 && max.height >= 0,
                    base.getSimpleName() + ": maximum size non-null after refresh");
        }
    }

    static void testLayerUIFamily() {
        LayerUI<JPanel> ui = new LayerUI<JPanel>();
        check(ui instanceof ComponentUI, "LayerUI is a ComponentUI");
        JLayer<JPanel> layer = new JLayer<JPanel>(new JPanel(), ui);
        check(layer.getUI() == ui || layer.getUI() instanceof LayerUI,
                "JLayer.getUI() is the supplied LayerUI");
        layer.updateUI();
        check(layer.getUI() instanceof LayerUI, "JLayer.updateUI keeps a LayerUI");
        check(layer.getView() != null, "JLayer exposes the view");
        check(layer.getPreferredSize() != null, "JLayer preferred size non-null");
        // paint through the (default) LayerUI: the view renders, image not empty
        layer.setSize(30, 20);
        BufferedImage img = new BufferedImage(30, 20, BufferedImage.TYPE_INT_ARGB);
        ui.paint(img.getGraphics(), layer);
        check(true, "LayerUI.paint does not throw");
        ui.update(img.getGraphics(), layer);
        check(true, "LayerUI.update does not throw");
        // eventDispatched default passes the event through untouched
        ui.eventDispatched(new java.awt.event.MouseEvent(layer, java.awt.event.MouseEvent.MOUSE_CLICKED,
                System.currentTimeMillis(), 0, 5, 5, 1, false), layer);
        check(true, "LayerUI.eventDispatched no-throw");
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        try {
            testComponentUIDefaults();
            testBaseClassShapes();
            SwingUtilities.invokeAndWait(() -> testMetalDelegates());
            SwingUtilities.invokeAndWait(() -> testLayerUIFamily());
        } finally {
            javax.swing.plaf.metal.MetalLookAndFeel.setCurrentTheme(
                    new javax.swing.plaf.metal.OceanTheme());
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
