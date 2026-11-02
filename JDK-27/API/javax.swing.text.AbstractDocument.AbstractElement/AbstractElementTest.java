/*
 * @test
 * @summary javax.swing.text.AbstractDocument.AbstractElement conformance: type shape and supertypes.
 * @run main AbstractElementTest
 */

import java.lang.reflect.Modifier;

public class AbstractElementTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.AbstractDocument$AbstractElement");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getInterfaces().length == 4, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.text.Element"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.swing.text.MutableAttributeSet"), "interface 1");
        check(c.getInterfaces()[2].getName().equals("java.io.Serializable"), "interface 2");
        check(c.getInterfaces()[3].getName().equals("javax.swing.tree.TreeNode"), "interface 3");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
