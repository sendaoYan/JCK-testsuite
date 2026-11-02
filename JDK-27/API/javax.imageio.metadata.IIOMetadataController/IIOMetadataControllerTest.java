/*
 * @test
 * @summary javax.imageio.metadata.IIOMetadataController conformance: interface hierarchy and methods.
 * @run main IIOMetadataControllerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class IIOMetadataControllerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.imageio.metadata.IIOMetadataController");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("activate"), "method activate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
