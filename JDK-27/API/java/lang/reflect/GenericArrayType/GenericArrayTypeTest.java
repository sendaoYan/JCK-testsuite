/*
 * @test
 * @summary java.lang.reflect.GenericArrayType conformance: white-box structure + black-box behavior.
 * @run main GenericArrayTypeTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
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

    static class Holder<T> {
        T[] arrayOfT;
        List<T>[] arrayOfListT;
        String[] plain;
        T[][] matrix;
    }

    // ---- Part 1: white-box (structural, retained from previous revision) ----
    static void testWhiteBox() throws Exception {
        Field f = Sample.class.getDeclaredField("arr");
        check(f.getGenericType() instanceof GenericArrayType, "is GenericArrayType");
        GenericArrayType gat = (GenericArrayType) f.getGenericType();
        check(gat.getGenericComponentType() instanceof ParameterizedType,
              "component is ParameterizedType");
        check(GenericArrayType.class.isInterface(), "is an interface");
        check(Type.class.isAssignableFrom(GenericArrayType.class), "extends Type");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. component of a raw parameterized array
        GenericArrayType gat = (GenericArrayType) Sample.class.getDeclaredField("arr").getGenericType();
        ParameterizedType comp = (ParameterizedType) gat.getGenericComponentType();
        check(comp.getRawType() == List.class, "component raw List");
        check(comp.getActualTypeArguments()[0] == String.class, "component arg String");
        check(gat.getTypeName().equals("java.util.List<java.lang.String>[]"), "typeName");
        check(gat.toString().equals("java.util.List<java.lang.String>[]"), "toString");

        // 2. component is a type variable
        GenericArrayType tvArr = (GenericArrayType) Holder.class.getDeclaredField("arrayOfT").getGenericType();
        Type tvc = tvArr.getGenericComponentType();
        check(tvc instanceof TypeVariable, "component is TypeVariable");
        check("T".equals(tvc.getTypeName()), "component name T");
        check(((TypeVariable<?>) tvc).getGenericDeclaration() == Holder.class,
              "component TV declared by the class");
        check(tvArr.getTypeName().equals("T[]"), "T[] typeName");

        // 3. nested: List<T>[]
        GenericArrayType ltArr = (GenericArrayType) Holder.class.getDeclaredField("arrayOfListT").getGenericType();
        ParameterizedType ltc = (ParameterizedType) ltArr.getGenericComponentType();
        check(ltc.getRawType() == List.class, "List<T>[] component raw List");
        check(ltc.getActualTypeArguments()[0] instanceof TypeVariable, "List<T> arg is TV");
        check(ltArr.getTypeName().equals("java.util.List<T>[]"), "List<T>[] typeName");

        // 4. plain arrays are Classes, not GAT; multi-dim generic peels one layer at a time
        Type plain = Holder.class.getDeclaredField("plain").getGenericType();
        check(plain == String[].class, "String[] is a Class");
        check(!(plain instanceof GenericArrayType), "plain array not GAT");
        GenericArrayType matrix = (GenericArrayType) Holder.class.getDeclaredField("matrix").getGenericType();
        check(matrix.getGenericComponentType() instanceof GenericArrayType,
              "T[][] peels to T[] first");
        check(matrix.getGenericComponentType().getTypeName().equals("T[]"), "inner layer name");
        check(matrix.getTypeName().equals("T[][]"), "T[][] typeName");

        // 5. generic array as method return type and equality
        Method mk = GenericArrayTypeTest.class.getDeclaredMethod("make");
        Type rt = mk.getGenericReturnType();
        check(rt instanceof GenericArrayType, "method return is GAT");
        check(rt.getTypeName().equals("T[]"), "method return name");
        GenericArrayType again = (GenericArrayType) Holder.class.getDeclaredField("arrayOfT").getGenericType();
        check(tvArr.equals(again), "GAT equals by value");
        check(tvArr.hashCode() == again.hashCode(), "GAT hashCode consistent");
        check(!tvArr.equals(ltArr), "different GATs not equal");
    }

    static <T> T[] make() {
        return null;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
