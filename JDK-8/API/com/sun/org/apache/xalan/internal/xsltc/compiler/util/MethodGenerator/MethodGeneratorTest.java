/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator conformance: reflection.
 * @run main MethodGeneratorTest
 */

public class MethodGeneratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.compiler.util.MethodGenerator", false, MethodGeneratorTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("MethodGenerator"), "simple name MethodGenerator");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.bcel.internal.generic.MethodGen"), "superclass com.sun.org.apache.bcel.internal.generic.MethodGen");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.xalan.internal.xsltc.compiler.Constants"), "interface com.sun.org.apache.xalan.internal.xsltc.compiler.Constants");
        check(has(c, "addLocalVariable"), "method addLocalVariable");
        check(has(c, "addLocalVariable2"), "method addLocalVariable2");
        check(has(c, "removeLocalVariable"), "method removeLocalVariable");
        check(has(c, "loadDOM"), "method loadDOM");
        check(has(c, "storeDOM"), "method storeDOM");
        check(has(c, "storeHandler"), "method storeHandler");
        check(has(c, "loadHandler"), "method loadHandler");
        check(has(c, "storeIterator"), "method storeIterator");
        check(has(c, "loadIterator"), "method loadIterator");
        check(has(c, "setStartNode"), "method setStartNode");
        check(has(c, "reset"), "method reset");
        check(has(c, "nextNode"), "method nextNode");
        check(has(c, "startElement"), "method startElement");
        check(has(c, "endElement"), "method endElement");
        check(has(c, "startDocument"), "method startDocument");
        check(has(c, "endDocument"), "method endDocument");
        check(has(c, "attribute"), "method attribute");
        check(has(c, "uniqueAttribute"), "method uniqueAttribute");
        check(has(c, "namespace"), "method namespace");
        check(has(c, "loadCurrentNode"), "method loadCurrentNode");
        check(has(c, "storeCurrentNode"), "method storeCurrentNode");
        check(has(c, "loadContextNode"), "method loadContextNode");
        check(has(c, "storeContextNode"), "method storeContextNode");
        check(has(c, "getLocalIndex"), "method getLocalIndex");
        check(has(c, "getLocalVariable"), "method getLocalVariable");
        check(has(c, "setMaxLocals"), "method setMaxLocals");
        check(has(c, "addInstructionList"), "method addInstructionList");
        check(has(c, "getInstructionList"), "method getInstructionList");
        check(has(c, "outlineChunks"), "method outlineChunks");
        check(has(c, "markChunkStart"), "method markChunkStart");
        check(has(c, "markChunkEnd"), "method markChunkEnd");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
