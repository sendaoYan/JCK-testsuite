/*
 * @test
 * @summary javax.swing.JInternalFrame conformance: type shape and supertypes.
 * @run main JInternalFrameTest
 */

import java.lang.reflect.Modifier;

public class JInternalFrameTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.JInternalFrame");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 3, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.accessibility.Accessible"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.swing.WindowConstants"), "interface 1");
        check(c.getInterfaces()[2].getName().equals("javax.swing.RootPaneContainer"), "interface 2");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
