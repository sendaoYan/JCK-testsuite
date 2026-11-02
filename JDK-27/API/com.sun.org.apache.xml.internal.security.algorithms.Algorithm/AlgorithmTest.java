/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.algorithms.Algorithm conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.algorithms:+open
 * @run main AlgorithmTest
 */
import java.lang.reflect.*;
import java.util.*;
public class AlgorithmTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.algorithms.Algorithm", false, AlgorithmTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.algorithms.Algorithm"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.security.utils.SignatureElementProxy"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("getAlgorithmURI"), "method:getAlgorithmURI");
        check(methods.contains("setAlgorithmURI"), "method:setAlgorithmURI");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
