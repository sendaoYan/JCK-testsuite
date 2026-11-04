/*
 * @test
 * @summary javax.imageio.IIOException conformance: behavioral.
 * @run main IIOExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class IIOExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.imageio.IIOException e = new javax.imageio.IIOException("boom");
        check("boom".equals(e.getMessage()), "message");
        check(e instanceof java.io.IOException, "is IOException");
        java.io.IOException cause = new java.io.IOException("c");
        javax.imageio.IIOException e2 = new javax.imageio.IIOException("m", cause);
        check(e2.getCause() == cause, "cause");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
