/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.keys.content.RetrievalMethod conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.keys.content:+open
 * @run main RetrievalMethodTest
 */
import java.lang.reflect.*;
import java.util.*;
public class RetrievalMethodTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.keys.content.RetrievalMethod", false, RetrievalMethodTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.keys.content.RetrievalMethod"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.security.utils.SignatureElementProxy"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("com.sun.org.apache.xml.internal.security.keys.content.KeyInfoContent"), "iface:com.sun.org.apache.xml.internal.security.keys.content.KeyInfoContent");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("getBaseLocalName"), "method:getBaseLocalName");
        check(methods.contains("getTransforms"), "method:getTransforms");
        check(methods.contains("getType"), "method:getType");
        check(methods.contains("getURI"), "method:getURI");
        check(methods.contains("getURIAttr"), "method:getURIAttr");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("TYPE_DSA"), "field:TYPE_DSA");
        check(fields.contains("TYPE_MGMT"), "field:TYPE_MGMT");
        check(fields.contains("TYPE_PGP"), "field:TYPE_PGP");
        check(fields.contains("TYPE_RAWX509"), "field:TYPE_RAWX509");
        check(fields.contains("TYPE_RSA"), "field:TYPE_RSA");
        check(fields.contains("TYPE_SPKI"), "field:TYPE_SPKI");
        check(fields.contains("TYPE_X509"), "field:TYPE_X509");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
