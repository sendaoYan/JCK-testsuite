/*
 * @test
 * @summary javax.smartcardio.ATR conformance: answer to reset.
 * @run main ATRTest
 */

public class ATRTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        byte[] raw = new byte[]{0x3B, 0x00};
        javax.smartcardio.ATR atr = new javax.smartcardio.ATR(raw);
        byte[] got = atr.getBytes();
        check(got.length == 2, "getBytes length");
        check(got[0] == 0x3B && got[1] == 0x00, "getBytes content");
        check(atr.getHistoricalBytes().length == 0, "historical empty");
        check(atr.toString() != null, "toString");
        javax.smartcardio.ATR atr2 = new javax.smartcardio.ATR(new byte[]{0x3B, 0x00});
        check(atr.equals(atr2), "equals");
        check(atr.hashCode() == atr2.hashCode(), "hashCode");
        javax.smartcardio.ATR atr3 = new javax.smartcardio.ATR(new byte[]{0x3F, 0x00});
        check(!atr.equals(atr3), "not equals different ATR");
        boolean threw = false;
        try { new javax.smartcardio.ATR(null); } catch (NullPointerException e) { threw = true; }
        check(threw, "null ATR throws NPE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
