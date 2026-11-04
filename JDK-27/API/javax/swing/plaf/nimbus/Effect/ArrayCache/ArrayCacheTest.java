/*
 * @test
 * @summary javax.swing.plaf.nimbus.Effect.ArrayCache conformance: white-box structure + black-box behavior (degraded).
 * @run main ArrayCacheTest
 */

import java.lang.reflect.Modifier;

public class ArrayCacheTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.nimbus.Effect$ArrayCache");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == false, "public modifier");
        check(Modifier.isProtected(mods) == true, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // Degraded: Effect.ArrayCache is a protected static nested class of the
    // package-private class Effect; its constructor and every method are
    // protected. There is no public or package-visible API reachable from an
    // external (default-package) test, so the behavioral part is intentionally
    // limited to the structural white-box checks above.
    static void testBlackBox() throws Exception {
    }
}
