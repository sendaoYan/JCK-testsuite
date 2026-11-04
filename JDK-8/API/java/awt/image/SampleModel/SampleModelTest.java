/*
 * @test
 * @summary java.awt.image.SampleModel conformance: white-box structure + black-box contract.
 * @run main SampleModelTest
 */

import java.awt.Point;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;

public class SampleModelTest {
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testCustomSubclass();
        testPixelInterleaved();
        testSinglePixelPacked();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
