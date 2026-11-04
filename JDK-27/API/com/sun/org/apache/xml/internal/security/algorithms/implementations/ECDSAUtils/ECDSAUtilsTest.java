/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.algorithms.implementations.ECDSAUtils conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.algorithms.implementations:+open
 * @run main ECDSAUtilsTest
 */
import java.lang.reflect.*;
import java.util.*;
public class ECDSAUtilsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.algorithms.implementations.ECDSAUtils", false, ECDSAUtilsTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.algorithms.implementations.ECDSAUtils"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("convertASN1toXMLDSIG"), "method:convertASN1toXMLDSIG");
        check(methods.contains("convertXMLDSIGtoASN1"), "method:convertXMLDSIGtoASN1");
        check(methods.contains("decodePoint"), "method:decodePoint");
        check(methods.contains("encodePoint"), "method:encodePoint");
        check(methods.contains("getECCurveDefinition"), "method:getECCurveDefinition");
        check(methods.contains("getOIDFromPublicKey"), "method:getOIDFromPublicKey");
        check(methods.contains("stripLeadingZeros"), "method:stripLeadingZeros");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
