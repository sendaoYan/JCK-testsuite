/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.utils.ElementProxy conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.utils:+open
 * @run main ElementProxyTest
 */
import java.lang.reflect.*;
import java.util.*;
public class ElementProxyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.utils.ElementProxy", false, ElementProxyTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.utils.ElementProxy"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("addBase64Element"), "method:addBase64Element");
        check(methods.contains("addBase64Text"), "method:addBase64Text");
        check(methods.contains("addBigIntegerElement"), "method:addBigIntegerElement");
        check(methods.contains("addReturnToSelf"), "method:addReturnToSelf");
        check(methods.contains("addText"), "method:addText");
        check(methods.contains("addTextElement"), "method:addTextElement");
        check(methods.contains("appendOther"), "method:appendOther");
        check(methods.contains("appendSelf"), "method:appendSelf");
        check(methods.contains("createElementForFamily"), "method:createElementForFamily");
        check(methods.contains("createElementForFamilyLocal"), "method:createElementForFamilyLocal");
        check(methods.contains("createText"), "method:createText");
        check(methods.contains("getBaseLocalName"), "method:getBaseLocalName");
        check(methods.contains("getBaseNamespace"), "method:getBaseNamespace");
        check(methods.contains("getBaseURI"), "method:getBaseURI");
        check(methods.contains("getBigIntegerFromChildElement"), "method:getBigIntegerFromChildElement");
        check(methods.contains("getBytesFromTextChild"), "method:getBytesFromTextChild");
        check(methods.contains("getDefaultPrefix"), "method:getDefaultPrefix");
        check(methods.contains("getDocument"), "method:getDocument");
        check(methods.contains("getElement"), "method:getElement");
        check(methods.contains("getElementPlusReturns"), "method:getElementPlusReturns");
        check(methods.contains("getFirstChild"), "method:getFirstChild");
        check(methods.contains("getLocalAttribute"), "method:getLocalAttribute");
        check(methods.contains("getTextFromChildElement"), "method:getTextFromChildElement");
        check(methods.contains("getTextFromTextChild"), "method:getTextFromTextChild");
        check(methods.contains("length"), "method:length");
        check(methods.contains("registerDefaultPrefixes"), "method:registerDefaultPrefixes");
        check(methods.contains("setDefaultPrefix"), "method:setDefaultPrefix");
        check(methods.contains("setDocument"), "method:setDocument");
        check(methods.contains("setElement"), "method:setElement");
        check(methods.contains("setLocalAttribute"), "method:setLocalAttribute");
        check(methods.contains("setLocalIdAttribute"), "method:setLocalIdAttribute");
        check(methods.contains("setXPathNamespaceContext"), "method:setXPathNamespaceContext");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("LOG"), "field:LOG");
        check(fields.contains("baseURI"), "field:baseURI");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
