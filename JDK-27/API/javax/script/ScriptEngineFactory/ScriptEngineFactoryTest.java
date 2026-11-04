/*
 * @test
 * @summary javax.script.ScriptEngineFactory conformance: white-box structure + black-box behavior.
 * @run main ScriptEngineFactoryTest
 */

import java.lang.reflect.*;
import java.io.*;
import java.util.*;

public class ScriptEngineFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.script.ScriptEngineFactory");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getEngineName"), "declares getEngineName");
        check(hasMethod(c, "getEngineVersion"), "declares getEngineVersion");
        check(hasMethod(c, "getExtensions"), "declares getExtensions");
        check(hasMethod(c, "getMimeTypes"), "declares getMimeTypes");
        check(hasMethod(c, "getNames"), "declares getNames");
    }

    static void testBlackBox() throws Exception {
        javax.script.ScriptEngineFactory f = new javax.script.ScriptEngineFactory() {
            public String getEngineName() { return "MyEngine"; }
            public String getEngineVersion() { return "1.0"; }
            public List<String> getExtensions() { return List.of("me", "mye"); }
            public List<String> getMimeTypes() { return List.of("application/x-myengine"); }
            public List<String> getNames() { return List.of("MyEngine", "myengine"); }
            public String getLanguageName() { return "MyLang"; }
            public String getLanguageVersion() { return "2.0"; }
            public Object getParameter(String key) {
                if (javax.script.ScriptEngine.ENGINE.equals(key)) return getEngineName();
                if (javax.script.ScriptEngine.ENGINE_VERSION.equals(key)) return getEngineVersion();
                if (javax.script.ScriptEngine.LANGUAGE.equals(key)) return getLanguageName();
                if (javax.script.ScriptEngine.LANGUAGE_VERSION.equals(key)) return getLanguageVersion();
                if (javax.script.ScriptEngine.NAME.equals(key)) return getNames().get(0);
                return null;
            }
            public String getMethodCallSyntax(String obj, String m, String... args) {
                return obj + "." + m + "(" + String.join(",", args) + ")";
            }
            public String getOutputStatement(String toDisplay) { return "print(" + toDisplay + ")"; }
            public String getProgram(String... statements) { return String.join(";\n", statements); }
            public javax.script.ScriptEngine getScriptEngine() { return new javax.script.AbstractScriptEngine() {
                public Object eval(String s, javax.script.ScriptContext ctx) { return "E"; }
                public Object eval(Reader r, javax.script.ScriptContext ctx) { return "E"; }
                public javax.script.Bindings createBindings() { return new javax.script.SimpleBindings(); }
                public javax.script.ScriptEngineFactory getFactory() { return null; }
            }; }
        };

        check("MyEngine".equals(f.getEngineName()), "getEngineName");
        check("1.0".equals(f.getEngineVersion()), "getEngineVersion");
        check(f.getExtensions().size() == 2 && "me".equals(f.getExtensions().get(0)), "getExtensions");
        check(f.getMimeTypes().size() == 1, "getMimeTypes");
        check(f.getNames().size() == 2 && "MyEngine".equals(f.getNames().get(0)), "getNames");
        check("MyLang".equals(f.getLanguageName()), "getLanguageName");
        check("2.0".equals(f.getLanguageVersion()), "getLanguageVersion");

        check("MyEngine".equals(f.getParameter(javax.script.ScriptEngine.ENGINE)), "getParameter ENGINE");
        check("1.0".equals(f.getParameter(javax.script.ScriptEngine.ENGINE_VERSION)), "getParameter ENGINE_VERSION");
        check("MyLang".equals(f.getParameter(javax.script.ScriptEngine.LANGUAGE)), "getParameter LANGUAGE");
        check("2.0".equals(f.getParameter(javax.script.ScriptEngine.LANGUAGE_VERSION)), "getParameter LANGUAGE_VERSION");
        check("MyEngine".equals(f.getParameter(javax.script.ScriptEngine.NAME)), "getParameter NAME");
        check(f.getParameter("unknown.key") == null, "getParameter unknown null");

        check("obj.method(a,b)".equals(f.getMethodCallSyntax("obj", "method", "a", "b")), "getMethodCallSyntax");
        check("print(x)".equals(f.getOutputStatement("x")), "getOutputStatement");
        check("a;\nb".equals(f.getProgram("a", "b")), "getProgram");
        check(f.getScriptEngine() != null, "getScriptEngine non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
