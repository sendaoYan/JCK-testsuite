/*
 * @test
 * @summary java.net.URLClassLoader conformance: urls/close.
 * @run main URLClassLoaderTest
 */


public class URLClassLoaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.io.File dir = new java.io.File("/tmp");
        java.net.URLClassLoader cl = new java.net.URLClassLoader(new java.net.URL[]{dir.toURI().toURL()});
        check(cl.getURLs().length == 1, "getURLs");
        cl.close();
        java.net.URLClassLoader cl2 = new java.net.URLClassLoader(new java.net.URL[0]);
        check(cl2.getURLs().length == 0, "empty urls");
        cl2.close();        System.out.println("PASS (" + passed + " assertions)");
    }
}
