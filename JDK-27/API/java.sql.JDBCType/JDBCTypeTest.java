/*
 * @test
 * @summary java.sql.JDBCType conformance: enum constants and SQLType behavior
 * @run main JDBCTypeTest
 */

public class JDBCTypeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = java.sql.JDBCType.class;
        check(c.isEnum(), "is enum");
        check(c.getInterfaces().length == 1 && c.getInterfaces()[0].getName().equals("java.sql.SQLType"), "implements SQLType");
        check(java.sql.JDBCType.values().length == 41, "41 enum constants");
        check(java.sql.JDBCType.INTEGER.getVendorTypeNumber() == 4, "INTEGER vendor type 4");
        check(java.sql.JDBCType.VARCHAR.getVendorTypeNumber() == 12, "VARCHAR vendor type 12");
        check(java.sql.JDBCType.JAVA_OBJECT.getVendorTypeNumber() == 2000, "JAVA_OBJECT vendor type 2000");
        check(java.sql.JDBCType.BIT.getVendorTypeNumber() == -7, "BIT vendor type -7");
        check(java.sql.JDBCType.BOOLEAN.getVendorTypeNumber() == 16, "BOOLEAN vendor type 16");
        check("INTEGER".equals(java.sql.JDBCType.INTEGER.getName()), "getName");
        check("java.sql".equals(java.sql.JDBCType.INTEGER.getVendor()), "getVendor");
        check(java.sql.JDBCType.valueOf(4) == java.sql.JDBCType.INTEGER, "valueOf(int)");
        check(java.sql.JDBCType.valueOf("BIGINT") == java.sql.JDBCType.BIGINT, "valueOf(String)");
        check(java.sql.JDBCType.INTEGER instanceof java.sql.SQLType, "instanceof SQLType");        System.out.println("PASS (" + passed + " assertions)");
    }
}
