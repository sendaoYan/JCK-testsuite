/*
 * @test
 * @summary jdk.jshell.execution.RemoteExecutionControl conformance: remote execution control.
 * @run main/othervm RemoteExecutionControlTest
 */

import jdk.jshell.execution.*;
import jdk.jshell.spi.*;
import jdk.jshell.spi.ExecutionControl.*;
import java.util.*;

public class RemoteExecutionControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.execution.RemoteExecutionControl");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("close")) f=true; check(f, "has close"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("invoke")) f=true; check(f, "has invoke"); }
        check(c.getSuperclass().getName().equals("jdk.jshell.execution.DirectExecutionControl"),
              "extends DirectExecutionControl");
        // the remote agent main() entry point exists
        { boolean f=false; for (java.lang.reflect.Method mm : c.getDeclaredMethods()) if (mm.getName().equals("main")) f=true; check(f, "declares main(String[]) agent entry"); }
    }

    static void testBlackBox() throws Exception {

        RemoteExecutionControl ec = new RemoteExecutionControl();
        check(ec instanceof ExecutionControl, "implements ExecutionControl");
        ec.close();
    }

    // ---- deep contract: redefine routes to the delegate, stop is JDI-handled, real invoke ----

    public static int rseven() { return 7; }

    static class RecordingDelegate extends ClassLoader implements LoaderDelegate {
        final List<String> log = new ArrayList<>();
        final Map<String, Class<?>> defined = new HashMap<>();
        RecordingDelegate() { super(RemoteExecutionControlTest.class.getClassLoader()); }
        @Override public void load(ClassBytecodes[] cbcs) {
            for (ClassBytecodes c : cbcs) defined.put(c.name(), defineClass(c.name(), c.bytecodes(), 0, c.bytecodes().length));
            log.add("load:" + cbcs.length);
        }
        @Override public void classesRedefined(ClassBytecodes[] cbcs) { log.add("classesRedefined:" + cbcs.length); }
        @Override public void addToClasspath(String path) { log.add("cp:" + path); }
        @Override public Class<?> findClass(String name) throws ClassNotFoundException {
            Class<?> c = defined.get(name);
            if (c != null) return c;
            return Class.forName(name, false, RemoteExecutionControlTest.class.getClassLoader());
        }
    }

    static void testRemoteSemantics() throws Exception {
        RecordingDelegate del = new RecordingDelegate();
        RemoteExecutionControl ec = new RemoteExecutionControl((LoaderDelegate) del);
        // unlike DirectExecutionControl, the remote agent forwards redefine to classesRedefined
        ec.redefine(new ClassBytecodes[]{ new ClassBytecodes("G1", new byte[]{1}) });
        check(del.log.contains("classesRedefined:1"), "redefine() routes to LoaderDelegate.classesRedefined");
        // stop() is handled out-of-band through JDI on the remote side: no exception
        ec.stop();
        check(true, "remote stop() does not throw (JDI-handled)");
        // real invoke over the delegate
        check(ec.invoke(RemoteExecutionControlTest.class.getName(), "rseven").equals("7"), "remote invoke runs user code");
        ec.close();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testRemoteSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
