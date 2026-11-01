/*
 * @test
 * @summary java.lang.reflect.AccessibleObject conformance: setAccessible/isAccessible and
 *          annotation accessors via a Field.
 * @run main AccessibleObjectTest
 */

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;

public class AccessibleObjectTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Sample {
        public int pub;
    }

    static void testAccessibleObject() throws Exception {
        Field f = Sample.class.getField("pub");
        check(!f.isAccessible(), "not accessible by default");
        f.setAccessible(true);
        check(f.isAccessible(), "setAccessible(true)");
        f.setAccessible(false);
        check(!f.isAccessible(), "setAccessible(false)");
        check(f.getAnnotations().length == 0, "getAnnotations");
        check(f.getDeclaredAnnotations().length == 0, "getDeclaredAnnotations");
        check(!f.isAnnotationPresent(Deprecated.class), "isAnnotationPresent false");
        check(f.getAnnotation(Deprecated.class) == null, "getAnnotation null");

        AccessibleObject.setAccessible(new AccessibleObject[]{f}, true);
        check(f.isAccessible(), "static setAccessible(array)");
    }

    public static void main(String[] args) throws Exception {
        testAccessibleObject();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
