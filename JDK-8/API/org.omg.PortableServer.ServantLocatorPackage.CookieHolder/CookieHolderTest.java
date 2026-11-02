/*
 * @test
 * @summary org.omg.PortableServer.ServantLocatorPackage.CookieHolder conformance: holder value field.
 * @run main CookieHolderTest
 */

public class CookieHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.PortableServer.ServantLocatorPackage.CookieHolder h0 = new org.omg.PortableServer.ServantLocatorPackage.CookieHolder();
        check(h0.value == null, "no-arg default value");
        java.lang.Object v = null;
        org.omg.PortableServer.ServantLocatorPackage.CookieHolder h1 = new org.omg.PortableServer.ServantLocatorPackage.CookieHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
