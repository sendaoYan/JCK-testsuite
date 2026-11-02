/*
 * @test
 * @summary sun.misc.Unsafe conformance: unsafe.
 * @run main UnsafeTest
 */

public class UnsafeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        check(sun.misc.Unsafe.INVALID_FIELD_OFFSET == -1, "INVALID_FIELD_OFFSET");
        check(sun.misc.Unsafe.ADDRESS_SIZE == 8 || sun.misc.Unsafe.ADDRESS_SIZE == 4, "ADDRESS_SIZE");
        check(sun.misc.Unsafe.ARRAY_BYTE_INDEX_SCALE == 1, "ARRAY_BYTE_INDEX_SCALE");
        check(sun.misc.Unsafe.ARRAY_BOOLEAN_BASE_OFFSET >= 0, "ARRAY_BOOLEAN_BASE_OFFSET");
        boolean threw = false;
        try { sun.misc.Unsafe.getUnsafe(); } catch (SecurityException e) { threw = true; }
        check(threw, "getUnsafe throws SecurityException from app classloader");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
