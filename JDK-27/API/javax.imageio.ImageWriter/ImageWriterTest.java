/*
 * @test
 * @summary javax.imageio.ImageWriter conformance: abstract hierarchy and methods.
 * @run main ImageWriterTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ImageWriterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.imageio.ImageWriter");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        Set<String> si = new HashSet<>(); for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("javax.imageio.ImageTranscoder"), "implements javax.imageio.ImageTranscoder");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getOriginatingProvider"), "method getOriginatingProvider");
        check(dm.contains("setOutput"), "method setOutput");
        check(dm.contains("getOutput"), "method getOutput");
        check(dm.contains("getAvailableLocales"), "method getAvailableLocales");
        check(dm.contains("setLocale"), "method setLocale");
        check(dm.contains("getLocale"), "method getLocale");
        check(dm.contains("getDefaultWriteParam"), "method getDefaultWriteParam");
        check(dm.contains("getDefaultStreamMetadata"), "method getDefaultStreamMetadata");
        check(dm.contains("getDefaultImageMetadata"), "method getDefaultImageMetadata");
        check(dm.contains("convertStreamMetadata"), "method convertStreamMetadata");
        check(dm.contains("convertImageMetadata"), "method convertImageMetadata");
        check(dm.contains("getNumThumbnailsSupported"), "method getNumThumbnailsSupported");
        check(dm.contains("getPreferredThumbnailSizes"), "method getPreferredThumbnailSizes");
        check(dm.contains("canWriteRasters"), "method canWriteRasters");
        check(dm.contains("write"), "method write");
        check(dm.contains("write"), "method write");
        check(dm.contains("write"), "method write");
        check(dm.contains("canWriteSequence"), "method canWriteSequence");
        check(dm.contains("prepareWriteSequence"), "method prepareWriteSequence");
        check(dm.contains("writeToSequence"), "method writeToSequence");
        check(dm.contains("endWriteSequence"), "method endWriteSequence");
        check(dm.contains("canReplaceStreamMetadata"), "method canReplaceStreamMetadata");
        check(dm.contains("replaceStreamMetadata"), "method replaceStreamMetadata");
        check(dm.contains("canReplaceImageMetadata"), "method canReplaceImageMetadata");
        check(dm.contains("replaceImageMetadata"), "method replaceImageMetadata");
        check(dm.contains("canInsertImage"), "method canInsertImage");
        check(dm.contains("writeInsert"), "method writeInsert");
        check(dm.contains("canRemoveImage"), "method canRemoveImage");
        check(dm.contains("removeImage"), "method removeImage");
        check(dm.contains("canWriteEmpty"), "method canWriteEmpty");
        check(dm.contains("prepareWriteEmpty"), "method prepareWriteEmpty");
        check(dm.contains("endWriteEmpty"), "method endWriteEmpty");
        check(dm.contains("canInsertEmpty"), "method canInsertEmpty");
        check(dm.contains("prepareInsertEmpty"), "method prepareInsertEmpty");
        check(dm.contains("endInsertEmpty"), "method endInsertEmpty");
        check(dm.contains("canReplacePixels"), "method canReplacePixels");
        check(dm.contains("prepareReplacePixels"), "method prepareReplacePixels");
        check(dm.contains("replacePixels"), "method replacePixels");
        check(dm.contains("replacePixels"), "method replacePixels");
        check(dm.contains("endReplacePixels"), "method endReplacePixels");
        check(dm.contains("abort"), "method abort");
        check(dm.contains("addIIOWriteWarningListener"), "method addIIOWriteWarningListener");
        check(dm.contains("removeIIOWriteWarningListener"), "method removeIIOWriteWarningListener");
        check(dm.contains("removeAllIIOWriteWarningListeners"), "method removeAllIIOWriteWarningListeners");
        check(dm.contains("addIIOWriteProgressListener"), "method addIIOWriteProgressListener");
        check(dm.contains("removeIIOWriteProgressListener"), "method removeIIOWriteProgressListener");
        check(dm.contains("removeAllIIOWriteProgressListeners"), "method removeAllIIOWriteProgressListeners");
        check(dm.contains("reset"), "method reset");
        check(dm.contains("dispose"), "method dispose");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
