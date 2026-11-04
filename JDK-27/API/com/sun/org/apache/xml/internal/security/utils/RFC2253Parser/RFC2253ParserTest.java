/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.utils.RFC2253Parser conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.utils:+open
 * @run main RFC2253ParserTest
 */
import java.lang.reflect.*;
import java.util.*;
public class RFC2253ParserTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.utils.RFC2253Parser", false, RFC2253ParserTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.utils.RFC2253Parser"), "name");
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
        check(methods.contains("normalize"), "method:normalize");
        check(methods.contains("rfc2253toXMLdsig"), "method:rfc2253toXMLdsig");
        check(methods.contains("xmldsigtoRFC2253"), "method:xmldsigtoRFC2253");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
