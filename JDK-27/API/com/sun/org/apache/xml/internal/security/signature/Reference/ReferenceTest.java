/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.signature.Reference conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.signature:+open
 * @run main ReferenceTest
 */
import java.lang.reflect.*;
import java.util.*;
public class ReferenceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.signature.Reference", false, ReferenceTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.signature.Reference"), "name");
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
        check(methods.contains("dereferenceURIandPerformTransforms"), "method:dereferenceURIandPerformTransforms");
        check(methods.contains("generateDigestValue"), "method:generateDigestValue");
        check(methods.contains("getBaseLocalName"), "method:getBaseLocalName");
        check(methods.contains("getContentsAfterTransformation"), "method:getContentsAfterTransformation");
        check(methods.contains("getContentsBeforeTransformation"), "method:getContentsBeforeTransformation");
        check(methods.contains("getDigestValue"), "method:getDigestValue");
        check(methods.contains("getHTMLRepresentation"), "method:getHTMLRepresentation");
        check(methods.contains("getId"), "method:getId");
        check(methods.contains("getMessageDigestAlgorithm"), "method:getMessageDigestAlgorithm");
        check(methods.contains("getNodesetBeforeFirstCanonicalization"), "method:getNodesetBeforeFirstCanonicalization");
        check(methods.contains("getReferenceData"), "method:getReferenceData");
        check(methods.contains("getReferencedBytes"), "method:getReferencedBytes");
        check(methods.contains("getTransforms"), "method:getTransforms");
        check(methods.contains("getTransformsOutput"), "method:getTransformsOutput");
        check(methods.contains("getType"), "method:getType");
        check(methods.contains("getURI"), "method:getURI");
        check(methods.contains("setId"), "method:setId");
        check(methods.contains("setType"), "method:setType");
        check(methods.contains("setURI"), "method:setURI");
        check(methods.contains("typeIsReferenceToManifest"), "method:typeIsReferenceToManifest");
        check(methods.contains("typeIsReferenceToObject"), "method:typeIsReferenceToObject");
        check(methods.contains("verify"), "method:verify");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("MANIFEST_URI"), "field:MANIFEST_URI");
        check(fields.contains("MAXIMUM_TRANSFORM_COUNT"), "field:MAXIMUM_TRANSFORM_COUNT");
        check(fields.contains("OBJECT_URI"), "field:OBJECT_URI");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
