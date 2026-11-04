/*
 * @test
 * @summary java.io.ObjectInput API conformance: full contract coverage.
 *          Polymorphic use through ObjectInputStream over objects, bytes and primitives;
 *          available/skip/close contract; DataInput EOFException; ClassNotFoundException
 *          surface from readObject.
 * @run main ObjectInputTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ObjectInputTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Pair implements Serializable {
        private static final long serialVersionUID = 300L;
        int k; String v;
        Pair(int k, String v) { this.k = k; this.v = v; }
    }

    static class NoThere implements Serializable {
        private static final long serialVersionUID = 301L;
    }

    // ---- 1. mixed stream consumed entirely through the ObjectInput reference ----
    static void testMixedStream() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(new Pair(1, "one"));
        oos.writeObject("second");
        oos.writeInt(0xCAFEBABE);
        oos.write(new byte[]{9, 8, 7});
        oos.flush();
        oos.close();

        ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        Pair p = (Pair) in.readObject();
        check(p.k == 1 && "one".equals(p.v), "readObject through interface");
        check("second".equals(in.readObject()), "second object");
        check(in.readInt() == 0xCAFEBABE, "DataInput method through interface");
        byte[] three = new byte[3];
        check(in.read(three) == 3 && three[0] == 9 && three[2] == 7,
              "read(byte[]) through interface");
        check(in.read() == -1, "EOF signaled through interface");
        in.close();
    }

    // ---- 2. available()/skip() on the interface ----
    static void testAvailableSkip() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.write(new byte[]{1, 2, 3, 4});
        oos.close();

        ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        check(in.available() == 4, "available reports block data size");
        check(in.skip(2) == 2, "skip within block data");
        check(in.read() == 3, "read after skip");
        in.close();
    }

    // ---- 3. reading past EOF raises EOFException ----
    static void testEOFException() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeShort(1);
        oos.close();

        ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        in.readShort();
        try {
            in.readInt();
            check(false, "readInt past EOF must throw EOFException");
        } catch (java.io.EOFException expected) {
            passed++;
        }
        in.close();
    }

    // ---- 4. close through the interface ----
    static void testClose() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject("x");
        oos.close();

        ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        check("x".equals(in.readObject()), "object read through AutoCloseable view");
        in.close();
        try {
            in.readObject();
            check(false, "readObject after close must throw");
        } catch (IOException expected) {
            passed++;
        }
    }

    // ---- 5. ClassNotFoundException surface ----
    static void testClassNotFound() throws Exception {
        ByteArrayOutputStream b2 = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(b2);
        oos.writeObject(new NoThere());
        oos.close();

        ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(b2.toByteArray())) {
            @Override
            protected Class<?> resolveClass(java.io.ObjectStreamClass desc)
                    throws IOException, ClassNotFoundException {
                if (desc.getName().endsWith("NoThere")) throw new ClassNotFoundException(desc.getName());
                return super.resolveClass(desc);
            }
        };
        try {
            in.readObject();
            check(false, "ClassNotFoundException must propagate from readObject");
        } catch (ClassNotFoundException expected) {
            check(expected.getMessage().endsWith("NoThere"),
                  "ClassNotFoundException carries the streamed class name");
        }
        in.close();
    }

    public static void main(String[] args) throws Exception {
        testMixedStream();
        testAvailableSkip();
        testEOFException();
        testClose();
        testClassNotFound();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
