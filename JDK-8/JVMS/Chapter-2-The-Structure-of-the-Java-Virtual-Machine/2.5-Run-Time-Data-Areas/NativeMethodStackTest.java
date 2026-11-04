/*
 * @test
 * @summary JVMS 2.5.6: the Java Virtual Machine keeps a native method stack
 *          per thread, used for native (JNI) frames.  A native method may
 *          call back into the JVM and interleave native and Java frames to
 *          arbitrary depth; exhausting the stack with mutually recursive
 *          native and Java frames raises a catchable StackOverflowError
 *          without corrupting the VM, and execution continues normally
 *          afterwards.
 * @run main/othervm NativeMethodStackTest
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class NativeMethodStackTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static final String C_SRC =
        "#include <jni.h>\n" +
        "static jint upcall(JNIEnv* e, jclass c, const char* name, jint d) {\n" +
        "    jmethodID m = (*e)->GetStaticMethodID(e, c, name, \"(I)I\");\n" +
        "    if (m == 0) return -1;\n" +
        "    return (jint)(*e)->CallStaticIntMethod(e, c, m, d);\n" +
        "}\n" +
        "/* Each invocation pushes one native frame that re-enters the JVM. */\n" +
        "JNIEXPORT jint JNICALL Java_NativeMethodStackTest_ping(JNIEnv* e, jclass c, jint d) {\n" +
        "    return upcall(e, c, \"pong\", d + 1);\n" +
        "}\n" +
        "JNIEXPORT jint JNICALL Java_NativeMethodStackTest_pingOverflow(JNIEnv* e, jclass c, jint d) {\n" +
        "    return upcall(e, c, \"pongOverflow\", d + 1);\n" +
        "}\n";

    static native int ping(int depth);
    static native int pingOverflow(int depth);

    static final int DEPTH = 100;

    static int pong(int depth) {
        return depth >= DEPTH ? depth : ping(depth + 1);
    }

    static int pongOverflow(int depth) {
        return pingOverflow(depth + 1);
    }

    static Path jdkHome() {
        Path p = Paths.get(System.getProperty("java.home"));
        return p.endsWith("jre") ? p.getParent() : p;
    }

    static void compileAndLoad() throws Exception {
        Path dir = Paths.get(System.getProperty("user.dir"));
        Path cFile = dir.resolve("jcknstack.c");
        Path so = dir.resolve("libjcknstack.so");
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

    public static void main(String[] args) throws Exception {
        compileAndLoad();

        // 100 native frames and 100 Java frames interleave on the same thread;
        // the native call ping(d) re-enters pong(d+1), so the terminal depth
        // reported from the Java side is one past the requested depth.
        check(ping(0) == DEPTH + 1,
                "mutual native/Java recursion reaches depth " + (DEPTH + 1));
        check(ping(DEPTH - 1) == DEPTH,
                "a second recursion run reaches the expected depth");

        // Unbounded mutual recursion exhausts the native method stack (or the
        // Java stack, whichever limit is hit first); either way the failure
        // surfaces as a catchable StackOverflowError propagating through the
        // native frames.
        boolean overflowed = false;
        try {
            pingOverflow(0);
        } catch (StackOverflowError expected) {
            overflowed = true;
        }
        check(overflowed, "unbounded native/Java recursion throws StackOverflowError");

        // The VM remains usable after the overflow.
        check(ping(0) == DEPTH + 1, "native method stack works again after the overflow");
        check(pong(DEPTH + 5) == DEPTH + 5, "pure Java side of the recursion still works");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
