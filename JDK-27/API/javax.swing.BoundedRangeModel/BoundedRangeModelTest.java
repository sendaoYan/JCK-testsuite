/*
 * @test
 * @summary javax.swing.BoundedRangeModel conformance: type shape and supertypes.
 * @run main BoundedRangeModelTest
 */

import java.lang.reflect.Modifier;

public class BoundedRangeModelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.BoundedRangeModel");
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
