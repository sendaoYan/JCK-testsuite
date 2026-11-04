/*
 * @test
 * @summary javax.sql.rowset.serial.SQLInputImpl conformance: readObject/readInt.
 * @run main SQLInputImplTest
 */


public class SQLInputImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.util.Map<String, Class<?>> map = new java.util.HashMap<String, Class<?>>();
        javax.sql.rowset.serial.SQLInputImpl in = new javax.sql.rowset.serial.SQLInputImpl(new Object[]{"hello", Integer.valueOf(42)}, map);
        check("hello".equals(in.readObject()), "readObject");
        check(in.readInt() == 42, "readInt");        System.out.println("PASS (" + passed + " assertions)");
    }
}
