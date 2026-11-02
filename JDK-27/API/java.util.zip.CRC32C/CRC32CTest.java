/*
 * @test
 * @summary java.util.zip.CRC32C conformance: update/reset/getValue check value.
 * @run main CRC32CTest
 */
import java.util.zip.CRC32C;

public class CRC32CTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        CRC32C crc = new CRC32C();
        check(crc.getValue() == 0L, "initial zero");
        crc.update("123456789".getBytes());
        check(crc.getValue() == 0xe3069283L, "check value 0xE3069283");
        crc.reset();
        check(crc.getValue() == 0L, "reset");
        crc.update(65);
        check(crc.getValue() != 0L, "update(int)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
