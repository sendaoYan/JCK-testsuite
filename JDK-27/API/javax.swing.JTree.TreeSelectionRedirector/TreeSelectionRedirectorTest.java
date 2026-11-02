/*
 * @test
 * @summary javax.swing.JTree.TreeSelectionRedirector conformance: type shape and supertypes.
 * @run main TreeSelectionRedirectorTest
 */

import java.lang.reflect.Modifier;

public class TreeSelectionRedirectorTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.JTree$TreeSelectionRedirector");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == false, "public modifier");
        check(Modifier.isProtected(mods) == true, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.io.Serializable"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.swing.event.TreeSelectionListener"), "interface 1");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
