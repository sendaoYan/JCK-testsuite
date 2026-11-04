/*
 * @test
 * @summary javax.sound.sampled.Port.Info conformance: behavioral.
 * @run main InfoTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class InfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.sound.sampled.Port.Info mic = javax.sound.sampled.Port.Info.MICROPHONE;
        check(mic != null, "MICROPHONE");
        check(mic.isSource(), "MICROPHONE source");
        check(!javax.sound.sampled.Port.Info.SPEAKER.isSource(), "SPEAKER not source");
        check(javax.sound.sampled.Port.Info.LINE_IN != null, "LINE_IN");
        check(javax.sound.sampled.Port.Info.HEADPHONE != null, "HEADPHONE");
        check(mic.getLineClass() == javax.sound.sampled.Port.class, "lineClass");
        javax.sound.sampled.Port.Info custom = new javax.sound.sampled.Port.Info(javax.sound.sampled.Port.class, "Custom", true);
        check(custom.isSource(), "custom source");
        check("Custom".equals(custom.getName()), "custom name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
