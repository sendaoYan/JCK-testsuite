/*
 * @test
 * @summary javax.net.ssl.TrustManager conformance: marker interface contract.
 * @run main TrustManagerTest
 */
import java.lang.reflect.*;
public class TrustManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.TrustManager");
        check(c.isInterface() && Modifier.isPublic(c.getModifiers()), "public interface");
        check(c.getDeclaredMethods().length == 0, "marker interface: no declared methods");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        javax.net.ssl.TrustManager tm = new javax.net.ssl.TrustManager() { };
        check(tm instanceof javax.net.ssl.TrustManager, "anonymous implementation assignable");
        javax.net.ssl.TrustManager[] arr = new javax.net.ssl.TrustManager[] { tm };
        check(arr[0] == tm, "array element semantics");
        check(javax.net.ssl.X509TrustManager.class.getInterfaces().length == 1
                && javax.net.ssl.X509TrustManager.class.getInterfaces()[0].getName().equals("javax.net.ssl.TrustManager"),
                "X509TrustManager extends TrustManager");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
