/*
 * @test
 * @summary javax.swing.plaf.multi conformance: MultiLookAndFeel metadata, createUIs multiplexing, uisToArray conversion, MultiXxxUI family shape (headless, Metal default LaF).
 * @run main MultiLookAndFeelContractTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.multi.MultiButtonUI;
import javax.swing.plaf.multi.MultiLabelUI;
import javax.swing.plaf.multi.MultiLookAndFeel;

/**
 * The multiplexing look-and-feel forwards every component to the default LaF's
 * delegate plus any auxiliary delegates. Without auxiliaries installed the
 * public static createUIs collapses to the single default delegate - the key
 * documented contract, verified here without needing to install Multi itself.
 */
public class MultiLookAndFeelContractTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMetadata() {
        MultiLookAndFeel lf = new MultiLookAndFeel();
        check("Multiplexing Look and Feel".equals(lf.getName()), "getName");
        check("Multiplex".equals(lf.getID()), "getID");
        check("Allows multiple UI instances per component instance".equals(lf.getDescription()),
                "getDescription");
        check(lf.isSupportedLookAndFeel(), "supported");
        check(!lf.isNativeLookAndFeel(), "not native");
        check(!lf.getSupportsWindowDecorations(), "no window decorations");
        check(lf.getDefaults() != null, "getDefaults non-null");
        check(lf.toString().contains("multiple UI instances"),
                "toString is based on the description");
    }

    static void testCreateUIsCollapsesToDefault() {
        JButton b = new JButton();
        b.updateUI();
        Vector<ComponentUI> uis = new Vector<ComponentUI>();
        MultiButtonUI mui = new MultiButtonUI();
        ComponentUI result = MultiLookAndFeel.createUIs(mui, uis, b);
        // single default LaF: no multiplexing wrapper needed
        check(uis.size() == 1, "exactly the default delegate collected");
        check(result == uis.elementAt(0), "createUIs returns the default delegate itself");
        check(result instanceof javax.swing.plaf.metal.MetalButtonUI
                        || result != null,
                "default delegate non-null");
        check(!(result instanceof MultiButtonUI), "no Multi wrapper without auxiliaries");
        // repeat call is deterministic and independent
        Vector<ComponentUI> uis2 = new Vector<ComponentUI>();
        ComponentUI result2 = MultiLookAndFeel.createUIs(new MultiButtonUI(), uis2, b);
        check(uis2.size() == 1 && result2 != null, "second call collapses the same way");

        // a different component family collapses to its own default delegate
        JLabel l = new JLabel();
        l.updateUI();
        Vector<ComponentUI> uis3 = new Vector<ComponentUI>();
        MultiLabelUI mlabel = new MultiLabelUI();
        ComponentUI labelResult = MultiLookAndFeel.createUIs(mlabel, uis3, l);
        check(uis3.size() == 1 && labelResult instanceof javax.swing.plaf.LabelUI,
                "label multiplexer collapses to the label default delegate");
    }

    static void testUisToArray() {
        // exposed through the public getUIs(): empty uis -> null (documented quirk)
        MultiButtonUI fresh = new MultiButtonUI();
        check(fresh.getUIs() == null, "getUIs() returns null while no UIs are collected");
        // populated uis -> exact array copy (feed the delegate's own vector)
        JButton b = new JButton();
        b.updateUI();
        ProbeButtonUI pb = new ProbeButtonUI();
        ComponentUI result = MultiLookAndFeel.createUIs(pb, pb.ownUis(), b);
        ComponentUI[] arr = pb.getUIs();
        check(arr != null && arr.length == 1, "getUIs() converts the collected vector");
        check(arr[0] == result, "array element identity preserved");
    }

    static void testMultiFamilyShape() throws Exception {
        // every MultiXxxUI is concrete, extends the matching base and vends a
        // public static createUI
        String[][] family = {
                { "MultiButtonUI", "javax.swing.plaf.ButtonUI" },
                { "MultiColorChooserUI", "javax.swing.plaf.ColorChooserUI" },
                { "MultiComboBoxUI", "javax.swing.plaf.ComboBoxUI" },
                { "MultiDesktopIconUI", "javax.swing.plaf.DesktopIconUI" },
                { "MultiDesktopPaneUI", "javax.swing.plaf.DesktopPaneUI" },
                { "MultiFileChooserUI", "javax.swing.plaf.FileChooserUI" },
                { "MultiInternalFrameUI", "javax.swing.plaf.InternalFrameUI" },
                { "MultiLabelUI", "javax.swing.plaf.LabelUI" },
                { "MultiListUI", "javax.swing.plaf.ListUI" },
                { "MultiMenuBarUI", "javax.swing.plaf.MenuBarUI" },
                { "MultiMenuItemUI", "javax.swing.plaf.MenuItemUI" },
                { "MultiOptionPaneUI", "javax.swing.plaf.OptionPaneUI" },
                { "MultiPanelUI", "javax.swing.plaf.PanelUI" },
                { "MultiPopupMenuUI", "javax.swing.plaf.PopupMenuUI" },
                { "MultiProgressBarUI", "javax.swing.plaf.ProgressBarUI" },
                { "MultiRootPaneUI", "javax.swing.plaf.RootPaneUI" },
                { "MultiScrollBarUI", "javax.swing.plaf.ScrollBarUI" },
                { "MultiScrollPaneUI", "javax.swing.plaf.ScrollPaneUI" },
                { "MultiSeparatorUI", "javax.swing.plaf.SeparatorUI" },
                { "MultiSliderUI", "javax.swing.plaf.SliderUI" },
                { "MultiSpinnerUI", "javax.swing.plaf.SpinnerUI" },
                { "MultiSplitPaneUI", "javax.swing.plaf.SplitPaneUI" },
                { "MultiTabbedPaneUI", "javax.swing.plaf.TabbedPaneUI" },
                { "MultiTableHeaderUI", "javax.swing.plaf.TableHeaderUI" },
                { "MultiTableUI", "javax.swing.plaf.TableUI" },
                { "MultiTextUI", "javax.swing.plaf.TextUI" },
                { "MultiToolBarUI", "javax.swing.plaf.ToolBarUI" },
                { "MultiToolTipUI", "javax.swing.plaf.ToolTipUI" },
                { "MultiTreeUI", "javax.swing.plaf.TreeUI" },
                { "MultiViewportUI", "javax.swing.plaf.ViewportUI" },
        };
        for (String[] row : family) {
            Class<?> c = Class.forName("javax.swing.plaf.multi." + row[0]);
            check(!Modifier.isAbstract(c.getModifiers()), row[0] + " concrete");
            check(c.getSuperclass().getName().equals(row[1]),
                    row[0] + " extends " + row[1].substring(row[1].lastIndexOf('.') + 1));
            Method createUI = c.getMethod("createUI", JComponent.class);
            check(Modifier.isStatic(createUI.getModifiers())
                            && createUI.getReturnType() == ComponentUI.class,
                    row[0] + ".createUI static factory");
            Method getUIs = c.getMethod("getUIs");
            check(getUIs.getReturnType() == ComponentUI[].class, row[0] + ".getUIs array");
        }
        // a representative delegate from the family collapses to Metal when invoked directly
        JButton b = new JButton();
        b.updateUI();
        ComponentUI viaMulti = MultiButtonUI.createUI(b);
        check(viaMulti != null && !(viaMulti instanceof MultiButtonUI),
                "MultiButtonUI.createUI collapses to the default delegate");
    }

    /**
     * uisToArray is protected static; subclasses expose it. Empty/null vectors
     * map to a null/empty result without throwing.
     */
    static class ProbeMulti extends MultiLookAndFeel {
        static ComponentUI[] convert(Vector<? extends ComponentUI> v) {
            return MultiLookAndFeel.uisToArray(v);
        }
    }

    /** Exposes the protected uis vector of a multiplexing delegate. */
    static class ProbeButtonUI extends MultiButtonUI {
        Vector<ComponentUI> ownUis() { return uis; }
    }

    static void testUisToArrayConversion() {
        check(ProbeMulti.convert(null) != null && ProbeMulti.convert(null).length == 0,
                "uisToArray(null) -> empty array");
        check(ProbeMulti.convert(new Vector<ComponentUI>()) == null,
                "uisToArray(empty) -> null");
        Vector<ComponentUI> v = new Vector<ComponentUI>();
        v.addElement(new MultiButtonUI());
        v.addElement(new MultiLabelUI());
        ComponentUI[] arr = ProbeMulti.convert(v);
        check(arr != null && arr.length == 2, "uisToArray preserves the count");
        check(arr[0] instanceof MultiButtonUI && arr[1] instanceof MultiLabelUI,
                "uisToArray preserves the order");
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        testMetadata();
        testCreateUIsCollapsesToDefault();
        testUisToArray();
        testUisToArrayConversion();
        testMultiFamilyShape();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
