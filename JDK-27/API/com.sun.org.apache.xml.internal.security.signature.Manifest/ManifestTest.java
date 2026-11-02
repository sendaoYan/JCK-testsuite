/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.signature.Manifest conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.signature:+open
 * @run main ManifestTest
 */
import java.lang.reflect.*;
import java.util.*;
public class ManifestTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.signature.Manifest", false, ManifestTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.signature.Manifest"), "name");
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
        check(methods.contains("addDocument"), "method:addDocument");
        check(methods.contains("addResourceResolver"), "method:addResourceResolver");
        check(methods.contains("generateDigestValues"), "method:generateDigestValues");
        check(methods.contains("getBaseLocalName"), "method:getBaseLocalName");
        check(methods.contains("getId"), "method:getId");
        check(methods.contains("getLength"), "method:getLength");
        check(methods.contains("getPerManifestResolvers"), "method:getPerManifestResolvers");
        check(methods.contains("getReferencedContentAfterTransformsItem"), "method:getReferencedContentAfterTransformsItem");
        check(methods.contains("getReferencedContentBeforeTransformsItem"), "method:getReferencedContentBeforeTransformsItem");
        check(methods.contains("getResolverProperties"), "method:getResolverProperties");
        check(methods.contains("getResolverProperty"), "method:getResolverProperty");
        check(methods.contains("getSignedContentItem"), "method:getSignedContentItem");
        check(methods.contains("getSignedContentLength"), "method:getSignedContentLength");
        check(methods.contains("getVerificationResult"), "method:getVerificationResult");
        check(methods.contains("getVerificationResults"), "method:getVerificationResults");
        check(methods.contains("isSecureValidation"), "method:isSecureValidation");
        check(methods.contains("item"), "method:item");
        check(methods.contains("setId"), "method:setId");
        check(methods.contains("setResolverProperty"), "method:setResolverProperty");
        check(methods.contains("verifyReferences"), "method:verifyReferences");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("MAXIMUM_REFERENCE_COUNT"), "field:MAXIMUM_REFERENCE_COUNT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
