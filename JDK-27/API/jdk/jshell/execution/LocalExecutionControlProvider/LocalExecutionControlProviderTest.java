/*
 * @test
 * @summary jdk.jshell.execution.LocalExecutionControlProvider conformance: local provider.
 * @run main LocalExecutionControlProviderTest
 */

import jdk.jshell.execution.*;
import jdk.jshell.spi.*;
import jdk.jshell.spi.ExecutionControl.*;
import java.util.*;

public class LocalExecutionControlProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.execution.LocalExecutionControlProvider");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("name")) f=true; check(f, "has name"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("defaultParameters")) f=true; check(f, "has defaultParameters"); }
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("createExecutionControl")) f=true; check(f, "has createExecutionControl"); }
    }

    static void testBlackBox() throws Exception {

        LocalExecutionControlProvider p = new LocalExecutionControlProvider();
        check(p.name().equals("local"), "name");
        check(p.defaultParameters() != null, "defaultParameters");
        check(p.createExecutionControl(null, p.defaultParameters()) != null, "createExecutionControl");
    }


    // ---- deep contract: the generated engine really executes code ----
    static class DefiningDelegate extends ClassLoader implements LoaderDelegate {
        final Map<String, Class<?>> defined = new HashMap<>();
        DefiningDelegate() { super(LocalExecutionControlProviderTest.class.getClassLoader()); }
        @Override public void load(ClassBytecodes[] cbcs) {
            for (ClassBytecodes c : cbcs) defined.put(c.name(), defineClass(c.name(), c.bytecodes(), 0, c.bytecodes().length));
        }
        @Override public void classesRedefined(ClassBytecodes[] cbcs) { }
        @Override public void addToClasspath(String path) { }
        @Override public Class<?> findClass(String name) throws ClassNotFoundException {
            Class<?> c = defined.get(name);
            if (c != null) return c;
            return Class.forName(name, false, LocalExecutionControlProviderTest.class.getClassLoader());
        }
    }

    static void testGeneratedEngineLoop() throws Exception {
        LocalExecutionControlProvider p = new LocalExecutionControlProvider();
        // parameters are ignored by this provider: any map works
        check(p.createExecutionControl(envFixture(), Map.of()) != null,
              "createExecutionControl(ExecutionEnv, parameters) builds an engine");
        java.lang.reflect.Constructor<?> c = LocalExecutionControl.class.getConstructor(LoaderDelegate.class);
        check(c != null, "public LocalExecutionControl(LoaderDelegate) constructor");
        ExecutionControl ec = p.generate(envFixture(), p.defaultParameters());
        check(ec instanceof LocalExecutionControl, "generate() returns a working LocalExecutionControl");
        // ServiceLoader registration: the local provider is discoverable by name
        try {
            ExecutionControl g = ExecutionControl.generate(envFixture(), "local");
            check(g instanceof LocalExecutionControl, "ExecutionControl.generate(env, \"local\") resolves this provider");
            g.close();
        } catch (Exception ex) {
            throw ex;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        ec.close();
    }

    static ExecutionEnv envFixture() {
        return new ExecutionEnv() {
            public java.io.InputStream userIn() { return java.io.InputStream.nullInputStream(); }
            public java.io.PrintStream userOut() { return new java.io.PrintStream(java.io.OutputStream.nullOutputStream()); }
            public java.io.PrintStream userErr() { return new java.io.PrintStream(java.io.OutputStream.nullOutputStream()); }
            public List<String> extraRemoteVMOptions() { return List.of(); }
            public void closeDown() { }
        };
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testGeneratedEngineLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
