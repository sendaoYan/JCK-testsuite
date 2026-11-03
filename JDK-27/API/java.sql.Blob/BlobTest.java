/*
 * @test
 * @summary java.sql.Blob conformance: white-box structure + black-box behavior.
 * @run main BlobTest
 */
public class BlobTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    interface Throwing { void run() throws Throwable; }
    static void checkThrows(Class<? extends Throwable> t, String label, Throwing r) {
        try { r.run(); } catch (Throwable x) { check(t.isInstance(x), label + " throws " + t.getSimpleName()); return; }
        throw new AssertionError(label + ": expected " + t.getSimpleName());
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.sql.Blob");
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 1, "1 direct superinterface(s)");
        check(ifs[0].getName().equals("java.lang.AutoCloseable"), "superinterface 0");
        check(c.getDeclaredMethods().length == 12, "declared method count 12");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.sql.rowset.serial.SerialBlob b = new javax.sql.rowset.serial.SerialBlob(new byte[]{1, 2, 3, 4, 5});
        check(b.length() == 5, "length");
        check(java.util.Arrays.equals(b.getBytes(1, 5), new byte[]{1, 2, 3, 4, 5}), "getBytes full");
        check(java.util.Arrays.equals(b.getBytes(2, 3), new byte[]{2, 3, 4}), "getBytes slice");
        check(b.position(new byte[]{3}, 1) == 3, "position found");
        check(b.position(new byte[]{9}, 1) == -1, "position absent");
        b.truncate(3);
        check(b.length() == 3, "truncate");
        b.free();
        check(java.sql.Blob.class.isAssignableFrom(javax.sql.rowset.serial.SerialBlob.class), "SerialBlob implements Blob");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
