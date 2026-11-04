/*
 * @test
 * @summary java.lang.reflect.Type conformance: white-box structure + black-box behavior.
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
        Map<String, List<Integer>> nested;
        List<? extends Number> bounded;
        String[] arrayOfString;
    }

    static class GenericBox<T, U extends Number> {
        T first;
        U second;
        T[] arrayOfT;
        List<String>[] arrayOfList;
    }

    static class Extra {
        int[] ints;
    }

    // ---- Part 1: white-box (structural, retained from previous revision) ----
    static void testWhiteBox() throws Exception {
        Field pf = Sample.class.getDeclaredField("plain");
        check(pf.getGenericType() instanceof Type, "primitive generic type is a Type");
        check(pf.getGenericType() == int.class, "int class as Type");

        Field gf = Sample.class.getDeclaredField("generic");
        check(gf.getGenericType() instanceof Type, "generic type is a Type");
        check(gf.getGenericType() instanceof ParameterizedType, "generic type is parameterized");
        check(Type.class.isInterface(), "Type is an interface");
        int abstractCount = 0;
        for (Method m : Type.class.getMethods())
            if (!m.isDefault() && java.lang.reflect.Modifier.isAbstract(m.getModifiers()))
                abstractCount++;
        check(abstractCount == 0, "no required abstract methods (getTypeName has default)");
        check(Type.class.getMethod("getTypeName").isDefault(), "getTypeName is a default method");
        boolean widelyImplemented = List.class instanceof Type && String.class instanceof Type;
        check(widelyImplemented, "Class instances are Types");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. getTypeName for classes: primitives, arrays, nested arrays
        check(int.class.getTypeName().equals("int"), "primitive typeName");
        check(String.class.getTypeName().equals("java.lang.String"), "class typeName binary");
        check(String[].class.getTypeName().equals("java.lang.String[]"), "array typeName");
        check(String[][].class.getTypeName().equals("java.lang.String[][]"), "nested array typeName");
        check(void.class.getTypeName().equals("void"), "void typeName");

        // 2. getTypeName for each reflective Type flavor (exact strings)
        Field gen = Sample.class.getDeclaredField("generic");
        Type pt = gen.getGenericType();
        check(pt.getTypeName().equals("java.util.List<java.lang.String>"), "PT typeName");
        Field nst = Sample.class.getDeclaredField("nested");
        check(nst.getGenericType().getTypeName()
              .equals("java.util.Map<java.lang.String, java.util.List<java.lang.Integer>>"),
              "nested PT typeName");
        Field bnd = Sample.class.getDeclaredField("bounded");
        Type wt = ((ParameterizedType) bnd.getGenericType()).getActualTypeArguments()[0];
        check(wt instanceof WildcardType, "wildcard is a Type");
        check(wt.getTypeName().equals("? extends java.lang.Number"), "wildcard typeName");
        Field aot = GenericBox.class.getDeclaredField("arrayOfT");
        Type gat = aot.getGenericType();
        check(gat instanceof GenericArrayType, "T[] is GenericArrayType");
        check(gat.getTypeName().equals("T[]"), "GAT of type variable typeName");
        Field aol = GenericBox.class.getDeclaredField("arrayOfList");
        check(aol.getGenericType().getTypeName().equals("java.util.List<java.lang.String>[]"),
              "GAT of PT typeName");
        TypeVariable<?> tv = GenericBox.class.getTypeParameters()[0];
        check(tv instanceof Type, "type variable is a Type");
        check(tv.getTypeName().equals("T"), "TV typeName");

        // 3. toString of reflective types matches getTypeName content
        check(pt.toString().equals("java.util.List<java.lang.String>"), "PT toString");
        check(gat.toString().equals("T[]"), "GAT toString");
        check(tv.toString().equals("T"), "TV toString");

        // 4. type identity across access paths
        Field again = Sample.class.getDeclaredField("generic");
        check(again.getGenericType().equals(pt) || again.getGenericType().getTypeName().equals(pt.getTypeName()),
              "repeated lookup yields equal type");
        Method decl = TypeTest.class.getDeclaredMethod("swap", Object.class, Object.class);
        Type[] gps = decl.getGenericParameterTypes();
        check(gps.length == 2 && gps[0] instanceof TypeVariable, "method generic params are Types");
        check(gps[0].getTypeName().equals("X") && gps[1].getTypeName().equals("X"),
              "both params share method type variable X");

        // 5. arrays of class vs generic array distinction
        Field aos = Sample.class.getDeclaredField("arrayOfString");
        check(aos.getGenericType() instanceof Class, "String[] generic type is Class (not GAT)");
        check(aos.getGenericType() == String[].class, "String[] equals its Class");
        check(!(aos.getGenericType() instanceof GenericArrayType), "plain arrays are not GAT");
    }

    // ---- Part 3: deepened contract matrix (2026-11-05 batch) ----
    static void testContractMatrix() throws Exception {
        // 1. every flavor of Type from self-made samples is a Type
        Object[] flavors = {
            String.class,
            Sample.class.getDeclaredField("generic").getGenericType(),
            GenericBox.class.getTypeParameters()[0],
            ((ParameterizedType) Sample.class.getDeclaredField("bounded").getGenericType())
                    .getActualTypeArguments()[0],
            GenericBox.class.getDeclaredField("arrayOfT").getGenericType(),
        };
        for (int i = 0; i < flavors.length; i++) {
            check(flavors[i] instanceof Type, "matrix flavor " + i + " is a Type");
        }
        check(flavors[2] instanceof TypeVariable && flavors[3] instanceof WildcardType,
              "matrix middle flavors are TV and wildcard");
        check(flavors[4] instanceof GenericArrayType, "matrix last flavor is GAT");

        // 2. nested classes: getTypeName is the binary name ($ separated)
        check(Map.Entry.class.getTypeName().equals("java.util.Map$Entry"), "Map.Entry typeName");
        check(TypeTest.class.getTypeName().equals(TypeTest.class.getName()),
              "typeName equals getName for classes");
        check(Map.Entry.class.getTypeName().equals(Map.Entry.class.getName()),
              "nested interface typeName equals getName");

        // 3. interface surface: exactly one declared method, default, no args
        Method[] ms = Type.class.getDeclaredMethods();
        check(ms.length == 1 && ms[0].getName().equals("getTypeName"), "single declared method");
        check(ms[0].getParameterCount() == 0 && ms[0].getReturnType() == String.class,
              "getTypeName shape");

        // 4. strict equality and hashCode across independent lookups
        Field g1 = Sample.class.getDeclaredField("generic");
        Field g2 = Sample.class.getDeclaredField("generic");
        check(g1.getGenericType().equals(g2.getGenericType()), "strict PT equals");
        check(g1.getGenericType().hashCode() == g2.getGenericType().hashCode(), "strict PT hashCode");

        // 5. generic array names always end with []
        check(GenericBox.class.getDeclaredField("arrayOfT").getGenericType().getTypeName()
              .endsWith("[]"), "T[] suffix");
        check(GenericBox.class.getDeclaredField("arrayOfList").getGenericType().getTypeName()
              .endsWith("[]"), "List<String>[] suffix");

        // 6. primitive arrays stay Classes
        Type ints = Extra.class.getDeclaredField("ints").getGenericType();
        check(ints == int[].class, "int[] is its Class");
        check(!(ints instanceof GenericArrayType), "primitive array not GAT");
    }

    public static <X> void swap(X a, X b) {
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
