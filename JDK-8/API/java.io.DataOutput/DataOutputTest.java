/*
 * @test
 * @summary java.io.DataOutput API conformance: writeInt/writeUTF via a DataOutputStream.
 * @run main DataOutputTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;

public class DataOutputTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDataOutput() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutput out = new DataOutputStream(bos);
        out.writeInt(42);
        out.writeUTF("hi");

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(bos.toByteArray()));
        check(in.readInt() == 42 && "hi".equals(in.readUTF()), "write roundtrip");
    }

    public static void main(String[] args) throws Exception {
        testDataOutput();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
