/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMXMLSignatureFactory conformance: internal package reflection.
 * @modules java.xml.crypto/org.jcp.xml.dsig.internal.dom:+open
 * @run main DOMXMLSignatureFactoryTest
 */
import java.lang.reflect.*;
import java.util.*;
public class DOMXMLSignatureFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMXMLSignatureFactory", false, DOMXMLSignatureFactoryTest.class.getClassLoader());
        check(c.getName().equals("org.jcp.xml.dsig.internal.dom.DOMXMLSignatureFactory"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("javax.xml.crypto.dsig.XMLSignatureFactory"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("getURIDereferencer"), "method:getURIDereferencer");
        check(methods.contains("isFeatureSupported"), "method:isFeatureSupported");
        check(methods.contains("newCanonicalizationMethod"), "method:newCanonicalizationMethod");
        check(methods.contains("newDigestMethod"), "method:newDigestMethod");
        check(methods.contains("newManifest"), "method:newManifest");
        check(methods.contains("newReference"), "method:newReference");
        check(methods.contains("newSignatureMethod"), "method:newSignatureMethod");
        check(methods.contains("newSignatureProperties"), "method:newSignatureProperties");
        check(methods.contains("newSignatureProperty"), "method:newSignatureProperty");
        check(methods.contains("newSignedInfo"), "method:newSignedInfo");
        check(methods.contains("newTransform"), "method:newTransform");
        check(methods.contains("newXMLObject"), "method:newXMLObject");
        check(methods.contains("newXMLSignature"), "method:newXMLSignature");
        check(methods.contains("unmarshalXMLSignature"), "method:unmarshalXMLSignature");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
