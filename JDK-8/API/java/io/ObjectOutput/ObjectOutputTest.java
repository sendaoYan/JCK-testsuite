/*
 * @test
 * @summary java.io.ObjectOutput API conformance: writeObject (extends DataOutput).
 * @run main ObjectOutputTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

public class ObjectOutputTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testObjectOutput() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);
        out.writeObject("hello");
        out.flush();
        out.close();

        ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        check("hello".equals(in.readObject()), "writeObject round-trip");
        in.close();
    }

    public static void main(String[] args) throws Exception {
        testObjectOutput();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
