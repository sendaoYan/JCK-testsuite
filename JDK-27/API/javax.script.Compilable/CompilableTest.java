/*
 * @test
 * @summary javax.script.Compilable conformance: white-box structure + black-box behavior.
 * @run main CompilableTest
 */

import java.lang.reflect.*;
import java.io.*;

public class CompilableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static class MinEngine extends javax.script.AbstractScriptEngine {
        public Object eval(String s, javax.script.ScriptContext ctx) { return "E"; }
        public Object eval(Reader r, javax.script.ScriptContext ctx) { return "E"; }
        public javax.script.Bindings createBindings() { return new javax.script.SimpleBindings(); }
        public javax.script.ScriptEngineFactory getFactory() { return null; }
    }

    static class MinCompiled extends javax.script.CompiledScript {
        final javax.script.ScriptEngine eng = new MinEngine();
        public Object eval(javax.script.ScriptContext ctx) { return "RESULT"; }
        public javax.script.ScriptEngine getEngine() { return eng; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.script.Compilable");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "compile"), "declares compile");
    }

    static void testBlackBox() throws Exception {
        javax.script.Compilable compilable = new javax.script.Compilable() {
            public javax.script.CompiledScript compile(String script) { return new MinCompiled(); }
            public javax.script.CompiledScript compile(Reader script) { return new MinCompiled(); }
        };
        javax.script.CompiledScript cs1 = compilable.compile("var x = 1;");
        javax.script.CompiledScript cs2 = compilable.compile(new StringReader("var x = 2;"));
        check(cs1 != null, "compile(String) non-null");
        check(cs2 != null, "compile(Reader) non-null");
        check(cs1.getEngine() != null, "compiled script engine non-null");
        check("RESULT".equals(cs1.eval()), "compiled script evals");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
