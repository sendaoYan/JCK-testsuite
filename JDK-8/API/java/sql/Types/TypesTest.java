/*
 * @test
 * @summary java.sql.Types conformance: constant values.
 * @run main TypesTest
 */


public class TypesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        check(java.sql.Types.BIT == -7, "BIT");
        check(java.sql.Types.INTEGER == 4, "INTEGER");
        check(java.sql.Types.VARCHAR == 12, "VARCHAR");
        check(java.sql.Types.DOUBLE == 8, "DOUBLE");
        check(java.sql.Types.NULL == 0, "NULL");
        check(java.sql.Types.BOOLEAN == 16, "BOOLEAN");
        check(java.sql.Types.BIGINT == -5, "BIGINT");
        check(java.sql.Types.class.getFields().length >= 30, "many constants");        System.out.println("PASS (" + passed + " assertions)");
    }
}
