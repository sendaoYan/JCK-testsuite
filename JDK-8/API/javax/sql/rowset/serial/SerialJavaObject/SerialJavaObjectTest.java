/*
 * @test
 * @summary javax.sql.rowset.serial.SerialJavaObject conformance: getObject/getFields.
 * @run main SerialJavaObjectTest
 */


public class SerialJavaObjectTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.sql.rowset.serial.SerialJavaObject o = new javax.sql.rowset.serial.SerialJavaObject(Integer.valueOf(5));
        check(Integer.valueOf(5).equals(o.getObject()), "getObject");
        check(o.getFields().length >= 1, "getFields");        System.out.println("PASS (" + passed + " assertions)");
    }
}
