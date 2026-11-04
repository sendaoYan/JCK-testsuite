/*
 * @test
 * @summary java.io.Externalizable API conformance: writeExternal/readExternal round-trip.
 * @run main ExternalizableTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

public class ExternalizableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Ext implements Externalizable {
        int x;

        public Ext() {}

        Ext(int x) {
            this.x = x;
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(x);
        }

        public void readExternal(ObjectInput in) throws IOException {
            x = in.readInt();
        }
    }

    static void testExternalizable() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new Ext(42));
        }
        try (ObjectInputStream ois =
                 new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            Ext e = (Ext) ois.readObject();
            check(e.x == 42, "externalizable round-trip preserves state");
        }
    }

    public static void main(String[] args) throws Exception {
        testExternalizable();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
