/*
 * @test
 * @summary jdk.internal.net.http.quic.QuicConnectionIdFactory conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic:+open
 * @run main QuicConnectionIdFactoryTest
 */
import java.lang.reflect.*;
import java.util.*;
public class QuicConnectionIdFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.QuicConnectionIdFactory", false, QuicConnectionIdFactoryTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.QuicConnectionIdFactory"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("connectionIdLength"), "method:connectionIdLength");
        check(methods.contains("getClient"), "method:getClient");
        check(methods.contains("getConnectionIdLength"), "method:getConnectionIdLength");
        check(methods.contains("getConnectionIdToken"), "method:getConnectionIdToken");
        check(methods.contains("getServer"), "method:getServer");
        check(methods.contains("newConnectionId"), "method:newConnectionId");
        check(methods.contains("newToken"), "method:newToken");
        check(methods.contains("statelessReset"), "method:statelessReset");
        check(methods.contains("statelessTokenFor"), "method:statelessTokenFor");
        check(methods.contains("unsafeConnectionIdFor"), "method:unsafeConnectionIdFor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
