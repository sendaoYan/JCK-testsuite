/*
 * @test
 * @summary javax.imageio.IIOParam conformance: abstract hierarchy and methods.
 * @run main IIOParamTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class IIOParamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.imageio.IIOParam");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("setSourceRegion"), "method setSourceRegion");
        check(dm.contains("getSourceRegion"), "method getSourceRegion");
        check(dm.contains("setSourceSubsampling"), "method setSourceSubsampling");
        check(dm.contains("getSourceXSubsampling"), "method getSourceXSubsampling");
        check(dm.contains("getSourceYSubsampling"), "method getSourceYSubsampling");
        check(dm.contains("getSubsamplingXOffset"), "method getSubsamplingXOffset");
        check(dm.contains("getSubsamplingYOffset"), "method getSubsamplingYOffset");
        check(dm.contains("setSourceBands"), "method setSourceBands");
        check(dm.contains("getSourceBands"), "method getSourceBands");
        check(dm.contains("setDestinationType"), "method setDestinationType");
        check(dm.contains("getDestinationType"), "method getDestinationType");
        check(dm.contains("setDestinationOffset"), "method setDestinationOffset");
        check(dm.contains("getDestinationOffset"), "method getDestinationOffset");
        check(dm.contains("setController"), "method setController");
        check(dm.contains("getController"), "method getController");
        check(dm.contains("getDefaultController"), "method getDefaultController");
        check(dm.contains("hasController"), "method hasController");
        check(dm.contains("activateController"), "method activateController");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
