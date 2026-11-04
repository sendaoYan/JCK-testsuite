/*
 * @test
 * @summary javax.imageio.spi.ImageReaderWriterSpi conformance: abstract hierarchy and methods.
 * @run main ImageReaderWriterSpiTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ImageReaderWriterSpiTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.imageio.spi.ImageReaderWriterSpi");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("javax.imageio.spi.IIOServiceProvider"), "extends javax.imageio.spi.IIOServiceProvider");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getFormatNames"), "method getFormatNames");
        check(dm.contains("getFileSuffixes"), "method getFileSuffixes");
        check(dm.contains("getMIMETypes"), "method getMIMETypes");
        check(dm.contains("getPluginClassName"), "method getPluginClassName");
        check(dm.contains("isStandardStreamMetadataFormatSupported"), "method isStandardStreamMetadataFormatSupported");
        check(dm.contains("getNativeStreamMetadataFormatName"), "method getNativeStreamMetadataFormatName");
        check(dm.contains("getExtraStreamMetadataFormatNames"), "method getExtraStreamMetadataFormatNames");
        check(dm.contains("isStandardImageMetadataFormatSupported"), "method isStandardImageMetadataFormatSupported");
        check(dm.contains("getNativeImageMetadataFormatName"), "method getNativeImageMetadataFormatName");
        check(dm.contains("getExtraImageMetadataFormatNames"), "method getExtraImageMetadataFormatNames");
        check(dm.contains("getStreamMetadataFormat"), "method getStreamMetadataFormat");
        check(dm.contains("getImageMetadataFormat"), "method getImageMetadataFormat");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
