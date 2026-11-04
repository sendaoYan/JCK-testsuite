/*
 * @test
 * @summary java.security.CryptoPrimitive conformance: enum constants.
 * @run main CryptoPrimitiveTest
 */

public class CryptoPrimitiveTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.CryptoPrimitive");
        check(c.isEnum(), "is enum");
        Object[] cs = c.getEnumConstants();
        check(cs.length == 10, "constant count");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (Object o : cs) names.add(o.toString());
        String[] expect = { "BLOCK_CIPHER", "KEY_AGREEMENT", "KEY_ENCAPSULATION", "KEY_WRAP", "MAC", "MESSAGE_DIGEST", "PUBLIC_KEY_ENCRYPTION", "SECURE_RANDOM", "SIGNATURE", "STREAM_CIPHER" };
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expect))), "exact constants");

        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- appended contract dimensions ----
    static void testContract() throws Exception {
        java.security.CryptoPrimitive[] vs = java.security.CryptoPrimitive.values();

        // valueOf identity for every constant
        for (java.security.CryptoPrimitive p : vs)
            check(java.security.CryptoPrimitive.valueOf(p.name()) == p, "valueOf identity " + p.name());
        check(java.security.CryptoPrimitive.valueOf("SIGNATURE") == java.security.CryptoPrimitive.SIGNATURE,
                "valueOf SIGNATURE");
        check(java.security.CryptoPrimitive.valueOf("KEY_WRAP") == java.security.CryptoPrimitive.KEY_WRAP,
                "valueOf KEY_WRAP");

        // declaration order fixes ordinals 0..9
        for (int i = 0; i < vs.length; i++)
            check(vs[i].ordinal() == i, "ordinal " + vs[i].name() + " == " + i);

        // compareTo consistent with ordinals
        check(java.security.CryptoPrimitive.MESSAGE_DIGEST
                .compareTo(java.security.CryptoPrimitive.SIGNATURE) < 0, "MESSAGE_DIGEST < SIGNATURE");
        check(java.security.CryptoPrimitive.KEY_AGREEMENT
                .compareTo(java.security.CryptoPrimitive.BLOCK_CIPHER) > 0, "KEY_AGREEMENT > BLOCK_CIPHER");
        check(java.security.CryptoPrimitive.MAC.compareTo(java.security.CryptoPrimitive.MAC) == 0,
                "compareTo self zero");

        // equals/hashCode identity semantics; name()/toString() agree
        for (java.security.CryptoPrimitive p : vs) {
            check(p.toString().equals(p.name()), "toString==name " + p.name());
            check(p.hashCode() == p.hashCode(), "hashCode stable " + p.name());
        }
        check(!java.security.CryptoPrimitive.MAC.equals(java.security.CryptoPrimitive.SIGNATURE),
                "distinct constants unequal");

        // values() returns a fresh, freely mutable array each call
        java.security.CryptoPrimitive[] a1 = java.security.CryptoPrimitive.values();
        java.security.CryptoPrimitive[] a2 = java.security.CryptoPrimitive.values();
        check(a1 != a2, "values() fresh array");
        check(java.util.Arrays.equals(a1, a2), "values() same content");

        // getDeclaringClass / getEnumConstants
        check(java.security.CryptoPrimitive.SIGNATURE.getDeclaringClass() == java.security.CryptoPrimitive.class,
                "getDeclaringClass");
        check(java.security.CryptoPrimitive.class.getEnumConstants().length == 10, "getEnumConstants length");

        // bogus / null names rejected
        try {
            java.security.CryptoPrimitive.valueOf("signature");
            check(false, "valueOf case-sensitive");
        } catch (IllegalArgumentException e) { passed++; }
        try {
            java.security.CryptoPrimitive.valueOf("NO_SUCH");
            check(false, "valueOf bogus IAE");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("NO_SUCH"), "IAE message names constant");
        }
        try {
            java.security.CryptoPrimitive.valueOf(null);
            check(false, "valueOf null NPE");
        } catch (NullPointerException e) { passed++; }

        // enum constants serialize by name back to the singleton
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(java.security.CryptoPrimitive.SIGNATURE);
        oos.close();
        java.io.ObjectInputStream ois =
                new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()));
        check(ois.readObject() == java.security.CryptoPrimitive.SIGNATURE, "serialization restores singleton");

        // EnumSet usage per javadoc ("reasonable combination" pattern)
        java.util.EnumSet<java.security.CryptoPrimitive> digests =
                java.util.EnumSet.of(java.security.CryptoPrimitive.MESSAGE_DIGEST);
        java.util.EnumSet<java.security.CryptoPrimitive> sym =
                java.util.EnumSet.of(java.security.CryptoPrimitive.BLOCK_CIPHER,
                        java.security.CryptoPrimitive.STREAM_CIPHER);
        check(digests.size() == 1 && sym.size() == 2, "EnumSet sizes");
        check(java.util.Collections.disjoint(digests, sym), "disjoint primitive groups");
        check(java.util.EnumSet.allOf(java.security.CryptoPrimitive.class).size() == 10, "EnumSet allOf");
        check(java.util.EnumSet.range(java.security.CryptoPrimitive.BLOCK_CIPHER,
                java.security.CryptoPrimitive.MAC).size() == 3, "EnumSet range BLOCK_CIPHER..MAC size 3");
    }
}
