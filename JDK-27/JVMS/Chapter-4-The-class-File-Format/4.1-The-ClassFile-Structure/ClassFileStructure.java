/*
 * @test
 * @summary JVMS 4.1: a class file begins with the magic number 0xCAFEBABE and a
 *          version number, followed by a constant pool, access flags, and
 *          this_class/super_class indices.
 * @run main ClassFileStructure
 */

import java.io.InputStream;

public class ClassFileStructure {
    public static void main(String[] args) throws Exception {
        byte[] b;
        try (InputStream in = ClassFileStructure.class.getResourceAsStream("ClassFileStructure.class")) {
            b = in.readAllBytes();
        }

        // 4.1: magic
        if (readInt(b, 0) != 0xCAFEBABE)
            throw new AssertionError("magic == 0x" + Integer.toHexString(readInt(b, 0)));

        // minor_version and major_version (JDK 27 -> 71.0)
        int minor = readUShort(b, 4), major = readUShort(b, 6);
        if (minor != 0 || major != 71)
            throw new AssertionError("version " + major + "." + minor);

        // constant_pool_count
        int cpCount = readUShort(b, 8);
        if (cpCount <= 0) throw new AssertionError("constant_pool_count == 0");

        // Parse the constant pool (JVMS 4.4). This both validates the structure and
        // lets us locate this_class/super_class afterwards.
        int off = 10;
        for (int i = 1; i < cpCount; i++) {
            int tag = b[off++] & 0xff;
            switch (tag) {
                case 1:  {                    // CONSTANT_Utf8
                    int len = readUShort(b, off);
                    off += 2 + len;
                    break;
                }
                case 3: case 4:              // CONSTANT_Integer / Float
                    off += 4;
                    break;
                case 5: case 6:              // CONSTANT_Long / Double (2 entries)
                    off += 8;
                    i++;
                    break;
                case 7: case 8: case 16:     // Class / String / MethodType
                case 19: case 20:            // Module / Package
                    off += 2;
                    break;
                case 9: case 10: case 11:    // Fieldref / Methodref / InterfaceMethodref
                case 12: case 17: case 18:   // NameAndType / Dynamic / InvokeDynamic
                    off += 4;
                    break;
                case 15:                     // CONSTANT_MethodHandle
                    off += 3;
                    break;
                default:
                    throw new AssertionError("unknown constant pool tag " + tag);
            }
        }

        // access_flags: this class is public (and ACC_SUPER is set)
        int accessFlags = readUShort(b, off);
        if ((accessFlags & 0x0001) == 0)
            throw new AssertionError("ACC_PUBLIC not set: 0x" + Integer.toHexString(accessFlags));
        off += 2;

        int thisClass = readUShort(b, off); off += 2;
        int superClass = readUShort(b, off); off += 2;
        if (thisClass == 0) throw new AssertionError("this_class == 0");
        if (superClass == 0) throw new AssertionError("super_class == 0");

        System.out.println("PASS");
    }

    static int readInt(byte[] b, int off) {
        return ((b[off] & 0xff) << 24) | ((b[off + 1] & 0xff) << 16)
             | ((b[off + 2] & 0xff) << 8)  |  (b[off + 3] & 0xff);
    }

    static int readUShort(byte[] b, int off) {
        return ((b[off] & 0xff) << 8) | (b[off + 1] & 0xff);
    }
}
