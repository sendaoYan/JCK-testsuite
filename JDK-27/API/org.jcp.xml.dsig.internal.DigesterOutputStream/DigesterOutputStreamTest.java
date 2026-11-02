/*
 * @test
 * @summary org.jcp.xml.dsig.internal.DigesterOutputStream conformance: internal package reflection.
 * @modules java.xml.crypto/org.jcp.xml.dsig.internal:+open
 * @run main DigesterOutputStreamTest
 */
import java.lang.reflect.*;
import java.util.*;
public class DigesterOutputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.DigesterOutputStream", false, DigesterOutputStreamTest.class.getClassLoader());
        check(c.getName().equals("org.jcp.xml.dsig.internal.DigesterOutputStream"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.io.OutputStream"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("close"), "method:close");
        check(methods.contains("getDigestValue"), "method:getDigestValue");
        check(methods.contains("getInputStream"), "method:getInputStream");
        check(methods.contains("write"), "method:write");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
