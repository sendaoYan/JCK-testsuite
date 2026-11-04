/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.DOMUtils conformance: internal package reflection.
 * @modules java.xml.crypto/org.jcp.xml.dsig.internal.dom:+open
 * @run main DOMUtilsTest
 */
import java.lang.reflect.*;
import java.util.*;
public class DOMUtilsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.DOMUtils", false, DOMUtilsTest.class.getClassLoader());
        check(c.getName().equals("org.jcp.xml.dsig.internal.dom.DOMUtils"), "name");
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
        check(methods.contains("appendChild"), "method:appendChild");
        check(methods.contains("createElement"), "method:createElement");
        check(methods.contains("getAttributeValue"), "method:getAttributeValue");
        check(methods.contains("getFirstChildElement"), "method:getFirstChildElement");
        check(methods.contains("getIdAttributeValue"), "method:getIdAttributeValue");
        check(methods.contains("getLastChildElement"), "method:getLastChildElement");
        check(methods.contains("getNSPrefix"), "method:getNSPrefix");
        check(methods.contains("getNextSiblingElement"), "method:getNextSiblingElement");
        check(methods.contains("getOwnerDocument"), "method:getOwnerDocument");
        check(methods.contains("getQNameString"), "method:getQNameString");
        check(methods.contains("getSignaturePrefix"), "method:getSignaturePrefix");
        check(methods.contains("isNamespace"), "method:isNamespace");
        check(methods.contains("nodesEqual"), "method:nodesEqual");
        check(methods.contains("paramsEqual"), "method:paramsEqual");
        check(methods.contains("removeAllChildren"), "method:removeAllChildren");
        check(methods.contains("setAttribute"), "method:setAttribute");
        check(methods.contains("setAttributeID"), "method:setAttributeID");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
