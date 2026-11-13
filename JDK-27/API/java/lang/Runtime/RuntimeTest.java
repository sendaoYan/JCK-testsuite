/*
 * @test
 * @summary java.lang.Runtime conformance: white-box structure + black-box behavior.
 * @run main RuntimeTest
 */

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class RuntimeTest {
    static int passed;

    interface IOAction { void run() throws IOException; }

    static void expectIOException(IOAction r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected IOException");
        } catch (IOException expected) {
            passed++;
        }
    }

    static void expectUnsatisfiedLinkError(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected UnsatisfiedLinkError");
        } catch (UnsatisfiedLinkError expected) {
            passed++;
        }
    }

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Runtime");
        check(Modifier.isFinal(c.getModifiers()), "Runtime is final");
        check(!c.isInterface(), "Runtime is a class");
        check(c.getConstructors().length == 0, "Runtime has no public constructors");
        check(Modifier.isStatic(c.getMethod("getRuntime").getModifiers()), "getRuntime static");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testSingleton() {
        Runtime r1 = Runtime.getRuntime();
        Runtime r2 = Runtime.getRuntime();
        check(r1 != null, "getRuntime non-null");
        check(r1 == r2, "getRuntime returns the same instance");
    }

    static void testProcessors() {
        check(Runtime.getRuntime().availableProcessors() >= 1, "availableProcessors >= 1");
    }

    static void testMemory() {
        Runtime r = Runtime.getRuntime();
        long max = r.maxMemory();
        long total = r.totalMemory();
        long free = r.freeMemory();
        check(max >= total, "maxMemory >= totalMemory");
        check(total >= free, "totalMemory >= freeMemory");
        check(free >= 0, "freeMemory >= 0");
    }

    static void testVersion() {
        Runtime.Version v = Runtime.version();
        check(v != null, "version() non-null");
        check(v.toString() != null && !v.toString().isEmpty(), "version string non-empty");
    }

    // ================= 契约深化（2026-11）追加 =================
    static void testVersionContract() {
        Runtime.Version v = Runtime.version();
        check(v.feature() == 27, "version().feature() == 27 [JDK27]");
        check(v.major() == 27, "version().major() == 27 (legacy alias)");
        check(v.interim() == 0, "version().interim() == 0");
        // This build is 27.0.1.0.2: feature 27, interim 0, update 1, patch 0.
        check(v.update() == 1, "version().update() == 1 [27.0.1.0.2 build]");
        check(v.patch() == 0, "version().patch() == 0");
        check(v.version().get(0).intValue() == 27, "version().version() list head == 27");
        check(v.toString().startsWith("27"), "version string starts with 27");
        check(Runtime.version().feature() == v.feature(), "version() stable across calls");
        check(v.equals(v), "version equals self");
        check(v.hashCode() == v.hashCode(), "version hashCode stable");
        check(v.compareTo(v) == 0, "version compareTo self == 0");
        check(v.compareToIgnoreOptional(v) == 0, "version compareToIgnoreOptional self == 0");
        check(v.equalsIgnoreOptional(v), "version equalsIgnoreOptional self");
        // parse() round-trip: parsing the textual form yields an equal Version.
        check(Runtime.Version.parse(v.toString()).equals(v), "Version.parse round-trip equals");
        check(Runtime.Version.parse("27").feature() == 27, "parse('27').feature() == 27");
        check(!v.equals(Runtime.Version.parse("26")), "version differs from other feature release");
    }

    static void testSurfaceReflection() throws Exception {
        // Presence (and shape) of the entire public surface relevant here;
        // exit/halt are reflected but NEVER invoked. traceInstructions/
        // traceMethodCalls were removed in JDK-27 (verified absent).
        Method[] names = new Method[] {
            Runtime.class.getMethod("getRuntime"),
            Runtime.class.getMethod("gc"),
            Runtime.class.getMethod("runFinalization"),
            Runtime.class.getMethod("exit", int.class),
            Runtime.class.getMethod("halt", int.class),
            Runtime.class.getMethod("addShutdownHook", Thread.class),
            Runtime.class.getMethod("removeShutdownHook", Thread.class),
            Runtime.class.getMethod("availableProcessors"),
            Runtime.class.getMethod("freeMemory"),
            Runtime.class.getMethod("totalMemory"),
            Runtime.class.getMethod("maxMemory"),
            Runtime.class.getMethod("load", String.class),
            Runtime.class.getMethod("loadLibrary", String.class),
            Runtime.class.getMethod("exec", String.class),
            Runtime.class.getMethod("exec", String.class, String[].class),
            Runtime.class.getMethod("exec", String.class, String[].class, File.class),
            Runtime.class.getMethod("exec", String[].class),
            Runtime.class.getMethod("exec", String[].class, String[].class),
            Runtime.class.getMethod("exec", String[].class, String[].class, File.class),
            Runtime.class.getMethod("version"),
        };
        check(names.length == 20, "Runtime public surface methods resolvable [JDK27]");
        boolean traceGone = false;
        try {
            Runtime.class.getMethod("traceInstructions", boolean.class);
        } catch (NoSuchMethodException expected) {
            traceGone = true;
        }
        check(traceGone, "traceInstructions removed in JDK-27");
        try {
            Runtime.class.getMethod("traceMethodCalls", boolean.class);
            throw new AssertionError("traceMethodCalls should be removed in JDK-27");
        } catch (NoSuchMethodException expected) {
            passed++;
        }
        check(Runtime.class.getMethod("getRuntime").getReturnType() == Runtime.class,
              "getRuntime returns Runtime");
        check(Runtime.class.getMethod("version").getReturnType() == Runtime.Version.class,
              "version returns Runtime.Version");
        check(Runtime.class.getMethod("freeMemory").getReturnType() == long.class, "freeMemory returns long");
        check(Runtime.class.getMethod("totalMemory").getReturnType() == long.class, "totalMemory returns long");
        check(Runtime.class.getMethod("maxMemory").getReturnType() == long.class, "maxMemory returns long");
        check(Runtime.class.getMethod("availableProcessors").getReturnType() == int.class,
              "availableProcessors returns int");
        check(Runtime.class.getMethod("exit", int.class).getReturnType() == void.class, "exit returns void");
        check(Runtime.class.getMethod("halt", int.class).getReturnType() == void.class, "halt returns void");
        check(Runtime.class.getMethod("addShutdownHook", Thread.class).getReturnType() == void.class,
              "addShutdownHook returns void");
        check(Runtime.class.getMethod("removeShutdownHook", Thread.class).getReturnType() == boolean.class,
              "removeShutdownHook returns boolean");
        check(Runtime.class.getMethod("exec", String.class).getReturnType() == Process.class,
              "exec returns Process");
        check(Runtime.class.getMethod("exec", String.class, String[].class).getReturnType() == Process.class,
              "exec(env) returns Process");
        check(Runtime.class.getMethod("exec", String.class, String[].class, File.class).getReturnType() == Process.class,
              "exec(env,dir) returns Process");
        check(Runtime.class.getMethod("exec", String[].class).getReturnType() == Process.class,
              "exec(String[]) returns Process");
        check(Runtime.class.getMethod("exec", String[].class, String[].class).getReturnType() == Process.class,
              "exec(String[],env) returns Process");
        check(Runtime.class.getMethod("exec", String[].class, String[].class, File.class).getReturnType() == Process.class,
              "exec(String[],env,dir) returns Process");
        check(Runtime.class.getMethod("load", String.class).getReturnType() == void.class, "load returns void");
        check(Runtime.class.getMethod("loadLibrary", String.class).getReturnType() == void.class,
              "loadLibrary returns void");
        check(Modifier.isNative(Runtime.class.getMethod("gc").getModifiers()), "gc is native");
        check(Modifier.isNative(Runtime.class.getMethod("availableProcessors").getModifiers()),
              "availableProcessors is native");
        check(Modifier.isNative(Runtime.class.getMethod("freeMemory").getModifiers()), "freeMemory is native");
        check(Modifier.isNative(Runtime.class.getMethod("totalMemory").getModifiers()), "totalMemory is native");
        check(Modifier.isNative(Runtime.class.getMethod("maxMemory").getModifiers()), "maxMemory is native");
        int execOverloads = 0;
        for (Method m : Runtime.class.getMethods()) {
            if (m.getName().equals("exec")) execOverloads++;
        }
        check(execOverloads == 6, "6 exec overloads");
        check(Runtime.class.getSuperclass() == Object.class, "Runtime extends Object");
    }

    static void testSingletonContract() throws Exception {
        final Runtime main = Runtime.getRuntime();
        check(Runtime.getRuntime() == main, "singleton call site 2");
        check(Runtime.getRuntime() == main, "singleton call site 3");
        check(Runtime.getRuntime() == main, "singleton call site 4");
        check(Runtime.getRuntime() == main, "singleton call site 5");
        check(Runtime.getRuntime() == main, "singleton call site 6");
        check(main.getClass().getName().equals("java.lang.Runtime"), "runtime class name");
        check(main.equals(main), "runtime equals self");
        check(main.hashCode() == main.hashCode(), "runtime hashCode stable");
        final Runtime[] seen = new Runtime[2];
        Thread t1 = new Thread(() -> { seen[0] = Runtime.getRuntime(); });
        t1.start();
        t1.join();
        check(seen[0] == main, "getRuntime from spawned thread");
        Thread t2 = new Thread(() -> { seen[1] = Runtime.getRuntime(); });
        t2.setDaemon(true);
        t2.start();
        t2.join();
        check(seen[1] == main, "getRuntime from daemon thread");
    }

    static void testProcessorsContract() throws Exception {
        Runtime r = Runtime.getRuntime();
        int p1 = r.availableProcessors();
        check(p1 >= 1, "availableProcessors >= 1");
        check(p1 <= 1024, "availableProcessors sane upper bound");
        check(r.availableProcessors() == p1, "availableProcessors stable");
        check(Runtime.getRuntime().availableProcessors() == p1, "availableProcessors stable via 2nd handle");
        final int[] other = new int[1];
        Thread t = new Thread(() -> { other[0] = Runtime.getRuntime().availableProcessors(); });
        t.start();
        t.join();
        check(other[0] == p1, "availableProcessors consistent across threads");
    }

    static void testMemoryContract() {
        Runtime r = Runtime.getRuntime();
        long max1 = r.maxMemory();
        long total1 = r.totalMemory();
        long free1 = r.freeMemory();
        check(max1 > 0, "maxMemory > 0");
        check(total1 > 0, "totalMemory > 0");
        check(total1 - free1 >= 0, "used memory = total - free >= 0");
        check(r.maxMemory() == max1, "maxMemory stable across calls");
        byte[] ballast = new byte[16 * 1024 * 1024];
        ballast[0] = 1;
        long total2 = r.totalMemory();
        long free2 = r.freeMemory();
        check(r.maxMemory() >= total2, "maxMemory >= totalMemory after alloc");
        check(total2 >= free2, "totalMemory >= freeMemory after alloc");
        check(free2 >= 0, "freeMemory >= 0 after alloc");
        check(r.freeMemory() >= 0, "freeMemory >= 0 (repeat)");
        check(total2 - free2 >= 0, "used memory >= 0 after alloc");
        ballast = null;
    }

    static void testGcContract() {
        Runtime r = Runtime.getRuntime();
        r.gc();
        r.gc();
        r.runFinalization();
        r.runFinalization();
        passed++;
        check(r.freeMemory() >= 0, "freeMemory >= 0 after gc");
        check(r.totalMemory() <= r.maxMemory(), "totalMemory <= maxMemory after gc");
        check(r.freeMemory() <= r.totalMemory(), "freeMemory <= totalMemory after gc");
    }

    static void testObjectSemantics() {
        Runtime r = Runtime.getRuntime();
        check(r.toString() != null, "runtime toString non-null");
        check(r.toString().contains("java.lang.Runtime"), "runtime toString contains class name");
        check(!java.io.Serializable.class.isAssignableFrom(Runtime.class),
              "Runtime is not Serializable");
    }

    static void testShutdownHooksContract() throws Exception {
        Runtime r = Runtime.getRuntime();
        try {
            r.removeShutdownHook(null);
            throw new AssertionError("removeShutdownHook(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
        Thread hook = new Thread(() -> {});
        r.addShutdownHook(hook);
        check(hook.getState() == Thread.State.NEW, "hook stays NEW after add");
        check(!hook.isAlive(), "hook not alive after add");
        check(r.removeShutdownHook(hook), "remove after add");
        check(hook.getState() == Thread.State.NEW, "hook stays NEW after remove");
        Thread finished = new Thread(() -> {});
        finished.start();
        finished.join();
        r.addShutdownHook(finished);
        check(r.removeShutdownHook(finished), "finished thread hook removable");
        Thread alive = new Thread(() -> {
            try { Thread.sleep(3000); } catch (InterruptedException ignored) { }
        });
        alive.setDaemon(true);
        alive.start();
        try {
            r.addShutdownHook(alive);
            throw new AssertionError("alive hook: expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            check("Hook already running".equals(e.getMessage()), "alive hook exact message [JDK27]");
        }
        Thread[] hooks = new Thread[5];
        for (int i = 0; i < 5; i++) {
            hooks[i] = new Thread("lcF-hook-" + i) { public void run() { } };
            r.addShutdownHook(hooks[i]);
        }
        boolean allNew = true;
        for (int i = 0; i < 5; i++) allNew &= (hooks[i].getState() == Thread.State.NEW);
        check(allNew, "all 5 hooks stay NEW while registered");
        boolean allRemoved = true;
        for (int i = 4; i >= 0; i--) allRemoved &= r.removeShutdownHook(hooks[i]);
        check(allRemoved, "5 hooks removed in reverse order");
        check(!r.removeShutdownHook(hooks[2]), "double remove false");
        check(hooks[0].getName() != null, "hook name non-null");
        check(!r.removeShutdownHook(new Thread("lcF-never-added")), "remove never-added hook false");
        r.addShutdownHook(hook);
        r.removeShutdownHook(hook);
        r.addShutdownHook(hook);
        check(r.removeShutdownHook(hook), "hook re-registerable after removal");
        Runnable noop = () -> { };
        Thread h1 = new Thread(noop);
        Thread h2 = new Thread(noop);
        r.addShutdownHook(h1);
        r.addShutdownHook(h2);
        check(r.removeShutdownHook(h1), "distinct hook 1 removable");
        check(r.removeShutdownHook(h2), "distinct hook 2 removable");
    }

    static void testExecNegatives() {
        Runtime r = Runtime.getRuntime();
        try {
            r.exec("");
            throw new AssertionError("exec(\"\"): expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            check("Empty command".equals(e.getMessage()), "exec empty exact message [JDK27]");
        } catch (IOException e) {
            throw new AssertionError("exec(\"\"): unexpected IOException");
        }
        try {
            r.exec("   ");
            throw new AssertionError("exec(whitespace): expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException expected) {
            passed++;
        } catch (IOException e) {
            throw new AssertionError("exec(whitespace): unexpected IOException");
        }
        try {
            r.exec((String) null);
            throw new AssertionError("exec((String)null): expected NPE");
        } catch (NullPointerException expected) {
            passed++;
        } catch (IOException e) {
            throw new AssertionError("exec((String)null): unexpected IOException");
        }
        try {
            r.exec((String[]) null);
            throw new AssertionError("exec((String[])null): expected NPE");
        } catch (NullPointerException expected) {
            passed++;
        } catch (IOException e) {
            throw new AssertionError("exec((String[])null): unexpected IOException");
        }
        try {
            r.exec(new String[0]);
            throw new AssertionError("exec(empty String[]): expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException expected) {
            passed++;
        } catch (IOException e) {
            throw new AssertionError("exec(empty String[]): unexpected IOException");
        }
        try {
            r.exec(new String[]{null});
            throw new AssertionError("exec({null}): expected NPE");
        } catch (NullPointerException expected) {
            passed++;
        } catch (IOException e) {
            throw new AssertionError("exec({null}): unexpected IOException");
        }
        try {
            r.exec("jck-no-such-cmd-xyz", new String[]{"LCF=1"});
            throw new AssertionError("exec(missing, env): expected IOException");
        } catch (IOException expected) {
            passed++;
        }
        try {
            r.exec("jck-no-such-cmd-xyz", new String[0], new File("/nonexistent-lcF-dir"));
            throw new AssertionError("exec(missing, bad dir): expected IOException");
        } catch (IOException expected) {
            passed++;
        }
        try {
            r.exec(new String[]{"jck-no-such-cmd-xyz"}, (String[]) null);
            throw new AssertionError("exec(String[], null envp): expected IOException");
        } catch (IOException expected) {
            passed++;
        }
    }

    static void testExecReal() throws Exception {
        Runtime r = Runtime.getRuntime();
        String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        Process p = r.exec(new String[]{javaBin, "-version"});
        check(p != null, "exec real process non-null");
        java.io.InputStream err = p.getErrorStream();
        java.io.InputStream out = p.getInputStream();
        check(err != null && out != null, "process streams non-null");
        StringBuilder sb = new StringBuilder();
        byte[] buf = new byte[8192];
        int n;
        while ((n = err.read(buf)) > 0) sb.append(new String(buf, 0, n, "UTF-8"));
        check(p.waitFor() == 0, "java -version exit code 0");
        check(sb.toString().contains("version"), "-version output mentions version");
        check(sb.length() > 0, "-version stderr non-empty");
        check(p.exitValue() == 0, "exitValue after waitFor");
        check(p.exitValue() == p.waitFor(), "exitValue consistent with waitFor");
        check(!p.isAlive(), "not alive after waitFor");
        check(p.waitFor() == 0, "waitFor idempotent");
        check(out.read() == -1, "stdout at EOF for -version");
        check(p.getOutputStream() != null, "process output stream non-null");
        p.destroy();
        check(!p.isAlive(), "not alive after destroy");
    }

    static void testLoadNegatives() {
        Runtime r = Runtime.getRuntime();
        // An absolute path is required before the library is even looked up, so
        // the probe path must be absolute on every platform (a leading slash is
        // drive-relative on Windows).
        String missing = new java.io.File(System.getProperty("java.io.tmpdir"),
                                          "lcF-definitely-missing-lib").getAbsolutePath();
        try {
            r.load(missing);
            throw new AssertionError("Runtime.load(missing): expected UnsatisfiedLinkError");
        } catch (UnsatisfiedLinkError e) {
            check(("Can't load library: " + missing).equals(e.getMessage()),
                  "Runtime.load(missing) exact message [JDK27]: " + e.getMessage());
        }
        try {
            r.load("lcF/relative.so");
            throw new AssertionError("Runtime.load(relative): expected UnsatisfiedLinkError");
        } catch (UnsatisfiedLinkError e) {
            check("Expecting an absolute path of the library: lcF/relative.so".equals(e.getMessage()),
                  "Runtime.load(relative) exact message [JDK27]");
        }
        try {
            r.load("");
            throw new AssertionError("Runtime.load(empty): expected UnsatisfiedLinkError");
        } catch (UnsatisfiedLinkError e) {
            check("Expecting an absolute path of the library: ".equals(e.getMessage()),
                  "Runtime.load(empty) exact message [JDK27]");
        }
        try {
            r.loadLibrary("lcFNoSuchLib");
            throw new AssertionError("Runtime.loadLibrary(missing): expected UnsatisfiedLinkError");
        } catch (UnsatisfiedLinkError e) {
            check(e.getMessage() != null && e.getMessage().startsWith("no lcFNoSuchLib in java.library.path"),
                  "Runtime.loadLibrary(missing) message prefix [JDK27]");
        }
        // The guard tests the platform's own separator, so the probe must use it.
        String separated = "a" + java.io.File.separatorChar + "b";
        try {
            r.loadLibrary(separated);
            throw new AssertionError("Runtime.loadLibrary(separator): expected UnsatisfiedLinkError");
        } catch (UnsatisfiedLinkError e) {
            check(("Directory separator should not appear in library name: " + separated)
                      .equals(e.getMessage()),
                  "Runtime.loadLibrary(separator) exact message [JDK27]: " + e.getMessage());
        }
        try {
            r.loadLibrary("");
            throw new AssertionError("Runtime.loadLibrary(empty): expected UnsatisfiedLinkError");
        } catch (UnsatisfiedLinkError e) {
            check(e.getMessage() != null && e.getMessage().startsWith("no  in java.library.path"),
                  "Runtime.loadLibrary(empty) message prefix [JDK27]");
        }
        try {
            r.load(null);
            throw new AssertionError("Runtime.load(null): expected NPE");
        } catch (NullPointerException expected) {
            passed++;
        }
        try {
            r.loadLibrary(null);
            throw new AssertionError("Runtime.loadLibrary(null): expected NPE");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    static void testGC() {
        Runtime r = Runtime.getRuntime();
        r.gc();
        r.runFinalization();
        passed++;
    }

    // ---- Part 2b: black-box for shutdown hooks, exec, load/loadLibrary ----
    static void testBlackBox() {
        Runtime r = Runtime.getRuntime();

        // addShutdownHook / removeShutdownHook roundtrip (no leftover hooks).
        Thread hook = new Thread(() -> {});
        r.addShutdownHook(hook);
        try {
            r.addShutdownHook(hook);
            throw new AssertionError("addShutdownHook twice: expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
        check(r.removeShutdownHook(hook), "removeShutdownHook returns true after add");
        check(!r.removeShutdownHook(hook), "removeShutdownHook returns false when not registered");
        try {
            r.addShutdownHook(null);
            throw new AssertionError("addShutdownHook(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }

        // exec overloads: a missing command yields IOException before any process is spawned.
        expectIOException(() -> r.exec("jck-no-such-cmd-xyz"), "exec(String)");
        expectIOException(() -> r.exec("jck-no-such-cmd-xyz", new String[0]), "exec(String,String[])");
        expectIOException(() -> r.exec("jck-no-such-cmd-xyz", new String[0], new File(".")),
                          "exec(String,String[],File)");
        expectIOException(() -> r.exec(new String[]{"jck-no-such-cmd-xyz"}), "exec(String[])");
        expectIOException(() -> r.exec(new String[]{"jck-no-such-cmd-xyz"}, new String[0]),
                          "exec(String[],String[])");
        expectIOException(() -> r.exec(new String[]{"jck-no-such-cmd-xyz"}, new String[0], new File(".")),
                          "exec(String[],String[],File)");

        // load / loadLibrary with a missing library -> UnsatisfiedLinkError.
        expectUnsatisfiedLinkError(() -> r.load("/jck/no/such/lib.so"), "load(missing path)");
        expectUnsatisfiedLinkError(() -> r.loadLibrary("jck_no_such_lib_xyz"), "loadLibrary(missing)");

        // exit(int) and halt(int) terminate the JVM, so they are intentionally not invoked.
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testSingleton();
        testProcessors();
        testMemory();
        testVersion();
        testGC();
        testBlackBox();
        testVersionContract();
        testSurfaceReflection();
        testSingletonContract();
        testProcessorsContract();
        testMemoryContract();
        testGcContract();
        testObjectSemantics();
        testShutdownHooksContract();
        testExecNegatives();
        testExecReal();
        testLoadNegatives();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
