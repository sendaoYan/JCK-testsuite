/*
 * @test
 * @summary javax.swing.plaf.metal.OceanTheme conformance: white-box structure + black-box behavior.
 * @run main OceanThemeTest
 */

import java.lang.reflect.Modifier;
import javax.swing.UIDefaults;
import javax.swing.plaf.metal.MetalTheme;
import javax.swing.plaf.metal.OceanTheme;

public class OceanThemeTest {
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
        Class<?> c = Class.forName("javax.swing.plaf.metal.OceanTheme");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.metal.DefaultMetalTheme"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        OceanTheme o = new OceanTheme();
        check(o instanceof MetalTheme, "instanceof MetalTheme");
        check("Ocean".equals(o.getName()), "name");

        check(o.getControlTextColor() != null, "controlTextColor");
        check(o.getDesktopColor() != null, "desktopColor");
        check(o.getInactiveControlTextColor() != null, "inactiveControlTextColor");
        check(o.getMenuDisabledForeground() != null, "menuDisabledForeground");

        check(o.getControlTextFont() != null, "controlTextFont");
        check(o.getSystemTextFont() != null, "systemTextFont");
        check(o.getUserTextFont() != null, "userTextFont");
        check(o.getMenuTextFont() != null, "menuTextFont");
        check(o.getWindowTitleFont() != null, "windowTitleFont");
        check(o.getSubTextFont() != null, "subTextFont");

        assertNoThrow(() -> o.addCustomEntriesToTable(new UIDefaults()), "addCustomEntriesToTable");
    }
}
