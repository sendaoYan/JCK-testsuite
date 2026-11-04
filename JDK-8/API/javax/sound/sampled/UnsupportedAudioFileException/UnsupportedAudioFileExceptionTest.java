/*
 * @test
 * @summary javax.sound.sampled.UnsupportedAudioFileException conformance: behavioral.
 * @run main UnsupportedAudioFileExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class UnsupportedAudioFileExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.sound.sampled.UnsupportedAudioFileException e = new javax.sound.sampled.UnsupportedAudioFileException();
        check(e instanceof Exception, "is Exception");
        javax.sound.sampled.UnsupportedAudioFileException e2 = new javax.sound.sampled.UnsupportedAudioFileException("m");
        check("m".equals(e2.getMessage()), "message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
