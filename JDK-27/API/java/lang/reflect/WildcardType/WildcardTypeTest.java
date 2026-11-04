/*
 * @test
 * @summary java.lang.reflect.WildcardType conformance: white-box structure + black-box behavior.
 * @run main WildcardTypeTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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

    static class More {
        List<?> unbounded;
        java.util.Map<? extends String, ? super Long> bothEnds;
        Class<?> any;
    }

    // ---- Part 1: white-box (structural, retained from previous revision) ----
    static void testWhiteBox() throws Exception {
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
        check(WildcardType.class.isInterface(), "is an interface");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. unbounded wildcard: implicit Object upper, no lower
        WildcardType un = arg(More.class.getDeclaredField("unbounded"));
        check(un.getUpperBounds().length == 1 && un.getUpperBounds()[0] == Object.class,
              "unbounded implicit Object upper");
        check(un.getLowerBounds().length == 0, "unbounded no lower");
        check(un.getTypeName().equals("?"), "unbounded typeName");
        check(un.toString().equals("?"), "unbounded toString");

        // 2. upper-bounded and lower-bounded wildcards, exact types
        WildcardType up = arg(Sample.class.getDeclaredField("upper"));
        check(up.getUpperBounds()[0] == Number.class, "upper exact Number");
        check(up.getLowerBounds().length == 0, "upper-bounded no lower");
        check(up.getTypeName().equals("? extends java.lang.Number"), "upper typeName");
        WildcardType lo = arg(Sample.class.getDeclaredField("lower"));
        check(lo.getLowerBounds()[0] == Integer.class, "lower exact Integer");
        check(lo.getUpperBounds()[0] == Object.class, "lower implicit Object upper");
        check(lo.getTypeName().equals("? super java.lang.Integer"), "lower typeName");

        // 3. both ends in a single parameterized type
        ParameterizedType both = (ParameterizedType) More.class.getDeclaredField("bothEnds").getGenericType();
        WildcardType k = (WildcardType) both.getActualTypeArguments()[0];
        WildcardType v = (WildcardType) both.getActualTypeArguments()[1];
        check(k.getUpperBounds()[0] == String.class, "key upper String");
        check(k.getLowerBounds().length == 0, "key no lower");
        check(v.getLowerBounds()[0] == Long.class, "value lower Long");
        check(v.getUpperBounds()[0] == Object.class, "value implicit upper");
        check(both.getTypeName().equals("java.util.Map<? extends java.lang.String, ? super java.lang.Long>"),
              "map with both wildcards typeName");

        // 4. wildcard from a method signature behaves identically
        Method m = WildcardTypeTest.class.getDeclaredMethod("consume", List.class);
        WildcardType mp = (WildcardType) ((ParameterizedType) m.getGenericParameterTypes()[0])
                .getActualTypeArguments()[0];
        check(mp.getUpperBounds()[0] == Number.class, "method param wildcard upper");
        check(mp.getLowerBounds().length == 0, "method param wildcard no lower");

        // 5. equality/hashCode value semantics for repeated lookups
        WildcardType up2 = arg(Sample.class.getDeclaredField("upper"));
        check(up.equals(up2), "wildcard equals by value");
        check(up.hashCode() == up2.hashCode(), "wildcard hashCode consistent");
        check(!up.equals(un) && !up.equals(lo), "distinct bounds not equal");
        check(arg(More.class.getDeclaredField("any")).equals(un), "same-shape wildcards equal");
    }

    static WildcardType arg(Field f) throws Exception {
        return (WildcardType) ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0];
    }

    static void consume(List<? extends Number> xs) {
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
