/*
 * @test
 * @summary javax.sql.rowset.serial.SQLInputImpl conformance: typed value reads
 * @run main SQLInputImplTest
 */

public class SQLInputImplTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        java.util.Map<String, Class<?>> m = new java.util.HashMap<String, Class<?>>();
        javax.sql.rowset.serial.SQLInputImpl in = new javax.sql.rowset.serial.SQLInputImpl(new Object[]{42, "abc", Boolean.TRUE, Double.valueOf(1.5)}, m);
        check(in.readInt() == 42, "readInt");
        check("abc".equals(in.readString()), "readString");
        check(in.readBoolean() == true, "readBoolean");
        check(in.readDouble() == 1.5, "readDouble");
        check(in.wasNull() == false, "wasNull false after non-null reads");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
