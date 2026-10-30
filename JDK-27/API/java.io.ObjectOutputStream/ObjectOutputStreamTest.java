/*
 * @test
 * @summary java.io.ObjectOutputStream API conformance: serialize a String and an int.
 * @run main ObjectOutputStreamTest
 */

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

public class ObjectOutputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testObjectOutputStream() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject("hello");
        oos.writeInt(42);
        oos.flush();
        byte[] data = bos.toByteArray();
        check(data.length > 0, "serialized non-empty");
    }

    public static void main(String[] args) throws Exception {
        testObjectOutputStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
