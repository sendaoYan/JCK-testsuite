/*
 * @test
 * @summary javax.swing.plaf.nimbus.AbstractRegionPainter.PaintContext.CacheMode conformance: white-box structure + black-box behavior.
 * @run main CacheModeTest
 */

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

public class CacheModeTest {
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
    }

    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.nimbus.AbstractRegionPainter$PaintContext$CacheMode");
        Object[] vals = (Object[]) c.getMethod("values").invoke(null);
        check(vals.length == 3, "values length 3");
        check(((Enum<?>) vals[0]).name().equals("NO_CACHING"), "value 0 NO_CACHING");
        check(((Enum<?>) vals[1]).name().equals("FIXED_SIZES"), "value 1 FIXED_SIZES");
        check(((Enum<?>) vals[2]).name().equals("NINE_SQUARE_SCALE"), "value 2 NINE_SQUARE_SCALE");
        check(((Enum<?>) vals[0]).ordinal() == 0, "ordinal 0");
        check(((Enum<?>) vals[1]).ordinal() == 1, "ordinal 1");
        check(((Enum<?>) vals[2]).ordinal() == 2, "ordinal 2");

        Object noCaching = c.getMethod("valueOf", String.class).invoke(null, "NO_CACHING");
        Object fixedSizes = c.getMethod("valueOf", String.class).invoke(null, "FIXED_SIZES");
        Object nineSquare = c.getMethod("valueOf", String.class).invoke(null, "NINE_SQUARE_SCALE");
        check(noCaching == vals[0], "valueOf NO_CACHING identity");
        check(fixedSizes == vals[1], "valueOf FIXED_SIZES identity");
        check(nineSquare == vals[2], "valueOf NINE_SQUARE_SCALE identity");

        check(((Enum<?>) vals[0]).name().equals(((Enum<?>) vals[0]).toString()), "name == toString");

        boolean iae = false;
        try {
            c.getMethod("valueOf", String.class).invoke(null, "BOGUS");
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof IllegalArgumentException) iae = true;
        }
        check(iae, "valueOf unknown throws IAE");
    }
}
