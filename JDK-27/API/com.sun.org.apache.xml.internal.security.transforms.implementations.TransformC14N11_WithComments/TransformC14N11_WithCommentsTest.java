/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.transforms.implementations.TransformC14N11_WithComments conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.transforms.implementations:+open
 * @run main TransformC14N11_WithCommentsTest
 */
import java.lang.reflect.*;
import java.util.*;
public class TransformC14N11_WithCommentsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.transforms.implementations.TransformC14N11_WithComments", false, TransformC14N11_WithCommentsTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.transforms.implementations.TransformC14N11_WithComments"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.security.transforms.implementations.TransformC14N"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("engineGetURI"), "method:engineGetURI");
        check(methods.contains("getCanonicalizer"), "method:getCanonicalizer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
