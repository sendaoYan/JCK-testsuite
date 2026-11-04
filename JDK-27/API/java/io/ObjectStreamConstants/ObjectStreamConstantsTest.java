/*
 * @test
 * @summary java.io.ObjectStreamConstants conformance: white-box structure + black-box
 *          behavior (exact wire-protocol constant values, use from an implementing class,
 *          protocol-version round-trip through ObjectOutputStream).
 * @run main ObjectStreamConstantsTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamConstants;
import java.io.SerializablePermission;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ObjectStreamConstantsTest implements ObjectStreamConstants {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.ObjectStreamConstants");
        check(c.isInterface(), "is an interface");
        check(c.getMethods().length == 0, "no abstract methods (constants only)");
        String[] names = {"STREAM_MAGIC", "STREAM_VERSION", "TC_BASE", "TC_NULL",
                          "TC_REFERENCE", "TC_CLASSDESC", "TC_OBJECT", "TC_STRING",
                          "TC_ARRAY", "TC_CLASS", "TC_BLOCKDATA", "TC_ENDBLOCKDATA",
                          "TC_RESET", "TC_BLOCKDATALONG", "TC_EXCEPTION",
                          "TC_LONGSTRING", "TC_PROXYCLASSDESC", "TC_ENUM", "TC_MAX",
                          "baseWireHandle", "SC_WRITE_METHOD", "SC_BLOCK_DATA",
                          "SC_SERIALIZABLE", "SC_EXTERNALIZABLE", "SC_ENUM",
                          "PROTOCOL_VERSION_1", "PROTOCOL_VERSION_2"};
        for (String n : names) {
            Field f = c.getField(n);
            check(Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers())
                  && Modifier.isFinal(f.getModifiers()), "constant " + n + " public static final");
        }
        check(c.getField("SUBSTITUTION_PERMISSION").getType() == SerializablePermission.class,
              "SUBSTITUTION_PERMISSION is a SerializablePermission");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. stream header: the first 4 bytes of every serialization stream are
        //    STREAM_MAGIC then STREAM_VERSION -- readable as raw shorts
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject("x");
        }
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bos.toByteArray()));
        check(dis.readShort() == STREAM_MAGIC, "stream header magic is 0xACED");
        check(dis.readShort() == STREAM_VERSION, "stream header version is 5");
        check(STREAM_MAGIC == (short) 0xACED, "STREAM_MAGIC value");
        check(STREAM_VERSION == 5, "STREAM_VERSION value");

        // 2. wire format tag values (byte TC_* constants)
        check(TC_NULL == 0x70 && TC_REFERENCE == 0x71 && TC_CLASSDESC == 0x72, "TC_NULL/REFERENCE/CLASSDESC");
        check(TC_OBJECT == 0x73 && TC_STRING == 0x74 && TC_ARRAY == 0x75, "TC_OBJECT/STRING/ARRAY");
        check(TC_CLASS == 0x76 && TC_BLOCKDATA == 0x77 && TC_ENDBLOCKDATA == 0x78, "TC_CLASS/BLOCKDATA/ENDBLOCKDATA");
        check(TC_RESET == 0x79 && TC_BLOCKDATALONG == 0x7A && TC_EXCEPTION == 0x7B, "TC_RESET/BLOCKDATALONG/EXCEPTION");
        check(TC_LONGSTRING == 0x7C && TC_PROXYCLASSDESC == 0x7D && TC_ENUM == 0x7E, "TC_LONGSTRING/PROXYCLASSDESC/ENUM");
        check(TC_BASE == TC_NULL && TC_MAX == TC_ENUM, "TC_BASE..TC_MAX range endpoints");

        // 3. handle numbering, flags, protocol versions
        check(baseWireHandle == 0x7E0000, "baseWireHandle value");
        check(SC_WRITE_METHOD == 0x01 && SC_BLOCK_DATA == 0x08, "SC_WRITE_METHOD/SC_BLOCK_DATA");
        check(SC_SERIALIZABLE == 0x02 && SC_EXTERNALIZABLE == 0x04 && SC_ENUM == 0x10, "SC_SERIALIZABLE/EXTERNALIZABLE/ENUM");
        check(PROTOCOL_VERSION_1 == 1 && PROTOCOL_VERSION_2 == 2, "protocol version values");

        // 4. permission constants are usable security permissions
        check("enableSubstitution".equals(SUBSTITUTION_PERMISSION.getName()),
              "SUBSTITUTION_PERMISSION name");
        check("enableSubclassImplementation".equals(SUBCLASS_IMPLEMENTATION_PERMISSION.getName()),
              "SUBCLASS_IMPLEMENTATION_PERMISSION name");
        check("serialFilter".equals(SERIAL_FILTER_PERMISSION.getName()),
              "SERIAL_FILTER_PERMISSION name");

        // 5. PROTOCOL_VERSION_1/2 both accepted by useProtocolVersion and round-trip
        for (int version : new int[]{PROTOCOL_VERSION_1, PROTOCOL_VERSION_2}) {
            ByteArrayOutputStream b2 = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(b2)) {
                oos.useProtocolVersion(version);
                oos.writeObject("v" + version);
            }
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(b2.toByteArray()))) {
                check(("v" + version).equals(ois.readObject()), "protocol " + version + " round-trip");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
