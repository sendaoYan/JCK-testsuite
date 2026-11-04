/*
 * @test
 * @summary java.awt.image.renderable.ParameterBlock conformance: white-box structure plus black-box behavior.
 * @run main ParameterBlockTest
 */

import java.awt.image.*;
import java.awt.image.renderable.*;
import java.io.Serializable;
import java.util.*;
import java.lang.reflect.*;

public class ParameterBlockTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.renderable.ParameterBlock");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ParameterBlock pb = new ParameterBlock();
        RenderedImage src = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        pb.addSource(src);
        check(pb.getNumSources() == 1, "numSources 1");
        check(pb.getSource(0) == src, "getSource");
        check(pb.getRenderedSource(0) == src, "getRenderedSource");
        check(pb.getSources().size() == 1, "getSources vector");
        pb.setSource(src, 0);
        pb.removeSources();
        check(pb.getNumSources() == 0, "removeSources clears");

        pb.add(42);
        pb.add(3.14);
        pb.add("x");
        check(pb.getNumParameters() == 3, "numParameters 3");
        check(pb.getObjectParameter(2).equals("x"), "getObjectParameter");
        check(pb.getIntParameter(0) == 42, "getIntParameter");
        check(pb.getDoubleParameter(1) == 3.14, "getDoubleParameter");
        pb.set(7, 0);
        check(pb.getIntParameter(0) == 7, "set parameter");
        check(pb.getParameters().size() == 3, "getParameters vector");
        pb.removeParameters();
        check(pb.getNumParameters() == 0, "removeParameters clears");

        ParameterBlock pb2 = new ParameterBlock();
        pb2.addSource(new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB));
        pb2.add(1.5f);
        ParameterBlock cloned = (ParameterBlock) pb2.clone();
        check(cloned != pb2 && cloned.getNumSources() == 1, "clone shallow copies sources");
        check(cloned.getFloatParameter(0) == 1.5f, "clone copies parameters");
        check(pb2.shallowClone() != pb2, "shallowClone non-null");

        Class<?>[] classes = pb2.getParamClasses();
        check(classes.length == 1 && classes[0] == Float.TYPE, "getParamClasses primitive float");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
