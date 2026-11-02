/*
 * @test
 * @summary javax.swing.plaf.basic.BasicSplitPaneUI.BasicHorizontalLayoutManager conformance: type shape and supertypes.
 * @run main BasicHorizontalLayoutManagerTest
 */

import java.lang.reflect.Modifier;

public class BasicHorizontalLayoutManagerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicSplitPaneUI$BasicHorizontalLayoutManager");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.awt.LayoutManager2"), "interface 0");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
