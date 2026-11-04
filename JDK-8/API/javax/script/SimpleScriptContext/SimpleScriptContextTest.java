/*
 * @test
 * @summary javax.script.SimpleScriptContext conformance: simple script context.
 * @run main SimpleScriptContextTest
 */

public class SimpleScriptContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        javax.script.SimpleScriptContext c = new javax.script.SimpleScriptContext();
        check(c.getScopes() != null, "getScopes");
        check(c.getScopes().size() == 2, "two scopes");
        check(c.getWriter() != null, "writer");
        check(c.getErrorWriter() != null, "errorWriter");
        check(c.getReader() != null, "reader");
        java.io.StringWriter sw = new java.io.StringWriter();
        c.setWriter(sw);
        check(c.getWriter() == sw, "setWriter");
        java.io.StringReader sr = new java.io.StringReader("x");
        c.setReader(sr);
        check(c.getReader() == sr, "setReader");
        c.setAttribute("n", "val", javax.script.ScriptContext.ENGINE_SCOPE);
        check("val".equals(c.getAttribute("n")), "engine getAttribute");
        check(c.getAttributesScope("n") == javax.script.ScriptContext.ENGINE_SCOPE, "getAttributesScope");
        check("val".equals(c.removeAttribute("n", javax.script.ScriptContext.ENGINE_SCOPE)), "removeAttribute");
        c.setBindings(new javax.script.SimpleBindings(), javax.script.ScriptContext.GLOBAL_SCOPE);
        c.setAttribute("g", "gv", javax.script.ScriptContext.GLOBAL_SCOPE);
        check("gv".equals(c.getAttribute("g", javax.script.ScriptContext.GLOBAL_SCOPE)), "global attribute");
        check(c.getBindings(javax.script.ScriptContext.ENGINE_SCOPE) != null, "engine bindings");
        c.setBindings(new javax.script.SimpleBindings(), javax.script.ScriptContext.ENGINE_SCOPE);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
