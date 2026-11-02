/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.signature.XMLSignature conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.signature:+open
 * @run main XMLSignatureTest
 */
import java.lang.reflect.*;
import java.util.*;
public class XMLSignatureTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.signature.XMLSignature", false, XMLSignatureTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.signature.XMLSignature"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.security.utils.SignatureElementProxy"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("addDocument"), "method:addDocument");
        check(methods.contains("addKeyInfo"), "method:addKeyInfo");
        check(methods.contains("addResourceResolver"), "method:addResourceResolver");
        check(methods.contains("appendObject"), "method:appendObject");
        check(methods.contains("checkSignatureValue"), "method:checkSignatureValue");
        check(methods.contains("createSecretKey"), "method:createSecretKey");
        check(methods.contains("getBaseLocalName"), "method:getBaseLocalName");
        check(methods.contains("getId"), "method:getId");
        check(methods.contains("getKeyInfo"), "method:getKeyInfo");
        check(methods.contains("getObjectItem"), "method:getObjectItem");
        check(methods.contains("getObjectLength"), "method:getObjectLength");
        check(methods.contains("getSignatureValue"), "method:getSignatureValue");
        check(methods.contains("getSignedInfo"), "method:getSignedInfo");
        check(methods.contains("setFollowNestedManifests"), "method:setFollowNestedManifests");
        check(methods.contains("setId"), "method:setId");
        check(methods.contains("sign"), "method:sign");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("ALGO_ID_MAC_HMAC_NOT_RECOMMENDED_MD5"), "field:ALGO_ID_MAC_HMAC_NOT_RECOMMENDED_MD5");
        check(fields.contains("ALGO_ID_MAC_HMAC_RIPEMD160"), "field:ALGO_ID_MAC_HMAC_RIPEMD160");
        check(fields.contains("ALGO_ID_MAC_HMAC_SHA1"), "field:ALGO_ID_MAC_HMAC_SHA1");
        check(fields.contains("ALGO_ID_MAC_HMAC_SHA224"), "field:ALGO_ID_MAC_HMAC_SHA224");
        check(fields.contains("ALGO_ID_MAC_HMAC_SHA256"), "field:ALGO_ID_MAC_HMAC_SHA256");
        check(fields.contains("ALGO_ID_MAC_HMAC_SHA384"), "field:ALGO_ID_MAC_HMAC_SHA384");
        check(fields.contains("ALGO_ID_MAC_HMAC_SHA512"), "field:ALGO_ID_MAC_HMAC_SHA512");
        check(fields.contains("ALGO_ID_SIGNATURE_DSA"), "field:ALGO_ID_SIGNATURE_DSA");
        check(fields.contains("ALGO_ID_SIGNATURE_DSA_SHA256"), "field:ALGO_ID_SIGNATURE_DSA_SHA256");
        check(fields.contains("ALGO_ID_SIGNATURE_ECDSA_RIPEMD160"), "field:ALGO_ID_SIGNATURE_ECDSA_RIPEMD160");
        check(fields.contains("ALGO_ID_SIGNATURE_ECDSA_SHA1"), "field:ALGO_ID_SIGNATURE_ECDSA_SHA1");
        check(fields.contains("ALGO_ID_SIGNATURE_ECDSA_SHA224"), "field:ALGO_ID_SIGNATURE_ECDSA_SHA224");
        check(fields.contains("ALGO_ID_SIGNATURE_ECDSA_SHA256"), "field:ALGO_ID_SIGNATURE_ECDSA_SHA256");
        check(fields.contains("ALGO_ID_SIGNATURE_ECDSA_SHA384"), "field:ALGO_ID_SIGNATURE_ECDSA_SHA384");
        check(fields.contains("ALGO_ID_SIGNATURE_ECDSA_SHA3_224"), "field:ALGO_ID_SIGNATURE_ECDSA_SHA3_224");
        check(fields.contains("ALGO_ID_SIGNATURE_ECDSA_SHA3_256"), "field:ALGO_ID_SIGNATURE_ECDSA_SHA3_256");
        check(fields.contains("ALGO_ID_SIGNATURE_ECDSA_SHA3_384"), "field:ALGO_ID_SIGNATURE_ECDSA_SHA3_384");
        check(fields.contains("ALGO_ID_SIGNATURE_ECDSA_SHA3_512"), "field:ALGO_ID_SIGNATURE_ECDSA_SHA3_512");
        check(fields.contains("ALGO_ID_SIGNATURE_ECDSA_SHA512"), "field:ALGO_ID_SIGNATURE_ECDSA_SHA512");
        check(fields.contains("ALGO_ID_SIGNATURE_EDDSA_ED25519"), "field:ALGO_ID_SIGNATURE_EDDSA_ED25519");
        check(fields.contains("ALGO_ID_SIGNATURE_EDDSA_ED448"), "field:ALGO_ID_SIGNATURE_EDDSA_ED448");
        check(fields.contains("ALGO_ID_SIGNATURE_NOT_RECOMMENDED_RSA_MD5"), "field:ALGO_ID_SIGNATURE_NOT_RECOMMENDED_RSA_MD5");
        check(fields.contains("ALGO_ID_SIGNATURE_RSA"), "field:ALGO_ID_SIGNATURE_RSA");
        check(fields.contains("ALGO_ID_SIGNATURE_RSA_PSS"), "field:ALGO_ID_SIGNATURE_RSA_PSS");
        check(fields.contains("ALGO_ID_SIGNATURE_RSA_RIPEMD160"), "field:ALGO_ID_SIGNATURE_RSA_RIPEMD160");
        check(fields.contains("ALGO_ID_SIGNATURE_RSA_SHA1"), "field:ALGO_ID_SIGNATURE_RSA_SHA1");
        check(fields.contains("ALGO_ID_SIGNATURE_RSA_SHA1_MGF1"), "field:ALGO_ID_SIGNATURE_RSA_SHA1_MGF1");
        check(fields.contains("ALGO_ID_SIGNATURE_RSA_SHA224"), "field:ALGO_ID_SIGNATURE_RSA_SHA224");
        check(fields.contains("ALGO_ID_SIGNATURE_RSA_SHA224_MGF1"), "field:ALGO_ID_SIGNATURE_RSA_SHA224_MGF1");
        check(fields.contains("ALGO_ID_SIGNATURE_RSA_SHA256"), "field:ALGO_ID_SIGNATURE_RSA_SHA256");
        check(fields.contains("ALGO_ID_SIGNATURE_RSA_SHA256_MGF1"), "field:ALGO_ID_SIGNATURE_RSA_SHA256_MGF1");
        check(fields.contains("ALGO_ID_SIGNATURE_RSA_SHA384"), "field:ALGO_ID_SIGNATURE_RSA_SHA384");
        check(fields.contains("ALGO_ID_SIGNATURE_RSA_SHA384_MGF1"), "field:ALGO_ID_SIGNATURE_RSA_SHA384_MGF1");
        check(fields.contains("ALGO_ID_SIGNATURE_RSA_SHA3_224_MGF1"), "field:ALGO_ID_SIGNATURE_RSA_SHA3_224_MGF1");
        check(fields.contains("ALGO_ID_SIGNATURE_RSA_SHA3_256_MGF1"), "field:ALGO_ID_SIGNATURE_RSA_SHA3_256_MGF1");
        check(fields.contains("ALGO_ID_SIGNATURE_RSA_SHA3_384_MGF1"), "field:ALGO_ID_SIGNATURE_RSA_SHA3_384_MGF1");
        check(fields.contains("ALGO_ID_SIGNATURE_RSA_SHA3_512_MGF1"), "field:ALGO_ID_SIGNATURE_RSA_SHA3_512_MGF1");
        check(fields.contains("ALGO_ID_SIGNATURE_RSA_SHA512"), "field:ALGO_ID_SIGNATURE_RSA_SHA512");
        check(fields.contains("ALGO_ID_SIGNATURE_RSA_SHA512_MGF1"), "field:ALGO_ID_SIGNATURE_RSA_SHA512_MGF1");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
