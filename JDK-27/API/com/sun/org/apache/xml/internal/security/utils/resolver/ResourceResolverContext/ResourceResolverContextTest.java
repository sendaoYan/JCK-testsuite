/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.utils.resolver.ResourceResolverContext conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.utils.resolver:+open
 * @run main ResourceResolverContextTest
 */
import java.lang.reflect.*;
import java.util.*;
public class ResourceResolverContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.utils.resolver.ResourceResolverContext", false, ResourceResolverContextTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.utils.resolver.ResourceResolverContext"), "name");
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
        check(methods.contains("getProperties"), "method:getProperties");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("attr"), "field:attr");
        check(fields.contains("baseUri"), "field:baseUri");
        check(fields.contains("secureValidation"), "field:secureValidation");
        check(fields.contains("uriToResolve"), "field:uriToResolve");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
