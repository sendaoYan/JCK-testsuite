/*
 * @test
 * @summary org.omg.PortableServer.IdUniquenessPolicyValue conformance: enumeration values.
 * @run main IdUniquenessPolicyValueTest
 */

public class IdUniquenessPolicyValueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableServer.IdUniquenessPolicyValue.UNIQUE_ID != null, "UNIQUE_ID constant non-null");
        check(org.omg.PortableServer.IdUniquenessPolicyValue.UNIQUE_ID.value() == org.omg.PortableServer.IdUniquenessPolicyValue._UNIQUE_ID, "value() matches int constant");
        check(org.omg.PortableServer.IdUniquenessPolicyValue.from_int(org.omg.PortableServer.IdUniquenessPolicyValue._UNIQUE_ID) == org.omg.PortableServer.IdUniquenessPolicyValue.UNIQUE_ID, "from_int round-trips");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
