/*
 * @test
 * @summary javax.sound.sampled.spi.FormatConversionProvider conformance: abstract hierarchy and methods.
 * @run main FormatConversionProviderTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class FormatConversionProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.spi.FormatConversionProvider");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getSourceEncodings"), "method getSourceEncodings");
        check(dm.contains("getTargetEncodings"), "method getTargetEncodings");
        check(dm.contains("isSourceEncodingSupported"), "method isSourceEncodingSupported");
        check(dm.contains("isTargetEncodingSupported"), "method isTargetEncodingSupported");
        check(dm.contains("getTargetEncodings"), "method getTargetEncodings");
        check(dm.contains("isConversionSupported"), "method isConversionSupported");
        check(dm.contains("getTargetFormats"), "method getTargetFormats");
        check(dm.contains("isConversionSupported"), "method isConversionSupported");
        check(dm.contains("getAudioInputStream"), "method getAudioInputStream");
        check(dm.contains("getAudioInputStream"), "method getAudioInputStream");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
