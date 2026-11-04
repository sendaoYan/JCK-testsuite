/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.signature.XMLSignatureInput conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.signature:+open
 * @run main XMLSignatureInputTest
 */
import java.lang.reflect.*;
import java.util.*;
public class XMLSignatureInputTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.signature.XMLSignatureInput", false, XMLSignatureInputTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.signature.XMLSignatureInput"), "name");
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
        check(methods.contains("addNodeFilter"), "method:addNodeFilter");
        check(methods.contains("getBytes"), "method:getBytes");
        check(methods.contains("getExcludeNode"), "method:getExcludeNode");
        check(methods.contains("getHTMLRepresentation"), "method:getHTMLRepresentation");
        check(methods.contains("getInputNodeSet"), "method:getInputNodeSet");
        check(methods.contains("getMIMEType"), "method:getMIMEType");
        check(methods.contains("getNodeFilters"), "method:getNodeFilters");
        check(methods.contains("getNodeSet"), "method:getNodeSet");
        check(methods.contains("getOctetStream"), "method:getOctetStream");
        check(methods.contains("getOctetStreamReal"), "method:getOctetStreamReal");
        check(methods.contains("getPreCalculatedDigest"), "method:getPreCalculatedDigest");
        check(methods.contains("getSourceURI"), "method:getSourceURI");
        check(methods.contains("getSubNode"), "method:getSubNode");
        check(methods.contains("isByteArray"), "method:isByteArray");
        check(methods.contains("isElement"), "method:isElement");
        check(methods.contains("isExcludeComments"), "method:isExcludeComments");
        check(methods.contains("isInitialized"), "method:isInitialized");
        check(methods.contains("isNeedsToBeExpanded"), "method:isNeedsToBeExpanded");
        check(methods.contains("isNodeSet"), "method:isNodeSet");
        check(methods.contains("isOctetStream"), "method:isOctetStream");
        check(methods.contains("isOutputStreamSet"), "method:isOutputStreamSet");
        check(methods.contains("isPreCalculatedDigest"), "method:isPreCalculatedDigest");
        check(methods.contains("isSecureValidation"), "method:isSecureValidation");
        check(methods.contains("setExcludeComments"), "method:setExcludeComments");
        check(methods.contains("setExcludeNode"), "method:setExcludeNode");
        check(methods.contains("setMIMEType"), "method:setMIMEType");
        check(methods.contains("setNeedsToBeExpanded"), "method:setNeedsToBeExpanded");
        check(methods.contains("setNodeSet"), "method:setNodeSet");
        check(methods.contains("setOutputStream"), "method:setOutputStream");
        check(methods.contains("setSecureValidation"), "method:setSecureValidation");
        check(methods.contains("setSourceURI"), "method:setSourceURI");
        check(methods.contains("toString"), "method:toString");
        check(methods.contains("updateOutputStream"), "method:updateOutputStream");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
