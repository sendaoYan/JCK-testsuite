/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.utils.XMLUtils conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.utils:+open
 * @run main XMLUtilsTest
 */
import java.lang.reflect.*;
import java.util.*;
public class XMLUtilsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.utils.XMLUtils", false, XMLUtilsTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.utils.XMLUtils"), "name");
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
        check(methods.contains("addReturnBeforeChild"), "method:addReturnBeforeChild");
        check(methods.contains("addReturnToElement"), "method:addReturnToElement");
        check(methods.contains("circumventBug2650"), "method:circumventBug2650");
        check(methods.contains("convertNodelistToSet"), "method:convertNodelistToSet");
        check(methods.contains("createElementInEncryption11Space"), "method:createElementInEncryption11Space");
        check(methods.contains("createElementInEncryptionSpace"), "method:createElementInEncryptionSpace");
        check(methods.contains("createElementInSignature11Space"), "method:createElementInSignature11Space");
        check(methods.contains("createElementInSignatureSpace"), "method:createElementInSignatureSpace");
        check(methods.contains("decode"), "method:decode");
        check(methods.contains("elementIsInEncryption11Space"), "method:elementIsInEncryption11Space");
        check(methods.contains("elementIsInEncryptionSpace"), "method:elementIsInEncryptionSpace");
        check(methods.contains("elementIsInSignature11Space"), "method:elementIsInSignature11Space");
        check(methods.contains("elementIsInSignatureSpace"), "method:elementIsInSignatureSpace");
        check(methods.contains("encodeToString"), "method:encodeToString");
        check(methods.contains("excludeNodeFromSet"), "method:excludeNodeFromSet");
        check(methods.contains("getBytes"), "method:getBytes");
        check(methods.contains("getFullTextChildrenFromNode"), "method:getFullTextChildrenFromNode");
        check(methods.contains("getNextElement"), "method:getNextElement");
        check(methods.contains("getOwnerDocument"), "method:getOwnerDocument");
        check(methods.contains("getSet"), "method:getSet");
        check(methods.contains("getStrFromNode"), "method:getStrFromNode");
        check(methods.contains("ignoreLineBreaks"), "method:ignoreLineBreaks");
        check(methods.contains("isDescendantOrSelf"), "method:isDescendantOrSelf");
        check(methods.contains("isIgnoreLineBreaks"), "method:isIgnoreLineBreaks");
        check(methods.contains("outputDOM"), "method:outputDOM");
        check(methods.contains("outputDOMc14nWithComments"), "method:outputDOMc14nWithComments");
        check(methods.contains("protectAgainstWrappingAttack"), "method:protectAgainstWrappingAttack");
        check(methods.contains("read"), "method:read");
        check(methods.contains("readResource"), "method:readResource");
        check(methods.contains("selectDs11Node"), "method:selectDs11Node");
        check(methods.contains("selectDs11Nodes"), "method:selectDs11Nodes");
        check(methods.contains("selectDsNode"), "method:selectDsNode");
        check(methods.contains("selectDsNodes"), "method:selectDsNodes");
        check(methods.contains("selectNode"), "method:selectNode");
        check(methods.contains("selectNodes"), "method:selectNodes");
        check(methods.contains("selectXencNode"), "method:selectXencNode");
        check(methods.contains("setDs11Prefix"), "method:setDs11Prefix");
        check(methods.contains("setDsPrefix"), "method:setDsPrefix");
        check(methods.contains("setXenc11Prefix"), "method:setXenc11Prefix");
        check(methods.contains("setXencPrefix"), "method:setXencPrefix");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
