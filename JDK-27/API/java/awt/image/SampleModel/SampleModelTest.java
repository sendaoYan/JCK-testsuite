/*
 * @test
 * @summary java.awt.image.SampleModel conformance: white-box structure + black-box behavior.
 * @run main SampleModelTest
 */

import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.awt.image.SampleModel;
import java.awt.image.DataBuffer;
import java.lang.reflect.*;
import java.awt.Point;
import java.awt.image.DataBufferByte;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.SinglePixelPackedSampleModel;

public class SampleModelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.SampleModel", false, SampleModelTest.class.getClassLoader());
        check(c != null, "type loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(Modifier.isAbstract(m), "abstract class");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(hasMethod(c, "getNumDataElements"), "getNumDataElements()");
        check(hasMethod(c, "getSampleSize", int.class), "getSampleSize(int)");
        check(hasMethod(c, "getSampleSize"), "getSampleSize()");
        check(hasMethod(c, "getWidth"), "getWidth()");
        check(hasMethod(c, "getHeight"), "getHeight()");
        check(hasMethod(c, "getNumBands"), "getNumBands()");
        check(hasMethod(c, "getDataType"), "getDataType()");
        check(hasMethod(c, "getTransferType"), "getTransferType()");
        check(hasMethod(c, "createCompatibleSampleModel", int.class, int.class), "createCompatibleSampleModel");
        check(hasMethod(c, "createSubsetSampleModel", int[].class), "createSubsetSampleModel");
        check(hasMethod(c, "createDataBuffer"), "createDataBuffer()");
        check(hasMethod(c, "getPixel", int.class, int.class, int[].class, DataBuffer.class), "getPixel");
        check(hasMethod(c, "setPixel", int.class, int.class, int[].class, DataBuffer.class), "setPixel");
        check(hasMethod(c, "getSample", int.class, int.class, int.class, DataBuffer.class), "getSample");
        check(hasMethod(c, "setSample", int.class, int.class, int.class, int.class, DataBuffer.class), "setSample");
        check(hasMethod(c, "getDataElements", int.class, int.class, Object.class, DataBuffer.class), "getDataElements");
    }

    static void testBlackBox() {
        WritableRaster wr = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, 4, 3, 3, null);
        SampleModel sm = wr.getSampleModel();
        check(sm.getWidth() == 4 && sm.getHeight() == 3, "dims");
        check(sm.getNumBands() == 3, "3 bands");
        check(sm.getDataType() == DataBuffer.TYPE_BYTE, "data type byte");
        check(sm.getTransferType() == DataBuffer.TYPE_BYTE, "transfer type byte");
        check(sm.getNumDataElements() == 3, "3 data elements");
        int[] sizes = sm.getSampleSize();
        check(sizes.length == 3 && sizes[0] == 8 && sizes[1] == 8 && sizes[2] == 8, "8-bit sample sizes");
        check(sm.getSampleSize(0) == 8 && sm.getSampleSize(2) == 8, "getSampleSize per band");

        DataBuffer db = wr.getDataBuffer();
        sm.setSample(2, 1, 0, 200, db);
        sm.setSample(2, 1, 1, 100, db);
        sm.setSample(2, 1, 2, 50, db);
        check(sm.getSample(2, 1, 0, db) == 200, "getSample band 0");
        check(sm.getSample(2, 1, 1, db) == 100, "getSample band 1");
        check(sm.getSample(2, 1, 2, db) == 50, "getSample band 2");

        int[] px = sm.getPixel(2, 1, (int[]) null, db);
        check(px.length == 3 && px[0] == 200 && px[1] == 100 && px[2] == 50, "getPixel");

        int[] np = new int[] { 7, 8, 9 };
        sm.setPixel(0, 0, np, db);
        int[] rp = sm.getPixel(0, 0, (int[]) null, db);
        check(rp[0] == 7 && rp[1] == 8 && rp[2] == 9, "setPixel/getPixel");

        DataBuffer db2 = sm.createDataBuffer();
        check(db2.getDataType() == DataBuffer.TYPE_BYTE, "createDataBuffer type");
        check(db2.getSize() >= 12, "createDataBuffer sized");

        SampleModel comp = sm.createCompatibleSampleModel(2, 2);
        check(comp.getWidth() == 2 && comp.getHeight() == 2, "compatible sample model dims");
        check(comp.getNumBands() == 3, "compatible sample model bands");
        check(comp.getClass() == sm.getClass(), "compatible same class");

        SampleModel subset = sm.createSubsetSampleModel(new int[] { 0, 2 });
        check(subset.getNumBands() == 2, "subset sample model 2 bands");
        check(subset.getWidth() == 4 && subset.getHeight() == 3, "subset sample model dims");

        // getDataElements / setDataElements round-trip
        Object de = sm.getDataElements(2, 1, null, db);
        sm.setDataElements(2, 1, de, db);
        check(sm.getSample(2, 1, 0, db) == 200, "getDataElements/setDataElements round-trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        passed += SampleModelDeepContract.run();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class SampleModelDeepContract {
    // Deepened contract coverage ported from the JDK-8 mirror (JDK-27 divergences adjusted).
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
        Class<?> c = Class.forName("java.awt.image.SampleModel");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getWidth"), "method getWidth");
        check(hasMethod(c, "getHeight"), "method getHeight");
        check(hasMethod(c, "getNumBands"), "method getNumBands");
        check(hasMethod(c, "getNumDataElements"), "method getNumDataElements");
        check(hasMethod(c, "getDataType"), "method getDataType");
        check(hasMethod(c, "getTransferType"), "method getTransferType");
        check(hasMethod(c, "getPixel"), "method getPixel");
        check(hasMethod(c, "getDataElements"), "method getDataElements");
        check(hasMethod(c, "setDataElements"), "method setDataElements");
        check(hasMethod(c, "getPixels"), "method getPixels");
        check(hasMethod(c, "getSample"), "method getSample");
        check(hasMethod(c, "getSampleFloat"), "method getSampleFloat");
        check(hasMethod(c, "getSampleDouble"), "method getSampleDouble");
        check(hasMethod(c, "getSamples"), "method getSamples");
        check(hasMethod(c, "setPixel"), "method setPixel");
        check(hasMethod(c, "setPixels"), "method setPixels");
        check(hasMethod(c, "setSample"), "method setSample");
        check(hasMethod(c, "setSamples"), "method setSamples");
        check(hasMethod(c, "createCompatibleSampleModel"), "method createCompatibleSampleModel");
        check(hasMethod(c, "createSubsetSampleModel"), "method createSubsetSampleModel");
        check(hasMethod(c, "createDataBuffer"), "method createDataBuffer");
        check(hasMethod(c, "getSampleSize"), "method getSampleSize");
    }

    /** Minimal concrete subclass: exercises the protected constructor and the abstract surface. */
    static class OneBand extends SampleModel {
        OneBand(int w, int h) { super(DataBuffer.TYPE_BYTE, w, h, 1); }
        public int getNumDataElements() { return 1; }
        public Object getDataElements(int x, int y, Object obj, DataBuffer data) {
            byte[] e = obj instanceof byte[] ? (byte[]) obj : new byte[1];
            e[0] = (byte) data.getElem(x + y * width);
            return e;
        }
        public void setDataElements(int x, int y, Object obj, DataBuffer data) {
            data.setElem(x + y * width, ((byte[]) obj)[0]);
        }
        public int getSample(int x, int y, int b, DataBuffer data) {
            return data.getElem(x + y * width) & 0xff;
        }
        public void setSample(int x, int y, int b, int s, DataBuffer data) {
            data.setElem(x + y * width, s);
        }
        public SampleModel createCompatibleSampleModel(int w, int h) { return new OneBand(w, h); }
        public SampleModel createSubsetSampleModel(int[] bands) { return new OneBand(width, height); }
        public DataBuffer createDataBuffer() { return new DataBufferByte(width * height); }
        public int[] getSampleSize() { return new int[] { 8 }; }
        public int getSampleSize(int band) { return 8; }
    }

    static void testCustomSubclass() {
        OneBand m = new OneBand(3, 2);
        check(m.getWidth() == 3 && m.getHeight() == 2, "subclass dims");
        check(m.getNumBands() == 1, "subclass bands");
        check(m.getNumDataElements() == 1, "subclass data elements");
        check(m.getDataType() == DataBuffer.TYPE_BYTE, "subclass data type");
        check(m.getTransferType() == DataBuffer.TYPE_BYTE, "subclass transfer type");
        check(m.getSampleSize(0) == 8 && m.getSampleSize()[0] == 8, "subclass sample sizes");

        DataBuffer db = m.createDataBuffer();
        check(db.getDataType() == DataBuffer.TYPE_BYTE && db.getSize() == 6, "subclass createDataBuffer");

        // The default-method surface on SampleModel routes to the abstract primitives.
        m.setSample(1, 1, 0, 42, db);
        check(m.getSample(1, 1, 0, db) == 42, "subclass get/setSample");
        check(m.getSampleFloat(1, 1, 0, db) == 42.0f, "getSampleFloat default");
        check(m.getSampleDouble(1, 1, 0, db) == 42.0, "getSampleDouble default");
        m.setSample(0, 0, 0, 2.9f, db);
        check(m.getSample(0, 0, 0, db) == 2, "setSample(float) truncates");
        m.setSample(0, 1, 0, 3.9, db);
        check(m.getSample(0, 1, 0, db) == 3, "setSample(double) truncates");

        int[] pi = m.getPixel(1, 1, (int[]) null, db);
        check(pi.length == 1 && pi[0] == 42, "getPixel(int[]) default");
        float[] pf = m.getPixel(1, 1, (float[]) null, db);
        check(pf[0] == 42.0f, "getPixel(float[]) default");
        double[] pd = m.getPixel(1, 1, (double[]) null, db);
        check(pd[0] == 42.0, "getPixel(double[]) default");
        m.setPixel(2, 0, new int[] { 7 }, db);
        check(m.getPixel(2, 0, (int[]) null, db)[0] == 7, "setPixel(int[]) default");
        m.setPixel(2, 1, new float[] { 5.5f }, db);
        check(m.getSample(2, 1, 0, db) == 5, "setPixel(float[]) truncates");
        m.setPixel(0, 0, new double[] { 6.5 }, db);
        check(m.getSample(0, 0, 0, db) == 6, "setPixel(double[]) truncates");

        int[] region = m.getPixels(0, 0, 3, 1, (int[]) null, db);
        check(region.length == 3 && region[2] == 7, "getPixels default");
        m.setPixels(0, 0, 3, 1, new int[] { 1, 2, 3 }, db);
        check(m.getSample(0, 0, 0, db) == 1 && m.getSample(2, 0, 0, db) == 3, "setPixels default");
        int[] band = m.getSamples(0, 0, 3, 1, 0, (int[]) null, db);
        check(band.length == 3 && band[1] == 2, "getSamples default");
        m.setSamples(0, 1, 3, 1, 0, new int[] { 4, 5, 6 }, db);
        check(m.getSample(2, 1, 0, db) == 6, "setSamples default");

        Object e = m.getDataElements(1, 1, (Object) null, db);
        check(e instanceof byte[], "getDataElements array type");
        m.setDataElements(0, 0, e, db);
        check(m.getSample(0, 0, 0, db) == m.getSample(1, 1, 0, db), "setDataElements copies");
        // Region data elements default method.
        Object rect = m.getDataElements(0, 0, 2, 1, (Object) null, db);
        check(rect != null, "getDataElements region default");
        m.setDataElements(0, 1, 2, 1, rect, db);
        check(m.getSample(0, 1, 0, db) == m.getSample(0, 0, 0, db), "setDataElements region copies");

        // Compatible/subset models.
        SampleModel compat = m.createCompatibleSampleModel(5, 4);
        check(compat.getWidth() == 5 && compat.getHeight() == 4, "createCompatibleSampleModel dims");
        SampleModel sub = m.createSubsetSampleModel(new int[] { 0 });
        check(sub.getNumBands() == 1, "createSubsetSampleModel bands");
    }

    static void testPixelInterleaved() {
        PixelInterleavedSampleModel m = new PixelInterleavedSampleModel(DataBuffer.TYPE_BYTE, 4, 4, 3, 16,
                new int[] { 0, 1, 2 });
        check(m.getWidth() == 4 && m.getHeight() == 4, "interleaved dims");
        check(m.getNumBands() == 3, "interleaved bands");
        check(m.getNumDataElements() == 3, "interleaved data elements");
        DataBuffer db = m.createDataBuffer();
        m.setSample(2, 3, 0, 123, db);
        check(m.getSample(2, 3, 0, db) == 123, "interleaved sample round-trip");
        m.setPixel(1, 1, new int[] { 10, 11, 12 }, db);
        check(java.util.Arrays.equals(m.getPixel(1, 1, (int[]) null, db), new int[] { 10, 11, 12 }),
              "interleaved pixel round-trip");
        Object e = m.getDataElements(1, 1, (Object) null, db);
        check(e instanceof byte[] && ((byte[]) e).length == 3, "interleaved data elements");
        m.setDataElements(2, 2, e, db);
        check(m.getPixel(2, 2, (int[]) null, db)[0] == 10, "interleaved setDataElements");
        boolean threw = false;
        try { m.getSample(99, 0, 0, db); } catch (ArrayIndexOutOfBoundsException ex) { threw = true; }
        check(threw, "interleaved getSample OOB -> AIOOBE");
    }

    static void testSinglePixelPacked() {
        SinglePixelPackedSampleModel m = new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT, 3, 2,
                new int[] { 0xff0000, 0xff00, 0xff });
        check(m.getNumDataElements() == 1, "packed one data element");
        check(m.getSampleSize(0) == 8 && m.getSampleSize(2) == 8, "packed sample sizes");
        DataBuffer db = m.createDataBuffer();
        db.setElem(0, 0x112233);
        check(m.getSample(0, 0, 0, db) == 0x11, "packed red from packed int");
        check(m.getSample(0, 0, 1, db) == 0x22, "packed green");
        check(m.getSample(0, 0, 2, db) == 0x33, "packed blue");
        m.setSample(0, 0, 0, 0xAA, db);
        check(m.getSample(0, 0, 0, db) == 0xAA, "packed setSample writes band");
        Object e = m.getDataElements(0, 0, (Object) null, db);
        check(e instanceof int[], "packed data elements int[]");
        SampleModel sub = m.createSubsetSampleModel(new int[] { 1, 2 });
        check(sub.getNumBands() == 2, "packed subset bands");
    }



    static int run() throws Exception {
        testWhiteBox();
        testCustomSubclass();
        testPixelInterleaved();
        testSinglePixelPacked();
        return passed;
    }
}
