/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.ApacheNodeSetData conformance: internal package reflection.
 * @modules java.xml.crypto/org.jcp.xml.dsig.internal.dom:+open
 * @run main ApacheNodeSetDataTest
 */
import java.lang.reflect.*;
import java.util.*;
public class ApacheNodeSetDataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.ApacheNodeSetData", false, ApacheNodeSetDataTest.class.getClassLoader());
        check(c.getName().equals("org.jcp.xml.dsig.internal.dom.ApacheNodeSetData"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("javax.xml.crypto.NodeSetData"), "iface:javax.xml.crypto.NodeSetData");
        check(ifaces.contains("org.jcp.xml.dsig.internal.dom.ApacheData"), "iface:org.jcp.xml.dsig.internal.dom.ApacheData");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("getXMLSignatureInput"), "method:getXMLSignatureInput");
        check(methods.contains("iterator"), "method:iterator");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
