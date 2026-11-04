/*
 * @test
 * @summary java.io.ObjectInputStream API conformance: deserialize a String and an int.
 * @run main ObjectInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ObjectInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testObjectInputStream() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject("hello");
        oos.writeInt(42);
        oos.flush();

        ObjectInputStream ois =
            new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        check("hello".equals(ois.readObject()), "readObject");
        check(ois.readInt() == 42, "readInt");
    }

    public static void main(String[] args) throws Exception {
        testObjectInputStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
