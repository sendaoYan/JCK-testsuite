/*
 * @test
 * @summary java.lang.reflect.ParameterizedType conformance: white-box structure + black-box behavior.
 * @run main ParameterizedTypeTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
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
        List<String> list;
        Map<String, List<Integer>> nested;
        Map.Entry<String, Integer> entry;
        Outer.Inner<String> inner;
        Class<?> clazz;
    }

    static class Outer {
        class Inner<Q> {
        }
    }

    static class StringList extends ArrayList<String> {
    }

    static class G<T extends Number, U extends List<String>> implements Comparable<G<T, U>> {
        public int compareTo(G<T, U> other) {
            return 0;
        }
    }

    static class SubG extends G<Integer, ArrayList<String>> {
    }

    static class Raw {
        List raw;
    }

    // ---- Part 1: white-box (structural, retained from previous revision) ----
    static void testWhiteBox() throws Exception {
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
        check(ParameterizedType.class.isInterface(), "is an interface");
        check(ParameterizedTypeTest.countAbstract(ParameterizedType.class) == 3,
              "three abstract accessors");
    }

    static int countAbstract(Class<?> c) {
        int n = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods())
            if (java.lang.reflect.Modifier.isAbstract(m.getModifiers())) n++;
        return n;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. simple parameterized field
        ParameterizedType pt = (ParameterizedType) Types.class.getDeclaredField("list").getGenericType();
        check(pt.getRawType() == List.class, "list raw type");
        check(pt.getActualTypeArguments().length == 1
              && pt.getActualTypeArguments()[0] == String.class, "list single arg String");
        check(pt.getOwnerType() == null, "top-level type has no owner");
        check(pt.getTypeName().equals("java.util.List<java.lang.String>"), "list typeName");

        // 2. nested parameterization resolves recursively
        ParameterizedType npt = (ParameterizedType) Types.class.getDeclaredField("nested").getGenericType();
        check(npt.getRawType() == Map.class, "nested raw Map");
        Type arg1 = npt.getActualTypeArguments()[1];
        check(arg1 instanceof ParameterizedType, "nested arg is PT");
        check(((ParameterizedType) arg1).getRawType() == List.class, "inner raw List");
        check(((ParameterizedType) arg1).getActualTypeArguments()[0] == Integer.class,
              "inner arg Integer");
        check(npt.getTypeName()
              .equals("java.util.Map<java.lang.String, java.util.List<java.lang.Integer>>"),
              "nested typeName");

        // 3. owner type for inner generic types
        ParameterizedType ept = (ParameterizedType) Types.class.getDeclaredField("entry").getGenericType();
        check(ept.getOwnerType() == Map.class, "Map.Entry owner is Map");
        check(ept.getRawType() == Map.Entry.class, "raw is Map.Entry");
        check(ept.getActualTypeArguments()[0] == String.class
              && ept.getActualTypeArguments()[1] == Integer.class, "entry args");
        ParameterizedType ipt = (ParameterizedType) Types.class.getDeclaredField("inner").getGenericType();
        check(ipt.getOwnerType() == Outer.class, "Outer.Inner owner is Outer");
        check(ipt.getRawType() == Outer.Inner.class, "raw is Outer.Inner");
        check(ipt.getActualTypeArguments()[0] == String.class, "inner arg String");

        // 4. wildcard argument keeps its own structure
        ParameterizedType wpt = (ParameterizedType) Types.class.getDeclaredField("clazz").getGenericType();
        Type warg = wpt.getActualTypeArguments()[0];
        check(warg instanceof java.lang.reflect.WildcardType, "Class<?> arg is wildcard");
        check(warg.getTypeName().equals("?"), "unbounded wildcard name");

        // 5. superclass parameterization
        ParameterizedType spt = (ParameterizedType) StringList.class.getGenericSuperclass();
        check(spt.getRawType() == ArrayList.class, "generic superclass raw ArrayList");
        check(spt.getActualTypeArguments()[0] == String.class, "superclass arg String");
        check(spt.getOwnerType() == null, "superclass no owner");

        // 6. equality and string forms across repeated lookups
        ParameterizedType pt2 = (ParameterizedType) Types.class.getDeclaredField("list").getGenericType();
        check(pt.equals(pt2), "PT equals by value");
        check(pt.hashCode() == pt2.hashCode(), "PT hashCode consistent");
        check(pt.toString().equals("java.util.List<java.lang.String>"), "PT toString");
        check(!pt.equals(npt), "different PTs not equal");
    }

    // ---- Part 3: deepened contracts (2026-11-05 batch) ----
    static void testTwoArgSuperclassAndInterface() {
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

    static void testRawAndEqualityEdges() throws Exception {
        Type rawT = Raw.class.getDeclaredField("raw").getGenericType();
        check(rawT == List.class, "raw use yields the raw Class");
        check(!(rawT instanceof ParameterizedType), "raw use is not parameterized");
        ParameterizedType pt = (ParameterizedType) Types.class.getDeclaredField("list")
                .getGenericType();
        check(pt.equals(pt), "equals reflexive");
        check(!pt.equals(null), "not equal to null");
        check(!pt.equals(List.class), "PT not equal to its raw Class");
        check(pt.getActualTypeArguments()[0] instanceof Type, "argument is a Type");
        check(pt.getTypeName().startsWith("java.util.List<"), "typeName prefix");
        check(pt.getTypeName().endsWith("java.lang.String>"), "typeName suffix");
        check(ParameterizedType.class.getDeclaredMethods().length == 3, "three declared accessors");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testTwoArgSuperclassAndInterface();
        testRawAndEqualityEdges();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
