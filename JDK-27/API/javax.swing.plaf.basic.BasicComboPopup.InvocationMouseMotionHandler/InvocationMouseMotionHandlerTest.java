/*
 * @test
 * @summary javax.swing.plaf.basic.BasicComboPopup.InvocationMouseMotionHandler conformance: type shape and supertypes.
 * @run main InvocationMouseMotionHandlerTest
 */

import java.lang.reflect.Modifier;

public class InvocationMouseMotionHandlerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicComboPopup$InvocationMouseMotionHandler");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == false, "public modifier");
        check(Modifier.isProtected(mods) == true, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.event.MouseMotionAdapter"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
