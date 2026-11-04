/*
 * @test
 * @summary javax.smartcardio.CommandAPDU conformance: command apdu.
 * @run main CommandAPDUTest
 */

public class CommandAPDUTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        javax.smartcardio.CommandAPDU c = new javax.smartcardio.CommandAPDU(0x00, 0xA4, 0x04, 0x00);
        check(c.getCLA() == 0x00, "CLA");
        check(c.getINS() == 0xA4, "INS");
        check(c.getP1() == 0x04, "P1");
        check(c.getP2() == 0x00, "P2");
        check(c.getNc() == 0, "Nc");
        check(c.getNe() == 0, "Ne");
        check(c.getBytes().length == 4, "bytes length");
        javax.smartcardio.CommandAPDU d = new javax.smartcardio.CommandAPDU(0x00, 0xA4, 0x04, 0x00, new byte[]{1, 2, 3});
        check(d.getNc() == 3, "Nc with data");
        check(d.getData().length == 3, "data length");
        check(d.getBytes().length == 8, "bytes length with data");
        javax.smartcardio.CommandAPDU e = new javax.smartcardio.CommandAPDU(new byte[]{0x00, (byte)0xA4, 0x04, 0x00});
        check(e.getCLA() == 0 && e.getINS() == 0xA4, "ctor byte[]");
        javax.smartcardio.CommandAPDU f = new javax.smartcardio.CommandAPDU(0, 0xA4, 4, 0, new byte[]{1,2,3}, 0, 3);
        check(f.getData().length == 3, "ctor offset");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
