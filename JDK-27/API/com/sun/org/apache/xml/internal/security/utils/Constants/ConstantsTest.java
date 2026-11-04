/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.utils.Constants conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.utils:+open
 * @run main ConstantsTest
 */
import java.lang.reflect.*;
import java.util.*;
public class ConstantsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.utils.Constants", false, ConstantsTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.utils.Constants"), "name");
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
        check(fields.contains("ALGO_ID_DIGEST_SHA1"), "field:ALGO_ID_DIGEST_SHA1");
        check(fields.contains("ALGO_ID_SIGNATURE_ECDSA_CERTICOM"), "field:ALGO_ID_SIGNATURE_ECDSA_CERTICOM");
        check(fields.contains("MoreAlgorithmsSpecNS"), "field:MoreAlgorithmsSpecNS");
        check(fields.contains("NamespaceSpecNS"), "field:NamespaceSpecNS");
        check(fields.contains("SIGNATURESPECIFICATION_URL"), "field:SIGNATURESPECIFICATION_URL");
        check(fields.contains("SignatureSpec11NS"), "field:SignatureSpec11NS");
        check(fields.contains("SignatureSpecNS"), "field:SignatureSpecNS");
        check(fields.contains("XML_DSIG_NS_MORE_07_05"), "field:XML_DSIG_NS_MORE_07_05");
        check(fields.contains("XML_DSIG_NS_MORE_21_04"), "field:XML_DSIG_NS_MORE_21_04");
        check(fields.contains("XML_LANG_SPACE_SpecNS"), "field:XML_LANG_SPACE_SpecNS");
        check(fields.contains("_ATT_ALGORITHM"), "field:_ATT_ALGORITHM");
        check(fields.contains("_ATT_ENCODING"), "field:_ATT_ENCODING");
        check(fields.contains("_ATT_ID"), "field:_ATT_ID");
        check(fields.contains("_ATT_MIMETYPE"), "field:_ATT_MIMETYPE");
        check(fields.contains("_ATT_TARGET"), "field:_ATT_TARGET");
        check(fields.contains("_ATT_TYPE"), "field:_ATT_TYPE");
        check(fields.contains("_ATT_URI"), "field:_ATT_URI");
        check(fields.contains("_TAG_CANONICALIZATIONMETHOD"), "field:_TAG_CANONICALIZATIONMETHOD");
        check(fields.contains("_TAG_DERENCODEDKEYVALUE"), "field:_TAG_DERENCODEDKEYVALUE");
        check(fields.contains("_TAG_DIGESTMETHOD"), "field:_TAG_DIGESTMETHOD");
        check(fields.contains("_TAG_DIGESTVALUE"), "field:_TAG_DIGESTVALUE");
        check(fields.contains("_TAG_DSAKEYVALUE"), "field:_TAG_DSAKEYVALUE");
        check(fields.contains("_TAG_ECKEYVALUE"), "field:_TAG_ECKEYVALUE");
        check(fields.contains("_TAG_EXPONENT"), "field:_TAG_EXPONENT");
        check(fields.contains("_TAG_G"), "field:_TAG_G");
        check(fields.contains("_TAG_HMACOUTPUTLENGTH"), "field:_TAG_HMACOUTPUTLENGTH");
        check(fields.contains("_TAG_J"), "field:_TAG_J");
        check(fields.contains("_TAG_KEYINFO"), "field:_TAG_KEYINFO");
        check(fields.contains("_TAG_KEYINFOREFERENCE"), "field:_TAG_KEYINFOREFERENCE");
        check(fields.contains("_TAG_KEYNAME"), "field:_TAG_KEYNAME");
        check(fields.contains("_TAG_KEYVALUE"), "field:_TAG_KEYVALUE");
        check(fields.contains("_TAG_MANIFEST"), "field:_TAG_MANIFEST");
        check(fields.contains("_TAG_METHODS"), "field:_TAG_METHODS");
        check(fields.contains("_TAG_MGF"), "field:_TAG_MGF");
        check(fields.contains("_TAG_MGMTDATA"), "field:_TAG_MGMTDATA");
        check(fields.contains("_TAG_MODULUS"), "field:_TAG_MODULUS");
        check(fields.contains("_TAG_OBJECT"), "field:_TAG_OBJECT");
        check(fields.contains("_TAG_P"), "field:_TAG_P");
        check(fields.contains("_TAG_PGENCOUNTER"), "field:_TAG_PGENCOUNTER");
        check(fields.contains("_TAG_PGPDATA"), "field:_TAG_PGPDATA");
        check(fields.contains("_TAG_PGPKEYID"), "field:_TAG_PGPKEYID");
        check(fields.contains("_TAG_PGPKEYPACKET"), "field:_TAG_PGPKEYPACKET");
        check(fields.contains("_TAG_Q"), "field:_TAG_Q");
        check(fields.contains("_TAG_RAWX509CERTIFICATE"), "field:_TAG_RAWX509CERTIFICATE");
        check(fields.contains("_TAG_REFERENCE"), "field:_TAG_REFERENCE");
        check(fields.contains("_TAG_RETRIEVALMETHOD"), "field:_TAG_RETRIEVALMETHOD");
        check(fields.contains("_TAG_RSAKEYVALUE"), "field:_TAG_RSAKEYVALUE");
        check(fields.contains("_TAG_RSAPSSPARAMS"), "field:_TAG_RSAPSSPARAMS");
        check(fields.contains("_TAG_SALT"), "field:_TAG_SALT");
        check(fields.contains("_TAG_SALTLENGTH"), "field:_TAG_SALTLENGTH");
        check(fields.contains("_TAG_SEED"), "field:_TAG_SEED");
        check(fields.contains("_TAG_SIGNATURE"), "field:_TAG_SIGNATURE");
        check(fields.contains("_TAG_SIGNATUREMETHOD"), "field:_TAG_SIGNATUREMETHOD");
        check(fields.contains("_TAG_SIGNATUREPROPERTIES"), "field:_TAG_SIGNATUREPROPERTIES");
        check(fields.contains("_TAG_SIGNATUREPROPERTY"), "field:_TAG_SIGNATUREPROPERTY");
        check(fields.contains("_TAG_SIGNATUREVALUE"), "field:_TAG_SIGNATUREVALUE");
        check(fields.contains("_TAG_SIGNEDINFO"), "field:_TAG_SIGNEDINFO");
        check(fields.contains("_TAG_SPKIDATA"), "field:_TAG_SPKIDATA");
        check(fields.contains("_TAG_SPKISEXP"), "field:_TAG_SPKISEXP");
        check(fields.contains("_TAG_TRAILERFIELD"), "field:_TAG_TRAILERFIELD");
        check(fields.contains("_TAG_TRANSFORM"), "field:_TAG_TRANSFORM");
        check(fields.contains("_TAG_TRANSFORMS"), "field:_TAG_TRANSFORMS");
        check(fields.contains("_TAG_X509CERTIFICATE"), "field:_TAG_X509CERTIFICATE");
        check(fields.contains("_TAG_X509CRL"), "field:_TAG_X509CRL");
        check(fields.contains("_TAG_X509DATA"), "field:_TAG_X509DATA");
        check(fields.contains("_TAG_X509DIGEST"), "field:_TAG_X509DIGEST");
        check(fields.contains("_TAG_X509ISSUERNAME"), "field:_TAG_X509ISSUERNAME");
        check(fields.contains("_TAG_X509ISSUERSERIAL"), "field:_TAG_X509ISSUERSERIAL");
        check(fields.contains("_TAG_X509SERIALNUMBER"), "field:_TAG_X509SERIALNUMBER");
        check(fields.contains("_TAG_X509SKI"), "field:_TAG_X509SKI");
        check(fields.contains("_TAG_X509SUBJECTNAME"), "field:_TAG_X509SUBJECTNAME");
        check(fields.contains("_TAG_XPATH"), "field:_TAG_XPATH");
        check(fields.contains("_TAG_Y"), "field:_TAG_Y");
        check(fields.contains("configurationFile"), "field:configurationFile");
        check(fields.contains("configurationFileNew"), "field:configurationFileNew");
        check(fields.contains("exceptionMessagesResourceBundleBase"), "field:exceptionMessagesResourceBundleBase");
        check(fields.contains("exceptionMessagesResourceBundleDir"), "field:exceptionMessagesResourceBundleDir");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
