/*
 * @test
 * @summary sun.security.util.KnownOIDs conformance: internal package reflection.
 * @modules java.base/sun.security.util:+open
 * @run main KnownOIDsTest
 */
import java.lang.reflect.*;
import java.util.*;

public class KnownOIDsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.util.KnownOIDs", false, KnownOIDsTest.class.getClassLoader());
        check(c != null, "loaded");
        check("sun.security.util.KnownOIDs".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.lang.Enum".equals(c.getSuperclass() == null ? "" : c.getSuperclass().getName()), "superclass");
        check(interfaceNames(c).equals(new TreeSet<String>()), "interfaces");
        check(methodNames(c).equals(new TreeSet<>(Arrays.asList("aliases", "findMatch", "stdName", "value", "valueOf", "values"))), "methods");
        check(fieldNames(c).equals(new TreeSet<String>()), "fields");
        check(enumConstantNames(c).equals(new TreeSet<>(Arrays.asList("AD_TimeStamping", "AES", "AES_128$CBC$NoPadding", "AES_128$CFB$NoPadding", "AES_128$ECB$NoPadding", "AES_128$GCM$NoPadding", "AES_128$KW$NoPadding", "AES_128$KWP$NoPadding", "AES_128$OFB$NoPadding", "AES_192$CBC$NoPadding", "AES_192$CFB$NoPadding", "AES_192$ECB$NoPadding", "AES_192$GCM$NoPadding", "AES_192$KW$NoPadding", "AES_192$KWP$NoPadding", "AES_192$OFB$NoPadding", "AES_256$CBC$NoPadding", "AES_256$CFB$NoPadding", "AES_256$ECB$NoPadding", "AES_256$GCM$NoPadding", "AES_256$KW$NoPadding", "AES_256$KWP$NoPadding", "AES_256$OFB$NoPadding", "ARCFOUR", "AuthInfoAccess", "AuthorityKeyID", "BasicConstraints", "Blowfish", "CE_CERT_POLICIES_ANY", "CHACHA20_POLY1305", "CMSAlgorithmProtection", "CRLDistributionPoints", "CRLNumber", "CertBag", "CertTypeX509", "CertificateIssuer", "CertificatePolicies", "ChallengePassword", "CommonName", "ContentType", "CounterSignature", "CountryName", "DESede", "DESede$CBC$NoPadding", "DNQualifier", "DSA", "Data", "DeltaCRLIndicator", "DiffieHellman", "DigestedData", "EC", "ECDH", "Ed25519", "Ed448", "EmailAddress", "EncryptedData", "EnvelopedData", "ExtendedCertificateAttributes", "ExtensionRequest", "FreshestCRL", "FriendlyName", "GenerationQualifier", "GivenName", "HSSLMS", "HmacSHA1", "HmacSHA224", "HmacSHA256", "HmacSHA384", "HmacSHA3_224", "HmacSHA3_256", "HmacSHA3_384", "HmacSHA3_512", "HmacSHA512", "HmacSHA512$224", "HmacSHA512$256", "HoldInstructionCode", "ITUX509_RSA", "InhibitAnyPolicy", "Initials", "InvalidityDate", "IssuerAlternativeName", "IssuerAndSerialNumber", "IssuingDistributionPoint", "JAVASOFT_JCEKeyProtector", "JAVASOFT_JDKKeyProtector", "JDK_OLD_Data", "JDK_OLD_SignedData", "KP_TimeStamping", "KeyUsage", "LocalKeyID", "LocalityName", "MD2", "MD2withRSA", "MD5", "MD5withRSA", "MGF1", "MICROSOFT_ExportApproved", "ML_DSA_44", "ML_DSA_65", "ML_DSA_87", "ML_KEM_1024", "ML_KEM_512", "ML_KEM_768", "MessageDigest", "NETSCAPE_CertSequence", "NETSCAPE_CertType", "NETSCAPE_ExportApproved", "NameConstraints", "OAEP", "OCSP", "OCSPBasicResponse", "OCSPNoCheck", "OCSPNonceExt", "OCSPSigning", "OIW_DES_CBC", "OIW_DSA", "OIW_JDK_SHA1withDSA", "OIW_SHA1withDSA", "OIW_SHA1withRSA", "OIW_SHA1withRSA_Odd", "ORACLE_TrustedKeyUsage", "OrgName", "OrgUnitName", "PBES2", "PBEWithMD5AndDES", "PBEWithMD5AndRC2", "PBEWithSHA1AndDES", "PBEWithSHA1AndDESede", "PBEWithSHA1AndRC2", "PBEWithSHA1AndRC2_128", "PBEWithSHA1AndRC2_40", "PBEWithSHA1AndRC4_128", "PBEWithSHA1AndRC4_40", "PBKDF2", "PBMAC1", "PKCS1", "PKCS7", "PKCS8ShroudedKeyBag", "PSpecified", "PolicyConstraints", "PolicyMappings", "PrivateKeyUsage", "RC2$CBC$PKCS5Padding", "RC5$CBC$PKCS5Padding", "RSA", "RSASSA_PSS", "ReasonCode", "SHA1withDSA", "SHA1withECDSA", "SHA1withRSA", "SHA224withDSA", "SHA224withECDSA", "SHA224withRSA", "SHA256withDSA", "SHA256withECDSA", "SHA256withRSA", "SHA384withDSA", "SHA384withECDSA", "SHA384withRSA", "SHA3_224", "SHA3_224withDSA", "SHA3_224withECDSA", "SHA3_224withRSA", "SHA3_256", "SHA3_256withDSA", "SHA3_256withECDSA", "SHA3_256withRSA", "SHA3_384", "SHA3_384withDSA", "SHA3_384withECDSA", "SHA3_384withRSA", "SHA3_512", "SHA3_512withDSA", "SHA3_512withECDSA", "SHA3_512withRSA", "SHA512$224withRSA", "SHA512$256withRSA", "SHA512withDSA", "SHA512withECDSA", "SHA512withRSA", "SHAKE128_256", "SHAKE128_LEN", "SHAKE256_512", "SHAKE256_LEN", "SHA_1", "SHA_224", "SHA_256", "SHA_384", "SHA_512", "SHA_512$224", "SHA_512$256", "SMIMECapability", "SecretBag", "SerialNumber", "SignatureTimestampToken", "SignedAndEnvelopedData", "SignedData", "SigningCertificate", "SigningTime", "SkipIPAddress", "SpecifiedSHA2withECDSA", "StateName", "StreetAddress", "SubjectAlternativeName", "SubjectDirectoryAttributes", "SubjectInfoAccess", "SubjectKeyID", "Surname", "TimeStampTokenInfo", "Title", "UCL_DomainComponent", "UCL_UserID", "UnstructuredAddress", "UnstructuredName", "X25519", "X448", "X942_DH", "anyExtendedKeyUsage", "brainpoolP160r1", "brainpoolP192r1", "brainpoolP224r1", "brainpoolP256r1", "brainpoolP320r1", "brainpoolP384r1", "brainpoolP512r1", "c2tnb191v1", "c2tnb191v2", "c2tnb191v3", "c2tnb239v1", "c2tnb239v2", "c2tnb239v3", "c2tnb359v1", "c2tnb431r1", "caIssuers", "caRepository", "clientAuth", "codeSigning", "emailProtection", "extendedKeyUsage", "ipsecEndSystem", "ipsecTunnel", "ipsecUser", "prime192v2", "prime192v3", "prime239v1", "prime239v2", "prime239v3", "secp112r1", "secp112r2", "secp128r1", "secp128r2", "secp160k1", "secp160r1", "secp160r2", "secp192k1", "secp192r1", "secp224k1", "secp224r1", "secp256k1", "secp256r1", "secp384r1", "secp521r1", "sect113r1", "sect113r2", "sect131r1", "sect131r2", "sect163k1", "sect163r1", "sect163r2", "sect193r1", "sect193r2", "sect233k1", "sect233r1", "sect239k1", "sect283k1", "sect283r1", "sect409k1", "sect409r1", "sect571k1", "sect571r1", "serverAuth"))), "enumConstants");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static Set<String> interfaceNames(Class<?> c) {
        Set<String> s = new TreeSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new TreeSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mm = m.getModifiers();
            if (Modifier.isPublic(mm) || Modifier.isProtected(mm)) s.add(m.getName());
        }
        return s;
    }
    static Set<String> fieldNames(Class<?> c) {
        Set<String> s = new TreeSet<>();
        for (Field fld : c.getDeclaredFields()) {
            if (fld.isEnumConstant()) continue;
            int fm = fld.getModifiers();
            if (Modifier.isPublic(fm) || Modifier.isProtected(fm)) s.add(fld.getName());
        }
        return s;
    }
    static Set<String> enumConstantNames(Class<?> c) {
        Set<String> s = new TreeSet<>();
        for (Field fld : c.getDeclaredFields()) {
            if (fld.isEnumConstant()) s.add(fld.getName());
        }
        return s;
    }
}
