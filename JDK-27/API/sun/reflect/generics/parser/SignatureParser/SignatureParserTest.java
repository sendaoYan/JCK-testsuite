/*
 * @test
 * @summary sun.reflect.generics.parser.SignatureParser conformance: internal package reflection.
 * @modules java.base/sun.reflect.generics.parser:+open
 * @run main SignatureParserTest
 */
public class SignatureParserTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.generics.parser.SignatureParser", false, SignatureParserTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "make"), "method make");
        check(hasMethod(c, "parseClassSig"), "method parseClassSig");
        check(hasMethod(c, "parseMethodSig"), "method parseMethodSig");
        check(hasMethod(c, "parseTypeSig"), "method parseTypeSig");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "DEBUG"), "field DEBUG");
        check(hasField(c, "EOI"), "field EOI");
        check(hasField(c, "index"), "field index");
        check(hasField(c, "input"), "field input");
        check(hasField(c, "mark"), "field mark");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
