/*
 * @test
 * @summary java.lang.reflect.ParameterizedType conformance: raw type/actual type args/owner.
 * @run main ParameterizedTypeTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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

    public static void main(String[] args) throws Exception {
        testParameterizedType();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
