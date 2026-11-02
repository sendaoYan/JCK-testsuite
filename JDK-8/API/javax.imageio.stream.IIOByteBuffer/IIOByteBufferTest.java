/*
 * @test
 * @summary javax.imageio.stream.IIOByteBuffer conformance: behavioral.
 * @run main IIOByteBufferTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class IIOByteBufferTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        byte[] data = new byte[10];
        javax.imageio.stream.IIOByteBuffer b = new javax.imageio.stream.IIOByteBuffer(data, 2, 4);
        check(b.getData() == data, "getData");
        check(b.getOffset() == 2, "offset");
        check(b.getLength() == 4, "length");
        byte[] d2 = new byte[5];
        b.setData(d2); b.setOffset(1); b.setLength(3);
        check(b.getData() == d2 && b.getOffset() == 1 && b.getLength() == 3, "setters");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
