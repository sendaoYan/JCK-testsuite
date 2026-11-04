/*
 * @test
 * @summary javax.smartcardio.ResponseAPDU conformance: response apdu.
 * @run main ResponseAPDUTest
 */

public class ResponseAPDUTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        javax.smartcardio.ResponseAPDU r = new javax.smartcardio.ResponseAPDU(new byte[]{0x11, 0x22, (byte)0x90, 0x00});
        check(r.getNr() == 2, "getNr");
        check(r.getData().length == 2, "getData length");
        check(r.getSW1() == 0x90, "getSW1");
        check(r.getSW2() == 0x00, "getSW2");
        check(r.getSW() == 0x9000, "getSW");
        check(r.getBytes().length == 4, "getBytes length");
        check(r.toString() != null, "toString");
        javax.smartcardio.ResponseAPDU r2 = new javax.smartcardio.ResponseAPDU(new byte[]{0x11, 0x22, (byte)0x90, 0x00});
        check(r.equals(r2), "equals");
        check(r.hashCode() == r2.hashCode(), "hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
