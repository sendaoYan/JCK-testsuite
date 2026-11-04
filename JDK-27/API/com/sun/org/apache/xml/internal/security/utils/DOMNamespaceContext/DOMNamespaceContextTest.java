/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.utils.DOMNamespaceContext conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.utils:+open
 * @run main DOMNamespaceContextTest
 */
import java.lang.reflect.*;
import java.util.*;
public class DOMNamespaceContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.utils.DOMNamespaceContext", false, DOMNamespaceContextTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.utils.DOMNamespaceContext"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("javax.xml.namespace.NamespaceContext"), "iface:javax.xml.namespace.NamespaceContext");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("getNamespaceURI"), "method:getNamespaceURI");
        check(methods.contains("getPrefix"), "method:getPrefix");
        check(methods.contains("getPrefixes"), "method:getPrefixes");
        check(methods.contains("setContext"), "method:setContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
