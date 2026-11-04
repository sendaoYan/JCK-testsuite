/*
 * @test
 * @summary java.io.ObjectInput API conformance: readObject (extends DataInput).
 * @run main ObjectInputTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ObjectInputTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testObjectInput() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject("hello");
        oos.close();

        ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        check("hello".equals(in.readObject()), "readObject");
        in.close();
    }

    public static void main(String[] args) throws Exception {
        testObjectInput();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
