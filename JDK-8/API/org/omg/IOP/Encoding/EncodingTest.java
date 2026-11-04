/*
 * @test
 * @summary org.omg.IOP.Encoding conformance: value type fields and constructors.
 * @run main EncodingTest
 */

public class EncodingTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.IOP.Encoding s0 = new org.omg.IOP.Encoding();
        check(s0.format == (short)0, "no-arg format default");
        check(s0.major_version == (byte)0, "no-arg major_version default");
        check(s0.minor_version == (byte)0, "no-arg minor_version default");
        org.omg.IOP.Encoding s1 = new org.omg.IOP.Encoding((short)7, (byte)7, (byte)7);
        check(s1.format == (short)7, "ctor format stored");
        check(s1.major_version == (byte)7, "ctor major_version stored");
        check(s1.minor_version == (byte)7, "ctor minor_version stored");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
