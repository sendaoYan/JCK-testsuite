/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.utils.resolver.ResourceResolverException conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.utils.resolver:+open
 * @run main ResourceResolverExceptionTest
 */
import java.lang.reflect.*;
import java.util.*;
public class ResourceResolverExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.utils.resolver.ResourceResolverException", false, ResourceResolverExceptionTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.utils.resolver.ResourceResolverException"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.security.exceptions.XMLSecurityException"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("getURI"), "method:getURI");
        check(methods.contains("getbaseURI"), "method:getbaseURI");
        check(methods.contains("setURI"), "method:setURI");
        check(methods.contains("setbaseURI"), "method:setbaseURI");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
