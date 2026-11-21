/*
 * @test
 * @summary JNI function matrix - strings: NewString (from UTF-16 code units),
 *          GetStringChars/ReleaseStringChars round trip, GetStringRegion,
 *          GetStringUTFRegion (modified UTF-8 bytes, including the surrogate
 *          pair 3+3 encoding), and GetStringCritical/ReleaseStringCritical.
 * @library /lib
 * @run main/othervm --enable-native-access=ALL-UNNAMED JniStringTest
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Conformance test for the JNIEnv string functions against a real native
 * library compiled at run time (same pipeline as {@code JniSemanticsTest}: the
 * canonical C source {@code jnistring.c} is embedded below, written to the
 * scratch dir, built with gcc against the JDK under test's own jni.h, and
 * loaded via System.load).
 */
public class JniStringTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean gccAvailable() {
        try {
            Process p = new ProcessBuilder("gcc", "--version").start();
            try { p.getInputStream().close(); } catch (Exception ignore) { }
            try { p.getOutputStream().close(); } catch (Exception ignore) { }
            try { p.getErrorStream().close(); } catch (Exception ignore) { }
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    // ------------------------------------------------------------------
    // Native library source (canonical copy of jnistring.c)
    // ------------------------------------------------------------------
    static final String C_SRC =
                "/*\n" +
        " * Canonical native source for JniStringTest (JNI string matrix).\n" +
        " *\n" +
        " * Covers: NewString (build from UTF-16 code units), GetStringChars /\n" +
        " * ReleaseStringChars, GetStringRegion, GetStringUTFRegion (modified UTF-8\n" +
        " * bytes), GetStringCritical / ReleaseStringCritical.\n" +
        " *\n" +
        " * This file is the single source of truth: `make native` compiles it into\n" +
        " * build/native/<side>-<arch>/libjnistring.so, and JniStringTest.java embeds an\n" +
        " * identical copy compiled at run time when no -nativepath is supplied.\n" +
        " *\n" +
        " * Manual build command:\n" +
        " *   gcc -shared -fPIC -I<jdk>/include -I<jdk>/include/linux \\\n" +
        " *       -o libjnistring.so jnistring.c\n" +
        " */\n" +
        "#include <jni.h>\n" +
        "#include <stdlib.h>\n" +
        "\n" +
        "/* NewString: build a jstring from UTF-16 code units. */\n" +
        "JNIEXPORT jstring JNICALL Java_JniStringTest_newStringFromChars(JNIEnv* e, jclass c, jcharArray chars) {\n" +
        "    jsize n = (*e)->GetArrayLength(e, chars);\n" +
        "    jchar* buf = (jchar*)malloc((size_t)(n > 0 ? n : 1) * sizeof(jchar));\n" +
        "    jstring r;\n" +
        "    if (buf == 0) return 0;\n" +
        "    if (n > 0) (*e)->GetCharArrayRegion(e, chars, 0, n, buf);\n" +
        "    r = (*e)->NewString(e, buf, n);\n" +
        "    free(buf);\n" +
        "    return r;\n" +
        "}\n" +
        "\n" +
        "/* GetStringChars + NewString + ReleaseStringChars round trip. */\n" +
        "JNIEXPORT jstring JNICALL Java_JniStringTest_echoChars(JNIEnv* e, jclass c, jstring s) {\n" +
        "    const jchar* u = (*e)->GetStringChars(e, s, 0);\n" +
        "    jstring r;\n" +
        "    jsize n;\n" +
        "    if (u == 0) return 0;\n" +
        "    n = (*e)->GetStringLength(e, s);\n" +
        "    r = (*e)->NewString(e, u, n);\n" +
        "    (*e)->ReleaseStringChars(e, s, u);\n" +
        "    return r;\n" +
        "}\n" +
        "\n" +
        "/* GetStringRegion: copy len UTF-16 chars starting at start into a new char[]. */\n" +
        "JNIEXPORT jcharArray JNICALL Java_JniStringTest_getStringRegion(JNIEnv* e, jclass c, jstring s, jint start, jint len) {\n" +
        "    jcharArray r = (*e)->NewCharArray(e, len);\n" +
        "    jchar* buf;\n" +
        "    if (r == 0) return 0;\n" +
        "    if (len == 0) return r;\n" +
        "    buf = (jchar*)malloc((size_t)len * sizeof(jchar));\n" +
        "    if (buf == 0) return 0;\n" +
        "    (*e)->GetStringRegion(e, s, start, len, buf);\n" +
        "    (*e)->SetCharArrayRegion(e, r, 0, len, buf);\n" +
        "    free(buf);\n" +
        "    return r;\n" +
        "}\n" +
        "\n" +
        "/* GetStringUTFRegion: copy len chars starting at start as modified UTF-8 into a\n" +
        "   byte[].  The whole string is copied when start==0 && len==length, so the\n" +
        "   buffer is sized to GetStringUTFLength (plus one spare byte in case the\n" +
        "   implementation null-terminates; only n bytes are handed back to Java). */\n" +
        "JNIEXPORT jbyteArray JNICALL Java_JniStringTest_getStringUTFRegion(JNIEnv* e, jclass c, jstring s, jint start, jint len) {\n" +
        "    jsize n = (*e)->GetStringUTFLength(e, s);\n" +
        "    jbyteArray r = (*e)->NewByteArray(e, n);\n" +
        "    jbyte* buf;\n" +
        "    if (r == 0) return 0;\n" +
        "    if (n == 0) return r;\n" +
        "    buf = (jbyte*)malloc((size_t)n + 1);\n" +
        "    if (buf == 0) return 0;\n" +
        "    (*e)->GetStringUTFRegion(e, s, start, len, buf);\n" +
        "    (*e)->SetByteArrayRegion(e, r, 0, n, buf);\n" +
        "    free(buf);\n" +
        "    return r;\n" +
        "}\n" +
        "\n" +
        "/* GetStringCritical / ReleaseStringCritical: sum the UTF-16 code units of the\n" +
        "   critical copy.  No JNI calls are made inside the critical region. */\n" +
        "JNIEXPORT jint JNICALL Java_JniStringTest_stringCriticalSum(JNIEnv* e, jclass c, jstring s) {\n" +
        "    jsize n = (*e)->GetStringLength(e, s);\n" +
        "    const jchar* p = (*e)->GetStringCritical(e, s, 0);\n" +
        "    jint sum = 0;\n" +
        "    jsize i;\n" +
        "    if (p == 0) return -1;\n" +
        "    for (i = 0; i < n; i++) sum += (jint)p[i];\n" +
        "    (*e)->ReleaseStringCritical(e, s, p);\n" +
        "    return sum;\n" +
        "}\n";

    // ------------------------------------------------------------------
    // Native method declarations (bound by the library above)
    // ------------------------------------------------------------------
    native static String newStringFromChars(char[] chars);
    native static String echoChars(String s);
    native static char[] getStringRegion(String s, int start, int len);
    native static byte[] getStringUTFRegion(String s, int start, int len);
    native static int stringCriticalSum(String s);

    // ------------------------------------------------------------------
    // Build helpers: locate jni.h for the JDK under test and compile
    // ------------------------------------------------------------------
    static Path jdkHome() {
        Path p = Paths.get(System.getProperty("java.home"));
        return p.endsWith("jre") ? p.getParent() : p;
    }

    static void compileAndLoad() throws Exception {
        // Cross-arch support: with jtreg -nativepath set, load the prebuilt
        // library directly (built by `make native` from jnistring.c).
        String np = System.getProperty("test.nativepath");
        if (np != null) {
            Path pre = Paths.get(np).resolve("libjnistring.so");
            if (Files.exists(pre)) {
                System.load(pre.toAbsolutePath().toString());
                return;
            }
            throw new AssertionError("test.nativepath set but no prebuilt JNI library found (run: make native)");
        }
        Path dir = Paths.get(System.getProperty("user.dir"));
        Path cFile = dir.resolve("jnistring.c");
        Path so = dir.resolve("libjnistring.so");
        Files.write(cFile, C_SRC.getBytes("UTF-8"));
        Path inc = jdkHome().resolve("include");
        ProcessBuilder pb = new ProcessBuilder("gcc", "-shared", "-fPIC",
                "-I" + inc, "-I" + inc.resolve("linux"),
                "-o", so.toString(), cFile.toString());
        pb.redirectErrorStream(true);
        Process p = pb.start();
        byte[] all = readAll(p.getInputStream());
        if (!p.waitFor(60, TimeUnit.SECONDS)) {
            p.destroyForcibly();
            throw new AssertionError("gcc did not finish within 60s");
        }
        if (p.exitValue() != 0)
            throw new AssertionError("gcc failed rc=" + p.exitValue()
                    + " output: " + new String(all, "UTF-8"));
        System.load(so.toAbsolutePath().toString());
    }

    static byte[] readAll(java.io.InputStream in) throws java.io.IOException {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
        return out.toByteArray();
    }

    // ------------------------------------------------------------------
    public static void main(String[] args) throws Exception {
        if (System.getProperty("test.nativepath") == null && !gccAvailable()) {
            throw new jtreg.SkippedException("no C compiler (gcc) available; native library cannot be built");
        }
        compileAndLoad();

        testStrings();

        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testStrings() {
        check("".equals(newStringFromChars(new char[0])), "NewString from empty");
        char[] utf16 = {'A', '\u0000', '中', '\uD83D', '\uDE00'};
        String built = newStringFromChars(utf16);
        check("A\u0000中😀".equals(built),
                "NewString from UTF-16 (NUL + CJK + surrogate pair)");

        check("".equals(echoChars("")), "GetStringChars round trip empty");
        check("hello".equals(echoChars("hello")), "GetStringChars round trip ascii");
        String cjk = "中文Abc";
        check(cjk.equals(echoChars(cjk)), "GetStringChars round trip CJK");
        check("😀".equals(echoChars("😀")),
                "GetStringChars round trip surrogate pair");
        String echoedNul = echoChars("a\u0000b");
        check(echoedNul.length() == 3 && echoedNul.charAt(1) == '\u0000',
                "GetStringChars preserves embedded NUL");

        check(Arrays.equals(getStringRegion("abcdef", 0, 6),
                new char[]{'a', 'b', 'c', 'd', 'e', 'f'}), "GetStringRegion whole");
        check(Arrays.equals(getStringRegion("abcdef", 1, 3),
                new char[]{'b', 'c', 'd'}), "GetStringRegion subrange");
        check(getStringRegion("abc", 0, 0).length == 0, "GetStringRegion len 0");

        check(Arrays.equals(getStringUTFRegion("abc", 0, 3),
                new byte[]{0x61, 0x62, 0x63}), "GetStringUTFRegion ascii");
        check(Arrays.equals(getStringUTFRegion("中", 0, 1),
                new byte[]{(byte) 0xE4, (byte) 0xB8, (byte) 0xAD}),
                "GetStringUTFRegion CJK (3-byte modified UTF-8)");
        check(Arrays.equals(getStringUTFRegion("😀", 0, 2),
                new byte[]{(byte) 0xED, (byte) 0xA0, (byte) 0xBD,
                        (byte) 0xED, (byte) 0xB8, (byte) 0x80}),
                "GetStringUTFRegion surrogate pair (3+3 modified UTF-8)");

        check(stringCriticalSum("AB") == 131, "GetStringCritical sums UTF-16 units");
        check(stringCriticalSum("😀") == 0xD83D + 0xDE00,
                "GetStringCritical surrogate pair units");
        check(stringCriticalSum("") == 0, "GetStringCritical empty");
    }
}
