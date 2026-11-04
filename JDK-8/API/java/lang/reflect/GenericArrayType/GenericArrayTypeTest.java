/*
 * @test
 * @summary java.lang.reflect.GenericArrayType conformance: generic component type for TV and
 *          parameterized components, nested dimensions, method signatures, plain-array negatives.
 * @run main GenericArrayTypeTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;

public class GenericArrayTypeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Sample {
        List<String>[] arr;
    }

    static abstract class Holder<T> {
        T[] tField;
        T[][] matrix;
        List<String>[] lsField;
        List<?>[] wildField;
        String[] plain;
        int[] ints;
    }

    static <T> T[] make() {
        return null;
    }

    static <T> void take(T[] a) {
    }

    static void testGenericArrayType() throws Exception {
        Field f = Sample.class.getDeclaredField("arr");
        check(f.getGenericType() instanceof GenericArrayType, "is GenericArrayType");
        GenericArrayType gat = (GenericArrayType) f.getGenericType();
        check(gat.getGenericComponentType() instanceof ParameterizedType,
              "component is ParameterizedType");
    }

    // ---- deepened contracts (2026-11-05 batch) ----
    static void testComponentFlavors() throws Exception {
        GenericArrayType ls = (GenericArrayType) Sample.class.getDeclaredField("arr").getGenericType();
        ParameterizedType comp = (ParameterizedType) ls.getGenericComponentType();
        check(comp.getRawType() == List.class, "List<String>[] component raw List");
        check(comp.getActualTypeArguments()[0] == String.class, "component argument String");
        check(comp.getOwnerType() == null, "top-level component has no owner");
        check(comp instanceof Type, "component is a Type");
        check(ls.getTypeName().equals("java.util.List<java.lang.String>[]"), "List<String>[] typeName");
        check(ls.toString().equals(ls.getTypeName()), "toString matches typeName");

        GenericArrayType tvArr = (GenericArrayType) Holder.class.getDeclaredField("tField")
                .getGenericType();
        Type c = tvArr.getGenericComponentType();
        check(c instanceof TypeVariable, "T[] component is a TypeVariable");
        check(((TypeVariable<?>) c).getName().equals("T"), "component variable name T");
        check(c == Holder.class.getTypeParameters()[0], "component is the class type variable itself");
        check(tvArr.getTypeName().equals("T[]"), "T[] typeName");
        check(tvArr.getTypeName().endsWith("[]"), "T[] name ends with []");
        GenericArrayType lsFieldAgain = (GenericArrayType) Holder.class.getDeclaredField("lsField")
                .getGenericType();
        check(lsFieldAgain.equals(ls), "same-shape arrays from different holders equal");
    }

    static void testNestedDimensions() throws Exception {
        GenericArrayType mx = (GenericArrayType) Holder.class.getDeclaredField("matrix")
                .getGenericType();
        check(mx.getGenericComponentType() instanceof GenericArrayType, "T[][] peels one dimension");
        GenericArrayType inner = (GenericArrayType) mx.getGenericComponentType();
        check(inner instanceof GenericArrayType, "inner dimension is also a GAT");
        check(inner.getGenericComponentType() == Holder.class.getTypeParameters()[0],
              "innermost component is the type variable");
        check(inner.getTypeName().equals("T[]"), "inner dimension name");
        check(mx.getTypeName().equals("T[][]"), "T[][] typeName");
        check(mx.getTypeName().endsWith("[]"), "matrix name ends with []");

        GenericArrayType wArr = (GenericArrayType) Holder.class.getDeclaredField("wildField")
                .getGenericType();
        ParameterizedType wComp = (ParameterizedType) wArr.getGenericComponentType();
        check(wComp.getRawType() == List.class, "List<?>[] component raw List");
        check(wComp.getActualTypeArguments()[0] instanceof WildcardType,
              "component argument is a wildcard");
        check(wComp.getActualTypeArguments()[0].getTypeName().equals("?"), "component argument name");
        check(wArr.getTypeName().equals("java.util.List<?>[]"), "List<?>[] typeName");
    }

    static void testMethodSignatures() throws Exception {
        Method mk = GenericArrayTypeTest.class.getDeclaredMethod("make");
        Type rt = mk.getGenericReturnType();
        check(rt instanceof GenericArrayType, "generic return T[] is a GAT");
        check(rt.getTypeName().equals("T[]"), "return typeName");
        check(((GenericArrayType) rt).getGenericComponentType() instanceof TypeVariable,
              "return component is a TV");
        Method tk = GenericArrayTypeTest.class.getDeclaredMethod("take", Object[].class);
        Type pt = tk.getGenericParameterTypes()[0];
        check(pt instanceof GenericArrayType, "generic parameter T[] is a GAT");
        check(pt.getTypeName().equals("T[]"), "parameter typeName");
    }

    static void testPlainArraysAndEquality() throws Exception {
        Type plain = Holder.class.getDeclaredField("plain").getGenericType();
        check(plain == String[].class, "String[] generic type is its Class");
        check(!(plain instanceof GenericArrayType), "String[] is not a GAT");
        Type ints = Holder.class.getDeclaredField("ints").getGenericType();
        check(ints == int[].class, "int[] generic type is its Class");
        check(!(ints instanceof GenericArrayType), "int[] is not a GAT");
        GenericArrayType a = (GenericArrayType) Sample.class.getDeclaredField("arr").getGenericType();
        GenericArrayType b = (GenericArrayType) Sample.class.getDeclaredField("arr").getGenericType();
        check(a.equals(b), "GAT equals by value");
        check(a.hashCode() == b.hashCode(), "GAT hashCode consistent");
        check(a.equals(a), "equals reflexive");
        check(!a.equals(null), "not equal to null");
        check(!a.equals(Holder.class.getDeclaredField("tField").getGenericType()),
              "different components unequal");
        check(GenericArrayType.class.isInterface(), "GenericArrayType is an interface");
        check(Type.class.isAssignableFrom(GenericArrayType.class), "GenericArrayType extends Type");
    }

    public static void main(String[] args) throws Exception {
        testGenericArrayType();
        testComponentFlavors();
        testNestedDimensions();
        testMethodSignatures();
        testPlainArraysAndEquality();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
