/*
 * @test
 * @summary javax.sql.rowset.serial.SQLOutputImpl conformance: typed value writes
 * @run main SQLOutputImplTest
 */

public class SQLOutputImplTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        java.util.Vector<Object> v = new java.util.Vector<Object>();
        java.util.Map<String, ?> m = new java.util.HashMap<String, Object>();
        javax.sql.rowset.serial.SQLOutputImpl out = new javax.sql.rowset.serial.SQLOutputImpl(v, m);
        out.writeInt(42);
        out.writeString("abc");
        out.writeBoolean(true);
        out.writeDouble(1.5);
        check(v.size() == 4, "four values written");
        check(v.get(0).equals(Integer.valueOf(42)), "writeInt stored");
        check(v.get(1).equals("abc"), "writeString stored");
        check(v.get(2).equals(Boolean.TRUE), "writeBoolean stored");
        check(v.get(3).equals(Double.valueOf(1.5)), "writeDouble stored");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
