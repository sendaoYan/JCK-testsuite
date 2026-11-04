/*
 * @test
 * @summary java.security.KeyRep conformance: constructor and SerializationRep.
 *          Appended: null-argument NPE matrix (type/algorithm/format/encoded),
 *          deferred validation (constructor accepts arbitrary strings),
 *          readResolve resolution paths: SECRET+RAW -> SecretKeySpec,
 *          unrecognized type/format and unresolvable algorithm/encoding ->
 *          NotSerializableException with exact message shapes, real RSA
 *          public/private key writeReplace serialization round-trip (equals,
 *          algorithm/format/encoding preserved), protected readResolve,
 *          Serializable + Type enum reuse.
 * @run main KeyRepTest
 */

public class KeyRepTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static Object roundtrip(java.io.Serializable o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        java.io.ObjectInputStream ois =
                new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()));
        return ois.readObject();
    }

    public static void main(String[] args) throws Exception {
        java.security.KeyRep kr = new java.security.KeyRep(java.security.KeyRep.Type.SECRET, "AES", "RAW", new byte[]{1,2,3});
        check(kr instanceof java.io.Serializable, "implements Serializable");
        check("SECRET".equals(java.security.KeyRep.Type.SECRET.toString()), "Type constant");
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- appended contract dimensions ----
    static void testContract() throws Exception {
        byte[] enc = {1, 2, 3, 4};

        // null matrix: every argument required non-null, NPE at construction
        checkThrows(NullPointerException.class,
                () -> new java.security.KeyRep(null, "AES", "RAW", enc), "null type -> NPE");
        checkThrows(NullPointerException.class,
                () -> new java.security.KeyRep(java.security.KeyRep.Type.SECRET, null, "RAW", enc),
                "null algorithm -> NPE");
        checkThrows(NullPointerException.class,
                () -> new java.security.KeyRep(java.security.KeyRep.Type.SECRET, "AES", null, enc),
                "null format -> NPE");
        checkThrows(NullPointerException.class,
                () -> new java.security.KeyRep(java.security.KeyRep.Type.SECRET, "AES", "RAW", null),
                "null encoded -> NPE");

        // construction never validates content: arbitrary strings/empty arrays accepted
        java.security.KeyRep weird = new java.security.KeyRep(java.security.KeyRep.Type.SECRET, "?", "?", new byte[0]);
        check(weird != null, "constructor accepts arbitrary strings (deferred validation)");
        check(new java.security.KeyRep(java.security.KeyRep.Type.SECRET, "AES", "RAW", new byte[]{}) != null,
                "empty encoded array constructible");

        // readResolve: SECRET + RAW resolves to SecretKeySpec with identical material
        Object secret = roundtrip(new java.security.KeyRep(java.security.KeyRep.Type.SECRET, "AES", "RAW", enc));
        check(secret instanceof javax.crypto.spec.SecretKeySpec, "SECRET+RAW -> SecretKeySpec");
        check(secret instanceof java.security.Key, "resolved object is a Key");
        check("AES".equals(((java.security.Key) secret).getAlgorithm()), "resolved algorithm");
        check("RAW".equals(((java.security.Key) secret).getFormat()), "resolved format RAW");
        check(java.util.Arrays.equals(enc, ((java.security.Key) secret).getEncoded()), "resolved encoding preserved");

        // unrecognized type/format combination -> NotSerializableException naming the pair
        try {
            roundtrip(new java.security.KeyRep(java.security.KeyRep.Type.SECRET, "AES", "WEIRD", enc));
            check(false, "SECRET+WEIRD must fail");
        } catch (java.io.NotSerializableException e) {
            check(e.getMessage() != null && e.getMessage().contains("unrecognized type/format")
                    && e.getMessage().contains("SECRET") && e.getMessage().contains("WEIRD"),
                    "NotSerializableException names type/format pair");
        }
        try {
            roundtrip(new java.security.KeyRep(java.security.KeyRep.Type.PUBLIC, "RSA", "RAW", enc));
            check(false, "PUBLIC+RAW must fail");
        } catch (java.io.NotSerializableException e) {
            check(e.getMessage() != null && e.getMessage().contains("PUBLIC") && e.getMessage().contains("RAW"),
                    "PUBLIC+RAW unrecognized");
        }

        // unresolvable algorithm / undecodable encoding -> NotSerializableException describing the KeyRep
        try {
            roundtrip(new java.security.KeyRep(java.security.KeyRep.Type.PUBLIC, "BogusAlg", "X.509", enc));
            check(false, "PUBLIC bogus algorithm must fail");
        } catch (java.io.NotSerializableException e) {
            check(e.getMessage() != null && e.getMessage().contains("[PUBLIC]")
                    && e.getMessage().contains("[BogusAlg]") && e.getMessage().contains("[X.509]"),
                    "bogus algorithm message carries [TYPE] [alg] [format]");
        }
        try {
            roundtrip(new java.security.KeyRep(java.security.KeyRep.Type.PUBLIC, "RSA", "X.509", enc));
            check(false, "PUBLIC garbage encoding must fail");
        } catch (java.io.NotSerializableException e) {
            check(e.getMessage() != null && e.getMessage().contains("[PUBLIC]"),
                    "garbage encoding message carries type");
        }
        try {
            roundtrip(new java.security.KeyRep(java.security.KeyRep.Type.PRIVATE, "RSA", "PKCS#8", enc));
            check(false, "PRIVATE garbage encoding must fail");
        } catch (java.io.NotSerializableException e) {
            check(e.getMessage() != null && e.getMessage().contains("[PRIVATE]"),
                    "PRIVATE garbage message carries type");
        }

        // real keys serialize via writeReplace -> KeyRep -> readResolve -> equal key
        java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("RSA");
        kpg.initialize(512);
        java.security.KeyPair kp = kpg.generateKeyPair();
        java.security.Key pub = kp.getPublic();
        java.security.Key priv = kp.getPrivate();
        Object pubBack = roundtrip((java.io.Serializable) pub);
        Object privBack = roundtrip((java.io.Serializable) priv);
        check(!(pubBack instanceof java.security.KeyRep), "deserialized is not a raw KeyRep");
        check(pubBack instanceof java.security.PublicKey, "deserialized pub is PublicKey");
        check(privBack instanceof java.security.PrivateKey, "deserialized priv is PrivateKey");
        check(pubBack.equals(pub), "pub round-trip equals");
        check(privBack.equals(priv), "priv round-trip equals");
        check("RSA".equals(((java.security.Key) pubBack).getAlgorithm())
                && "RSA".equals(((java.security.Key) privBack).getAlgorithm()), "algorithms preserved");
        check("X.509".equals(((java.security.Key) pubBack).getFormat()), "pub format preserved");
        check("PKCS#8".equals(((java.security.Key) privBack).getFormat()), "priv format preserved");
        check(java.util.Arrays.equals(pub.getEncoded(), ((java.security.Key) pubBack).getEncoded()),
                "pub encoding preserved");
        check(java.util.Arrays.equals(priv.getEncoded(), ((java.security.Key) privBack).getEncoded()),
                "priv encoding preserved");
        check(pubBack.hashCode() == pub.hashCode(), "pub hashCode preserved");

        // structural: protected readResolve hook declares ObjectStreamException
        java.lang.reflect.Method rr = java.security.KeyRep.class.getDeclaredMethod("readResolve");
        check(java.lang.reflect.Modifier.isProtected(rr.getModifiers()), "readResolve protected");
        check(rr.getReturnType() == Object.class, "readResolve returns Object");
        boolean declaresOSE = false;
        for (Class<?> e : rr.getExceptionTypes()) if (e == java.io.ObjectStreamException.class) declaresOSE = true;
        check(declaresOSE, "readResolve throws ObjectStreamException");

        // enum-typed payload: the same Type constants are reused inside KeyRep round-trips
        Object secret2 = roundtrip(new java.security.KeyRep(java.security.KeyRep.Type.SECRET, "AES", "RAW", enc));
        check(secret2.equals(secret), "identical KeyRep payload resolves to equal key");
    }
}
