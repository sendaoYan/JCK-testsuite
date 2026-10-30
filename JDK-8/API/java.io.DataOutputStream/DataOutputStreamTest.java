/*
 * @test
 * @summary java.io.DataOutputStream API conformance: size() tracking after writing int/long/
 *          boolean primitives.
 * @run main DataOutputStreamTest
 */

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class DataOutputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDataOutputStream() throws Exception {
        DataOutputStream dos = new DataOutputStream(new ByteArrayOutputStream());
        dos.writeInt(42);
        check(dos.size() == 4, "size after writeInt");
        dos.writeLong(1L);
        check(dos.size() == 12, "size after writeLong");
        dos.writeBoolean(true);
        check(dos.size() == 13, "size after writeBoolean");
    }

    public static void main(String[] args) throws Exception {
        testDataOutputStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
