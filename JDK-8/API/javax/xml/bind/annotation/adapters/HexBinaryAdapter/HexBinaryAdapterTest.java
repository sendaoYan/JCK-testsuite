/*
 * @test
 * @summary javax.xml.bind.annotation.adapters.HexBinaryAdapter conformance: hex binary adapter.
 * @run main HexBinaryAdapterTest
 */


public class HexBinaryAdapterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        javax.xml.bind.annotation.adapters.HexBinaryAdapter a = new javax.xml.bind.annotation.adapters.HexBinaryAdapter();
        byte[] bts = a.unmarshal("CAFE");
        check(bts.length == 2 && (bts[0] & 0xff) == 0xca && (bts[1] & 0xff) == 0xfe, "unmarshal");
        check("CAFE".equals(a.marshal(new byte[]{(byte) 0xca, (byte) 0xfe})), "marshal");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
