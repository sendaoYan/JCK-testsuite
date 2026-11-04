/*
 * @test
 * @summary javax.swing.plaf.metal.MetalTheme conformance: white-box structure + black-box behavior.
 * @run main MetalThemeTest
 */

import java.lang.reflect.Modifier;
import javax.swing.UIDefaults;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalTheme;
import javax.swing.plaf.metal.OceanTheme;

public class MetalThemeTest {
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
        Class<?> c = Class.forName("javax.swing.plaf.metal.MetalTheme");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void checkTheme(MetalTheme theme, String label) {
        check(theme.getName() != null && theme.getName().length() > 0, label + " name non-empty");
        check(theme.getControlTextFont() != null, label + " controlTextFont");
        check(theme.getSystemTextFont() != null, label + " systemTextFont");
        check(theme.getUserTextFont() != null, label + " userTextFont");
        check(theme.getMenuTextFont() != null, label + " menuTextFont");
        check(theme.getWindowTitleFont() != null, label + " windowTitleFont");
        check(theme.getSubTextFont() != null, label + " subTextFont");
        check(theme.getControl() != null, label + " control");
        check(theme.getControlTextColor() != null, label + " controlTextColor");
        check(theme.getPrimaryControl() != null, label + " primaryControl");
        check(theme.getPrimaryControlShadow() != null, label + " primaryControlShadow");
        check(theme.getSystemTextColor() != null, label + " systemTextColor");
        check(theme.getInactiveControlTextColor() != null, label + " inactiveControlTextColor");
        check(theme.getMenuBackground() != null, label + " menuBackground");
        check(theme.getMenuForeground() != null, label + " menuForeground");
        check(theme.getWindowBackground() != null, label + " windowBackground");
        check(theme.getDesktopColor() != null, label + " desktopColor");
        check(theme.getFocusColor() != null, label + " focusColor");
        final MetalTheme t = theme;
        assertNoThrow(() -> t.addCustomEntriesToTable(new UIDefaults()), label + " addCustomEntriesToTable");
    }

    static void testBlackBox() throws Exception {
        // MetalTheme is abstract; exercise its API through the two concrete
        // subclasses shipped with the platform (Steel/Ocean).
        DefaultMetalTheme steel = new DefaultMetalTheme();
        OceanTheme ocean = new OceanTheme();
        checkTheme(steel, "steel");
        checkTheme(ocean, "ocean");

        check("Steel".equals(steel.getName()), "steel name");
        check("Ocean".equals(ocean.getName()), "ocean name");
    }
}
