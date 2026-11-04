/*
 * @test
 * @summary javax.sound.sampled.Line.Info conformance: behavioral.
 * @run main InfoTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class InfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.sound.sampled.Line.Info info = new javax.sound.sampled.Line.Info(javax.sound.sampled.SourceDataLine.class);
        check(info.getLineClass() == javax.sound.sampled.SourceDataLine.class, "lineClass");
        check(info.matches(info), "matches self");
        check(info.toString() != null, "toString");
        javax.sound.sampled.Line.Info info2 = new javax.sound.sampled.Line.Info(javax.sound.sampled.Clip.class);
        check(!info.matches(info2), "matches other false");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
