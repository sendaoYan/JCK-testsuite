/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.keys.keyresolver.KeyResolver conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.keys.keyresolver:+open
 * @run main KeyResolverTest
 */
import java.lang.reflect.*;
import java.util.*;
public class KeyResolverTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.keys.keyresolver.KeyResolver", false, KeyResolverTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.keys.keyresolver.KeyResolver"), "name");
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
        check(methods.contains("getPublicKey"), "method:getPublicKey");
        check(methods.contains("getX509Certificate"), "method:getX509Certificate");
        check(methods.contains("iterator"), "method:iterator");
        check(methods.contains("length"), "method:length");
        check(methods.contains("register"), "method:register");
        check(methods.contains("registerAtStart"), "method:registerAtStart");
        check(methods.contains("registerClassNames"), "method:registerClassNames");
        check(methods.contains("registerDefaultResolvers"), "method:registerDefaultResolvers");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
