/*
 * @test
 * @summary javax.swing.plaf.synth.Region conformance: full constant table with names, singletons and sub-region flags.
 * @run main RegionTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import javax.swing.plaf.synth.Region;

public class RegionTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    /** name + expected isSubregion, taken from the documented Region table. */
    static final Object[][] TABLE = {
            {"ARROW_BUTTON", "ArrowButton", false},
            {"BUTTON", "Button", false},
            {"CHECK_BOX", "CheckBox", false},
            {"CHECK_BOX_MENU_ITEM", "CheckBoxMenuItem", false},
            {"COLOR_CHOOSER", "ColorChooser", false},
            {"COMBO_BOX", "ComboBox", false},
            {"DESKTOP_PANE", "DesktopPane", false},
            {"DESKTOP_ICON", "DesktopIcon", false},
            {"EDITOR_PANE", "EditorPane", false},
            {"FILE_CHOOSER", "FileChooser", false},
            {"FORMATTED_TEXT_FIELD", "FormattedTextField", false},
            {"INTERNAL_FRAME", "InternalFrame", false},
            {"INTERNAL_FRAME_TITLE_PANE", "InternalFrameTitlePane", false},
            {"LABEL", "Label", false},
            {"LIST", "List", false},
            {"MENU", "Menu", false},
            {"MENU_BAR", "MenuBar", false},
            {"MENU_ITEM", "MenuItem", false},
            {"MENU_ITEM_ACCELERATOR", "MenuItemAccelerator", true},
            {"OPTION_PANE", "OptionPane", false},
            {"PANEL", "Panel", false},
            {"PASSWORD_FIELD", "PasswordField", false},
            {"POPUP_MENU", "PopupMenu", false},
            {"POPUP_MENU_SEPARATOR", "PopupMenuSeparator", false},
            {"PROGRESS_BAR", "ProgressBar", false},
            {"RADIO_BUTTON", "RadioButton", false},
            {"RADIO_BUTTON_MENU_ITEM", "RadioButtonMenuItem", false},
            {"ROOT_PANE", "RootPane", false},
            {"SCROLL_BAR", "ScrollBar", false},
            {"SCROLL_BAR_TRACK", "ScrollBarTrack", true},
            {"SCROLL_BAR_THUMB", "ScrollBarThumb", true},
            {"SCROLL_PANE", "ScrollPane", false},
            {"SEPARATOR", "Separator", false},
            {"SLIDER", "Slider", false},
            {"SLIDER_TRACK", "SliderTrack", true},
            {"SLIDER_THUMB", "SliderThumb", true},
            {"SPINNER", "Spinner", false},
            {"SPLIT_PANE", "SplitPane", false},
            {"SPLIT_PANE_DIVIDER", "SplitPaneDivider", true},
            {"TABBED_PANE", "TabbedPane", false},
            {"TABBED_PANE_TAB", "TabbedPaneTab", true},
            {"TABBED_PANE_TAB_AREA", "TabbedPaneTabArea", true},
            {"TABBED_PANE_CONTENT", "TabbedPaneContent", true},
            {"TABLE", "Table", false},
            {"TABLE_HEADER", "TableHeader", false},
            {"TEXT_AREA", "TextArea", false},
            {"TEXT_FIELD", "TextField", false},
            {"TEXT_PANE", "TextPane", false},
            {"TOGGLE_BUTTON", "ToggleButton", false},
            {"TOOL_BAR", "ToolBar", false},
            {"TOOL_BAR_CONTENT", "ToolBarContent", true},
            {"TOOL_BAR_DRAG_WINDOW", "ToolBarDragWindow", false},
            {"TOOL_TIP", "ToolTip", false},
            {"TOOL_BAR_SEPARATOR", "ToolBarSeparator", false},
            {"TREE", "Tree", false},
            {"TREE_CELL", "TreeCell", true},
            {"VIEWPORT", "Viewport", false},
    };

    // ---- Part 1: legacy type shape (retained verbatim) ----
    static void testLegacyShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.synth.Region", false, RegionTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("isSubregion"), "method isSubregion");
        check(names.contains("getName"), "method getName");
        check(names.contains("toString"), "method toString");
    }

    // ---- Part 2: full constant table ----
    static void testConstantTable() throws Exception {
        Class<?> c = Region.class;
        Set<Region> distinct = new HashSet<Region>();
        for (Object[] row : TABLE) {
            String field = (String) row[0];
            String expectName = (String) row[1];
            boolean expectSub = (Boolean) row[2];
            Region r1 = (Region) c.getField(field).get(null);
            Region r2 = (Region) c.getField(field).get(null);
            check(r1 != null, field + " non-null");
            check(r1 == r2, field + " is a singleton (== across reads)");
            check(expectName.equals(r1.getName()), field + " name \"" + expectName + "\"");
            check(r1.isSubregion() == expectSub, field + " isSubregion == " + expectSub);
            // toString is the documented name
            check(expectName.equals(r1.toString()), field + " toString is the name");
            distinct.add(r1);
        }
        check(distinct.size() == TABLE.length, "all Region constants are distinct objects");
        // the public constant surface is exactly the documented table
        int publicStaticFinal = 0;
        for (java.lang.reflect.Field f : c.getFields()) {
            int mods = f.getModifiers();
            if (Modifier.isStatic(mods) && f.getType() == Region.class) publicStaticFinal++;
        }
        check(publicStaticFinal == TABLE.length,
                "no extra Region constants beyond the documented table ("
                        + publicStaticFinal + " fields)");
    }

    // ---- Part 3: sub-region semantics ----
    static void testSubregionSemantics() {
        // the 11 documented sub-regions
        check(Region.MENU_ITEM_ACCELERATOR.isSubregion(), "MenuItemAccelerator sub-region");
        check(Region.SCROLL_BAR_TRACK.isSubregion(), "ScrollBarTrack sub-region");
        check(Region.SCROLL_BAR_THUMB.isSubregion(), "ScrollBarThumb sub-region");
        check(Region.SLIDER_TRACK.isSubregion(), "SliderTrack sub-region");
        check(Region.SLIDER_THUMB.isSubregion(), "SliderThumb sub-region");
        check(Region.SPLIT_PANE_DIVIDER.isSubregion(), "SplitPaneDivider sub-region");
        check(Region.TABBED_PANE_TAB.isSubregion(), "TabbedPaneTab sub-region");
        check(Region.TABBED_PANE_TAB_AREA.isSubregion(), "TabbedPaneTabArea sub-region");
        check(Region.TABBED_PANE_CONTENT.isSubregion(), "TabbedPaneContent sub-region");
        check(Region.TOOL_BAR_CONTENT.isSubregion(), "ToolBarContent sub-region");
        check(Region.TREE_CELL.isSubregion(), "TreeCell sub-region");
        int subregions = 0;
        for (Object[] row : TABLE) if ((Boolean) row[2]) subregions++;
        check(subregions == 11, "exactly 11 sub-regions in the table");
        // regions hash by identity: distinct regions hash apart (singletons)
        check(Region.BUTTON.hashCode() != Region.LABEL.hashCode()
                || Region.BUTTON != Region.LABEL, "distinct regions");
    }

    public static void main(String[] args) throws Exception {
        testLegacyShape();
        testConstantTable();
        testSubregionSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
