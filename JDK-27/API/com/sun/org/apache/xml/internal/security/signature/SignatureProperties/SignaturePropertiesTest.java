/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.signature.SignatureProperties conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.signature:+open
 * @run main SignaturePropertiesTest
 */
import java.lang.reflect.*;
import java.util.*;
public class SignaturePropertiesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.signature.SignatureProperties", false, SignaturePropertiesTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.signature.SignatureProperties"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.security.utils.SignatureElementProxy"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("addSignatureProperty"), "method:addSignatureProperty");
        check(methods.contains("getBaseLocalName"), "method:getBaseLocalName");
        check(methods.contains("getId"), "method:getId");
        check(methods.contains("getLength"), "method:getLength");
        check(methods.contains("item"), "method:item");
        check(methods.contains("setId"), "method:setId");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
