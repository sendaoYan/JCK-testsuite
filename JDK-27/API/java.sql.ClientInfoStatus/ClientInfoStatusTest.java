/*
 * @test
 * @summary java.sql.ClientInfoStatus conformance: enum constants and valueOf roundtrip
 * @run main ClientInfoStatusTest
 */

import java.sql.ClientInfoStatus;

public class ClientInfoStatusTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = ClientInfoStatus.class;
        check(c.isEnum(), "is enum");
        check(c.getEnumConstants().length == 4, "4 enum constants");
        check(ClientInfoStatus.values().length == 4, "values() length 4");
        check(ClientInfoStatus.valueOf("REASON_UNKNOWN") == ClientInfoStatus.REASON_UNKNOWN, "valueOf == REASON_UNKNOWN");
        check(ClientInfoStatus.valueOf("REASON_UNKNOWN").name().equals("REASON_UNKNOWN"), "name REASON_UNKNOWN");
        check(ClientInfoStatus.valueOf("REASON_UNKNOWN_PROPERTY") == ClientInfoStatus.REASON_UNKNOWN_PROPERTY, "valueOf == REASON_UNKNOWN_PROPERTY");
        check(ClientInfoStatus.valueOf("REASON_UNKNOWN_PROPERTY").name().equals("REASON_UNKNOWN_PROPERTY"), "name REASON_UNKNOWN_PROPERTY");
        check(ClientInfoStatus.valueOf("REASON_VALUE_INVALID") == ClientInfoStatus.REASON_VALUE_INVALID, "valueOf == REASON_VALUE_INVALID");
        check(ClientInfoStatus.valueOf("REASON_VALUE_INVALID").name().equals("REASON_VALUE_INVALID"), "name REASON_VALUE_INVALID");
        check(ClientInfoStatus.valueOf("REASON_VALUE_TRUNCATED") == ClientInfoStatus.REASON_VALUE_TRUNCATED, "valueOf == REASON_VALUE_TRUNCATED");
        check(ClientInfoStatus.valueOf("REASON_VALUE_TRUNCATED").name().equals("REASON_VALUE_TRUNCATED"), "name REASON_VALUE_TRUNCATED");        System.out.println("PASS (" + passed + " assertions)");
    }
}
