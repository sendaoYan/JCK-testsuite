/*
 * @test
 * @summary javax.swing.plaf.synth.Region conformance: white-box structure + black-box behavior.
 * @run main RegionTest
 */

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.plaf.synth.Region;

public class RegionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.synth.Region");
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
    }

    static void testBlackBox() throws Exception {
        Region[] regions = {
            Region.ARROW_BUTTON, Region.BUTTON, Region.CHECK_BOX, Region.CHECK_BOX_MENU_ITEM,
            Region.COLOR_CHOOSER, Region.COMBO_BOX, Region.DESKTOP_PANE, Region.DESKTOP_ICON,
            Region.EDITOR_PANE, Region.FILE_CHOOSER, Region.FORMATTED_TEXT_FIELD, Region.INTERNAL_FRAME,
            Region.INTERNAL_FRAME_TITLE_PANE, Region.LABEL, Region.LIST, Region.MENU,
            Region.MENU_BAR, Region.MENU_ITEM, Region.MENU_ITEM_ACCELERATOR, Region.OPTION_PANE,
            Region.PANEL, Region.PASSWORD_FIELD, Region.POPUP_MENU, Region.POPUP_MENU_SEPARATOR,
            Region.PROGRESS_BAR, Region.RADIO_BUTTON, Region.RADIO_BUTTON_MENU_ITEM, Region.ROOT_PANE,
            Region.SCROLL_BAR, Region.SCROLL_BAR_TRACK, Region.SCROLL_BAR_THUMB, Region.SCROLL_PANE,
            Region.SEPARATOR, Region.SLIDER, Region.SLIDER_TRACK, Region.SLIDER_THUMB,
            Region.SPINNER, Region.SPLIT_PANE, Region.SPLIT_PANE_DIVIDER, Region.TABBED_PANE,
            Region.TABBED_PANE_TAB, Region.TABBED_PANE_TAB_AREA, Region.TABBED_PANE_CONTENT, Region.TABLE,
            Region.TABLE_HEADER, Region.TEXT_AREA, Region.TEXT_FIELD, Region.TEXT_PANE,
            Region.TOGGLE_BUTTON, Region.TOOL_BAR, Region.TOOL_BAR_CONTENT, Region.TOOL_BAR_DRAG_WINDOW,
            Region.TOOL_TIP, Region.TOOL_BAR_SEPARATOR, Region.TREE, Region.TREE_CELL, Region.VIEWPORT
        };
        for (Region r : regions) {
            check(r != null, "region non-null");
            check(r.getName() != null, "region name non-null");
        }
        Set<Region> set = new HashSet<Region>();
        for (Region r : regions) set.add(r);
        check(set.size() == regions.length, "all regions distinct");

        check("ArrowButton".equals(Region.ARROW_BUTTON.getName()), "ARROW_BUTTON name");
        check("CheckBox".equals(Region.CHECK_BOX.getName()), "CHECK_BOX name");
        check("ScrollBar".equals(Region.SCROLL_BAR.getName()), "SCROLL_BAR name");
        check("Viewport".equals(Region.VIEWPORT.getName()), "VIEWPORT name");
        check(Region.BUTTON.getName().equals(Region.BUTTON.toString()), "getName == toString");

        check(Region.BUTTON.isSubregion() == false, "BUTTON not subregion");
        check(Region.LABEL.isSubregion() == false, "LABEL not subregion");
        check(Region.SCROLL_BAR_THUMB.isSubregion() == true, "SCROLL_BAR_THUMB subregion");
        check(Region.SCROLL_BAR_TRACK.isSubregion() == true, "SCROLL_BAR_TRACK subregion");
        check(Region.SLIDER_THUMB.isSubregion() == true, "SLIDER_THUMB subregion");
        check(Region.TREE_CELL.isSubregion() == true, "TREE_CELL subregion");
        check(Region.TABBED_PANE_TAB.isSubregion() == true, "TABBED_PANE_TAB subregion");
        check(Region.TOOL_BAR_CONTENT.isSubregion() == true, "TOOL_BAR_CONTENT subregion");
        check(Region.MENU_ITEM_ACCELERATOR.isSubregion() == true, "MENU_ITEM_ACCELERATOR subregion");

        Region custom = new Region("MyRegion", "MyRegionUI", false) {};
        check("MyRegion".equals(custom.getName()), "custom region name");
        check(custom.isSubregion() == false, "custom region not subregion");
        check("MyRegion".equals(custom.toString()), "custom region toString");
    }
}
