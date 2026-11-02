/*
 * @test
 * @summary javax.imageio.metadata.IIOMetadata conformance: abstract hierarchy and methods.
 * @run main IIOMetadataTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class IIOMetadataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.imageio.metadata.IIOMetadata");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("isStandardMetadataFormatSupported"), "method isStandardMetadataFormatSupported");
        check(dm.contains("isReadOnly"), "method isReadOnly");
        check(dm.contains("getNativeMetadataFormatName"), "method getNativeMetadataFormatName");
        check(dm.contains("getExtraMetadataFormatNames"), "method getExtraMetadataFormatNames");
        check(dm.contains("getMetadataFormatNames"), "method getMetadataFormatNames");
        check(dm.contains("getMetadataFormat"), "method getMetadataFormat");
        check(dm.contains("getAsTree"), "method getAsTree");
        check(dm.contains("mergeTree"), "method mergeTree");
        check(dm.contains("setFromTree"), "method setFromTree");
        check(dm.contains("reset"), "method reset");
        check(dm.contains("setController"), "method setController");
        check(dm.contains("getController"), "method getController");
        check(dm.contains("getDefaultController"), "method getDefaultController");
        check(dm.contains("hasController"), "method hasController");
        check(dm.contains("activateController"), "method activateController");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
