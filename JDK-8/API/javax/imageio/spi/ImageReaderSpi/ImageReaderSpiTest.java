/*
 * @test
 * @summary javax.imageio.spi.ImageReaderSpi conformance: abstract hierarchy and methods.
 * @run main ImageReaderSpiTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ImageReaderSpiTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.imageio.spi.ImageReaderSpi");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("javax.imageio.spi.ImageReaderWriterSpi"), "extends javax.imageio.spi.ImageReaderWriterSpi");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getInputTypes"), "method getInputTypes");
        check(dm.contains("canDecodeInput"), "method canDecodeInput");
        check(dm.contains("createReaderInstance"), "method createReaderInstance");
        check(dm.contains("createReaderInstance"), "method createReaderInstance");
        check(dm.contains("isOwnReader"), "method isOwnReader");
        check(dm.contains("getImageWriterSpiNames"), "method getImageWriterSpiNames");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
