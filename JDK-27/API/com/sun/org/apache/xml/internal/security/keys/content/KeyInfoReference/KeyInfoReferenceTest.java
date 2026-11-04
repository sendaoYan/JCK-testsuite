/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.keys.content.KeyInfoReference conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.keys.content:+open
 * @run main KeyInfoReferenceTest
 */
import java.lang.reflect.*;
import java.util.*;
public class KeyInfoReferenceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.keys.content.KeyInfoReference", false, KeyInfoReferenceTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.keys.content.KeyInfoReference"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.security.utils.Signature11ElementProxy"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("com.sun.org.apache.xml.internal.security.keys.content.KeyInfoContent"), "iface:com.sun.org.apache.xml.internal.security.keys.content.KeyInfoContent");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("getBaseLocalName"), "method:getBaseLocalName");
        check(methods.contains("getId"), "method:getId");
        check(methods.contains("getURI"), "method:getURI");
        check(methods.contains("getURIAttr"), "method:getURIAttr");
        check(methods.contains("setId"), "method:setId");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
