/*
 * @test
 * @summary javax.imageio.metadata.IIOInvalidTreeException conformance: behavioral.
 * @run main IIOInvalidTreeExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class IIOInvalidTreeExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        org.w3c.dom.Node n = new javax.imageio.metadata.IIOMetadataNode("root");
        javax.imageio.metadata.IIOInvalidTreeException e = new javax.imageio.metadata.IIOInvalidTreeException("bad", n);
        check(e.getOffendingNode() == n, "offendingNode");
        check(e instanceof javax.imageio.IIOException, "is IIOException");
        javax.imageio.metadata.IIOInvalidTreeException e2 = new javax.imageio.metadata.IIOInvalidTreeException("m", new Exception(), n);
        check(e2.getOffendingNode() == n, "offendingNode2");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
