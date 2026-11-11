/*
 * @test
 * @summary JVMS 5.6: a native method declared in a class file has no Java
 *          implementation and is bound to a native library implementation at
 *          first invocation.  Before any library is loaded the invocation
 *          throws UnsatisfiedLinkError; after System.load of a library that
 *          exports the Java_<class>_<method> symbols, both static and
 *          instance natives invoke and re-invoke successfully.  The binding
 *          works for a class generated directly from a class file (jasm),
 *          not only for javac-produced classes.
 * @compile NativeBindingTarget.jasm
 * @run main/othervm --enable-native-access=ALL-UNNAMED BindingNativeMethodTest
 */

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class BindingNativeMethodTest {
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

    static final String C_SRC =
        "#include <jni.h>\n" +
        "JNIEXPORT jint JNICALL Java_NativeBindingTarget_add(JNIEnv* e, jclass c, jint a, jint b) {\n" +
        "    return a + b;\n" +
        "}\n" +
        "JNIEXPORT jstring JNICALL Java_NativeBindingTarget_greet(JNIEnv* e, jobject o) {\n" +
        "    return (*e)->NewStringUTF(e, \"jck-native\");\n" +
        "}\n" +
        "JNIEXPORT jint JNICALL Java_BindingNativeMethodTest_selfSquare(JNIEnv* e, jclass c, jint a) {\n" +
        "    return a * a;\n" +
        "}\n";

    static native int selfSquare(int a);

    static Path jdkHome() {
        Path p = Paths.get(System.getProperty("java.home"));
        return p.endsWith("jre") ? p.getParent() : p;
    }

    static void compileAndLoad() throws Exception {
        Path dir = Paths.get(System.getProperty("user.dir"));
        Path cFile = dir.resolve("jckbind.c");
        Path so = dir.resolve("libjckbind.so");
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
        if (!gccAvailable()) {
            System.out.println("SKIP: no C compiler (gcc) available; native library cannot be built");
            return;
        }
        Class<?> target = Class.forName("NativeBindingTarget");
        Method add = target.getMethod("add", int.class, int.class);
        Method greet = target.getMethod("greet");
        Object instance = target.getDeclaredConstructor().newInstance();

        // JVMS 5.6: before a library with the implementation is loaded, the
        // JVM cannot find a native implementation for the method.
        try {
            add.invoke(null, 20, 22);
            check(false, "static native must be unbound before load");
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause();
            check(cause instanceof UnsatisfiedLinkError,
                    "static native before load -> UnsatisfiedLinkError (got: " + cause + ")");
            check(String.valueOf(cause.getMessage()).contains("add"),
                    "static ULE names the method (got: " + cause.getMessage() + ")");
        }
        try {
            greet.invoke(instance);
            check(false, "instance native must be unbound before load");
        } catch (java.lang.reflect.InvocationTargetException e) {
            check(e.getCause() instanceof UnsatisfiedLinkError,
                    "instance native before load -> UnsatisfiedLinkError (got: " + e.getCause() + ")");
        }
        try {
            selfSquare(9);
            check(false, "test class native must be unbound before load");
        } catch (UnsatisfiedLinkError e) {
            check(true, "javac-declared native before load -> UnsatisfiedLinkError");
        }

        // After System.load the symbols are bound; the JVM resolves lazily,
        // so the previously failing invocations now succeed.
        compileAndLoad();
        check((Integer) add.invoke(null, 20, 22) == 42, "static native bound after load");
        check("jck-native".equals(greet.invoke(instance)), "instance native bound after load");
        check(selfSquare(9) == 81, "javac-declared native bound after load");

        // The binding persists for subsequent invocations.
        check((Integer) add.invoke(null, 1, 2) == 3, "static native re-invocation");
        check("jck-native".equals(greet.invoke(target.getDeclaredConstructor().newInstance())),
                "instance native re-invocation on a fresh instance");
        check(selfSquare(-4) == 16, "javac-declared native re-invocation");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
