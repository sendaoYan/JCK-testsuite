/*
 * @test
 * @summary javax.imageio.spi.ImageInputStreamSpi conformance: abstract hierarchy and methods.
 * @run main ImageInputStreamSpiTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ImageInputStreamSpiTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.imageio.spi.ImageInputStreamSpi");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("javax.imageio.spi.IIOServiceProvider"), "extends javax.imageio.spi.IIOServiceProvider");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getInputClass"), "method getInputClass");
        check(dm.contains("canUseCacheFile"), "method canUseCacheFile");
        check(dm.contains("needsCacheFile"), "method needsCacheFile");
        check(dm.contains("createInputStreamInstance"), "method createInputStreamInstance");
        check(dm.contains("createInputStreamInstance"), "method createInputStreamInstance");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
