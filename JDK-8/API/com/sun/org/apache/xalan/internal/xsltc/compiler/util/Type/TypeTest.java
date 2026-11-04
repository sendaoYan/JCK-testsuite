/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type conformance: reflection.
 * @run main TypeTest
 */

public class TypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type", false, TypeTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("Type"), "simple name Type");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.xalan.internal.xsltc.compiler.Constants"), "interface com.sun.org.apache.xalan.internal.xsltc.compiler.Constants");
        check(has(c, "newObjectType"), "method newObjectType");
        check(has(c, "toString"), "method toString");
        check(has(c, "identicalTo"), "method identicalTo");
        check(has(c, "isNumber"), "method isNumber");
        check(has(c, "implementedAsMethod"), "method implementedAsMethod");
        check(has(c, "isSimple"), "method isSimple");
        check(has(c, "toJCType"), "method toJCType");
        check(has(c, "distanceTo"), "method distanceTo");
        check(has(c, "toSignature"), "method toSignature");
        check(has(c, "translateTo"), "method translateTo");
        check(has(c, "translateToDesynthesized"), "method translateToDesynthesized");
        check(has(c, "translateFrom"), "method translateFrom");
        check(has(c, "translateBox"), "method translateBox");
        check(has(c, "translateUnBox"), "method translateUnBox");
        check(has(c, "getClassName"), "method getClassName");
        check(has(c, "ADD"), "method ADD");
        check(has(c, "SUB"), "method SUB");
        check(has(c, "MUL"), "method MUL");
        check(has(c, "DIV"), "method DIV");
        check(has(c, "REM"), "method REM");
        check(has(c, "NEG"), "method NEG");
        check(has(c, "LOAD"), "method LOAD");
        check(has(c, "STORE"), "method STORE");
        check(has(c, "POP"), "method POP");
        check(has(c, "GT"), "method GT");
        check(has(c, "GE"), "method GE");
        check(has(c, "LT"), "method LT");
        check(has(c, "LE"), "method LE");
        check(has(c, "CMP"), "method CMP");
        check(has(c, "DUP"), "method DUP");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
