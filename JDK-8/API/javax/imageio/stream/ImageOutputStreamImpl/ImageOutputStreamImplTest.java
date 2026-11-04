/*
 * @test
 * @summary javax.imageio.stream.ImageOutputStreamImpl conformance: abstract hierarchy and methods.
 * @run main ImageOutputStreamImplTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ImageOutputStreamImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.imageio.stream.ImageOutputStreamImpl");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("javax.imageio.stream.ImageInputStreamImpl"), "extends javax.imageio.stream.ImageInputStreamImpl");
        Set<String> si = new HashSet<>(); for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("javax.imageio.stream.ImageOutputStream"), "implements javax.imageio.stream.ImageOutputStream");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("write"), "method write");
        check(dm.contains("write"), "method write");
        check(dm.contains("write"), "method write");
        check(dm.contains("writeBoolean"), "method writeBoolean");
        check(dm.contains("writeByte"), "method writeByte");
        check(dm.contains("writeShort"), "method writeShort");
        check(dm.contains("writeChar"), "method writeChar");
        check(dm.contains("writeInt"), "method writeInt");
        check(dm.contains("writeLong"), "method writeLong");
        check(dm.contains("writeFloat"), "method writeFloat");
        check(dm.contains("writeDouble"), "method writeDouble");
        check(dm.contains("writeBytes"), "method writeBytes");
        check(dm.contains("writeChars"), "method writeChars");
        check(dm.contains("writeUTF"), "method writeUTF");
        check(dm.contains("writeShorts"), "method writeShorts");
        check(dm.contains("writeChars"), "method writeChars");
        check(dm.contains("writeInts"), "method writeInts");
        check(dm.contains("writeLongs"), "method writeLongs");
        check(dm.contains("writeFloats"), "method writeFloats");
        check(dm.contains("writeDoubles"), "method writeDoubles");
        check(dm.contains("writeBit"), "method writeBit");
        check(dm.contains("writeBits"), "method writeBits");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
