/*
 * @test
 * @summary javax.sql.rowset.serial.SerialJavaObject conformance: Java object serialization wrapper
 * @run main SerialJavaObjectTest
 */

public class SerialJavaObjectTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        javax.sql.rowset.serial.SerialJavaObject sj = new javax.sql.rowset.serial.SerialJavaObject("xyz");
        check("xyz".equals(sj.getObject()), "getObject");
        check(sj.getFields() != null, "getFields non-null");
        javax.sql.rowset.serial.SerialJavaObject sj2 = new javax.sql.rowset.serial.SerialJavaObject("xyz");
        check(sj.equals(sj2), "equals");
        check(sj.hashCode() == sj2.hashCode(), "hashCode");
        check(sj.clone() != null, "clone non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
