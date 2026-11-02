/*
 * @test
 * @summary javax.swing.plaf.nimbus.AbstractRegionPainter.PaintContext.CacheMode conformance: type shape and supertypes.
 * @run main CacheModeTest
 */

import java.lang.reflect.Modifier;

public class CacheModeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.nimbus.AbstractRegionPainter$PaintContext$CacheMode");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == true, "is enum");
        check(Modifier.isPublic(mods) == false, "public modifier");
        check(Modifier.isProtected(mods) == true, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
        Object[] ec = c.getEnumConstants();
        check(ec != null && ec.length == 3, "enum constant count");
        check(((Enum<?>) ec[0]).name().equals("NO_CACHING"), "enum constant 0");
        check(((Enum<?>) ec[1]).name().equals("FIXED_SIZES"), "enum constant 1");
        check(((Enum<?>) ec[2]).name().equals("NINE_SQUARE_SCALE"), "enum constant 2");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
