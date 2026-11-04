/*
 * @test
 * @summary jdk.internal.net.http.qpack.QPACK.StreamPairSupplier conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.qpack:+open
 * @run main StreamPairSupplierTest
 */
import java.lang.reflect.*;
import java.util.*;
public class StreamPairSupplierTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.qpack.QPACK$StreamPairSupplier", false, StreamPairSupplierTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.qpack.QPACK$StreamPairSupplier"), "name");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() == null, "superclass-null");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("create"), "method:create");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
