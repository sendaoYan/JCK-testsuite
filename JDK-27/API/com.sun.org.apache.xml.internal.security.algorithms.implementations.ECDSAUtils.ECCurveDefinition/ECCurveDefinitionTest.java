/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.algorithms.implementations.ECDSAUtils.ECCurveDefinition conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.algorithms.implementations:+open
 * @run main ECCurveDefinitionTest
 */
import java.lang.reflect.*;
import java.util.*;
public class ECCurveDefinitionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.algorithms.implementations.ECDSAUtils$ECCurveDefinition", false, ECCurveDefinitionTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.algorithms.implementations.ECDSAUtils$ECCurveDefinition"), "name");
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
        check(methods.contains("equals"), "method:equals");
        check(methods.contains("getA"), "method:getA");
        check(methods.contains("getB"), "method:getB");
        check(methods.contains("getField"), "method:getField");
        check(methods.contains("getH"), "method:getH");
        check(methods.contains("getN"), "method:getN");
        check(methods.contains("getName"), "method:getName");
        check(methods.contains("getOid"), "method:getOid");
        check(methods.contains("getX"), "method:getX");
        check(methods.contains("getY"), "method:getY");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
