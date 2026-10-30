/*
 * @test
 * @summary JLS 12.7: classes loaded by the bootstrap loader may not be unloaded;
 *          they remain available and have a null class loader.
 * @run main BootstrapLoaderNotUnloaded
 */

public class BootstrapLoaderNotUnloaded {
    public static void main(String[] args) {
        if (String.class.getClassLoader() != null)
            throw new AssertionError("bootstrap loader should be null");
        // A bootstrap-loaded class remains usable.
        if (!"abc".equals(String.valueOf(new char[] { 'a', 'b', 'c' })))
            throw new AssertionError("String should be available");
        System.out.println("PASS");
    }
}
