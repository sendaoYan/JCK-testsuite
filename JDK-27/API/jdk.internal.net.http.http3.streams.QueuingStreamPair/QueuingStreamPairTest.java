/*
 * @test
 * @summary jdk.internal.net.http.http3.streams.QueuingStreamPair conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.http3.streams:+open
 * @run main QueuingStreamPairTest
 */
import java.lang.reflect.*;
import java.util.*;
public class QueuingStreamPairTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.http3.streams.QueuingStreamPair", false, QueuingStreamPairTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.http3.streams.QueuingStreamPair"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("jdk.internal.net.http.http3.streams.UniStreamPair"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("credit"), "method:credit");
        check(methods.contains("submitData"), "method:submitData");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("writerQueue"), "field:writerQueue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
