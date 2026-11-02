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

        System.out.println("PASS (" + passed + " assertions)");
    }
}
