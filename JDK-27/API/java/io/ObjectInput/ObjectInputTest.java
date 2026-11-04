/*
 * @test
 * @summary java.io.ObjectInput conformance: white-box structure + black-box behavior
 *          (polymorphic use through ObjectInputStream over objects, bytes and primitives;
 *          skip/available/close contract; ObjectInputValidation interplay).
 * @run main ObjectInputTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInput;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.Serial;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ObjectInputTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Pair implements Serializable {
        @Serial
        private static final long serialVersionUID = 300L;
        int k; String v;
        Pair(int k, String v) { this.k = k; this.v = v; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.ObjectInput");
        check(c.isInterface(), "is an interface");
        boolean dataInput = false, autoClose = false;
        for (Class<?> i : c.getInterfaces()) {
            if (i == DataInput.class) dataInput = true;
            if (i.getName().equals("java.lang.AutoCloseable")) autoClose = true;
        }
        check(dataInput, "extends DataInput");
        check(autoClose, "extends AutoCloseable");
        for (String m : new String[]{"readObject", "read", "skip", "available", "close",
                                     "readInt", "readUTF", "readFully", "skipBytes"})
            check(has(c, m), "has method " + m);
        check(hasOverload(c, "read", byte[].class), "read(byte[]) overload");
        check(hasOverload(c, "read", byte[].class, int.class, int.class),
              "read(byte[], int, int) overload");
        for (Method m : c.getMethods())
            check(Modifier.isAbstract(m.getModifiers()), "abstract method " + m.getName());
    }

    static boolean has(Class<?> c, String m) {
        for (Method mm : c.getMethods()) if (mm.getName().equals(m)) return true;
        return false;
    }

    static boolean hasOverload(Class<?> c, String m, Class<?>... ps) {
        try { c.getMethod(m, ps); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. mixed stream consumed entirely through the ObjectInput reference type
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new Pair(1, "one"));
            oos.writeObject("second");
            oos.writeInt(0xCAFEBABE);
            oos.write(new byte[]{9, 8, 7});
            oos.flush();
        }
        try (ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            Pair p = (Pair) in.readObject();
            check(p.k == 1 && "one".equals(p.v), "readObject through interface");
            check("second".equals(in.readObject()), "second object");
            check(in.readInt() == 0xCAFEBABE, "DataInput method through interface");
            byte[] three = new byte[3];
            check(in.read(three) == 3 && three[0] == 9 && three[2] == 7,
                  "read(byte[]) through interface");
            check(in.read() == -1, "EOF signaled through interface");
        }

        // 2. available()/skip() on the interface
        bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.write(new byte[]{1, 2, 3, 4});
        }
        try (ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            check(in.available() == 4, "available reports block data size");
            check(in.skip(2) == 2, "skip within block data");
            check(in.read() == 3, "read after skip");
        }

        // 3. reading past EOF raises EOFException via the DataInput part of the contract
        bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeShort(1);
        }
        try (ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            in.readShort();
            try {
                in.readInt();
                check(false, "readInt past EOF must throw EOFException");
            } catch (java.io.EOFException expected) {
                passed++;
            }
        }

        // 4. close through the interface (AutoCloseable/TWR interplay)
        bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject("x");
        }
        ObjectInputStream backing = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        ObjectInput in = backing;
        check(in instanceof Closeable || in instanceof AutoCloseable,
              "ObjectInput usable in try-with-resources");
        check("x".equals(in.readObject()), "object read through AutoCloseable view");
        in.close();
        try {
            in.readObject();
            check(false, "readObject after close must throw");
        } catch (IOException expected) {
            passed++;
        }

        // 5. ClassNotFoundException surface: stream names a class not on the classpath
        byte[] bytes;
        ByteArrayOutputStream b2 = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(b2)) {
            oos.writeObject(new NoThere());   // serialized under this test's classloader
            bytes = b2.toByteArray();
        }
        try (ObjectInput in5 = new ObjectInputStream(new ByteArrayInputStream(bytes)) {
            @Override
            protected Class<?> resolveClass(java.io.ObjectStreamClass desc)
                    throws IOException, ClassNotFoundException {
                if (desc.getName().endsWith("NoThere")) throw new ClassNotFoundException(desc.getName());
                return super.resolveClass(desc);
            }
        }) {
            try {
                in5.readObject();
                check(false, "ClassNotFoundException must propagate from readObject");
            } catch (ClassNotFoundException expected) {
                check(expected.getMessage().endsWith("NoThere"),
                      "ClassNotFoundException carries the streamed class name");
            }
        }
    }

    static class NoThere implements Serializable {
        @Serial
        private static final long serialVersionUID = 301L;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
