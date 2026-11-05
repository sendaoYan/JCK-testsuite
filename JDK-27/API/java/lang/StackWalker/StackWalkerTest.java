/*
 * @test
 * @summary java.lang.StackWalker conformance: white-box structure + black-box behavior.
 * @run main StackWalkerTest
 */

import java.lang.StackWalker.StackFrame;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class StackWalkerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.StackWalker");
        check(Modifier.isFinal(c.getModifiers()), "StackWalker is final");
        check(!c.isInterface(), "StackWalker is a class");
    }

    // ---- Part 2: black-box (behavioral) ----
    static Class<?> caller() {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
    }

    static void testWalk() {
        StackWalker sw = StackWalker.getInstance();
        long count = sw.walk(s -> s.count());
        check(count >= 1, "walk counts at least one frame");
        boolean[] sawMain = {false};
        sw.walk(s -> {
            s.forEach(f -> { if ("main".equals(f.getMethodName())) sawMain[0] = true; });
            return null;
        });
        check(sawMain[0], "walk sees the calling main frame");
    }

    static void testForEach() {
        StackWalker.getInstance().forEach(f -> { /* no-op */ });
        passed++;
    }

    static void testGetCallerClass() {
        check(caller() == StackWalkerTest.class, "getCallerClass returns this class");
    }

    static void testInstances() {
        check(StackWalker.getInstance(StackWalker.Option.DROP_METHOD_INFO) != null,
              "getInstance(Option)");
        check(StackWalker.getInstance(Set.of(StackWalker.Option.RETAIN_CLASS_REFERENCE)) != null,
              "getInstance(Set)");
        check(StackWalker.getInstance(Set.of(), 32) != null, "getInstance(Set, estimate>0)");
        try {
            StackWalker.getInstance(Set.of(), 0);
            throw new AssertionError("getInstance(Set, 0): expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
    }

    
    static void testBlackBox() throws Exception {
        testWalk();
        testForEach();
        testGetCallerClass();
        testInstances();
    }
    // ============================================================
    // ---- Part 3: contract deepening (2026-11, appended) ----
    // ============================================================

    static java.util.Optional<StackFrame> firstFrame(StackWalker w) {
        return w.walk(s -> s.findFirst());
    }

    // The first frame of the walker stream is exactly the method that invoked walk().
    static void testWalkFirstFrameExact() {
        StackWalker w = StackWalker.getInstance();
        java.util.Optional<StackFrame> of = firstFrame(w);
        check(of.isPresent(), "findFirst frame present");
        StackFrame f = of.get();
        check("StackWalkerTest".equals(f.getClassName()), "first frame className exact");
        check("firstFrame".equals(f.getMethodName()) || "testWalkFirstFrameExact".equals(f.getMethodName()),
              "first frame methodName is the walk() invoker");
        StackFrame here = w.walk(s -> s.findFirst()).orElseThrow();
        check("testWalkFirstFrameExact".equals(here.getMethodName()),
              "walk invoked in this method names this method exactly");
        check("StackWalkerTest.java".equals(here.getFileName()), "getFileName exact");
        check(here.getFileName() != null && here.getFileName().contains(".java"),
              "getFileName is a .java source file");
        check(here.getLineNumber() > 0, "getLineNumber > 0");
        check(here.getByteCodeIndex() >= 0, "getByteCodeIndex >= 0 for a non-native frame");
        check(!here.isNativeMethod(), "isNativeMethod false for a Java frame");
        StackTraceElement ste = here.toStackTraceElement();
        check(here.getMethodName().equals(ste.getMethodName()), "toStackTraceElement method name agrees");
        check(here.getClassName().equals(ste.getClassName()), "toStackTraceElement class name agrees");
        check(here.getFileName().equals(ste.getFileName()), "toStackTraceElement file name agrees");
        check(here.getLineNumber() == ste.getLineNumber(), "toStackTraceElement line number agrees");
        check(!ste.isNativeMethod(), "toStackTraceElement not native");
        // RETAIN_CLASS_REFERENCE surface
        StackWalker r = StackWalker.getInstance(Set.of(StackWalker.Option.RETAIN_CLASS_REFERENCE));
        StackFrame rf = r.walk(s -> s.findFirst()).orElseThrow();
        check(rf.getDeclaringClass() == StackWalkerTest.class, "getDeclaringClass == test class");
        check("StackWalkerTest".equals(rf.getDeclaringClass().getName()),
              "getDeclaringClass().getName() is the binary name");
        check(rf.getByteCodeIndex() >= 0, "RETAIN frame getByteCodeIndex >= 0");
        check("()V".equals(rf.getDescriptor()), "getDescriptor ()V for static void no-arg method");
        check(rf.getMethodType() != null, "getMethodType non-null with RETAIN");
        check("()void".equals(rf.getMethodType().toString()), "getMethodType toString ()void");
        check("()V".equals(rf.getMethodType().toMethodDescriptorString()),
              "getMethodType descriptor ()V");
    }

    static void forEachTarget() {
        boolean saw = false;
        StackWalker.getInstance().forEach(f -> {
            if ("StackWalkerTest".equals(f.getClassName()) && "forEachTarget".equals(f.getMethodName())) {
                sawExact[0] = true;
            }
        });
        check(sawExact[0], "forEach sees a frame with this class and the exact method name");
        check(!saw, "sanity");
    }
    static final boolean[] sawExact = {false};

    static void testForEachRealStack() {
        forEachTarget();
        final int[] count = {0};
        StackWalker.getInstance().forEach(f -> count[0]++);
        check(count[0] >= 1, "forEach visits at least one frame");
    }

    // Without RETAIN_CLASS_REFERENCE the class-object accessors fail with exact messages.
    static void testNoRetainClassAccess() {
        StackWalker plain = StackWalker.getInstance();
        try {
            plain.walk(s -> s.findFirst().map(StackFrame::getDeclaringClass));
            throw new AssertionError("getDeclaringClass without RETAIN: expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            check("No access to RETAIN_CLASS_REFERENCE".equals(expected.getMessage()),
                  "getDeclaringClass UOE message exact");
        }
        try {
            plain.getCallerClass();
            throw new AssertionError("getCallerClass without RETAIN: expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            check("This stack walker does not have RETAIN_CLASS_REFERENCE access".equals(expected.getMessage()),
                  "getCallerClass UOE message exact");
        }
        try {
            plain.walk(s -> s.findFirst().map(StackFrame::getMethodType));
            throw new AssertionError("getMethodType without RETAIN: expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            check(true, "getMethodType UOE without RETAIN");
        }
        try {
            plain.walk(s -> s.findFirst().map(StackFrame::getDescriptor));
            throw new AssertionError("getDescriptor without RETAIN: expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            check(true, "getDescriptor UOE without RETAIN");
        }
    }

    // JDK-27 Option value set (4 values, declaration order locked).
    static void testOptionsEnum() {
        StackWalker.Option[] vals = StackWalker.Option.values();
        check(vals.length == 4, "Option has exactly 4 constants on JDK-27");
        List<String> names = new ArrayList<>();
        for (StackWalker.Option o : vals) names.add(o.name());
        check(names.equals(Arrays.asList("RETAIN_CLASS_REFERENCE", "DROP_METHOD_INFO",
                                         "SHOW_REFLECT_FRAMES", "SHOW_HIDDEN_FRAMES")),
              "Option declaration order locked");
        check(new java.util.HashSet<>(names).equals(new java.util.HashSet<>(Arrays.asList(
              "RETAIN_CLASS_REFERENCE", "DROP_METHOD_INFO", "SHOW_REFLECT_FRAMES", "SHOW_HIDDEN_FRAMES"))),
              "Option value set exact");
        check(StackWalker.Option.valueOf("RETAIN_CLASS_REFERENCE") == StackWalker.Option.RETAIN_CLASS_REFERENCE,
              "valueOf RETAIN_CLASS_REFERENCE");
        check(StackWalker.Option.valueOf("SHOW_HIDDEN_FRAMES") == StackWalker.Option.SHOW_HIDDEN_FRAMES,
              "valueOf SHOW_HIDDEN_FRAMES");
        check(StackWalker.Option.valueOf("SHOW_REFLECT_FRAMES") == StackWalker.Option.SHOW_REFLECT_FRAMES,
              "valueOf SHOW_REFLECT_FRAMES");
        check(StackWalker.Option.valueOf("DROP_METHOD_INFO") == StackWalker.Option.DROP_METHOD_INFO,
              "valueOf DROP_METHOD_INFO");
        vals[0] = null;
        check(StackWalker.Option.values()[0] != null, "values() returns a fresh array");
        check("RETAIN_CLASS_REFERENCE".equals(StackWalker.Option.RETAIN_CLASS_REFERENCE.name()),
              "Option name() exact");
        check(StackWalker.Option.values()[0].compareTo(StackWalker.Option.values()[1]) < 0,
              "Option enum order by compareTo");
        try {
            StackWalker.Option.valueOf("NO_SUCH_OPTION");
            throw new AssertionError("valueOf unknown: expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            check(true, "valueOf unknown constant -> IllegalArgumentException");
        }
        try {
            StackWalker.Option.valueOf(null);
            throw new AssertionError("valueOf(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            check(true, "valueOf(null) -> NullPointerException");
        }
    }

    static void testGetInstanceVariants() {
        check(StackWalker.getInstance() != null, "getInstance()");
        for (StackWalker.Option o : StackWalker.Option.values()) {
            check(StackWalker.getInstance(o) != null, "getInstance(Option." + o.name() + ")");
        }
        check(StackWalker.getInstance(Set.of()) != null, "getInstance(empty Set)");
        check(StackWalker.getInstance(Set.of(), 16) != null, "getInstance(Set, estimate)");
        try {
            StackWalker.getInstance(Set.of(), -1);
            throw new AssertionError("estimate -1: expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            check(true, "negative estimate -> IllegalArgumentException");
        }
        // empty option set behaves like the default instance
        StackWalker a = StackWalker.getInstance();
        StackWalker b = StackWalker.getInstance(Set.of());
        String ma = a.walk(s -> s.findFirst().map(StackFrame::getMethodName)).orElse("");
        String mb = b.walk(s -> s.findFirst().map(StackFrame::getMethodName)).orElse("");
        check(ma.equals(mb), "getInstance() and getInstance(Set.of()) behave alike");
        check(a.walk(s -> s.count()) >= 1, "default walker counts frames");
        check(b.walk(s -> s.count()) >= 1, "empty-set walker counts frames");
        StackWalker combo = StackWalker.getInstance(
            Set.of(StackWalker.Option.SHOW_HIDDEN_FRAMES, StackWalker.Option.SHOW_REFLECT_FRAMES), 8);
        check(combo != null, "combined option set with estimate");
        check("testGetInstanceVariants".equals(
                  combo.walk(s -> s.findFirst().map(StackFrame::getMethodName)).orElse("")),
              "combined options first frame exact");
        StackWalker retained = StackWalker.getInstance(Set.of(StackWalker.Option.RETAIN_CLASS_REFERENCE));
        check(retained.walk(s -> s.count()) >= 1, "retained walker counts frames");
    }

    // DROP_METHOD_INFO (JDK-27): class accessors live, method accessors throw UOE.
    static void testDropMethodInfo() {
        StackWalker w = StackWalker.getInstance(Set.of(StackWalker.Option.DROP_METHOD_INFO));
        StackFrame f = w.walk(s -> s.findFirst()).orElseThrow();
        check("StackWalkerTest".equals(f.getClassName()), "DROP: getClassName still works");
        check(w.walk(s -> s.count()) >= 1, "DROP: frames countable");
        try { f.getMethodName(); throw new AssertionError("DROP getMethodName: expected UOE"); }
        catch (UnsupportedOperationException expected) { check(true, "DROP: getMethodName UOE"); }
        try { f.getFileName(); throw new AssertionError("DROP getFileName: expected UOE"); }
        catch (UnsupportedOperationException expected) { check(true, "DROP: getFileName UOE"); }
        try { f.getLineNumber(); throw new AssertionError("DROP getLineNumber: expected UOE"); }
        catch (UnsupportedOperationException expected) { check(true, "DROP: getLineNumber UOE"); }
        try { f.getByteCodeIndex(); throw new AssertionError("DROP getByteCodeIndex: expected UOE"); }
        catch (UnsupportedOperationException expected) { check(true, "DROP: getByteCodeIndex UOE"); }
        try { f.isNativeMethod(); throw new AssertionError("DROP isNativeMethod: expected UOE"); }
        catch (UnsupportedOperationException expected) { check(true, "DROP: isNativeMethod UOE"); }
        try { f.toStackTraceElement(); throw new AssertionError("DROP toStackTraceElement: expected UOE"); }
        catch (UnsupportedOperationException expected) { check(true, "DROP: toStackTraceElement UOE"); }
        try { f.getMethodType(); throw new AssertionError("DROP getMethodType: expected UOE"); }
        catch (UnsupportedOperationException expected) { check(true, "DROP: getMethodType UOE"); }
        try { f.getDescriptor(); throw new AssertionError("DROP getDescriptor: expected UOE"); }
        catch (UnsupportedOperationException expected) { check(true, "DROP: getDescriptor UOE"); }
        StackWalker wr = StackWalker.getInstance(Set.of(StackWalker.Option.DROP_METHOD_INFO,
                                                        StackWalker.Option.RETAIN_CLASS_REFERENCE));
        StackFrame fr = wr.walk(s -> s.findFirst()).orElseThrow();
        check(fr.getDeclaringClass() == StackWalkerTest.class, "DROP+RETAIN: getDeclaringClass works");
        check("StackWalkerTest".equals(fr.getClassName()), "DROP+RETAIN: getClassName works");
    }

    static List<String> nestedWalk(int depth) {
        if (depth > 0) return nestedWalk(depth - 1);
        return StackWalker.getInstance().walk(s ->
            s.limit(3).map(f -> f.getClassName() + "." + f.getMethodName())
             .collect(java.util.stream.Collectors.toList()));
    }

    static void testWalkDeepNesting() {
        List<String> frames = nestedWalk(2);
        check(frames.size() == 3, "deep nesting: 3 frames collected");
        check(frames.get(0).equals("StackWalkerTest.nestedWalk"), "innermost frame is nestedWalk");
        check(frames.get(1).equals("StackWalkerTest.nestedWalk"), "recursive caller is nestedWalk");
        check(frames.get(2).startsWith("StackWalkerTest."), "outer frame belongs to this class");
        check(StackWalker.getInstance().walk(s -> s.count()) >= 3,
              "stack from a test method has at least 3 frames");
    }

    static class Inner {
        static String deepInfo() {
            StackWalker w = StackWalker.getInstance(Set.of(StackWalker.Option.RETAIN_CLASS_REFERENCE));
            return w.walk(s -> s.findFirst().map(f -> f.getClassName() + "/" + f.getMethodName()
                                                 + "/" + f.getDeclaringClass().getName()).orElse(""));
        }
    }

    static void testNestedClassFrame() throws Exception {
        String info = Inner.deepInfo();
        check(info.startsWith("StackWalkerTest$Inner/deepInfo/"), "nested frame cn/mn/declaringClass");
        check(Class.forName("StackWalkerTest$Inner") == Inner.class,
              "Class.forName nested frame class round trip");
    }

    // Cross-thread: a walker used in a child thread sees the child's own stack only.
    static class Child implements Runnable {
        final CountDownLatch done = new CountDownLatch(1);
        List<String> frames = new ArrayList<>();
        Throwable failure;
        public void run() {
            try {
                frames = StackWalker.getInstance().walk(s ->
                    s.limit(5).map(f -> f.getClassName() + "." + f.getMethodName())
                     .collect(java.util.stream.Collectors.toList()));
            } catch (Throwable t) {
                failure = t;
            } finally {
                done.countDown();
            }
        }
    }

    static void testCrossThreadStack() throws Exception {
        Child child = new Child();
        Thread t = new Thread(child, "lcI-child");
        t.start();
        check(child.done.await(30, java.util.concurrent.TimeUnit.SECONDS),
              "child thread finished (bounded latch)");
        check(child.failure == null, "child walk did not throw");
        check(!child.frames.isEmpty(), "child saw at least one frame");
        check(child.frames.get(0).equals("StackWalkerTest$Child.run"),
              "child's first frame is its own run() method");
        check(child.frames.contains("java.lang.Thread.run"), "child's stack bottom is Thread.run");
        boolean sawParentTestClassFrame = false;
        for (String f : child.frames) {
            if (f.startsWith("StackWalkerTest.")) sawParentTestClassFrame = true;
        }
        check(!sawParentTestClassFrame, "child's stack contains no parent test-class frames");
        check(child.frames.size() >= 2, "child's stack has at least 2 frames");
        check(StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                  .walk(s -> s.findFirst().map(StackFrame::getFileName))
                  .orElse("").equals("StackWalkerTest.java"),
              "first frame file name via RETAIN walker");
        t.join(30000);
    }

    static void reflectTarget() {
        List<String> show = StackWalker.getInstance(Set.of(StackWalker.Option.SHOW_REFLECT_FRAMES))
            .walk(s -> s.map(f -> f.getClassName() + "." + f.getMethodName())
                  .collect(java.util.stream.Collectors.toList()));
        int idxMain = -1;
        for (int i = 0; i < show.size(); i++) {
            if (show.get(i).endsWith(".main") && show.get(i).startsWith("StackWalkerTest")) { idxMain = i; break; }
        }
        check(idxMain > 0, "main frame located under SHOW_REFLECT_FRAMES");
        boolean sawInvoke = false;
        for (int i = 0; i < idxMain; i++) {
            if (show.get(i).equals("java.lang.reflect.Method.invoke")) sawInvoke = true;
        }
        check(sawInvoke, "SHOW_REFLECT_FRAMES exposes java.lang.reflect.Method.invoke below main");
        check(show.get(0).equals("StackWalkerTest.reflectTarget"), "reflected method is frame 0");
        List<String> plain = StackWalker.getInstance()
            .walk(s -> s.map(f -> f.getClassName() + "." + f.getMethodName())
                  .collect(java.util.stream.Collectors.toList()));
        boolean plainSawInvoke = false;
        int idxMainPlain = -1;
        for (int i = 0; i < plain.size(); i++) {
            if (plain.get(i).equals("StackWalkerTest.main")) { idxMainPlain = i; break; }
        }
        for (int i = 0; i < idxMainPlain; i++) {
            if (plain.get(i).equals("java.lang.reflect.Method.invoke")) plainSawInvoke = true;
        }
        check(!plainSawInvoke, "default walker hides Method.invoke below main");
    }

    static void testShowReflectFrames() throws Exception {
        java.lang.reflect.Method m = StackWalkerTest.class.getDeclaredMethod("reflectTarget");
        m.setAccessible(true);
        m.invoke(null);
    }

    static void testShowHiddenFrames() {
        StackWalker w = StackWalker.getInstance(Set.of(StackWalker.Option.SHOW_HIDDEN_FRAMES));
        check(w != null, "SHOW_HIDDEN_FRAMES walker non-null");
        check(w.walk(s -> s.count()) >= 1, "SHOW_HIDDEN_FRAMES walk non-empty");
        StackFrame f = w.walk(s -> s.findFirst()).orElseThrow();
        check("testShowHiddenFrames".equals(f.getMethodName()),
              "SHOW_HIDDEN_FRAMES first frame still the walk invoker");
        check(f.getLineNumber() > 0, "SHOW_HIDDEN_FRAMES frame line positive");
    }

    // walk() lifecycle contracts: null handling, closed stream, caller-frame skip.
    static void testWalkLifecycle() {
        StackWalker w = StackWalker.getInstance();
        check(w.walk(s -> null) == null, "walk may return null");
        check(w.walk(s -> s.limit(1).count()) == 1L, "walk limit 1 counts one frame");
        java.util.stream.Stream<StackFrame> st = w.walk(s -> s);
        try {
            st.count();
            throw new AssertionError("using the stream after walk: expected IllegalStateException");
        } catch (IllegalStateException expected) {
            check("This stack stream is not valid for walking.".equals(expected.getMessage()),
                  "stream-after-walk ISE message exact");
        }
        try {
            w.walk(null);
            throw new AssertionError("walk(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            check(true, "walk(null) -> NullPointerException");
        }
        try {
            w.forEach(null);
            throw new AssertionError("forEach(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            check(true, "forEach(null) -> NullPointerException");
        }
        // skip(1) yields the caller of this method: compare against Throwable's view of frame 1
        String expectedCaller = new Throwable().getStackTrace()[1].getMethodName();
        String got = w.walk(s -> s.skip(1).findFirst().map(StackFrame::getMethodName)).orElse("");
        check(got.equals(expectedCaller), "skip(1) frame matches Throwable.getStackTrace()[1]");
        check(w.walk(s -> s.skip(2).findFirst()).isPresent(), "skip(2) frame still present");
        check(w.walk(s -> s.count()) >= 1, "walker reusable across calls");
        check(w.walk(s -> s.limit(1000).count()) == w.walk(s -> s.count()),
              "limit larger than the stack does not drop frames");
    }

    static Class<?> callerOne() {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
    }
    static Class<?> callerTwo() { return callerOne(); }
    static class CallerInner { static Class<?> deep() { return callerOne(); } }

    static void testGetCallerClassDeep() {
        check(callerOne() == StackWalkerTest.class, "getCallerClass from test method");
        check(callerTwo() == StackWalkerTest.class, "getCallerClass through a helper chain");
        check(CallerInner.deep() == CallerInner.class, "getCallerClass from a nested class");
        check(callerOne() == callerOne(), "getCallerClass stable across calls");
    }

    // toStackTraceElement agrees with Throwable.getStackTrace for the same physical frame.
    static void consistencyTarget() {
        StackTraceElement[] arr = new Throwable().getStackTrace();
        StackFrame f = StackWalker.getInstance().walk(s -> s.findFirst()).orElseThrow();
        check("consistencyTarget".equals(arr[0].getMethodName()), "Throwable frame 0 is this method");
        check("consistencyTarget".equals(f.getMethodName()), "walker frame 0 is this method");
        StackTraceElement ste = f.toStackTraceElement();
        check(ste.getClassName().equals(arr[0].getClassName()), "same class name via walker and Throwable");
        check(ste.getMethodName().equals(arr[0].getMethodName()), "same method name via walker and Throwable");
        check(ste.getFileName().equals(arr[0].getFileName()), "same file name via walker and Throwable");
        check(ste.getLineNumber() > 0 && arr[0].getLineNumber() > 0, "both line numbers positive");
        check(!ste.isNativeMethod() && !arr[0].isNativeMethod(), "both views non-native");
        check(ste.toString().startsWith("StackWalkerTest.consistencyTarget("),
              "toStackTraceElement toString form agrees");
    }

    static void testToStackTraceElementConsistency() {
        consistencyTarget();
    }

    static void testDeepenedContract() throws Exception {
        testWalkFirstFrameExact();
        testForEachRealStack();
        testNoRetainClassAccess();
        testOptionsEnum();
        testGetInstanceVariants();
        testDropMethodInfo();
        testWalkDeepNesting();
        testNestedClassFrame();
        testCrossThreadStack();
        testShowReflectFrames();
        testShowHiddenFrames();
        testWalkLifecycle();
        testGetCallerClassDeep();
        testToStackTraceElementConsistency();
    }

public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepenedContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
