/*
 * @test
 * @summary java.lang.reflect.Type conformance: marker interface implemented by Class and
 *          generic types; getTypeName exact strings, flavor matrix, interface surface.
 * @run main TypeTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.Map;

public class TypeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Sample {
        int plain;
        List<String> generic;
    }

    static class Deep {
        Map<String, List<Integer>> nested;
        List<? extends Number> bounded;
        String[] arrayOfString;
        int[] ints;
    }

    static class Box<T> {
        T[] arrayOfT;
        List<String>[] arrayOfList;
    }

    static class Enclosing {
        class Inner {
        }
    }

    static void testType() throws Exception {
        Field pf = Sample.class.getDeclaredField("plain");
        check(pf.getGenericType() instanceof Type, "primitive generic type is a Type");
        check(pf.getGenericType() == int.class, "int class as Type");

        Field gf = Sample.class.getDeclaredField("generic");
        check(gf.getGenericType() instanceof Type, "generic type is a Type");
        check(gf.getGenericType() instanceof ParameterizedType, "generic type is parameterized");
    }

    // ---- deepened contracts (2026-11-05 batch) ----
    static void testTypeFlavorInstances() throws Exception {
        Object[] flavors = {
            String.class,
            Sample.class.getDeclaredField("generic").getGenericType(),
            Box.class.getTypeParameters()[0],
            ((ParameterizedType) Deep.class.getDeclaredField("bounded").getGenericType())
                    .getActualTypeArguments()[0],
            Box.class.getDeclaredField("arrayOfT").getGenericType(),
        };
        check(flavors.length == 5, "five flavors collected");
        for (int i = 0; i < flavors.length; i++) {
            check(flavors[i] instanceof Type, "flavor " + i + " is a Type");
        }
        check(flavors[0] instanceof Class, "Class flavor");
        check(flavors[1] instanceof ParameterizedType, "ParameterizedType flavor");
        check(flavors[2] instanceof TypeVariable, "TypeVariable flavor");
        check(flavors[3] instanceof WildcardType, "WildcardType flavor");
        check(flavors[4] instanceof GenericArrayType, "GenericArrayType flavor");
        check(!(flavors[0] instanceof ParameterizedType), "Class is not a ParameterizedType");
        check(!(flavors[3] instanceof ParameterizedType), "wildcard is not a ParameterizedType");
        check(!(flavors[2] instanceof GenericArrayType), "type variable is not a GenericArrayType");
        check(int.class instanceof Type, "primitive class is a Type");
        check(int[].class instanceof Type, "array class is a Type");
        check(void.class instanceof Type, "void class is a Type");
    }

    static void testGetTypeNameContracts() throws Exception {
        check(int.class.getTypeName().equals("int"), "primitive typeName");
        check(String.class.getTypeName().equals("java.lang.String"), "class typeName is binary name");
        check(String.class.getTypeName().equals(String.class.getName()), "typeName equals getName");
        check(String[].class.getTypeName().equals("java.lang.String[]"), "array typeName");
        check(String[][].class.getTypeName().equals("java.lang.String[][]"), "nested array typeName");
        check(void.class.getTypeName().equals("void"), "void typeName");
        check(Enclosing.Inner.class.getTypeName().equals(Enclosing.Inner.class.getName()),
              "nested class typeName is the binary name");
        check(Enclosing.Inner.class.getTypeName().endsWith("$Inner"), "nested class uses $ separator");
        check(Map.Entry.class.getTypeName().equals("java.util.Map$Entry"), "Map.Entry typeName");

        Type pt = Sample.class.getDeclaredField("generic").getGenericType();
        check(pt.getTypeName().equals("java.util.List<java.lang.String>"), "PT typeName");
        Type npt = Deep.class.getDeclaredField("nested").getGenericType();
        check(npt.getTypeName().equals("java.util.Map<java.lang.String, java.util.List<java.lang.Integer>>"),
              "nested PT typeName");
        check(npt.getTypeName().startsWith("java.util.Map<java.lang.String"), "nested PT typeName prefix");
        Type gat = Box.class.getDeclaredField("arrayOfT").getGenericType();
        check(gat.getTypeName().equals("T[]"), "GAT of TV typeName");
        check(gat.getTypeName().endsWith("[]"), "GAT name ends with []");
        Type aol = Box.class.getDeclaredField("arrayOfList").getGenericType();
        check(aol.getTypeName().equals("java.util.List<java.lang.String>[]"), "GAT of PT typeName");
        Type tv = Box.class.getTypeParameters()[0];
        check(tv.getTypeName().equals("T"), "TV typeName");
        Type wc = ((ParameterizedType) Deep.class.getDeclaredField("bounded").getGenericType())
                .getActualTypeArguments()[0];
        check(wc.getTypeName().equals("? extends java.lang.Number"), "wildcard typeName");
        check(pt.toString().equals(pt.getTypeName()), "PT toString matches typeName");
        check(gat.toString().equals(gat.getTypeName()), "GAT toString matches typeName");
        check(tv.toString().equals(tv.getTypeName()), "TV toString matches typeName");
        check(wc.toString().equals(wc.getTypeName()), "wildcard toString matches typeName");

        Type aos = Deep.class.getDeclaredField("arrayOfString").getGenericType();
        check(aos instanceof Class, "String[] generic type is a Class");
        check(aos == String[].class, "String[] equals its Class");
        check(!(aos instanceof GenericArrayType), "plain array is not a GAT");
        check(Deep.class.getDeclaredField("ints").getGenericType() == int[].class,
              "primitive array generic type stays a Class");
        check(!(Deep.class.getDeclaredField("ints").getGenericType() instanceof GenericArrayType),
              "primitive array is not a GAT");
        check(Sample.class.getDeclaredField("generic").getGenericType()
              .equals(Sample.class.getDeclaredField("generic").getGenericType()),
              "repeated lookup yields equal type");
    }

    static void testInterfaceSurface() throws Exception {
        check(Type.class.isInterface(), "Type is an interface");
        Method[] ms = Type.class.getDeclaredMethods();
        check(ms.length == 1, "exactly one declared method");
        check(ms[0].getName().equals("getTypeName"), "declared method is getTypeName");
        check(ms[0].isDefault(), "getTypeName has a default implementation");
        check(ms[0].getReturnType() == String.class, "getTypeName returns String");
        check(ms[0].getParameterCount() == 0, "getTypeName takes no parameters");
        check(Type.class.isAssignableFrom(Class.class), "Class implements Type");
        check(Type.class.isAssignableFrom(ParameterizedType.class), "ParameterizedType is a Type");
        check(Type.class.isAssignableFrom(TypeVariable.class), "TypeVariable is a Type");
        check(Type.class.isAssignableFrom(WildcardType.class), "WildcardType is a Type");
        check(Type.class.isAssignableFrom(GenericArrayType.class), "GenericArrayType is a Type");
    }

    public static void main(String[] args) throws Exception {
        testType();
        testTypeFlavorInstances();
        testGetTypeNameContracts();
        testInterfaceSurface();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
