/*
 * @test
 * @summary java.awt.image.Kernel conformance: white-box structure + black-box behavior.
 * @run main KernelTest
 */

import java.awt.image.Kernel;
import java.lang.reflect.*;

public class KernelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.Kernel", false, KernelTest.class.getClassLoader());
        check(c != null, "type loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(hasMethod(c, "getXOrigin"), "getXOrigin()");
        check(hasMethod(c, "getYOrigin"), "getYOrigin()");
        check(hasMethod(c, "getWidth"), "getWidth()");
        check(hasMethod(c, "getHeight"), "getHeight()");
        check(hasMethod(c, "getKernelData", float[].class), "getKernelData(float[])");
        check(hasMethod(c, "clone"), "clone()");
    }

    static void testBlackBox() {
        float[] data = new float[] { 0f, -1f, 0f, -1f, 5f, -1f, 0f, -1f, 0f };
        Kernel k = new Kernel(3, 3, data);
        check(k.getWidth() == 3, "width 3");
        check(k.getHeight() == 3, "height 3");
        check(k.getXOrigin() == 1, "x origin 1");
        check(k.getYOrigin() == 1, "y origin 1");

        float[] out = new float[9];
        k.getKernelData(out);
        check(out[0] == 0f && out[1] == -1f && out[4] == 5f && out[8] == 0f, "getKernelData copies values");
        check(out != data, "getKernelData returns copy array");
        data[4] = 99f;
        check(k.getKernelData(new float[9])[4] == 5f, "kernel copied input at construction");

        Kernel c = (Kernel) k.clone();
        check(c != k, "clone distinct object");
        check(c.getWidth() == 3 && c.getHeight() == 3 && c.getXOrigin() == 1, "clone metadata");
        float[] cd = c.getKernelData(null);
        check(cd[4] == 5f, "clone data");

        boolean threw = false;
        try { new Kernel(3, 3, new float[8]); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "wrong data length throws");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
