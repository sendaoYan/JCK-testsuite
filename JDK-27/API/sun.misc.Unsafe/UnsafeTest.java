/*
 * @test
 * @summary sun.misc.Unsafe conformance: white-box structure + black-box behavior.
 * @run main UnsafeTest
 */

import java.lang.reflect.*;
import java.util.*;
public class UnsafeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("sun.misc.Unsafe");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(hasMethod(c, "getInt"), "getInt");
        check(hasMethod(c, "putInt"), "putInt");
        check(hasMethod(c, "getObject"), "getObject");
        check(hasMethod(c, "putObject"), "putObject");
        check(hasMethod(c, "allocateMemory"), "allocateMemory");
        check(hasMethod(c, "freeMemory"), "freeMemory");
        check(hasMethod(c, "objectFieldOffset"), "objectFieldOffset");
        check(hasMethod(c, "arrayIndexScale"), "arrayIndexScale");

    }

    static void testBlackBox() throws Exception {
        check(sun.misc.Unsafe.INVALID_FIELD_OFFSET == -1, "INVALID_FIELD_OFFSET");
        check(sun.misc.Unsafe.ADDRESS_SIZE == 8 || sun.misc.Unsafe.ADDRESS_SIZE == 4, "ADDRESS_SIZE");
        check(sun.misc.Unsafe.ARRAY_BYTE_INDEX_SCALE == 1, "ARRAY_BYTE_INDEX_SCALE");
        check(sun.misc.Unsafe.ARRAY_BOOLEAN_BASE_OFFSET >= 0, "ARRAY_BOOLEAN_BASE_OFFSET");
        boolean threw = false;
        try { sun.misc.Unsafe.getUnsafe(); } catch (SecurityException e) { threw = true; }
        check(threw, "getUnsafe throws SecurityException from app classloader");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
