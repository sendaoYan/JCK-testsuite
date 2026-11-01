/*
 * @test
 * @summary java.lang.reflect.WildcardType conformance: upper/lower bounds.
 * @run main WildcardTypeTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.WildcardType;
import java.util.List;

public class WildcardTypeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Sample {
        List<? extends Number> upper;
        List<? super Integer> lower;
    }

    static void testWildcardType() throws Exception {
        Field uf = Sample.class.getDeclaredField("upper");
        ParameterizedType upt = (ParameterizedType) uf.getGenericType();
        check(upt.getActualTypeArguments()[0] instanceof WildcardType, "upper is WildcardType");
        WildcardType uw = (WildcardType) upt.getActualTypeArguments()[0];
        check(uw.getUpperBounds().length == 1, "one upper bound");
        check(uw.getUpperBounds()[0] == Number.class, "upper bound Number");
        check(uw.getLowerBounds().length == 0, "no lower bounds");

        Field lf = Sample.class.getDeclaredField("lower");
        ParameterizedType lpt = (ParameterizedType) lf.getGenericType();
        WildcardType lw = (WildcardType) lpt.getActualTypeArguments()[0];
        check(lw.getLowerBounds().length == 1, "one lower bound");
        check(lw.getLowerBounds()[0] == Integer.class, "lower bound Integer");
        check(lw.getUpperBounds().length == 1, "implicit upper bound Object");
        check(lw.getUpperBounds()[0] == Object.class, "upper bound Object");
    }

    public static void main(String[] args) throws Exception {
        testWildcardType();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
