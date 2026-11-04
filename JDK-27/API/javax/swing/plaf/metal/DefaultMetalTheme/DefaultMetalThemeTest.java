/*
 * @test
 * @summary javax.swing.plaf.metal.DefaultMetalTheme conformance: white-box structure + black-box behavior.
 * @run main DefaultMetalThemeTest
 */
import java.lang.reflect.Modifier;

public class DefaultMetalThemeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasDeclaredMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.getName().equals(name)) return true;
        }
        return false;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.metal.DefaultMetalTheme");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.metal.MetalTheme"), "superclass javax.swing.plaf.metal.MetalTheme");
        check(c.getInterfaces().length == 0, "no direct interfaces");

        check(hasDeclaredMethod(c, "getName"), "declares method getName");
        check(hasDeclaredMethod(c, "getControlTextFont"), "declares method getControlTextFont");
        check(hasDeclaredMethod(c, "getSystemTextFont"), "declares method getSystemTextFont");
        check(hasDeclaredMethod(c, "getUserTextFont"), "declares method getUserTextFont");
        check(hasDeclaredMethod(c, "getMenuTextFont"), "declares method getMenuTextFont");
        check(hasDeclaredMethod(c, "getWindowTitleFont"), "declares method getWindowTitleFont");
        check(hasDeclaredMethod(c, "getSubTextFont"), "declares method getSubTextFont");
    }

    static void testBlackBox() throws Exception {
        javax.swing.plaf.metal.DefaultMetalTheme t = new javax.swing.plaf.metal.DefaultMetalTheme();
        check("Steel".equals(t.getName()), "name Steel");
        check(t.getControlTextFont() != null && t.getControlTextFont() instanceof javax.swing.plaf.FontUIResource, "control text font");
        check(t.getSystemTextFont() != null, "system text font");
        check(t.getUserTextFont() != null, "user text font");
        check(t.getMenuTextFont() != null, "menu text font");
        check(t.getWindowTitleFont() != null, "window title font");
        check(t.getSubTextFont() != null, "sub text font");
    }

}
