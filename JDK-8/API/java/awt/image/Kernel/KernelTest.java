/*
 * @test
 * @summary java.awt.image.Kernel conformance: convolution kernel.
 * @run main KernelTest
 */


import java.awt.image.Kernel;
public class KernelTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        float[] data = {0f, 1f, 0f, 1f, -4f, 1f, 0f, 1f, 0f};
        Kernel k = new Kernel(3, 3, data);
        check(k.getWidth() == 3 && k.getHeight() == 3, "dimensions");
        check(k.getXOrigin() == 1 && k.getYOrigin() == 1, "origin center");
        float[] out = k.getKernelData(null);
        check(out.length == 9 && out[4] == -4f, "kernel data");
        check(k.getKernelData(new float[9])[4] == -4f, "kernel data with array");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
