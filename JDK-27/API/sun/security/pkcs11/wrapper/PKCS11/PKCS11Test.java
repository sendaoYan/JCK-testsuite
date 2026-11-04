/*
 * @test
 * @summary sun.security.pkcs11.wrapper.PKCS11 conformance: internal class reflection.
 * @modules jdk.crypto.cryptoki/sun.security.pkcs11.wrapper:+open
 * @run main PKCS11Test
 */
public class PKCS11Test {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.pkcs11.wrapper.PKCS11", false, PKCS11Test.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("sun.security.pkcs11.wrapper.PKCS11"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        {
            java.lang.Class<?>[] arr = c.getInterfaces();
            String[] n = new String[arr.length];
            for (int i = 0; i < arr.length; i++) n[i] = arr[i].getName();
            java.util.Arrays.sort(n);
            check(String.join(";", n).equals(""), "interfaces");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
                int mod = m.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(m.getName());
            }
            check(String.join(";", s).equals("C_CloseSession;C_CopyObject;C_CreateObject;C_Decrypt;C_DecryptFinal;C_DecryptInit;C_DecryptUpdate;C_DeriveKey;C_DestroyObject;C_DigestFinal;C_DigestInit;C_DigestKey;C_DigestSingle;C_DigestUpdate;C_Encrypt;C_EncryptFinal;C_EncryptInit;C_EncryptUpdate;C_Finalize;C_FindObjects;C_FindObjectsFinal;C_FindObjectsInit;C_GCMDecryptInitWithRetry;C_GCMEncryptInitWithRetry;C_GenerateKey;C_GenerateKeyPair;C_GenerateRandom;C_GetAttributeValue;C_GetInfo;C_GetMechanismInfo;C_GetMechanismList;C_GetOperationState;C_GetSessionInfo;C_GetSlotInfo;C_GetSlotList;C_GetTokenInfo;C_Login;C_Logout;C_OpenSession;C_SeedRandom;C_SessionCancel;C_SetAttributeValue;C_SetOperationState;C_Sign;C_SignFinal;C_SignInit;C_SignRecover;C_SignRecoverInit;C_SignUpdate;C_UnwrapKey;C_Verify;C_VerifyFinal;C_VerifyInit;C_VerifyRecover;C_VerifyRecoverInit;C_VerifyUpdate;C_WrapKey;createNativeKey;getInstance;getNativeKeyInfo;getVersion;loadNative;toString"), "methods");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                int mod = f.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(f.getName());
            }
            check(String.join(";", s).equals(""), "fields");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
