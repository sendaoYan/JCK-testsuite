/*
 * @test
 * @summary java.awt.image.Raster conformance: white-box structure + black-box contract.
 * @run main RasterTest
 */

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.RasterFormatException;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;

public class RasterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.Raster");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "createInterleavedRaster"), "method createInterleavedRaster");
        check(hasMethod(c, "createBandedRaster"), "method createBandedRaster");
        check(hasMethod(c, "createPackedRaster"), "method createPackedRaster");
        check(hasMethod(c, "createRaster"), "method createRaster");
        check(hasMethod(c, "createWritableRaster"), "method createWritableRaster");
        check(hasMethod(c, "getParent"), "method getParent");
        check(hasMethod(c, "getSampleModelTranslateX"), "method getSampleModelTranslateX");
        check(hasMethod(c, "getSampleModelTranslateY"), "method getSampleModelTranslateY");
        check(hasMethod(c, "createCompatibleWritableRaster"), "method createCompatibleWritableRaster");
        check(hasMethod(c, "createTranslatedChild"), "method createTranslatedChild");
        check(hasMethod(c, "createChild"), "method createChild");
        check(hasMethod(c, "getBounds"), "method getBounds");
        check(hasMethod(c, "getMinX"), "method getMinX");
        check(hasMethod(c, "getMinY"), "method getMinY");
        check(hasMethod(c, "getWidth"), "method getWidth");
        check(hasMethod(c, "getHeight"), "method getHeight");
        check(hasMethod(c, "getNumBands"), "method getNumBands");
        check(hasMethod(c, "getNumDataElements"), "method getNumDataElements");
        check(hasMethod(c, "getTransferType"), "method getTransferType");
        check(hasMethod(c, "getDataBuffer"), "method getDataBuffer");
        check(hasMethod(c, "getSampleModel"), "method getSampleModel");
        check(hasMethod(c, "getDataElements"), "method getDataElements");
        check(hasMethod(c, "getPixel"), "method getPixel");
        check(hasMethod(c, "getPixels"), "method getPixels");
        check(hasMethod(c, "getSample"), "method getSample");
        check(hasMethod(c, "getSampleFloat"), "method getSampleFloat");
        check(hasMethod(c, "getSampleDouble"), "method getSampleDouble");
        check(hasMethod(c, "getSamples"), "method getSamples");
    }

    static void testFactories() {
        // 5-arg interleaved factory computes strides itself.
        WritableRaster ir = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, 4, 3, 3, new Point(1, 2));
        check(ir.getWidth() == 4 && ir.getHeight() == 3, "interleaved dims");
        check(ir.getMinX() == 1 && ir.getMinY() == 2, "interleaved location honored");
        check(ir.getNumBands() == 3, "interleaved bands");
        check(ir.getSampleModelTranslateX() == 1 && ir.getSampleModelTranslateY() == 2, "interleaved sm translate");
        check(ir.getDataBuffer() != null && ir.getDataBuffer().getSize() >= 12, "interleaved buffer allocated");

        // Banded factory.
        WritableRaster br = Raster.createBandedRaster(DataBuffer.TYPE_USHORT, 3, 2, 2, new Point(0, 0));
        check(br.getNumBands() == 2, "banded bands");
        check(br.getTransferType() == DataBuffer.TYPE_USHORT, "banded transfer type");
        check(br.getNumDataElements() == 2, "banded one bank per band");

        // Packed factories: (dataType,w,h,bands,bitsPerBand) and (dataType,w,h,masks).
        WritableRaster pb = Raster.createPackedRaster(DataBuffer.TYPE_BYTE, 3, 2, 2, 4, new Point(0, 0));
        check(pb.getNumBands() == 2, "packed bits-per-band bands");
        check(pb.getSampleModel().getSampleSize(0) == 4, "packed bits per sample");
        WritableRaster pm = Raster.createPackedRaster(DataBuffer.TYPE_INT, 3, 2,
                new int[] { 0xff0000, 0xff00, 0xff }, new Point(0, 0));
        check(pm.getNumBands() == 3, "packed masks bands");
        check(pm.getNumDataElements() == 1, "packed masks one data element");
        check(pm.getSampleModel().getSampleSize(1) == 8, "packed masks 8-bit samples");

        // DataBuffer-backed factories.
        DataBufferByte dbb = new DataBufferByte(100);
        WritableRaster dbi = Raster.createInterleavedRaster(dbb, 4, 3, 12, 3, new int[] { 0, 1, 2 }, new Point(0, 0));
        check(dbi.getDataBuffer() == dbb, "interleaved uses given buffer");
        dbi.setSample(3, 2, 2, 55);
        check(dbi.getSample(3, 2, 2) == 55, "interleaved db sample round-trip");
        DataBufferUShort dbu = new DataBufferUShort(new short[][] { new short[8], new short[8], new short[8] }, 8);
        WritableRaster dbb2 = Raster.createBandedRaster(dbu, 3, 2, 3, new int[] { 0, 0, 0 }, new int[] { 0, 1, 2 },
                new Point(0, 0));
        check(dbb2.getDataBuffer() == dbu, "banded uses given buffer");
        DataBufferInt dbi2 = new DataBufferInt(6);
        WritableRaster dbp = Raster.createPackedRaster(dbi2, 3, 2, 4, new Point(0, 0));
        check(dbp.getDataBuffer() == dbi2, "packed uses given buffer");

        // createRaster gives a read-only view; createWritableRaster allocates a buffer.
        SampleModel sm = new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT, 3, 2,
                new int[] { 0xff0000, 0xff00, 0xff });
        Raster ro = Raster.createRaster(sm, new DataBufferInt(6), new Point(0, 0));
        check(ro != null && ro.getWidth() == 3, "createRaster works");
        WritableRaster wr = Raster.createWritableRaster(sm, new Point(0, 0));
        check(wr.getDataBuffer().getSize() == 6, "createWritableRaster allocates buffer");
        check(Raster.createWritableRaster(sm, new DataBufferInt(6), null) != null, "createWritableRaster with buffer");
    }

    static void testFactoryErrors() {
        boolean threw;
        threw = false;
        try { Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, 0, 3, 3, null); }
        catch (IllegalArgumentException e) { threw = true; }
        check(threw, "interleaved width 0 -> IAE");
        threw = false;
        try { Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, 3, 0, 3, null); }
        catch (IllegalArgumentException e) { threw = true; }
        check(threw, "interleaved height 0 -> IAE");
        threw = false;
        try { Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, 3, 3, 0, null); }
        catch (IllegalArgumentException e) { threw = true; }
        check(threw, "interleaved bands 0 -> IAE");
        threw = false;
        try { Raster.createInterleavedRaster(DataBuffer.TYPE_UNDEFINED, 3, 3, 3, null); }
        catch (IllegalArgumentException e) { threw = true; }
        check(threw, "interleaved unsupported data type -> IAE");
        threw = false;
        try { Raster.createPackedRaster(DataBuffer.TYPE_BYTE, 3, 2, 3, 4, null); }
        catch (IllegalArgumentException e) { threw = true; }
        check(threw, "packed bits*bands overflows data type -> IAE");
        threw = false;
        try { Raster.createBandedRaster(new DataBufferUShort(new short[][] { new short[2], new short[2] }, 2),
                3, 2, 3, new int[] { 0, 0, 0 }, new int[] { 0, 1, 2 }, null); }
        catch (RasterFormatException e) { threw = true; }
        check(threw, "banded buffer too small -> RasterFormatException");
        threw = false;
        try { Raster.createInterleavedRaster(new DataBufferByte(4), 4, 3, 12, 3, new int[] { 0, 1, 2 }, null); }
        catch (RasterFormatException e) { threw = true; }
        check(threw, "interleaved buffer too small -> RasterFormatException");
        threw = false;
        try { Raster.createWritableRaster(null, new Point(0, 0)); }
        catch (NullPointerException e) { threw = true; }
        check(threw, "createWritableRaster null model -> NPE");
    }

    static void testBoundsAndChildren() {
        WritableRaster pr = Raster.createPackedRaster(DataBuffer.TYPE_INT, 3, 2,
                new int[] { 0xff0000, 0xff00, 0xff }, new Point(0, 0));
        check(pr.getBounds().equals(new Rectangle(0, 0, 3, 2)), "getBounds");
        check(pr.getParent() == null, "root raster has no parent");

        // createTranslatedChild shifts the min corner, keeps parent link and size.
        Raster tr = pr.createTranslatedChild(10, 20);
        check(tr.getMinX() == 10 && tr.getMinY() == 20, "translated child min corner");
        check(tr.getWidth() == 3 && tr.getHeight() == 2, "translated child size");
        check(tr.getParent() == pr, "translated child parent link");
        pr.setSample(0, 0, 0, 5);
        check(tr.getSample(10, 20, 0) == 5, "translated child shares data");

        // createChild with translated parentMin and band subset.
        Raster child = pr.createChild(1, 0, 2, 2, 5, 6, new int[] { 0, 2 });
        check(child.getMinX() == 5 && child.getMinY() == 6, "child translated min corner");
        check(child.getWidth() == 2 && child.getHeight() == 2, "child size");
        check(child.getNumBands() == 2, "child band subset");
        check(child.getParent() == pr, "child parent link");

        // Out-of-range child rectangles.
        boolean threw = false;
        try { pr.createChild(0, 0, 99, 99, 0, 0, null); }
        catch (RasterFormatException e) { threw = true; }
        check(threw, "createChild overflow -> RasterFormatException");
        threw = false;
        try { pr.createChild(0, 0, -1, 1, 0, 0, null); }
        catch (RasterFormatException e) { threw = true; }
        check(threw, "createChild negative width -> RasterFormatException");

        // createCompatibleWritableRaster variants.
        WritableRaster c1 = pr.createCompatibleWritableRaster();
        check(c1.getWidth() == 3 && c1.getHeight() == 2, "compatible raster same dims");
        WritableRaster c2 = pr.createCompatibleWritableRaster(5, 4);
        check(c2.getWidth() == 5 && c2.getHeight() == 4, "compatible raster new dims");
        WritableRaster c3 = pr.createCompatibleWritableRaster(new Rectangle(1, 2, 5, 4));
        check(c3.getMinX() == 1 && c3.getMinY() == 2 && c3.getWidth() == 5 && c3.getHeight() == 4,
              "compatible raster from Rectangle");
        WritableRaster c4 = pr.createCompatibleWritableRaster(5, 4, 1, 2);
        check(c4.getMinX() == 5 && c4.getMinY() == 4 && c4.getWidth() == 1 && c4.getHeight() == 2,
              "compatible raster (x,y,w,h)");
    }

    static void testSampleAccess() {
        WritableRaster pr = Raster.createPackedRaster(DataBuffer.TYPE_INT, 3, 2,
                new int[] { 0xff0000, 0xff00, 0xff }, new Point(0, 0));
        pr.setSample(0, 0, 0, 7);
        check(pr.getSample(0, 0, 0) == 7, "getSample/setSample round-trip");
        check(pr.getSampleFloat(0, 0, 0) == 7.0f, "getSampleFloat");
        check(pr.getSampleDouble(0, 0, 0) == 7.0, "getSampleDouble");
        pr.setSample(1, 0, 0, 3.75f);
        check(pr.getSample(1, 0, 0) == 3, "setSample(float) truncates to int sample");
        pr.setSample(2, 0, 0, 5.25);
        check(pr.getSample(2, 0, 0) == 5, "setSample(double) truncates to int sample");

        // Pixel-level access in all three arities.
        pr.setPixel(1, 1, new int[] { 9, 8, 7 });
        int[] ip = pr.getPixel(1, 1, (int[]) null);
        check(ip.length == 3 && ip[0] == 9 && ip[1] == 8 && ip[2] == 7, "setPixel/getPixel(int[]) round-trip");
        float[] fp = pr.getPixel(1, 1, (float[]) null);
        check(fp.length == 3 && fp[0] == 9.0f && fp[2] == 7.0f, "getPixel(float[])");
        double[] dp = pr.getPixel(1, 1, (double[]) null);
        check(dp.length == 3 && dp[1] == 8.0, "getPixel(double[])");

        // Region and band access.
        int[] region = pr.getPixels(0, 0, 3, 1, (int[]) null);
        check(region.length == 9, "getPixels region length w*h*bands");
        pr.setPixels(0, 0, 3, 1, new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 });
        int[] band = pr.getSamples(0, 0, 3, 1, 1, (int[]) null);
        check(band.length == 3 && band[0] == 2 && band[1] == 5 && band[2] == 8, "getSamples single band");
        pr.setSamples(0, 1, 3, 1, 0, new int[] { 1, 2, 3 });
        check(pr.getSample(0, 1, 0) == 1 && pr.getSample(2, 1, 0) == 3, "setSamples writes band");

        // getDataElements returns a transfer-type array.
        Object de = pr.getDataElements(1, 1, (Object) null);
        check(de instanceof int[], "getDataElements transfer array");

        // Out-of-bounds access.
        boolean threw = false;
        try { pr.getSample(99, 0, 0); } catch (ArrayIndexOutOfBoundsException e) { threw = true; }
        check(threw, "getSample x out of bounds -> AIOOBE");
        threw = false;
        try { pr.getSample(0, 0, 99); } catch (ArrayIndexOutOfBoundsException e) { threw = true; }
        check(threw, "getSample band out of bounds -> AIOOBE");
        threw = false;
        try { pr.getSample(-1, 0, 0); } catch (ArrayIndexOutOfBoundsException e) { threw = true; }
        check(threw, "getSample x negative -> AIOOBE");
    }

    static void testSampleModelContract() {
        SinglePixelPackedSampleModel sm = new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT, 3, 2,
                new int[] { 0xff0000, 0xff00, 0xff });
        check(sm.getWidth() == 3 && sm.getHeight() == 2, "sample model dims");
        check(sm.getNumBands() == 3, "sample model bands");
        check(sm.getNumDataElements() == 1, "sample model data elements");
        check(sm.getDataType() == DataBuffer.TYPE_INT, "sample model data type");
        check(sm.getTransferType() == DataBuffer.TYPE_INT, "sample model transfer type");
        int[] ss = sm.getSampleSize();
        check(ss.length == 3 && ss[0] == 8 && ss[1] == 8 && ss[2] == 8, "sample sizes");
        check(sm.getSampleSize(2) == 8, "sample size per band");

        // createCompatibleSampleModel keeps layout, changes dims.
        SampleModel compat = sm.createCompatibleSampleModel(5, 4);
        check(compat.getWidth() == 5 && compat.getHeight() == 4 && compat.getNumBands() == 3,
              "compatible sample model");
        // createSubsetSampleModel selects bands.
        SampleModel subset = sm.createSubsetSampleModel(new int[] { 0, 2 });
        check(subset.getNumBands() == 2, "subset sample model bands");
        check(subset.getWidth() == 3, "subset sample model dims unchanged");
        // createDataBuffer produces a buffer of matching type.
        DataBuffer db = sm.createDataBuffer();
        check(db.getDataType() == DataBuffer.TYPE_INT, "created buffer type");
        check(db.getSize() >= 6, "created buffer covers raster");

        // SampleModel-level pixel round-trip with an explicit DataBuffer.
        PixelInterleavedSampleModel pism = new PixelInterleavedSampleModel(DataBuffer.TYPE_BYTE, 4, 4, 3, 12,
                new int[] { 0, 1, 2 });
        DataBuffer buf = pism.createDataBuffer();
        pism.setSample(2, 3, 0, 123, buf);
        check(pism.getSample(2, 3, 0, buf) == 123, "sample model set/get sample");
        int[] px = pism.getPixel(2, 3, (int[]) null, buf);
        check(px.length == 3 && px[0] == 123, "sample model getPixel");
        pism.setPixel(1, 1, new int[] { 10, 11, 12 }, buf);
        check(java.util.Arrays.equals(pism.getPixel(1, 1, (int[]) null, buf), new int[] { 10, 11, 12 }),
              "sample model set/get pixel");
        Object e = pism.getDataElements(1, 1, (Object) null, buf);
        check(e instanceof byte[] && ((byte[]) e).length == 3, "sample model getDataElements");
        pism.setDataElements(2, 2, e, buf);
        check(pism.getPixel(2, 2, (int[]) null, buf)[0] == 10, "sample model setDataElements copies");
        boolean threw = false;
        try { pism.getSample(99, 0, 0, buf); } catch (ArrayIndexOutOfBoundsException ex) { threw = true; }
        check(threw, "sample model getSample OOB -> AIOOBE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testFactories();
        testFactoryErrors();
        testBoundsAndChildren();
        testSampleAccess();
        testSampleModelContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
