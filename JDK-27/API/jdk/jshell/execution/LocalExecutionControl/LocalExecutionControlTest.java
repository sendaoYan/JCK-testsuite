/*
 * @test
 * @summary jdk.jshell.execution.LocalExecutionControl conformance: local execution control.
 * @run main/othervm LocalExecutionControlTest
 */

import jdk.jshell.execution.*;
import jdk.jshell.spi.*;
import jdk.jshell.spi.ExecutionControl.*;
import java.util.*;

public class LocalExecutionControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.execution.LocalExecutionControl");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("close")) f=true; check(f, "has close"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("invoke")) f=true; check(f, "has invoke"); }
        check(c.getSuperclass().getName().equals("jdk.jshell.execution.DirectExecutionControl"),
              "extends DirectExecutionControl");
        // three public constructors: default, delegate and parent-classloader
        check(c.getConstructors().length == 3, "three public constructors");
    }

    static void testBlackBox() throws Exception {

        LocalExecutionControl ec = new LocalExecutionControl();
        check(ec instanceof ExecutionControl, "implements ExecutionControl");
        ec.close();
    }

    // ---- deep contract: local in-thread execution, stop instrumentation, redefine/stop semantics ----

    public static String execGroup = "unset";
    public static int seven() {
        // the local engine runs user code on a dedicated thread group
        execGroup = Thread.currentThread().getThreadGroup().getName();
        return 7;
    }

    static class DefiningDelegate extends ClassLoader implements LoaderDelegate {
        final Map<String, Class<?>> defined = new HashMap<>();
        DefiningDelegate() { super(LocalExecutionControlTest.class.getClassLoader()); }
        @Override public void load(ClassBytecodes[] cbcs) {
            for (ClassBytecodes c : cbcs) defined.put(c.name(), defineClass(c.name(), c.bytecodes(), 0, c.bytecodes().length));
        }
        @Override public void classesRedefined(ClassBytecodes[] cbcs) { }
        @Override public void addToClasspath(String path) { }
        @Override public Class<?> findClass(String name) throws ClassNotFoundException {
            Class<?> c = defined.get(name);
            if (c != null) return c;
            return Class.forName(name, false, LocalExecutionControlTest.class.getClassLoader());
        }
    }

    static void testLocalInvokeLoop() throws Exception {
        DefiningDelegate del = new DefiningDelegate();
        LocalExecutionControl ec = new LocalExecutionControl((LoaderDelegate) del);
        String self = LocalExecutionControlTest.class.getName();
        check(ec.invoke(self, "seven").equals("7"), "local invoke runs real user code");
        check(execGroup.equals("JShell process local execution"), "user code runs in the local engine thread group: " + execGroup);
        // the engine transparently generates and defines its REPL.$Cancel$ stop-check class
        check(del.defined.containsKey("REPL.$Cancel$"), "REPL.$Cancel$ stop-instrumentation class auto-defined");
        // varValue works over the same delegate
        check(ec.varValue(self, "execGroup").equals("\"" + execGroup + "\""), "local varValue renders the field");
        ec.close();
    }

    static void testLocalSemantics() throws Exception {
        LocalExecutionControl ec = new LocalExecutionControl((LoaderDelegate) new DefiningDelegate());
        // stop() with nothing running is a harmless no-op for the local engine
        // (DirectExecutionControl would throw NotImplementedException here)
        ec.stop();
        check(true, "local stop() does not throw");
        // redefine is still unsupported, inherited from DirectExecutionControl
        try {
            ec.redefine(new ClassBytecodes[0]);
            check(false, "local redefine must throw NotImplementedException");
        } catch (NotImplementedException expected) {
            check(expected.getMessage().equals("redefine not supported"), "local redefine message exact");
        }
        // load() instruments the delivered bytecodes (stop-check weaving) before defining;
        // a byte array that is not a class file must be rejected
        try {
            ec.load(new ClassBytecodes[]{ new ClassBytecodes("Broken.NotAClass", new byte[]{1, 2, 3}) });
            check(false, "load() of malformed bytecode must fail");
        } catch (IllegalArgumentException expected) {
            check(expected.getMessage().contains("Bad magic number"),
                  "malformed bytecode rejected by the ClassFile parser: " + expected.getMessage());
        }
        ec.close();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLocalInvokeLoop();
        testLocalSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
