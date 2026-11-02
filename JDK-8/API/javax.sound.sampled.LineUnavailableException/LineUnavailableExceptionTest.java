/*
 * @test
 * @summary javax.sound.sampled.LineUnavailableException conformance: behavioral.
 * @run main LineUnavailableExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class LineUnavailableExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.sound.sampled.LineUnavailableException e = new javax.sound.sampled.LineUnavailableException();
        check(e instanceof Exception, "is Exception");
        javax.sound.sampled.LineUnavailableException e2 = new javax.sound.sampled.LineUnavailableException("m");
        check("m".equals(e2.getMessage()), "message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
