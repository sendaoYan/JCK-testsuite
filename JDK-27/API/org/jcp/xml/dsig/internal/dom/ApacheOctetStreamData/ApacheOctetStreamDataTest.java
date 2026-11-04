/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.ApacheOctetStreamData conformance: internal package reflection.
 * @modules java.xml.crypto/org.jcp.xml.dsig.internal.dom:+open
 * @run main ApacheOctetStreamDataTest
 */
import java.lang.reflect.*;
import java.util.*;
public class ApacheOctetStreamDataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.ApacheOctetStreamData", false, ApacheOctetStreamDataTest.class.getClassLoader());
        check(c.getName().equals("org.jcp.xml.dsig.internal.dom.ApacheOctetStreamData"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("javax.xml.crypto.OctetStreamData"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("org.jcp.xml.dsig.internal.dom.ApacheData"), "iface:org.jcp.xml.dsig.internal.dom.ApacheData");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("getXMLSignatureInput"), "method:getXMLSignatureInput");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
