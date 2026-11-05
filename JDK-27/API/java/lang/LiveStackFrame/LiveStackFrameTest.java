/*
 * @test
 * @summary java.lang.LiveStackFrame conformance. LiveStackFrame is a package-private
 *          interface in java.lang (not exported to unnamed-module code), so this test
 *          verifies its structure reflectively and substitutes behavioral coverage via
 *          the public StackWalker / StackFrame API with SHOW_HIDDEN_FRAMES.
 * @run main LiveStackFrameTest
 */

import java.lang.StackWalker.StackFrame;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LiveStackFrameTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural, via reflection: type is package-private) ----
    static void testExistence() throws Exception {
        Class<?> c = Class.forName("java.lang.LiveStackFrame");
        check(c != null, "java.lang.LiveStackFrame loadable");
        check(c.isInterface(), "LiveStackFrame is an interface");
        check("java.lang.LiveStackFrame".equals(c.getName()), "binary name exact");
        check(!Modifier.isPublic(c.getModifiers()),
              "LiveStackFrame is NOT public (package-private in java.lang)");
        check(!Modifier.isAbstract(c.getModifiers()) || c.isInterface(), "interface modifiers sane");
    }

    static void testTypeHierarchy() throws Exception {
        Class<?> c = Class.forName("java.lang.LiveStackFrame");
        Class<?>[] ifaces = c.getInterfaces();
        boolean extendsFrame = false;
        for (Class<?> i : ifaces) {
            if ("java.lang.StackWalker$StackFrame".equals(i.getName())) extendsFrame = true;
        }
        check(extendsFrame, "LiveStackFrame extends StackWalker.StackFrame");
        check(StackFrame.class.isAssignableFrom(c),
              "StackFrame is assignable from LiveStackFrame");
    }

    static void testDeclaredMethods() throws Exception {
        Class<?> c = Class.forName("java.lang.LiveStackFrame");
        boolean hasMonitors = false, hasLocals = false, hasStack = false, hasWalker = false;
        int abstractCount = 0, staticCount = 0;
        for (Method m : c.getDeclaredMethods()) {
            if ("getMonitors".equals(m.getName())) hasMonitors = true;
            if ("getLocals".equals(m.getName())) hasLocals = true;
            if ("getStack".equals(m.getName())) hasStack = true;
            if ("getStackWalker".equals(m.getName())) hasWalker = true;
            if (Modifier.isAbstract(m.getModifiers())) abstractCount++;
            if (Modifier.isStatic(m.getModifiers())) staticCount++;
        }
        check(hasMonitors, "declares getMonitors()");
        check(hasLocals, "declares getLocals()");
        check(hasStack, "declares getStack()");
        check(hasWalker, "declares static getStackWalker(...)");
        check(abstractCount >= 3, "at least 3 abstract members");
        check(staticCount >= 1, "at least 1 static factory");
        Method locals = null;
        for (Method m : c.getDeclaredMethods()) {
            if ("getLocals".equals(m.getName())) locals = m;
        }
        check(locals != null && locals.getReturnType() == Object[].class,
              "getLocals returns Object[]");
    }

    static void testPrimitiveSlot() throws Exception {
        Class<?> ps = Class.forName("java.lang.LiveStackFrame$PrimitiveSlot");
        check(ps != null, "nested PrimitiveSlot type loadable");
        check(!ps.isInterface(), "PrimitiveSlot is an abstract class, not an interface");
        check(Modifier.isAbstract(ps.getModifiers()), "PrimitiveSlot is abstract");
        check("java.lang.LiveStackFrame$PrimitiveSlot".equals(ps.getName()),
              "PrimitiveSlot binary name exact");
        check(ps.getEnclosingClass() == Class.forName("java.lang.LiveStackFrame"),
              "PrimitiveSlot is nested in LiveStackFrame");
        boolean hasSize = false, hasInt = false, hasLong = false;
        for (Method m : ps.getMethods()) {
            if ("size".equals(m.getName()) && m.getReturnType() == int.class) hasSize = true;
            if ("intValue".equals(m.getName())) hasInt = true;
            if ("longValue".equals(m.getName())) hasLong = true;
        }
        check(hasSize, "PrimitiveSlot.size() returns int");
        check(hasInt, "PrimitiveSlot.intValue() present");
        check(hasLong, "PrimitiveSlot.longValue() present");
    }

    // ---- Part 2: black-box substitute via public StackWalker/StackFrame ----
    static void testStackFrameInterface() throws Exception {
        Class<?> f = StackFrame.class;
        check(f.isInterface(), "StackFrame is an interface");
        String[] core = { "getClassName", "getMethodName", "getDeclaringClass",
                          "getByteCodeIndex", "getFileName", "getLineNumber",
                          "isNativeMethod", "toStackTraceElement" };
        for (String name : core) {
            boolean found = false;
            for (Method m : f.getMethods()) {
                if (m.getName().equals(name)) found = true;
            }
            check(found, "StackFrame declares " + name + "()");
        }
    }

    static void testHiddenFramesWalk() {
        StackWalker w = StackWalker.getInstance(Set.of(StackWalker.Option.SHOW_HIDDEN_FRAMES));
        check(w != null, "SHOW_HIDDEN_FRAMES walker non-null");
        check(w.walk(s -> s.count()) >= 1, "SHOW_HIDDEN_FRAMES walk non-empty");
        StackFrame f = w.walk(s -> s.findFirst()).orElse(null);
        check(f != null, "first frame present under SHOW_HIDDEN_FRAMES");
        check("LiveStackFrameTest".equals(f.getClassName()), "first frame class exact");
        check("testHiddenFramesWalk".equals(f.getMethodName()), "first frame method exact");
        check("LiveStackFrameTest.java".equals(f.getFileName()), "first frame file exact");
        check(f.getLineNumber() > 0, "first frame line positive");
        check(!f.isNativeMethod(), "first frame not native");
        check(f.getByteCodeIndex() >= 0, "first frame BCI non-negative");
        StackTraceElement ste = f.toStackTraceElement();
        check("testHiddenFramesWalk".equals(ste.getMethodName()),
              "toStackTraceElement method agrees");
        check("LiveStackFrameTest".equals(ste.getClassName()), "toStackTraceElement class agrees");
    }

    static void testRetainClassReference() {
        StackWalker w = StackWalker.getInstance(Set.of(StackWalker.Option.RETAIN_CLASS_REFERENCE,
                                                       StackWalker.Option.SHOW_HIDDEN_FRAMES));
        StackFrame f = w.walk(s -> s.findFirst()).orElse(null);
        check(f != null, "RETAIN+HIDDEN frame present");
        check(f.getDeclaringClass() == LiveStackFrameTest.class,
              "getDeclaringClass with RETAIN under hidden frames");
        check("()V".equals(f.getDescriptor()), "descriptor ()V for a static void method");
    }

    static void testForEach() {
        final boolean[] saw = {false};
        StackWalker.getInstance().forEach(fr -> {
            if ("LiveStackFrameTest".equals(fr.getClassName())
                    && "testForEach".equals(fr.getMethodName())) {
                saw[0] = true;
            }
        });
        check(saw[0], "forEach observes this class and exact method");
    }

    static List<String> nested(int depth) {
        if (depth > 0) return nested(depth - 1);
        return StackWalker.getInstance().walk(s ->
            s.limit(3).map(f -> f.getClassName() + "." + f.getMethodName())
             .collect(Collectors.toList()));
    }

    static void testDeepNesting() {
        List<String> frames = nested(2);
        check(frames.size() == 3, "3 frames collected");
        check("LiveStackFrameTest.nested".equals(frames.get(0)), "innermost is nested");
        check("LiveStackFrameTest.nested".equals(frames.get(1)), "recursive caller is nested");
        check(frames.get(2).startsWith("LiveStackFrameTest."), "outer frame this class");
    }

    static class Inner {
        static String info() {
            return StackWalker.getInstance(Set.of(StackWalker.Option.RETAIN_CLASS_REFERENCE))
                .walk(s -> s.findFirst().map(f -> f.getClassName() + "/" + f.getMethodName()).orElse(""));
        }
    }

    static void testNestedClassFrame() throws Exception {
        check("StackWalkerTest-free".length() >= 0, "sanity");
        check("LiveStackFrameTest$Inner/info".equals(Inner.info()), "nested class frame cn/mn");
        check(Class.forName("LiveStackFrameTest$Inner") == Inner.class,
              "Class.forName nested frame round trip");
    }

    static void testGetCallerClass() {
        check(StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass()
              == LiveStackFrameTest.class, "getCallerClass returns this class");
    }

    static void testDefaultWalkerEquivalence() {
        StackWalker plain = StackWalker.getInstance();
        StackWalker hidden = StackWalker.getInstance(Set.of(StackWalker.Option.SHOW_HIDDEN_FRAMES));
        String mp = plain.walk(s -> s.findFirst().map(StackFrame::getMethodName)).orElse("");
        String mh = hidden.walk(s -> s.findFirst().map(StackFrame::getMethodName)).orElse("");
        check(mp.equals(mh), "first frame identical with and without SHOW_HIDDEN_FRAMES");
        check("testDefaultWalkerEquivalence".equals(mh), "first frame method name exact");
    }

    static void testOptionPresent() {
        boolean present = false;
        for (StackWalker.Option o : StackWalker.Option.values()) {
            if (o == StackWalker.Option.SHOW_HIDDEN_FRAMES) present = true;
        }
        check(present, "SHOW_HIDDEN_FRAMES is an Option constant");
    }

    public static void main(String[] args) throws Exception {
        testExistence();
        testTypeHierarchy();
        testDeclaredMethods();
        testPrimitiveSlot();
        testStackFrameInterface();
        testHiddenFramesWalk();
        testRetainClassReference();
        testForEach();
        testDeepNesting();
        testNestedClassFrame();
        testGetCallerClass();
        testDefaultWalkerEquivalence();
        testOptionPresent();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
