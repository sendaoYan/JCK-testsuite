/*
 * @test
 * @summary javax.sound.sampled.spi.FormatConversionProvider conformance: white-box structure + black-box behavior.
 * @run main FormatConversionProviderTest
 */

import java.io.ByteArrayInputStream;
import java.lang.reflect.Modifier;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class FormatConversionProviderTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.spi.FormatConversionProvider");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        java.lang.reflect.Constructor<?> ctor = c.getDeclaredConstructor();
        check(Modifier.isProtected(ctor.getModifiers()), "protected constructor");
        check(Modifier.isAbstract(c.getMethod("getSourceEncodings").getModifiers()), "getSourceEncodings abstract");
        check(Modifier.isAbstract(c.getMethod("getTargetEncodings").getModifiers()), "getTargetEncodings() abstract");
        check(Modifier.isAbstract(c.getMethod("getTargetEncodings", AudioFormat.class).getModifiers()), "getTargetEncodings(format) abstract");
        check(Modifier.isAbstract(c.getMethod("getTargetFormats", AudioFormat.Encoding.class, AudioFormat.class).getModifiers()), "getTargetFormats abstract");
        check(Modifier.isAbstract(c.getMethod("getAudioInputStream", AudioFormat.Encoding.class, AudioInputStream.class).getModifiers()), "getAudioInputStream(encoding) abstract");
        check(Modifier.isAbstract(c.getMethod("getAudioInputStream", AudioFormat.class, AudioInputStream.class).getModifiers()), "getAudioInputStream(format) abstract");
        check(!Modifier.isAbstract(c.getMethod("isSourceEncodingSupported", AudioFormat.Encoding.class).getModifiers()), "isSourceEncodingSupported concrete");
        check(!Modifier.isAbstract(c.getMethod("isTargetEncodingSupported", AudioFormat.Encoding.class).getModifiers()), "isTargetEncodingSupported concrete");
        check(!Modifier.isAbstract(c.getMethod("isConversionSupported", AudioFormat.Encoding.class, AudioFormat.class).getModifiers()), "isConversionSupported(encoding,format) concrete");
        check(!Modifier.isAbstract(c.getMethod("isConversionSupported", AudioFormat.class, AudioFormat.class).getModifiers()), "isConversionSupported(format,format) concrete");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        AudioFormat fmt = new AudioFormat(8000f, 8, 1, true, false);

        // Concrete convenience methods on an anonymous subclass with no encodings.
        FormatConversionProviderImpl p = new FormatConversionProviderImpl();
        check(!p.isSourceEncodingSupported(AudioFormat.Encoding.PCM_SIGNED), "empty provider: source encoding unsupported");
        check(!p.isTargetEncodingSupported(AudioFormat.Encoding.PCM_SIGNED), "empty provider: target encoding unsupported");
        check(!p.isConversionSupported(AudioFormat.Encoding.PCM_SIGNED, fmt), "empty provider: conversion (encoding,format) unsupported");
        check(!p.isConversionSupported(fmt, fmt), "empty provider: conversion (format,format) unsupported");

        // Real conversion providers via AudioSystem public API.
        check(AudioSystem.getTargetEncodings(AudioFormat.Encoding.PCM_SIGNED) != null, "getTargetEncodings(encoding) non-null");
        check(AudioSystem.getTargetEncodings(fmt) != null, "getTargetEncodings(format) non-null");
        AudioFormat[] targetFormats = AudioSystem.getTargetFormats(AudioFormat.Encoding.PCM_SIGNED, fmt);
        check(targetFormats != null, "getTargetFormats non-null");
        boolean conversionSupported = AudioSystem.isConversionSupported(AudioFormat.Encoding.PCM_SIGNED, fmt);
        check(conversionSupported == (targetFormats.length > 0), "isConversionSupported consistent with getTargetFormats");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class FormatConversionProviderImpl extends javax.sound.sampled.spi.FormatConversionProvider {
    public AudioFormat.Encoding[] getSourceEncodings() { return new AudioFormat.Encoding[0]; }
    public AudioFormat.Encoding[] getTargetEncodings() { return new AudioFormat.Encoding[0]; }
    public AudioFormat.Encoding[] getTargetEncodings(AudioFormat f) { return new AudioFormat.Encoding[0]; }
    public AudioFormat[] getTargetFormats(AudioFormat.Encoding e, AudioFormat f) { return new AudioFormat[0]; }
    public AudioInputStream getAudioInputStream(AudioFormat.Encoding e, AudioInputStream s) { return null; }
    public AudioInputStream getAudioInputStream(AudioFormat f, AudioInputStream s) { return null; }
}
