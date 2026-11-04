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

    public static void main(String[] args) throws Exception {
        testStreamHeader();
        testTagValues();
        testHandlesFlagsProtocols();
        testPermissions();
        testProtocolRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
