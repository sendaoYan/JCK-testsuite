/*
 * @test
 * @summary javax.swing.plaf.basic.BasicTextUI conformance: white-box structure + black-box behavior.
 * @run main BasicTextUITest
 */
import java.lang.reflect.Modifier;

public class BasicTextUITest {
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
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicTextUI");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.TextUI"), "superclass javax.swing.plaf.TextUI");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.text.ViewFactory"), "interface 0");

        check(hasDeclaredMethod(c, "create"), "declares method create");
        check(hasDeclaredMethod(c, "modelToView"), "declares method modelToView");
        check(hasDeclaredMethod(c, "viewToModel"), "declares method viewToModel");
        check(hasDeclaredMethod(c, "getRootView"), "declares method getRootView");
        check(hasDeclaredMethod(c, "getEditorKit"), "declares method getEditorKit");
    }

    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicTextUI");
        check(c.getMethod("create", javax.swing.text.Element.class).getReturnType() == javax.swing.text.View.class, "create returns View");
        check(c.getMethod("getRootView", javax.swing.text.JTextComponent.class).getReturnType() == javax.swing.text.View.class, "getRootView returns View");
        java.lang.reflect.Method gp = c.getDeclaredMethod("getPropertyPrefix");
        check(java.lang.reflect.Modifier.isAbstract(gp.getModifiers()), "getPropertyPrefix abstract");
    }

}
