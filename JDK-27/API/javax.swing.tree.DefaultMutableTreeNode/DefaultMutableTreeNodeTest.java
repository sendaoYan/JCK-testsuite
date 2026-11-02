/*
 * @test
 * @summary javax.swing.tree.DefaultMutableTreeNode conformance: type shape and supertypes.
 * @run main DefaultMutableTreeNodeTest
 */

import java.lang.reflect.Modifier;

public class DefaultMutableTreeNodeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.tree.DefaultMutableTreeNode");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 3, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.lang.Cloneable"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.swing.tree.MutableTreeNode"), "interface 1");
        check(c.getInterfaces()[2].getName().equals("java.io.Serializable"), "interface 2");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
