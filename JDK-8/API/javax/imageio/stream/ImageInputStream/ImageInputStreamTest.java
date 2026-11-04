/*
 * @test
 * @summary javax.imageio.stream.ImageInputStream conformance: interface hierarchy and methods.
 * @run main ImageInputStreamTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ImageInputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.imageio.stream.ImageInputStream");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 2, "direct interface count");
        Set<String> si = new HashSet<>(); for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("java.io.DataInput"), "extends java.io.DataInput");
        check(si.contains("java.io.Closeable"), "extends java.io.Closeable");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("setByteOrder"), "method setByteOrder");
        check(dm.contains("getByteOrder"), "method getByteOrder");
        check(dm.contains("read"), "method read");
        check(dm.contains("read"), "method read");
        check(dm.contains("read"), "method read");
        check(dm.contains("readBytes"), "method readBytes");
        check(dm.contains("readBoolean"), "method readBoolean");
        check(dm.contains("readByte"), "method readByte");
        check(dm.contains("readUnsignedByte"), "method readUnsignedByte");
        check(dm.contains("readShort"), "method readShort");
        check(dm.contains("readUnsignedShort"), "method readUnsignedShort");
        check(dm.contains("readChar"), "method readChar");
        check(dm.contains("readInt"), "method readInt");
        check(dm.contains("readUnsignedInt"), "method readUnsignedInt");
        check(dm.contains("readLong"), "method readLong");
        check(dm.contains("readFloat"), "method readFloat");
        check(dm.contains("readDouble"), "method readDouble");
        check(dm.contains("readLine"), "method readLine");
        check(dm.contains("readUTF"), "method readUTF");
        check(dm.contains("readFully"), "method readFully");
        check(dm.contains("readFully"), "method readFully");
        check(dm.contains("readFully"), "method readFully");
        check(dm.contains("readFully"), "method readFully");
        check(dm.contains("readFully"), "method readFully");
        check(dm.contains("readFully"), "method readFully");
        check(dm.contains("readFully"), "method readFully");
        check(dm.contains("readFully"), "method readFully");
        check(dm.contains("getStreamPosition"), "method getStreamPosition");
        check(dm.contains("getBitOffset"), "method getBitOffset");
        check(dm.contains("setBitOffset"), "method setBitOffset");
        check(dm.contains("readBit"), "method readBit");
        check(dm.contains("readBits"), "method readBits");
        check(dm.contains("length"), "method length");
        check(dm.contains("skipBytes"), "method skipBytes");
        check(dm.contains("skipBytes"), "method skipBytes");
        check(dm.contains("seek"), "method seek");
        check(dm.contains("mark"), "method mark");
        check(dm.contains("reset"), "method reset");
        check(dm.contains("flushBefore"), "method flushBefore");
        check(dm.contains("flush"), "method flush");
        check(dm.contains("getFlushedPosition"), "method getFlushedPosition");
        check(dm.contains("isCached"), "method isCached");
        check(dm.contains("isCachedMemory"), "method isCachedMemory");
        check(dm.contains("isCachedFile"), "method isCachedFile");
        check(dm.contains("close"), "method close");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
