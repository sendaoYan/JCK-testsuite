/*
 * @test
 * @summary javax.swing.InternalFrameFocusTraversalPolicy conformance: type shape and supertypes.
 * @run main InternalFrameFocusTraversalPolicyTest
 */

import java.lang.reflect.Modifier;

public class InternalFrameFocusTraversalPolicyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.InternalFrameFocusTraversalPolicy");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.FocusTraversalPolicy"), "superclass");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
