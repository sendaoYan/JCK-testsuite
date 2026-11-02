/*
 * @test
 * @summary jdk.internal.net.http.qpack.readers.StringReader conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.qpack.readers:+open
 * @run main StringReaderTest
 */
import java.lang.reflect.*;
import java.util.*;
public class StringReaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.qpack.readers.StringReader", false, StringReaderTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.qpack.readers.StringReader"), "name");
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
        check(methods.contains("isHuffmanEncoded"), "method:isHuffmanEncoded");
        check(methods.contains("read"), "method:read");
        check(methods.contains("reset"), "method:reset");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
