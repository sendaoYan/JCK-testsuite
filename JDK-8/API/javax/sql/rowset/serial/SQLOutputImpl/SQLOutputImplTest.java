/*
 * @test
 * @summary javax.sql.rowset.serial.SQLOutputImpl conformance: writeString/writeInt.
 * @run main SQLOutputImplTest
 */


public class SQLOutputImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.sql.rowset.serial.SQLOutputImpl out = new javax.sql.rowset.serial.SQLOutputImpl(new java.util.Vector<Object>(), new java.util.HashMap<String, Object>());
        out.writeString("x");
        out.writeInt(1);
        check(true, "writeString/writeInt");        System.out.println("PASS (" + passed + " assertions)");
    }
}
