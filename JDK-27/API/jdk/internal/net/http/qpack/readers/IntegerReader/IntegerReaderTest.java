/*
 * @test
 * @summary jdk.internal.net.http.qpack.readers.IntegerReader conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.qpack.readers:+open
 * @run main IntegerReaderTest
 */
import java.lang.reflect.*;
import java.util.*;
public class IntegerReaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.qpack.readers.IntegerReader", false, IntegerReaderTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.qpack.readers.IntegerReader"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("configure"), "method:configure");
        check(methods.contains("get"), "method:get");
        check(methods.contains("read"), "method:read");
        check(methods.contains("reset"), "method:reset");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("QPACK_MAX_INTEGER_VALUE"), "field:QPACK_MAX_INTEGER_VALUE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
