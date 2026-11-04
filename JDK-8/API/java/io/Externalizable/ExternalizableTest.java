/*
 * @test
 * @summary java.io.Externalizable conformance (pure black-box): writeExternal /
 *          readExternal full control of the stream, no-arg constructor
 *          requirement, DataOutput/DataInput-style primitive round-trip.
 * @run main ExternalizableTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InvalidClassException;
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

    static byte[] ser(Object o) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(o);
        }
        return bos.toByteArray();
    }

    static Object de(byte[] b) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(b))) {
            return ois.readObject();
        }
    }

    /** Fully hand-written serial form using the ObjectOutput/DataOutput surface. */
    static class Ext implements Externalizable {
        private static final long serialVersionUID = 11L;
        int i;
        String s;
        long l;
        boolean b;
        transient int marker = 99;          // Externalizable ignores transient: WE decide
        static int instances;

        public Ext() { instances++; }

        Ext(int i, String s, long l, boolean b) {
            this.i = i; this.s = s; this.l = l; this.b = b;
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(i);
            out.writeUTF(s);
            out.writeLong(l);
            out.writeBoolean(b);
            out.writeInt(777);              // transient field written deliberately
        }

        public void readExternal(ObjectInput in) throws IOException {
            i = in.readInt();
            s = in.readUTF();
            l = in.readLong();
            b = in.readBoolean();
            marker = in.readInt();
        }
    }

    /** Externalizable class missing the public no-arg constructor. */
    static class NoCtor implements Externalizable {
        private static final long serialVersionUID = 12L;
        public NoCtor(int x) {}
        public void writeExternal(ObjectOutput out) throws IOException { out.writeInt(1); }
        public void readExternal(ObjectInput in) throws IOException {}
    }

    static void testBlackBox() throws Exception {
        // 1. full round-trip with hand-written form
        Ext src = new Ext(42, "ext", 1L << 40, true);
        Ext dst = (Ext) de(ser(src));
        check(dst.i == 42, "readExternal int");
        check("ext".equals(dst.s), "readExternal UTF string");
        check(dst.l == (1L << 40), "readExternal long");
        check(dst.b, "readExternal boolean");
        check(dst.marker == 777, "transient is honored only if the author omits it (we chose to write it)");

        // 2. the no-arg constructor is invoked on deserialization (fresh instance counter)
        Ext.instances = 0;
        de(ser(new Ext(1, "x", 2L, false)));
        check(Ext.instances >= 1, "public no-arg constructor invoked during readObject");

        // 3. Externalizable class without an accessible no-arg ctor is rejected on read
        byte[] bytes = ser(new NoCtor(1));
        try {
            de(bytes);
            check(false, "missing no-arg ctor must fail deserialization");
        } catch (InvalidClassException ice) {
            check(ice.getMessage() != null && ice.getMessage().endsWith("no valid constructor"),
                  "InvalidClassException reports no valid constructor, got: " + ice.getMessage());
        }

        // 4. field-order contract: readExternal consumes exactly what writeExternal wrote,
        //    and both directions share the DataOutput/DataInput primitive encoding
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new Ext(7, "v", -8L, false));
            oos.writeInt(1234);             // primitive data after the object
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            Ext e = (Ext) ois.readObject();
            check(e.i == 7 && "v".equals(e.s) && e.l == -8L && !e.b, "mixed object+primitive stream");
            check(ois.readInt() == 1234, "trailing primitive data still readable");
        }

        // 5. polymorphic use through the interface types
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        ObjectOutput oo = new ObjectOutputStream(bos2);
        oo.writeObject(new Ext(3, "p", 3L, true));
        oo.close();
        check(bos2.size() > 0, "ObjectOutput view of Externalizable works");
    }

    public static void main(String[] args) throws Exception {
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
