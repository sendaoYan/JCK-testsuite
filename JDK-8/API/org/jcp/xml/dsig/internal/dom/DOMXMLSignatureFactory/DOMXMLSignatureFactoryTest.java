/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMXMLSignatureFactory conformance: type shape via reflection (headless-safe).
 * @run main DOMXMLSignatureFactoryTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class DOMXMLSignatureFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMXMLSignatureFactory", false, DOMXMLSignatureFactoryTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.xml.crypto.dsig.XMLSignatureFactory"), "superclass javax.xml.crypto.dsig.XMLSignatureFactory");
        check(hasMethod(c, "getURIDereferencer"), "declares getURIDereferencer");
        check(hasMethod(c, "isFeatureSupported"), "declares isFeatureSupported");
        check(hasMethod(c, "newCanonicalizationMethod"), "declares newCanonicalizationMethod");
        check(hasMethod(c, "newDigestMethod"), "declares newDigestMethod");
        check(hasMethod(c, "newManifest"), "declares newManifest");
        check(hasMethod(c, "newReference"), "declares newReference");
        check(hasMethod(c, "newSignatureMethod"), "declares newSignatureMethod");
        check(hasMethod(c, "newSignatureProperties"), "declares newSignatureProperties");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
