/*
 * @test
 * @summary java.lang.reflect.WildcardType conformance: upper/lower bounds, unbounded and
 *          array-position wildcards, equality and exact string forms.
 * @run main WildcardTypeTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.Map;

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
        Map<String, List<? extends Number>> nestedWild;
        List<?>[] wildArray;
        Class<?> anyType;
    }

    static void consume(List<? super Number> xs) {
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

    // ---- deepened contracts (2026-11-05 batch) ----
    static WildcardType argOf(Field f) throws Exception {
        return (WildcardType) ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0];
    }

    static void testUnboundedWildcard() throws Exception {
        WildcardType un = argOf(More.class.getDeclaredField("unbounded"));
        check(un.getUpperBounds() != null, "upper bounds array non-null");
        check(un.getUpperBounds().length == 1, "implicit single upper bound");
        check(un.getUpperBounds()[0] == Object.class, "implicit upper is Object");
        check(un.getLowerBounds() != null, "lower bounds array non-null");
        check(un.getLowerBounds().length == 0, "unbounded has no lower bound");
        check(un.getTypeName().equals("?"), "unbounded typeName ?");
        check(un.toString().equals("?"), "unbounded toString ?");
    }

    static void testMethodWildcard() throws Exception {
        Method m = WildcardTypeTest.class.getDeclaredMethod("consume", List.class);
        WildcardType w = (WildcardType) ((ParameterizedType) m.getGenericParameterTypes()[0])
                .getActualTypeArguments()[0];
        check(w.getLowerBounds().length == 1, "method wildcard one lower bound");
        check(w.getLowerBounds()[0] == Number.class, "method lower bound Number");
        check(w.getUpperBounds().length == 1, "method wildcard implicit upper");
        check(w.getUpperBounds()[0] == Object.class, "method implicit upper Object");
        check(w.getTypeName().equals("? super java.lang.Number"), "method wildcard typeName");
    }

    static void testNestedAndArrayWildcards() throws Exception {
        ParameterizedType npt = (ParameterizedType) More.class.getDeclaredField("nestedWild")
                .getGenericType();
        Type inner = npt.getActualTypeArguments()[1];
        check(inner instanceof ParameterizedType, "nested value argument is parameterized");
        WildcardType iw = (WildcardType) ((ParameterizedType) inner).getActualTypeArguments()[0];
        check(iw.getUpperBounds()[0] == Number.class, "nested wildcard upper Number");
        check(iw.getLowerBounds().length == 0, "nested wildcard no lower");
        check(iw.getTypeName().equals("? extends java.lang.Number"), "nested wildcard typeName");
        check(npt.getTypeName().startsWith("java.util.Map<java.lang.String"), "enclosing map prefix");

        GenericArrayType gat = (GenericArrayType) More.class.getDeclaredField("wildArray")
                .getGenericType();
        ParameterizedType comp = (ParameterizedType) gat.getGenericComponentType();
        check(comp.getRawType() == List.class, "List<?>[] component raw List");
        check(comp.getActualTypeArguments()[0] instanceof WildcardType, "component arg is wildcard");
        check(comp.getActualTypeArguments()[0].getTypeName().equals("?"), "component arg name ?");
        check(gat.getTypeName().equals("java.util.List<?>[]"), "wildcard array typeName");
    }

    static void testEqualityAndShape() throws Exception {
        WildcardType any = argOf(More.class.getDeclaredField("anyType"));
        check(any.getUpperBounds().length == 1 && any.getUpperBounds()[0] == Object.class,
              "Class<?> arg keeps implicit Object upper");
        check(any.getLowerBounds().length == 0, "Class<?> arg has no lower");
        check(any.equals(argOf(More.class.getDeclaredField("unbounded"))),
              "same-shape wildcards from different fields equal");
        WildcardType up = argOf(Sample.class.getDeclaredField("upper"));
        WildcardType upAgain = argOf(Sample.class.getDeclaredField("upper"));
        check(up.equals(upAgain), "wildcard equals by value");
        check(up.hashCode() == upAgain.hashCode(), "wildcard hashCode consistent");
        check(up.equals(up), "equals reflexive");
        check(!up.equals(null), "not equal to null");
        WildcardType lo = argOf(Sample.class.getDeclaredField("lower"));
        check(!up.equals(lo), "upper-bounded and lower-bounded differ");
        check(!lo.equals(any), "lower-bounded differs from unbounded");
        check(up.getLowerBounds().length == 0, "upper-bounded keeps no lower (cross-check)");
        check(lo.getUpperBounds().length == 1, "lower-bounded keeps exactly one upper (cross-check)");
        check(WildcardType.class.isInterface(), "WildcardType is an interface");
        check(Type.class.isAssignableFrom(WildcardType.class), "WildcardType extends Type");
        check(up.toString().equals(up.getTypeName()), "upper toString matches typeName");
        check(lo.toString().equals(lo.getTypeName()), "lower toString matches typeName");
        check(up.getTypeName().equals("? extends java.lang.Number"), "upper typeName exact");
        check(lo.getTypeName().equals("? super java.lang.Integer"), "lower typeName exact");
    }

    public static void main(String[] args) throws Exception {
        testWildcardType();
        testUnboundedWildcard();
        testMethodWildcard();
        testNestedAndArrayWildcards();
        testEqualityAndShape();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
