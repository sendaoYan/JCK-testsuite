/*
 * @test
 * @summary javax.imageio.spi.ImageWriterSpi conformance: abstract hierarchy and methods.
 * @run main ImageWriterSpiTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ImageWriterSpiTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.imageio.spi.ImageWriterSpi");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("javax.imageio.spi.ImageReaderWriterSpi"), "extends javax.imageio.spi.ImageReaderWriterSpi");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("isFormatLossless"), "method isFormatLossless");
        check(dm.contains("getOutputTypes"), "method getOutputTypes");
        check(dm.contains("canEncodeImage"), "method canEncodeImage");
        check(dm.contains("canEncodeImage"), "method canEncodeImage");
        check(dm.contains("createWriterInstance"), "method createWriterInstance");
        check(dm.contains("createWriterInstance"), "method createWriterInstance");
        check(dm.contains("isOwnWriter"), "method isOwnWriter");
        check(dm.contains("getImageReaderSpiNames"), "method getImageReaderSpiNames");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
