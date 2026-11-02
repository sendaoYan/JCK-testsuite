/*
 * @test
 * @summary javax.swing.plaf.basic.DragRecognitionSupport.BeforeDrag conformance: type shape and supertypes.
 * @run main BeforeDragTest
 */

import java.lang.reflect.Modifier;

public class BeforeDragTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.DragRecognitionSupport$BeforeDrag");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
