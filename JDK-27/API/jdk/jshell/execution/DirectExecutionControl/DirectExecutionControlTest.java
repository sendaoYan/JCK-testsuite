/*
 * @test
 * @summary jdk.jshell.execution.DirectExecutionControl conformance: direct execution control.
 * @run main/othervm DirectExecutionControlTest
 */

import jdk.jshell.execution.*;
import jdk.jshell.spi.*;
import jdk.jshell.spi.ExecutionControl.*;
import java.util.*;

public class DirectExecutionControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.execution.DirectExecutionControl");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("close")) f=true; check(f, "has close"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("invoke")) f=true; check(f, "has invoke"); }
        // full command surface of the ExecutionControl interface
        for (String n : new String[]{"load", "redefine", "invoke", "varValue", "addToClasspath", "stop", "extensionCommand"})
            { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals(n)) f=true; check(f, "has " + n); }
        // both constructors are public: delegate and default-loading
        java.lang.reflect.Constructor<?>[] ctors = c.getConstructors();
        check(ctors.length == 2, "two public constructors");
    }

    static void testBlackBox() throws Exception {

        DirectExecutionControl ec = new DirectExecutionControl();
        check(ec instanceof ExecutionControl, "implements ExecutionControl");
        ec.close();
        // the default loader delegate only resolves classes delivered over load();
        // any class not loaded that way reports InternalException with the CNFE text
        try {
            ec.invoke("java.lang.String", "toString");
            check(false, "default delegate invoke should throw InternalException");
        } catch (InternalException expected) {
            check(expected.getMessage().contains("java.lang.String not found"),
                  "default delegate invoke -> InternalException(ClassNotFound): " + expected.getMessage());
        }
        try {
            ec.varValue("java.lang.System", "out");
            check(false, "default delegate varValue should throw InternalException");
        } catch (InternalException expected) {
            check(expected.getMessage().contains("not found"), "default delegate varValue -> InternalException");
        }
        // load() with an empty array is a documented no-op success
        ec.load(new ClassBytecodes[0]);
        ec.close();
        ec.close();
        check(true, "close() is an idempotent no-op");
    }

    // ---- deep contract: real invoke/varValue channels through a custom LoaderDelegate ----

    // user-code surface exercised through the engine
    public static int seven() { return 7; }
    public static String quoted() { return "h\"i"; }
    public static char nlChar() { return '\n'; }
    public static int[] iarr() { return new int[]{1, 2, 3}; }
    public static int[][] iarr2() { return new int[][]{{1, 2}, {3}}; }
    public static boolean yes() { return true; }
    public static void nothing() { }
    public static String boom() { throw new IllegalStateException("boom"); }
    public static Object circular() { throw circ0(); }
    static RuntimeException circ0() {
        // a genuinely circular cause chain: a -> b -> a
        RuntimeException a = new RuntimeException("a");
        Exception b = new Exception("b");
        a.initCause(b);
        b.initCause(a);
        return a;
    }
    public static Object unresolved() { throw new SPIResolutionException(42); }
    public static String sv = "str";
    public static long lv = 9L;
    public static int[] av = new int[]{5, 6};

    /** LoaderDelegate that records operations and resolves unknown names on the test classloader. */
    static class RecordingDelegate extends ClassLoader implements LoaderDelegate {
        final List<String> log = new ArrayList<>();
        final Map<String, Class<?>> defined = new HashMap<>();
        RecordingDelegate() { super(DirectExecutionControlTest.class.getClassLoader()); }
        @Override public void load(ClassBytecodes[] cbcs) {
            // a delegate receives the payloads; defining them is its choice
            for (ClassBytecodes c : cbcs) { defined.put(c.name(), null); log.add("bytes:" + c.name() + ":" + c.bytecodes().length); }
            log.add("load:" + cbcs.length);
        }
        @Override public void classesRedefined(ClassBytecodes[] cbcs) { log.add("classesRedefined:" + cbcs.length); }
        @Override public void addToClasspath(String path) { log.add("cp:" + path); }
        @Override public Class<?> findClass(String name) throws ClassNotFoundException {
            if (defined.containsKey(name) && defined.get(name) == null)
                throw new ClassNotFoundException(name + " recorded but not defined");
            Class<?> c = defined.get(name);
            if (c != null) return c;
            return Class.forName(name, false, DirectExecutionControlTest.class.getClassLoader());
        }
    }

    /** Subclass exposing the protected static valueString renderer. */
    static class ExposingControl extends DirectExecutionControl {
        ExposingControl(LoaderDelegate d) { super(d); }
        static String render(Object o) { return valueString(o); }
        Class<?> find(String n) throws ClassNotFoundException { return findClass(n); }
    }

    static void testRealInvokeChannel() throws Exception {
        RecordingDelegate del = new RecordingDelegate();
        DirectExecutionControl ec = new DirectExecutionControl(del);
        String self = DirectExecutionControlTest.class.getName();
        check(ec.invoke(self, "seven").equals("7"), "invoke static int -> 7");
        check(ec.invoke(self, "quoted").equals("\"h\\\"i\""), "invoke String is rendered quoted with escapes");
        check(ec.invoke(self, "nlChar").equals("'\\n'"), "invoke char rendered quoted with escape");
        check(ec.invoke(self, "iarr").equals("int[3] { 1, 2, 3 }"), "invoke int[] rendering");
        check(ec.invoke(self, "iarr2").equals("int[2][] { int[2] { 1, 2 }, int[1] { 3 } }"), "invoke int[][] nested rendering");
        check(ec.invoke(self, "yes").equals("true"), "invoke boolean -> toString");
        check(ec.invoke(self, "nothing").equals("null"), "invoke void method renders the null value as \"null\"");
        check(ec.varValue(self, "sv").equals("\"str\""), "varValue String field rendered quoted");
        check(ec.varValue(self, "lv").equals("9"), "varValue long field has no suffix");
        check(ec.varValue(self, "av").equals("int[2] { 5, 6 }"), "varValue array field rendering");
        // unknown member/class -> InternalException carrying the raw cause text
        try {
            ec.invoke(self, "nope");
            check(false, "unknown method must throw InternalException");
        } catch (InternalException expected) {
            check(expected.getMessage().contains("NoSuchMethodException"), "unknown method -> InternalException(NoSuchMethodException)");
        }
        try {
            ec.varValue(self, "noField");
            check(false, "unknown field must throw InternalException");
        } catch (InternalException expected) {
            check(expected.getMessage().contains("NoSuchFieldException"), "unknown field -> InternalException(NoSuchFieldException)");
        }
        try {
            ec.invoke("no.such.Cls", "m");
            check(false, "unknown class must throw InternalException");
        } catch (InternalException expected) {
            check(expected.getMessage().contains("ClassNotFoundException"), "unknown class -> InternalException(ClassNotFoundException)");
        }
        // delegate operations
        ec.load(new ClassBytecodes[]{ new ClassBytecodes("Fake.Gen", new byte[]{0, 1, 2}) });
        check(del.defined.containsKey("Fake.Gen"), "load() hands the bytecodes to the delegate");
        check(del.log.contains("bytes:Fake.Gen:3"), "delegate observes name and byte count");
        ec.addToClasspath("/tmp/jck-dec-cp");
        check(del.log.contains("cp:/tmp/jck-dec-cp"), "addToClasspath() forwarded to delegate");
        ec.close();
    }

    static void testExceptionConversion() throws Exception {
        DirectExecutionControl ec = new DirectExecutionControl(new RecordingDelegate());
        String self = DirectExecutionControlTest.class.getName();
        // plain user exception -> UserException preserving class name, message and stack
        try {
            ec.invoke(self, "boom");
            check(false, "user exception must surface as UserException");
        } catch (UserException expected) {
            check(expected.getMessage().equals("boom"), "UserException message preserved");
            check(expected.causeExceptionClass().equals("java.lang.IllegalStateException"), "UserException cause class FQN preserved");
            check(expected.getStackTrace().length > 0, "UserException carries the user stack trace");
        }
        // circular cause chain is cut with a sentinel message
        try {
            ec.invoke(self, "circular");
            check(false, "circular chain must surface as UserException");
        } catch (UserException expected) {
            List<String> msgs = new ArrayList<>();
            for (Throwable t = expected; t != null; t = t.getCause())
                msgs.add(t.getClass().getSimpleName() + ":" + t.getMessage());
            check(msgs.size() == 3, "circular chain cut after the first repeat: " + msgs);
            check(msgs.get(2).contains("CIRCULAR REFERENCE!"), "circular sentinel message: " + msgs.get(2));
        }
        // SPIResolutionException from user code -> ResolutionException with the id
        try {
            ec.invoke(self, "unresolved");
            check(false, "SPIResolutionException must surface as ResolutionException");
        } catch (ResolutionException expected) {
            check(expected.id() == 42, "ResolutionException id carried over");
            check(expected.getMessage().equals("resolution exception: 42"), "ResolutionException message format");
            check(expected.getStackTrace().length > 0, "ResolutionException carries stack trace");
        }
    }

    static void testUnsupportedOperations() throws Exception {
        DirectExecutionControl ec = new DirectExecutionControl(new RecordingDelegate());
        try {
            ec.redefine(new ClassBytecodes[0]);
            check(false, "redefine must throw NotImplementedException");
        } catch (NotImplementedException expected) {
            check(expected.getMessage().equals("redefine not supported"), "redefine message exact");
        }
        try {
            ec.stop();
            check(false, "stop must throw NotImplementedException");
        } catch (NotImplementedException expected) {
            check(expected.getMessage().equals("stop: Not supported."), "stop message exact");
        }
        try {
            ec.extensionCommand("mycmd", "arg");
            check(false, "extensionCommand must throw NotImplementedException");
        } catch (NotImplementedException expected) {
            check(expected.getMessage().equals("Unknown command: mycmd"), "extensionCommand message names the command");
        }
        ec.close();
    }

    static void testValueStringRenderer() {
        check(ExposingControl.render(null).equals("null"), "null -> null");
        check(ExposingControl.render(5L).equals("5"), "long has no suffix");
        check(ExposingControl.render("a\"b").equals("\"a\\\"b\""), "String quote escaped");
        check(ExposingControl.render('\'').equals("'\\''"), "char single-quote escaped");
        check(ExposingControl.render('\t').equals("'\\t'"), "char tab escape");
        check(ExposingControl.render('\\').equals("'\\\\'"), "char backslash escape");
        check(ExposingControl.render('x').equals("'x'"), "plain char quoted");
        check(ExposingControl.render(new int[0]).equals("int[0] {  }"), "empty array rendering");
        check(ExposingControl.render(new String[]{"x"}).equals("String[1] { \"x\" }"), "String[] rendering");
        check(ExposingControl.render(true).equals("true"), "non-special types use toString");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testRealInvokeChannel();
        testExceptionConversion();
        testUnsupportedOperations();
        testValueStringRenderer();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
