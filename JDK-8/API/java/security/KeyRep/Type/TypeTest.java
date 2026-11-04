/*
 * @test
 * @summary java.security.KeyRep.Type conformance: enum constants.
 * @run main TypeTest
 */

public class TypeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.KeyRep$Type");
        check(c.isEnum(), "is enum");
        Object[] cs = c.getEnumConstants();
        check(cs.length == 3, "constant count");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (Object o : cs) names.add(o.toString());
        String[] expect = { "PRIVATE", "PUBLIC", "SECRET" };
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expect))), "exact constants");

        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- appended contract dimensions ----
    static void testContract() throws Exception {
        java.security.KeyRep.Type[] vs = java.security.KeyRep.Type.values();

        // valueOf identity for every constant
        check(java.security.KeyRep.Type.valueOf("SECRET") == java.security.KeyRep.Type.SECRET, "valueOf SECRET");
        check(java.security.KeyRep.Type.valueOf("PUBLIC") == java.security.KeyRep.Type.PUBLIC, "valueOf PUBLIC");
        check(java.security.KeyRep.Type.valueOf("PRIVATE") == java.security.KeyRep.Type.PRIVATE, "valueOf PRIVATE");
        for (java.security.KeyRep.Type t : vs)
            check(java.security.KeyRep.Type.valueOf(t.name()) == t, "name/valueOf roundtrip " + t.name());

        // declaration order defines ordinals (SECRET, PUBLIC, PRIVATE)
        check(java.security.KeyRep.Type.SECRET.ordinal() == 0, "SECRET ordinal 0");
        check(java.security.KeyRep.Type.PUBLIC.ordinal() == 1, "PUBLIC ordinal 1");
        check(java.security.KeyRep.Type.PRIVATE.ordinal() == 2, "PRIVATE ordinal 2");

        // compareTo consistent with ordinal; equals/hashCode identity semantics
        check(java.security.KeyRep.Type.SECRET.compareTo(java.security.KeyRep.Type.PUBLIC) < 0, "SECRET < PUBLIC");
        check(java.security.KeyRep.Type.PRIVATE.compareTo(java.security.KeyRep.Type.SECRET) > 0, "PRIVATE > SECRET");
        check(java.security.KeyRep.Type.SECRET.compareTo(java.security.KeyRep.Type.SECRET) == 0, "compareTo self 0");
        check(java.security.KeyRep.Type.SECRET.hashCode() == java.security.KeyRep.Type.SECRET.hashCode(),
                "hashCode stable");
        check(!java.security.KeyRep.Type.SECRET.equals(java.security.KeyRep.Type.PUBLIC), "distinct constants unequal");

        // name()/toString() equal and exact
        for (java.security.KeyRep.Type t : vs)
            check(t.toString().equals(t.name()), "toString equals name " + t.name());

        // values() returns a fresh array each call (caller may mutate freely)
        java.security.KeyRep.Type[] first = java.security.KeyRep.Type.values();
        java.security.KeyRep.Type[] second = java.security.KeyRep.Type.values();
        check(first != second, "values() fresh array");
        check(first.length == second.length && java.util.Arrays.equals(first, second), "values() same content");

        // getDeclaringClass is the enum itself (not a subclass)
        check(java.security.KeyRep.Type.SECRET.getDeclaringClass() == java.security.KeyRep.Type.class,
                "getDeclaringClass");

        // Class.getEnumConstants mirrors values()
        check(java.security.KeyRep.Type.class.getEnumConstants().length == 3, "getEnumConstants length");

        // bogus names rejected with IllegalArgumentException; case-sensitive lookup
        try {
            java.security.KeyRep.Type.valueOf("secret");
            check(false, "valueOf lowercase rejected");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("secret"), "IAE message names constant");
        }
        try {
            java.security.KeyRep.Type.valueOf("NO_SUCH");
            check(false, "valueOf bogus rejected");
        } catch (IllegalArgumentException e) { passed++; }
        try {
            java.security.KeyRep.Type.valueOf(null);
            check(false, "valueOf null rejected");
        } catch (NullPointerException e) { passed++; }

        // enum constants serialize by name: round-trip restores the same singleton
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(java.security.KeyRep.Type.PUBLIC);
        oos.close();
        java.io.ObjectInputStream ois =
                new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()));
        check(ois.readObject() == java.security.KeyRep.Type.PUBLIC, "serialization restores singleton");

        // usable in EnumSet and switch
        java.util.EnumSet<java.security.KeyRep.Type> set =
                java.util.EnumSet.of(java.security.KeyRep.Type.SECRET, java.security.KeyRep.Type.PRIVATE);
        check(set.size() == 2 && !set.contains(java.security.KeyRep.Type.PUBLIC), "EnumSet contains");
        check(java.util.EnumSet.allOf(java.security.KeyRep.Type.class).size() == 3, "EnumSet allOf size");
        check(switchOf(java.security.KeyRep.Type.PRIVATE).equals("private"), "switch dispatch");
    }

    static String switchOf(java.security.KeyRep.Type t) {
        switch (t) {
            case SECRET: return "secret";
            case PUBLIC: return "public";
            case PRIVATE: return "private";
            default: return "other";
        }
    }
}
