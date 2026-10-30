/*
 * @test
 * @summary JVMS 4.1 (Java SE 8): a class file starts with the magic number
 *          0xCAFEBABE followed by minor_version and major_version; Java SE 8
 *          compiles to major 52, minor 0.
 * @run main ClassFileMagicTest
 */
import java.io.*;

public class ClassFileMagicTest {
    public static void main(String[] args) throws Exception {
        InputStream in = ClassFileMagicTest.class.getResourceAsStream("ClassFileMagicTest.class");
        DataInputStream d = new DataInputStream(in);
        int magic = d.readInt();
        int minor = d.readUnsignedShort();
        int major = d.readUnsignedShort();
        d.close();

        if (magic != 0xCAFEBABE) throw new AssertionError("bad magic: " + Integer.toHexString(magic));
        if (minor != 0) throw new AssertionError("minor version: " + minor);
        if (major != 52) throw new AssertionError("major version: " + major);
        System.out.println("PASS");
    }
}
