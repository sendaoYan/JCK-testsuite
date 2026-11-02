/*
 * @test
 * @summary java.sql.Types conformance: JDBC type constant values
 * @run main TypesTest
 */

public class TypesTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        check(java.sql.Types.INTEGER == 4, "INTEGER == 4");
        check(java.sql.Types.VARCHAR == 12, "VARCHAR == 12");
        check(java.sql.Types.JAVA_OBJECT == 2000, "JAVA_OBJECT == 2000");
        check(java.sql.Types.BOOLEAN == 16, "BOOLEAN == 16");
        check(java.sql.Types.BIT == -7, "BIT == -7");
        check(java.sql.Types.ARRAY == 2003, "ARRAY == 2003");
        check(java.sql.Types.BLOB == 2004, "BLOB == 2004");
        check(java.sql.Types.NULL == 0, "NULL == 0");
        check(java.sql.Types.DATE == 91, "DATE == 91");
        check(java.sql.Types.TIME == 92, "TIME == 92");
        check(java.sql.Types.TIMESTAMP == 93, "TIMESTAMP == 93");
        check(!java.sql.Types.class.isInterface(), "not interface");
        check(java.sql.Types.class.getDeclaredFields().length == 41, "41 int constant fields");        System.out.println("PASS (" + passed + " assertions)");
    }
}
