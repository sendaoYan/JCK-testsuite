/*
 * @test
 * @summary javax.imageio.ImageTranscoder conformance: interface hierarchy and methods.
 * @run main ImageTranscoderTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ImageTranscoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.imageio.ImageTranscoder");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("convertStreamMetadata"), "method convertStreamMetadata");
        check(dm.contains("convertImageMetadata"), "method convertImageMetadata");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
