/*
 * @test
 * @summary JLS 12.7 (Java SE 8): classes loaded by the bootstrap loader may not be
 *          unloaded; they have a null class loader and remain usable.
 * @run main BootstrapLoaderNotUnloaded
 */

public class BootstrapLoaderNotUnloaded {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(String.class.getClassLoader() == null, "bootstrap loader should be null");
        check("abc".equals(String.valueOf(new char[] { 'a', 'b', 'c' })), "String remains usable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
