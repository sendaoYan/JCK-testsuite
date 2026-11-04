/*
 * @test
 * @summary java.awt.image.Kernel conformance: white-box structure + black-box contract.
 * @run main KernelTest
 */

import java.awt.image.Kernel;

public class KernelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testKernel() {
        float[] data = { 0f, 1f, 0f, 1f, -4f, 1f, 0f, 1f, 0f };
        Kernel k = new Kernel(3, 3, data);
        check(k.getWidth() == 3 && k.getHeight() == 3, "dimensions");
        check(k.getXOrigin() == 1 && k.getYOrigin() == 1, "origin center");

        // getKernelData copies into the given array or allocates.
        float[] out = k.getKernelData(null);
        check(out.length == 9 && out[4] == -4f, "kernel data");
        float[] provided = new float[9];
        check(k.getKernelData(provided) == provided, "getKernelData reuses array");
        check(provided[4] == -4f, "kernel data with array");
        check(k.getKernelData(null) != out, "getKernelData allocates fresh");

        // The kernel copies the input array at construction.
        data[4] = 99f;
        check(k.getKernelData(new float[9])[4] == -4f, "kernel copied input at construction");

        // Non-square kernels: origin is width/2, height/2.
        Kernel wide = new Kernel(5, 1, new float[] { 1, 1, 1, 1, 1 });
        check(wide.getXOrigin() == 2 && wide.getYOrigin() == 0, "wide kernel origin");
        Kernel tall = new Kernel(1, 5, new float[] { 1, 1, 1, 1, 1 });
        check(tall.getXOrigin() == 0 && tall.getYOrigin() == 2, "tall kernel origin");
        Kernel dot = new Kernel(1, 1, new float[] { 2f });
        check(dot.getXOrigin() == 0 && dot.getYOrigin() == 0, "1x1 kernel origin");
        // Even-sized kernels are legal (no odd-dimension requirement); origin is (dim-1)/2.
        Kernel even2 = new Kernel(2, 2, new float[] { 1, 1, 1, 1 });
        check(even2.getXOrigin() == 0 && even2.getYOrigin() == 0, "2x2 kernel origin (dim-1)/2");
        Kernel even4 = new Kernel(4, 4, new float[16]);
        check(even4.getXOrigin() == 1 && even4.getYOrigin() == 1, "4x4 kernel origin (dim-1)/2");
        // Zero weight kernel is legal.
        check(new Kernel(3, 3, new float[9]).getKernelData(null)[8] == 0f, "all-zero kernel legal");
    }

    static void testClone() {
        Kernel k = new Kernel(3, 3, new float[] { 0f, 1f, 0f, 1f, -4f, 1f, 0f, 1f, 0f });
        Kernel c = (Kernel) k.clone();
        check(c != k, "clone distinct object");
        check(c.getClass() == Kernel.class, "clone exact class");
        check(c.getWidth() == 3 && c.getHeight() == 3 && c.getXOrigin() == 1 && c.getYOrigin() == 1,
              "clone metadata");
        check(c.getKernelData(null)[4] == -4f, "clone data");
        // Clone data is independent.
        float[] kd = c.getKernelData(null);
        kd[4] = 77f;
        check(k.getKernelData(new float[9])[4] == -4f, "clone data independent of original");
    }

    static void testErrors() {
        boolean threw = false;
        try { new Kernel(3, 3, new float[8]); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "data too short -> IAE");
        threw = false;
        try { new Kernel(2, 2, new float[9]); } catch (IllegalArgumentException e) { threw = true; }
        check(!threw, "data longer than w*h accepted");
        threw = false;
        try { new Kernel(3, 3, null); } catch (NullPointerException e) { threw = true; }
        check(threw, "null data -> NPE");
        threw = false;
        try { new Kernel(1, 1, new float[0]); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "empty data for 1x1 -> IAE");
    }

    public static void main(String[] args) {
        testKernel();
        testClone();
        testErrors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
