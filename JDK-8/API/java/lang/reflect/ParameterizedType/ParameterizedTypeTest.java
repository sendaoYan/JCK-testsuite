/*
 * @test
 * @summary java.lang.reflect.ParameterizedType conformance: raw type/actual type args/owner,
 *          nested recursion, generic superclasses/interfaces, equality, raw-use negatives.
 * @run main ParameterizedTypeTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ParameterizedTypeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Sample {
        Map<String, Integer> map;
    }

    static class Types {
        Map<String, List<Integer>> nested;
        Map.Entry<String, Integer> entry;
        Class<?> clazz;
        List raw;
    }

    static class Enclosing {
        class Inner<Q> {
        }
    }

    static class Owns {
        Enclosing.Inner<String> inner;
    }

    static class G<T extends Number, U extends List<String>> implements Comparable<G<T, U>> {
        public int compareTo(G<T, U> other) {
            return 0;
        }
    }

    static class SubG extends G<Integer, ArrayList<String>> {
    }

    static class StringList extends ArrayList<String> {
    }

    static void testParameterizedType() throws Exception {
        Field f = Sample.class.getDeclaredField("map");
        Type t = f.getGenericType();
        check(t instanceof ParameterizedType, "is ParameterizedType");
        ParameterizedType pt = (ParameterizedType) t;
        check(pt.getRawType() == Map.class, "getRawType");
        check(pt.getActualTypeArguments().length == 2, "two actual type arguments");
        check(pt.getActualTypeArguments()[0] == String.class, "String argument");
        check(pt.getActualTypeArguments()[1] == Integer.class, "Integer argument");
        check(pt.getOwnerType() == null, "getOwnerType null");
        check(pt.toString() != null, "toString");
    }

    // ---- deepened contracts (2026-11-05 batch) ----
    static void testNestedParameterization() throws Exception {
        ParameterizedType npt = (ParameterizedType) Types.class.getDeclaredField("nested")
                .getGenericType();
        check(npt.getRawType() == Map.class, "nested raw Map");
        check(npt.getActualTypeArguments().length == 2, "two actual arguments");
        check(npt.getActualTypeArguments()[0] == String.class, "outer argument String");
        Type a1 = npt.getActualTypeArguments()[1];
        check(a1 instanceof Type, "argument is a Type");
        check(a1 instanceof ParameterizedType, "inner argument is parameterized");
        ParameterizedType inner = (ParameterizedType) a1;
        check(inner.getRawType() == List.class, "inner raw List");
        check(inner.getActualTypeArguments().length == 1, "inner single argument");
        check(inner.getActualTypeArguments()[0] == Integer.class, "inner argument Integer");
        check(npt.getOwnerType() == null, "top-level type has no owner");
        check(npt.getTypeName().equals("java.util.Map<java.lang.String, java.util.List<java.lang.Integer>>"),
              "nested typeName exact");
        check(npt.getTypeName().startsWith("java.util.Map<java.lang.String"), "typeName prefix fragment");
        check(npt.getTypeName().contains("java.util.List<java.lang.Integer>"), "typeName inner fragment");
    }

    static void testOwnerType() throws Exception {
        ParameterizedType ept = (ParameterizedType) Types.class.getDeclaredField("entry")
                .getGenericType();
        check(ept.getOwnerType() == Map.class, "Map.Entry owner is Map");
        check(ept.getRawType() == Map.Entry.class, "raw type is Map.Entry");
        check(ept.getActualTypeArguments()[0] == String.class, "entry key argument");
        check(ept.getActualTypeArguments()[1] == Integer.class, "entry value argument");
        ParameterizedType ipt = (ParameterizedType) Owns.class.getDeclaredField("inner")
                .getGenericType();
        check(ipt.getOwnerType() == Enclosing.class, "inner class owner is the enclosing class");
        check(ipt.getRawType() == Enclosing.Inner.class, "raw type is Enclosing.Inner");
        check(ipt.getActualTypeArguments().length == 1, "inner class one argument");
        check(ipt.getActualTypeArguments()[0] == String.class, "inner argument String");
    }

    static void testGenericSupers() {
        ParameterizedType spt = (ParameterizedType) StringList.class.getGenericSuperclass();
        check(spt.getRawType() == ArrayList.class, "superclass raw ArrayList");
        check(spt.getActualTypeArguments()[0] == String.class, "superclass argument String");
        check(spt.getOwnerType() == null, "superclass has no owner");

        ParameterizedType gpt = (ParameterizedType) SubG.class.getGenericSuperclass();
        check(gpt.getRawType() == G.class, "two-arg superclass raw G");
        check(gpt.getActualTypeArguments().length == 2, "two superclass arguments");
        check(gpt.getActualTypeArguments()[0] == Integer.class, "first superclass argument");
        ParameterizedType uArg = (ParameterizedType) gpt.getActualTypeArguments()[1];
        check(uArg.getRawType() == ArrayList.class, "second argument raw ArrayList");
        check(uArg.getActualTypeArguments()[0] == String.class, "second argument nested String");

        ParameterizedType ifc = (ParameterizedType) G.class.getGenericInterfaces()[0];
        check(ifc.getRawType() == Comparable.class, "generic interface raw Comparable");
        ParameterizedType garg = (ParameterizedType) ifc.getActualTypeArguments()[0];
        check(garg.getRawType() == G.class, "interface argument raw G");
        check(garg.getActualTypeArguments()[0] instanceof TypeVariable,
              "interface argument holds the class TV");
        check(((TypeVariable<?>) garg.getActualTypeArguments()[0]).getName().equals("T"),
              "interface argument TV name T");
        check(((TypeVariable<?>) garg.getActualTypeArguments()[1]).getName().equals("U"),
              "interface argument TV name U");
    }

    static void testEqualityAndRawType() throws Exception {
        ParameterizedType pt = (ParameterizedType) Types.class.getDeclaredField("nested")
                .getGenericType();
        check(pt.equals(Types.class.getDeclaredField("nested").getGenericType()), "equals by value");
        check(pt.hashCode() == Types.class.getDeclaredField("nested").getGenericType().hashCode(),
              "hashCode consistent");
        check(pt.equals(pt), "equals reflexive");
        check(!pt.equals(null), "not equal to null");
        check(!pt.equals(Types.class.getDeclaredField("entry").getGenericType()),
              "distinct parameterizations unequal");
        Type rawT = Types.class.getDeclaredField("raw").getGenericType();
        check(rawT == List.class, "raw use yields the raw Class itself");
        check(!(rawT instanceof ParameterizedType), "raw use is not parameterized");
        ParameterizedType wpt = (ParameterizedType) Types.class.getDeclaredField("clazz")
                .getGenericType();
        check(wpt.getActualTypeArguments()[0] instanceof WildcardType, "Class<?> argument is wildcard");
        check(wpt.getActualTypeArguments()[0].getTypeName().equals("?"), "wildcard argument name");
        check(ParameterizedType.class.isInterface(), "ParameterizedType is an interface");
        check(Type.class.isAssignableFrom(ParameterizedType.class), "ParameterizedType extends Type");
        check(pt.toString().equals(pt.getTypeName()), "toString matches typeName");
    }

    public static void main(String[] args) throws Exception {
        testParameterizedType();
        testNestedParameterization();
        testOwnerType();
        testGenericSupers();
        testEqualityAndRawType();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
