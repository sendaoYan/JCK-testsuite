/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMKeyInfoFactory conformance: internal package reflection.
 * @modules java.xml.crypto/org.jcp.xml.dsig.internal.dom:+open
 * @run main DOMKeyInfoFactoryTest
 */
import java.lang.reflect.*;
import java.util.*;
public class DOMKeyInfoFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMKeyInfoFactory", false, DOMKeyInfoFactoryTest.class.getClassLoader());
        check(c.getName().equals("org.jcp.xml.dsig.internal.dom.DOMKeyInfoFactory"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("javax.xml.crypto.dsig.keyinfo.KeyInfoFactory"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("getURIDereferencer"), "method:getURIDereferencer");
        check(methods.contains("isFeatureSupported"), "method:isFeatureSupported");
        check(methods.contains("newKeyInfo"), "method:newKeyInfo");
        check(methods.contains("newKeyName"), "method:newKeyName");
        check(methods.contains("newKeyValue"), "method:newKeyValue");
        check(methods.contains("newPGPData"), "method:newPGPData");
        check(methods.contains("newRetrievalMethod"), "method:newRetrievalMethod");
        check(methods.contains("newX509Data"), "method:newX509Data");
        check(methods.contains("newX509IssuerSerial"), "method:newX509IssuerSerial");
        check(methods.contains("unmarshalKeyInfo"), "method:unmarshalKeyInfo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
