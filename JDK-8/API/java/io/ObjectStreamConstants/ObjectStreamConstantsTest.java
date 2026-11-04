/*
 * @test
 * @summary java.io.ObjectStreamConstants API conformance: full contract coverage.
 *          Exact wire-protocol constant values (STREAM_MAGIC/STREAM_VERSION, TC_* tags,
 *          baseWireHandle, SC_* flags, protocol versions), use from an implementing
 *          class, protocol-version round-trip through ObjectOutputStream.
 * @run main ObjectStreamConstantsTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.ObjectOutput;
import java.io.ObjectInput;
import java.io.ObjectStreamConstants;

public class ObjectStreamConstantsTest implements ObjectStreamConstants {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- 1. stream header + magic/version ----
    static void testStreamHeader() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject("x");
        oos.close();
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bos.toByteArray()));
        check(dis.readShort() == STREAM_MAGIC, "stream header magic is 0xACED");
        check(dis.readShort() == STREAM_VERSION, "stream header version is 5");
        check(STREAM_MAGIC == (short) 0xACED, "STREAM_MAGIC value");
        check(STREAM_VERSION == 5, "STREAM_VERSION value");
    }

    // ---- 2. wire format tag values ----
    static void testTagValues() {
        check(TC_NULL == 0x70 && TC_REFERENCE == 0x71 && TC_CLASSDESC == 0x72,
              "TC_NULL/REFERENCE/CLASSDESC");
        check(TC_OBJECT == 0x73 && TC_STRING == 0x74 && TC_ARRAY == 0x75,
              "TC_OBJECT/STRING/ARRAY");
        check(TC_CLASS == 0x76 && TC_BLOCKDATA == 0x77 && TC_ENDBLOCKDATA == 0x78,
              "TC_CLASS/BLOCKDATA/ENDBLOCKDATA");
        check(TC_RESET == 0x79 && TC_BLOCKDATALONG == 0x7A && TC_EXCEPTION == 0x7B,
              "TC_RESET/BLOCKDATALONG/EXCEPTION");
        check(TC_LONGSTRING == 0x7C && TC_PROXYCLASSDESC == 0x7D && TC_ENUM == 0x7E,
              "TC_LONGSTRING/PROXYCLASSDESC/ENUM");
        check(TC_BASE == TC_NULL && TC_MAX == TC_ENUM, "TC_BASE..TC_MAX range endpoints");
    }

    // ---- 3. handle numbering, flags, protocol versions ----
    static void testHandlesFlagsProtocols() {
        check(baseWireHandle == 0x7E0000, "baseWireHandle value");
        check(SC_WRITE_METHOD == 0x01 && SC_BLOCK_DATA == 0x08,
              "SC_WRITE_METHOD/SC_BLOCK_DATA");
        check(SC_SERIALIZABLE == 0x02 && SC_EXTERNALIZABLE == 0x04 && SC_ENUM == 0x10,
              "SC_SERIALIZABLE/EXTERNALIZABLE/ENUM");
        check(PROTOCOL_VERSION_1 == 1 && PROTOCOL_VERSION_2 == 2, "protocol version values");
    }

    // ---- 4. permission constants ----
    static void testPermissions() {
        check("enableSubstitution".equals(SUBSTITUTION_PERMISSION.getName()),
              "SUBSTITUTION_PERMISSION name");
        check("enableSubclassImplementation".equals(SUBCLASS_IMPLEMENTATION_PERMISSION.getName()),
              "SUBCLASS_IMPLEMENTATION_PERMISSION name");
    }

    // ---- 5. protocol versions round-trip ----
    static void testProtocolRoundTrip() throws Exception {
        for (int version : new int[]{PROTOCOL_VERSION_1, PROTOCOL_VERSION_2}) {
            ByteArrayOutputStream b2 = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(b2);
            oos.useProtocolVersion(version);
            oos.writeObject("v" + version);
            oos.close();
            ObjectInputStream ois =
                    new ObjectInputStream(new ByteArrayInputStream(b2.toByteArray()));
            check(("v" + version).equals(ois.readObject()), "protocol " + version + " round-trip");
            ois.close();
        }
    }


    // ================= deep additions (contract level) =================

    static class PlainSer implements Serializable {
        private static final long serialVersionUID = 500L;   // fixed for cross-version stability
        int x;
    }

    static class HookSer implements Serializable {
        private static final long serialVersionUID = 501L;
        private void writeObject(ObjectOutputStream o) throws java.io.IOException {
            o.defaultWriteObject();
        }
    }

    static class ExtSer implements java.io.Externalizable {
        private static final long serialVersionUID = 502L;
        public ExtSer() { }
        public void writeExternal(ObjectOutput out) throws java.io.IOException { }
        public void readExternal(ObjectInput in) throws java.io.IOException { }
    }

    enum TagEnum { A, B }

    /** Read the flags byte out of the first class descriptor of a real stream. */
    static int descriptorFlags(byte[] stream) throws java.io.IOException {
        java.io.DataInputStream d = new java.io.DataInputStream(
                new java.io.ByteArrayInputStream(stream));
        d.readShort(); d.readShort();                    // magic + version
        int tag = d.readUnsignedByte();
        if (tag == TC_OBJECT || tag == TC_ARRAY || tag == TC_ENUM)
            tag = d.readUnsignedByte();                  // wrapper tag precedes TC_CLASSDESC
        if (tag != TC_CLASSDESC) throw new IllegalStateException("tag=" + tag);
        d.readUTF(); d.readLong();                       // name + suid
        int flags = d.readUnsignedByte();
        d.close();
        return flags;
    }

    // ---- every constant pinned individually (javap -constants authoritative) ----
    static void testEachConstantValue() {
        check(STREAM_MAGIC == -21267, "STREAM_MAGIC == -21267");
        check(STREAM_MAGIC == (short) 0xACED, "STREAM_MAGIC == 0xACED");
        check(STREAM_VERSION == 5, "STREAM_VERSION == 5");
        check(TC_NULL == 0x70, "TC_NULL == 0x70");
        check(TC_REFERENCE == 0x71, "TC_REFERENCE == 0x71");
        check(TC_CLASSDESC == 0x72, "TC_CLASSDESC == 0x72");
        check(TC_OBJECT == 0x73, "TC_OBJECT == 0x73");
        check(TC_STRING == 0x74, "TC_STRING == 0x74");
        check(TC_ARRAY == 0x75, "TC_ARRAY == 0x75");
        check(TC_CLASS == 0x76, "TC_CLASS == 0x76");
        check(TC_BLOCKDATA == 0x77, "TC_BLOCKDATA == 0x77");
        check(TC_ENDBLOCKDATA == 0x78, "TC_ENDBLOCKDATA == 0x78");
        check(TC_RESET == 0x79, "TC_RESET == 0x79");
        check(TC_BLOCKDATALONG == 0x7A, "TC_BLOCKDATALONG == 0x7A");
        check(TC_EXCEPTION == 0x7B, "TC_EXCEPTION == 0x7B");
        check(TC_LONGSTRING == 0x7C, "TC_LONGSTRING == 0x7C");
        check(TC_PROXYCLASSDESC == 0x7D, "TC_PROXYCLASSDESC == 0x7D");
        check(TC_ENUM == 0x7E, "TC_ENUM == 0x7E");
        check(TC_BASE == TC_NULL && TC_MAX == TC_ENUM, "TC_BASE/TC_MAX endpoints");
        check(TC_MAX - TC_BASE == 14, "tags span 15 consecutive values");
        check(baseWireHandle == 0x7E0000, "baseWireHandle == 0x7E0000");
        check(baseWireHandle == 8257536, "baseWireHandle == 8257536");
        check(SC_WRITE_METHOD == 0x01, "SC_WRITE_METHOD == 0x01");
        check(SC_BLOCK_DATA == 0x08, "SC_BLOCK_DATA == 0x08");
        check(SC_SERIALIZABLE == 0x02, "SC_SERIALIZABLE == 0x02");
        check(SC_EXTERNALIZABLE == 0x04, "SC_EXTERNALIZABLE == 0x04");
        check(SC_ENUM == 0x10, "SC_ENUM == 0x10");
        check(PROTOCOL_VERSION_1 == 1, "PROTOCOL_VERSION_1 == 1");
        check(PROTOCOL_VERSION_2 == 2, "PROTOCOL_VERSION_2 == 2");
        // flag bit patterns are disjoint
        check((SC_WRITE_METHOD | SC_BLOCK_DATA | SC_SERIALIZABLE
               | SC_EXTERNALIZABLE | SC_ENUM) == 0x1F, "SC_* flags occupy distinct bits");
    }

    // ---- the real wire header is exactly AC ED 00 05 ----
    static void testRealStreamHeader() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject("x");
        oos.close();
        byte[] b = bos.toByteArray();
        check(b.length > 4, "stream has payload");
        check((b[0] & 0xff) == 0xAC && (b[1] & 0xff) == 0xED,
              "first two bytes are AC ED");
        check((b[2] & 0xff) == 0x00 && (b[3] & 0xff) == 0x05,
              "next two bytes are 00 05");
        check(((b[0] & 0xff) << 8 | (b[1] & 0xff)) == (STREAM_MAGIC & 0xffff),
              "wire magic equals STREAM_MAGIC");
        check(((b[2] & 0xff) << 8 | (b[3] & 0xff)) == STREAM_VERSION,
              "wire version equals STREAM_VERSION");
    }

    // ---- SC_* flags really appear in class descriptors ----
    static void testStreamFlagsReal() throws Exception {
        check(descriptorFlags(streamOf(new PlainSer())) == SC_SERIALIZABLE,
              "plain Serializable descriptor flags == SC_SERIALIZABLE");
        check(descriptorFlags(streamOf(new HookSer()))
              == (SC_SERIALIZABLE | SC_WRITE_METHOD),
              "writeObject hook adds SC_WRITE_METHOD");
        check(descriptorFlags(streamOf(new ExtSer()))
              == (SC_EXTERNALIZABLE | SC_BLOCK_DATA),
              "Externalizable descriptor flags == SC_EXTERNALIZABLE|SC_BLOCK_DATA");
        check(descriptorFlags(streamOf(TagEnum.A)) == (SC_SERIALIZABLE | SC_ENUM),
              "enum descriptor flags == SC_SERIALIZABLE|SC_ENUM");
    }

    static byte[] streamOf(Object o) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        return bos.toByteArray();
    }

    // ---- permission constants: name and type ----
    static void testPermissionsExact() {
        check("enableSubstitution".equals(SUBSTITUTION_PERMISSION.getName()),
              "SUBSTITUTION_PERMISSION name");
        check("".equals(SUBSTITUTION_PERMISSION.getActions()), "no actions");
        check("enableSubclassImplementation".equals(SUBCLASS_IMPLEMENTATION_PERMISSION.getName()),
              "SUBCLASS_IMPLEMENTATION_PERMISSION name");
        check("".equals(SUBCLASS_IMPLEMENTATION_PERMISSION.getActions()), "no actions");
        check(SUBSTITUTION_PERMISSION != SUBCLASS_IMPLEMENTATION_PERMISSION,
              "distinct constants");
        check(SUBSTITUTION_PERMISSION.getClass() == java.io.SerializablePermission.class,
              "constants are SerializablePermission instances");
        check(!SUBSTITUTION_PERMISSION.implies(SUBCLASS_IMPLEMENTATION_PERMISSION),
              "neither implies the other");
    }

    // ---- protocol version argument validation ----
    static void testProtocolVersionRejects() throws Exception {
        for (int bad : new int[]{0, 3, -1, Integer.MAX_VALUE}) {
            try {
                ObjectOutputStream oos = new ObjectOutputStream(
                        new java.io.ByteArrayOutputStream());
                oos.useProtocolVersion(bad);
                check(false, "version " + bad + " must be rejected");
            } catch (IllegalArgumentException iae) {
                check(iae.getMessage() != null
                      && iae.getMessage().contains("unknown version"),
                      "IAE for version " + bad + ": " + iae.getMessage());
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testStreamHeader();
        testTagValues();
        testHandlesFlagsProtocols();
        testPermissions();
        testProtocolRoundTrip();
        testEachConstantValue();
        testRealStreamHeader();
        testStreamFlagsReal();
        testPermissionsExact();
        testProtocolVersionRejects();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
