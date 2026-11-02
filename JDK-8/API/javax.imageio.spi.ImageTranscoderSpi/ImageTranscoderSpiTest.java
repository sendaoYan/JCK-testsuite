/*
 * @test
 * @summary javax.imageio.spi.ImageTranscoderSpi conformance: abstract hierarchy and methods.
 * @run main ImageTranscoderSpiTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ImageTranscoderSpiTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.imageio.spi.ImageTranscoderSpi");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("javax.imageio.spi.IIOServiceProvider"), "extends javax.imageio.spi.IIOServiceProvider");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getReaderServiceProviderName"), "method getReaderServiceProviderName");
        check(dm.contains("getWriterServiceProviderName"), "method getWriterServiceProviderName");
        check(dm.contains("createTranscoderInstance"), "method createTranscoderInstance");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
