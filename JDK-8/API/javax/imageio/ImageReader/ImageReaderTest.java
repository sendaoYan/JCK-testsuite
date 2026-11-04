/*
 * @test
 * @summary javax.imageio.ImageReader conformance: abstract hierarchy and methods.
 * @run main ImageReaderTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ImageReaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.imageio.ImageReader");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getFormatName"), "method getFormatName");
        check(dm.contains("getOriginatingProvider"), "method getOriginatingProvider");
        check(dm.contains("setInput"), "method setInput");
        check(dm.contains("setInput"), "method setInput");
        check(dm.contains("setInput"), "method setInput");
        check(dm.contains("getInput"), "method getInput");
        check(dm.contains("isSeekForwardOnly"), "method isSeekForwardOnly");
        check(dm.contains("isIgnoringMetadata"), "method isIgnoringMetadata");
        check(dm.contains("getMinIndex"), "method getMinIndex");
        check(dm.contains("getAvailableLocales"), "method getAvailableLocales");
        check(dm.contains("setLocale"), "method setLocale");
        check(dm.contains("getLocale"), "method getLocale");
        check(dm.contains("getNumImages"), "method getNumImages");
        check(dm.contains("getWidth"), "method getWidth");
        check(dm.contains("getHeight"), "method getHeight");
        check(dm.contains("isRandomAccessEasy"), "method isRandomAccessEasy");
        check(dm.contains("getAspectRatio"), "method getAspectRatio");
        check(dm.contains("getRawImageType"), "method getRawImageType");
        check(dm.contains("getImageTypes"), "method getImageTypes");
        check(dm.contains("getDefaultReadParam"), "method getDefaultReadParam");
        check(dm.contains("getStreamMetadata"), "method getStreamMetadata");
        check(dm.contains("getStreamMetadata"), "method getStreamMetadata");
        check(dm.contains("getImageMetadata"), "method getImageMetadata");
        check(dm.contains("getImageMetadata"), "method getImageMetadata");
        check(dm.contains("read"), "method read");
        check(dm.contains("read"), "method read");
        check(dm.contains("readAll"), "method readAll");
        check(dm.contains("readAll"), "method readAll");
        check(dm.contains("canReadRaster"), "method canReadRaster");
        check(dm.contains("readRaster"), "method readRaster");
        check(dm.contains("isImageTiled"), "method isImageTiled");
        check(dm.contains("getTileWidth"), "method getTileWidth");
        check(dm.contains("getTileHeight"), "method getTileHeight");
        check(dm.contains("getTileGridXOffset"), "method getTileGridXOffset");
        check(dm.contains("getTileGridYOffset"), "method getTileGridYOffset");
        check(dm.contains("readTile"), "method readTile");
        check(dm.contains("readTileRaster"), "method readTileRaster");
        check(dm.contains("readAsRenderedImage"), "method readAsRenderedImage");
        check(dm.contains("readerSupportsThumbnails"), "method readerSupportsThumbnails");
        check(dm.contains("hasThumbnails"), "method hasThumbnails");
        check(dm.contains("getNumThumbnails"), "method getNumThumbnails");
        check(dm.contains("getThumbnailWidth"), "method getThumbnailWidth");
        check(dm.contains("getThumbnailHeight"), "method getThumbnailHeight");
        check(dm.contains("readThumbnail"), "method readThumbnail");
        check(dm.contains("abort"), "method abort");
        check(dm.contains("addIIOReadWarningListener"), "method addIIOReadWarningListener");
        check(dm.contains("removeIIOReadWarningListener"), "method removeIIOReadWarningListener");
        check(dm.contains("removeAllIIOReadWarningListeners"), "method removeAllIIOReadWarningListeners");
        check(dm.contains("addIIOReadProgressListener"), "method addIIOReadProgressListener");
        check(dm.contains("removeIIOReadProgressListener"), "method removeIIOReadProgressListener");
        check(dm.contains("removeAllIIOReadProgressListeners"), "method removeAllIIOReadProgressListeners");
        check(dm.contains("addIIOReadUpdateListener"), "method addIIOReadUpdateListener");
        check(dm.contains("removeIIOReadUpdateListener"), "method removeIIOReadUpdateListener");
        check(dm.contains("removeAllIIOReadUpdateListeners"), "method removeAllIIOReadUpdateListeners");
        check(dm.contains("reset"), "method reset");
        check(dm.contains("dispose"), "method dispose");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
