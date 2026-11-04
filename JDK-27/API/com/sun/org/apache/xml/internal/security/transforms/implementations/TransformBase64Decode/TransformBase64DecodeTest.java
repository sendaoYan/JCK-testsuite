/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.transforms.implementations.TransformBase64Decode conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.transforms.implementations:+open
 * @run main TransformBase64DecodeTest
 */
import java.lang.reflect.*;
import java.util.*;
public class TransformBase64DecodeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.transforms.implementations.TransformBase64Decode", false, TransformBase64DecodeTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.transforms.implementations.TransformBase64Decode"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.security.transforms.TransformSpi"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("engineGetURI"), "method:engineGetURI");
        check(methods.contains("enginePerformTransform"), "method:enginePerformTransform");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
