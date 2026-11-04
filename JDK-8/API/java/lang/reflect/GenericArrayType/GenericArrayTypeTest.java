/*
 * @test
 * @summary java.lang.reflect.GenericArrayType conformance: generic component type.
 * @run main GenericArrayTypeTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
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

    static void testGenericArrayType() throws Exception {
        Field f = Sample.class.getDeclaredField("arr");
        check(f.getGenericType() instanceof GenericArrayType, "is GenericArrayType");
        GenericArrayType gat = (GenericArrayType) f.getGenericType();
        check(gat.getGenericComponentType() instanceof ParameterizedType,
              "component is ParameterizedType");
    }

    public static void main(String[] args) throws Exception {
        testGenericArrayType();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
