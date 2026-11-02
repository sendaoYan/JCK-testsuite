/*
 * @test
 * @summary java.security.KeyRep conformance: constructor and SerializationRep.
 * @run main KeyRepTest
 */

public class KeyRepTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.KeyRep kr = new java.security.KeyRep(java.security.KeyRep.Type.SECRET, "AES", "RAW", new byte[]{1,2,3});
        check(kr instanceof java.io.Serializable, "implements Serializable");
        check("SECRET".equals(java.security.KeyRep.Type.SECRET.toString()), "Type constant");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
