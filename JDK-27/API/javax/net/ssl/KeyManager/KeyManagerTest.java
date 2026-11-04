/*
 * @test
 * @summary javax.net.ssl.KeyManager conformance: marker interface contract.
 * @run main KeyManagerTest
 */
import java.lang.reflect.*;
public class KeyManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.KeyManager");
        check(c.isInterface() && Modifier.isPublic(c.getModifiers()), "public interface");
        check(c.getDeclaredMethods().length == 0, "marker interface: no declared methods");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        // implementable with a lambda-free anonymous class and usable in arrays/casts
        javax.net.ssl.KeyManager km = new javax.net.ssl.KeyManager() { };
        check(km instanceof javax.net.ssl.KeyManager, "anonymous implementation assignable");
        javax.net.ssl.KeyManager[] arr = new javax.net.ssl.KeyManager[] { km, null };
        check(arr.length == 2 && arr[0] == km && arr[1] == null, "array element semantics incl. null");
        // the base interface of every specific manager type
        check(javax.net.ssl.X509KeyManager.class.getInterfaces().length == 1
                && javax.net.ssl.X509KeyManager.class.getInterfaces()[0].getName().equals("javax.net.ssl.KeyManager"),
                "X509KeyManager extends KeyManager");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
