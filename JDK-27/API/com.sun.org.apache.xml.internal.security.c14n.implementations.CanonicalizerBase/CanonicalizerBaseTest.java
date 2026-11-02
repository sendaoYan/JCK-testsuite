/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.c14n.implementations.CanonicalizerBase conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.c14n.implementations:+open
 * @run main CanonicalizerBaseTest
 */
import java.lang.reflect.*;
import java.util.*;
public class CanonicalizerBaseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.c14n.implementations.CanonicalizerBase", false, CanonicalizerBaseTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.c14n.implementations.CanonicalizerBase"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.security.c14n.CanonicalizerSpi"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("engineCanonicalize"), "method:engineCanonicalize");
        check(methods.contains("engineCanonicalizeSubTree"), "method:engineCanonicalizeSubTree");
        check(methods.contains("engineCanonicalizeXPathNodeSet"), "method:engineCanonicalizeXPathNodeSet");
        check(methods.contains("getNullNode"), "method:getNullNode");
        check(methods.contains("handleParent"), "method:handleParent");
        check(methods.contains("isVisible"), "method:isVisible");
        check(methods.contains("isVisibleDO"), "method:isVisibleDO");
        check(methods.contains("isVisibleInt"), "method:isVisibleInt");
        check(methods.contains("outputAttrToWriter"), "method:outputAttrToWriter");
        check(methods.contains("outputCommentToWriter"), "method:outputCommentToWriter");
        check(methods.contains("outputPItoWriter"), "method:outputPItoWriter");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("COMPARE"), "field:COMPARE");
        check(fields.contains("NODE_AFTER_DOCUMENT_ELEMENT"), "field:NODE_AFTER_DOCUMENT_ELEMENT");
        check(fields.contains("NODE_BEFORE_DOCUMENT_ELEMENT"), "field:NODE_BEFORE_DOCUMENT_ELEMENT");
        check(fields.contains("NODE_NOT_BEFORE_OR_AFTER_DOCUMENT_ELEMENT"), "field:NODE_NOT_BEFORE_OR_AFTER_DOCUMENT_ELEMENT");
        check(fields.contains("XML"), "field:XML");
        check(fields.contains("XMLNS"), "field:XMLNS");
        check(fields.contains("XMLNS_URI"), "field:XMLNS_URI");
        check(fields.contains("XML_LANG_URI"), "field:XML_LANG_URI");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
