/*
 * @test
 * @summary java.awt.image.DataBufferInt conformance: int data buffer.
 * @run main DataBufferIntTest
 */


import java.awt.image.DataBufferInt;
public class DataBufferIntTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        DataBufferInt db = new DataBufferInt(8);
        check(db.getDataType() == java.awt.image.DataBuffer.TYPE_INT, "type int");
        check(db.getSize() == 8, "size");
        db.setElem(0, 42);
        check(db.getElem(0) == 42, "set/get elem");
        check(db.getNumBanks() == 1, "one bank");
        int[] backing = {1, 2, 3, 4};
        DataBufferInt db2 = new DataBufferInt(backing, 4);
        check(db2.getElem(3) == 4, "backing array");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
