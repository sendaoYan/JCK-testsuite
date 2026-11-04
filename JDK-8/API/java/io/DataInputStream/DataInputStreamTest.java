/*
 * @test
 * @summary java.io.DataInputStream API conformance: roundtrip readInt/readUTF/readBoolean/
 *          readLong/readDouble/readByte against a DataOutputStream.
 * @run main DataInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class DataInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDataInputStream() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeInt(42);
        dos.writeUTF("hello");
        dos.writeBoolean(true);
        dos.writeLong(123L);
        dos.writeDouble(2.5);
        dos.writeByte(7);
        dos.flush();

        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bos.toByteArray()));
        check(dis.readInt() == 42, "readInt");
        check("hello".equals(dis.readUTF()), "readUTF");
        check(dis.readBoolean(), "readBoolean");
        check(dis.readLong() == 123L, "readLong");
        check(dis.readDouble() == 2.5, "readDouble");
        check(dis.readByte() == 7, "readByte");
    }

    public static void main(String[] args) throws Exception {
        testDataInputStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
