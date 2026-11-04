/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.utils.EncryptionConstants conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.utils:+open
 * @run main EncryptionConstantsTest
 */
import java.lang.reflect.*;
import java.util.*;
public class EncryptionConstantsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.utils.EncryptionConstants", false, EncryptionConstantsTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.utils.EncryptionConstants"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("ALGO_ID_AUTHENTICATION_XMLSIGNATURE"), "field:ALGO_ID_AUTHENTICATION_XMLSIGNATURE");
        check(fields.contains("ALGO_ID_BLOCKCIPHER_AES128"), "field:ALGO_ID_BLOCKCIPHER_AES128");
        check(fields.contains("ALGO_ID_BLOCKCIPHER_AES128_GCM"), "field:ALGO_ID_BLOCKCIPHER_AES128_GCM");
        check(fields.contains("ALGO_ID_BLOCKCIPHER_AES192"), "field:ALGO_ID_BLOCKCIPHER_AES192");
        check(fields.contains("ALGO_ID_BLOCKCIPHER_AES192_GCM"), "field:ALGO_ID_BLOCKCIPHER_AES192_GCM");
        check(fields.contains("ALGO_ID_BLOCKCIPHER_AES256"), "field:ALGO_ID_BLOCKCIPHER_AES256");
        check(fields.contains("ALGO_ID_BLOCKCIPHER_AES256_GCM"), "field:ALGO_ID_BLOCKCIPHER_AES256_GCM");
        check(fields.contains("ALGO_ID_BLOCKCIPHER_CAMELLIA128"), "field:ALGO_ID_BLOCKCIPHER_CAMELLIA128");
        check(fields.contains("ALGO_ID_BLOCKCIPHER_CAMELLIA192"), "field:ALGO_ID_BLOCKCIPHER_CAMELLIA192");
        check(fields.contains("ALGO_ID_BLOCKCIPHER_CAMELLIA256"), "field:ALGO_ID_BLOCKCIPHER_CAMELLIA256");
        check(fields.contains("ALGO_ID_BLOCKCIPHER_SEED128"), "field:ALGO_ID_BLOCKCIPHER_SEED128");
        check(fields.contains("ALGO_ID_BLOCKCIPHER_TRIPLEDES"), "field:ALGO_ID_BLOCKCIPHER_TRIPLEDES");
        check(fields.contains("ALGO_ID_C14N_OMITCOMMENTS"), "field:ALGO_ID_C14N_OMITCOMMENTS");
        check(fields.contains("ALGO_ID_C14N_WITHCOMMENTS"), "field:ALGO_ID_C14N_WITHCOMMENTS");
        check(fields.contains("ALGO_ID_ENCODING_BASE64"), "field:ALGO_ID_ENCODING_BASE64");
        check(fields.contains("ALGO_ID_KEYAGREEMENT_DH"), "field:ALGO_ID_KEYAGREEMENT_DH");
        check(fields.contains("ALGO_ID_KEYTRANSPORT_RSA15"), "field:ALGO_ID_KEYTRANSPORT_RSA15");
        check(fields.contains("ALGO_ID_KEYTRANSPORT_RSAOAEP"), "field:ALGO_ID_KEYTRANSPORT_RSAOAEP");
        check(fields.contains("ALGO_ID_KEYTRANSPORT_RSAOAEP_11"), "field:ALGO_ID_KEYTRANSPORT_RSAOAEP_11");
        check(fields.contains("ALGO_ID_KEYWRAP_AES128"), "field:ALGO_ID_KEYWRAP_AES128");
        check(fields.contains("ALGO_ID_KEYWRAP_AES192"), "field:ALGO_ID_KEYWRAP_AES192");
        check(fields.contains("ALGO_ID_KEYWRAP_AES256"), "field:ALGO_ID_KEYWRAP_AES256");
        check(fields.contains("ALGO_ID_KEYWRAP_CAMELLIA128"), "field:ALGO_ID_KEYWRAP_CAMELLIA128");
        check(fields.contains("ALGO_ID_KEYWRAP_CAMELLIA192"), "field:ALGO_ID_KEYWRAP_CAMELLIA192");
        check(fields.contains("ALGO_ID_KEYWRAP_CAMELLIA256"), "field:ALGO_ID_KEYWRAP_CAMELLIA256");
        check(fields.contains("ALGO_ID_KEYWRAP_SEED128"), "field:ALGO_ID_KEYWRAP_SEED128");
        check(fields.contains("ALGO_ID_KEYWRAP_TRIPLEDES"), "field:ALGO_ID_KEYWRAP_TRIPLEDES");
        check(fields.contains("ENCRYPTIONSPECIFICATION_URL"), "field:ENCRYPTIONSPECIFICATION_URL");
        check(fields.contains("EncryptionSpec11NS"), "field:EncryptionSpec11NS");
        check(fields.contains("EncryptionSpecNS"), "field:EncryptionSpecNS");
        check(fields.contains("MGF1_SHA1"), "field:MGF1_SHA1");
        check(fields.contains("MGF1_SHA224"), "field:MGF1_SHA224");
        check(fields.contains("MGF1_SHA256"), "field:MGF1_SHA256");
        check(fields.contains("MGF1_SHA384"), "field:MGF1_SHA384");
        check(fields.contains("MGF1_SHA512"), "field:MGF1_SHA512");
        check(fields.contains("TYPE_CONTENT"), "field:TYPE_CONTENT");
        check(fields.contains("TYPE_ELEMENT"), "field:TYPE_ELEMENT");
        check(fields.contains("TYPE_MEDIATYPE"), "field:TYPE_MEDIATYPE");
        check(fields.contains("_ATT_ALGORITHM"), "field:_ATT_ALGORITHM");
        check(fields.contains("_ATT_ENCODING"), "field:_ATT_ENCODING");
        check(fields.contains("_ATT_ID"), "field:_ATT_ID");
        check(fields.contains("_ATT_MIMETYPE"), "field:_ATT_MIMETYPE");
        check(fields.contains("_ATT_RECIPIENT"), "field:_ATT_RECIPIENT");
        check(fields.contains("_ATT_TARGET"), "field:_ATT_TARGET");
        check(fields.contains("_ATT_TYPE"), "field:_ATT_TYPE");
        check(fields.contains("_ATT_URI"), "field:_ATT_URI");
        check(fields.contains("_TAG_AGREEMENTMETHOD"), "field:_TAG_AGREEMENTMETHOD");
        check(fields.contains("_TAG_CARRIEDKEYNAME"), "field:_TAG_CARRIEDKEYNAME");
        check(fields.contains("_TAG_CIPHERDATA"), "field:_TAG_CIPHERDATA");
        check(fields.contains("_TAG_CIPHERREFERENCE"), "field:_TAG_CIPHERREFERENCE");
        check(fields.contains("_TAG_CIPHERVALUE"), "field:_TAG_CIPHERVALUE");
        check(fields.contains("_TAG_DATAREFERENCE"), "field:_TAG_DATAREFERENCE");
        check(fields.contains("_TAG_ENCRYPTEDDATA"), "field:_TAG_ENCRYPTEDDATA");
        check(fields.contains("_TAG_ENCRYPTEDKEY"), "field:_TAG_ENCRYPTEDKEY");
        check(fields.contains("_TAG_ENCRYPTIONMETHOD"), "field:_TAG_ENCRYPTIONMETHOD");
        check(fields.contains("_TAG_ENCRYPTIONPROPERTIES"), "field:_TAG_ENCRYPTIONPROPERTIES");
        check(fields.contains("_TAG_ENCRYPTIONPROPERTY"), "field:_TAG_ENCRYPTIONPROPERTY");
        check(fields.contains("_TAG_KA_NONCE"), "field:_TAG_KA_NONCE");
        check(fields.contains("_TAG_KEYREFERENCE"), "field:_TAG_KEYREFERENCE");
        check(fields.contains("_TAG_KEYSIZE"), "field:_TAG_KEYSIZE");
        check(fields.contains("_TAG_MGF"), "field:_TAG_MGF");
        check(fields.contains("_TAG_OAEPPARAMS"), "field:_TAG_OAEPPARAMS");
        check(fields.contains("_TAG_ORIGINATORKEYINFO"), "field:_TAG_ORIGINATORKEYINFO");
        check(fields.contains("_TAG_RECIPIENTKEYINFO"), "field:_TAG_RECIPIENTKEYINFO");
        check(fields.contains("_TAG_REFERENCELIST"), "field:_TAG_REFERENCELIST");
        check(fields.contains("_TAG_TRANSFORMS"), "field:_TAG_TRANSFORMS");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
