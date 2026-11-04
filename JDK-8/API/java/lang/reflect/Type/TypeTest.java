/*
 * @test
 * @summary java.lang.reflect.Type conformance: marker interface implemented by Class and
 *          generic types.
 * @run main TypeTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

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

    static void testType() throws Exception {
        Field pf = Sample.class.getDeclaredField("plain");
        check(pf.getGenericType() instanceof Type, "primitive generic type is a Type");
        check(pf.getGenericType() == int.class, "int class as Type");

        Field gf = Sample.class.getDeclaredField("generic");
        check(gf.getGenericType() instanceof Type, "generic type is a Type");
        check(gf.getGenericType() instanceof ParameterizedType, "generic type is parameterized");
    }

    public static void main(String[] args) throws Exception {
        testType();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
