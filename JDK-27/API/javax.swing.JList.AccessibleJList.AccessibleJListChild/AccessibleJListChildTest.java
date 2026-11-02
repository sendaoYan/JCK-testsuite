/*
 * @test
 * @summary javax.swing.JList.AccessibleJList.AccessibleJListChild conformance: type shape and supertypes.
 * @run main AccessibleJListChildTest
 */

import java.lang.reflect.Modifier;

public class AccessibleJListChildTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.JList$AccessibleJList$AccessibleJListChild");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == false, "public modifier");
        check(Modifier.isProtected(mods) == true, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.accessibility.AccessibleContext"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 3, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.accessibility.Accessible"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.accessibility.AccessibleComponent"), "interface 1");
        check(c.getInterfaces()[2].getName().equals("javax.accessibility.AccessibleAction"), "interface 2");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
