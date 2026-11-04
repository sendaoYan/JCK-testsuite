/*
 * @test
 * @summary java.io.DataInput API conformance: readInt/readUTF via a DataInputStream.
 * @run main DataInputTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class DataInputTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDataInput() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeInt(42);
        dos.writeUTF("hi");
        dos.flush();

        DataInput in = new DataInputStream(new ByteArrayInputStream(bos.toByteArray()));
        check(in.readInt() == 42, "readInt");
        check("hi".equals(in.readUTF()), "readUTF");
    }

    public static void main(String[] args) throws Exception {
        testDataInput();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
