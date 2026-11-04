/*
 * @test
 * @summary sun.reflect.generics.visitor.TypeTreeVisitor conformance: internal package reflection.
 * @modules java.base/sun.reflect.generics.visitor:+open
 * @run main TypeTreeVisitorTest
 */
public class TypeTreeVisitorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.reflect.generics.visitor.TypeTreeVisitor", false, TypeTreeVisitorTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "superclass null");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getResult"), "method getResult");
        check(hasMethod(c, "visitArrayTypeSignature"), "method visitArrayTypeSignature");
        check(hasMethod(c, "visitBooleanSignature"), "method visitBooleanSignature");
        check(hasMethod(c, "visitBottomSignature"), "method visitBottomSignature");
        check(hasMethod(c, "visitByteSignature"), "method visitByteSignature");
        check(hasMethod(c, "visitCharSignature"), "method visitCharSignature");
        check(hasMethod(c, "visitClassTypeSignature"), "method visitClassTypeSignature");
        check(hasMethod(c, "visitDoubleSignature"), "method visitDoubleSignature");
        check(hasMethod(c, "visitFloatSignature"), "method visitFloatSignature");
        check(hasMethod(c, "visitFormalTypeParameter"), "method visitFormalTypeParameter");
        check(hasMethod(c, "visitIntSignature"), "method visitIntSignature");
        check(hasMethod(c, "visitLongSignature"), "method visitLongSignature");
        check(hasMethod(c, "visitShortSignature"), "method visitShortSignature");
        check(hasMethod(c, "visitSimpleClassTypeSignature"), "method visitSimpleClassTypeSignature");
        check(hasMethod(c, "visitTypeVariableSignature"), "method visitTypeVariableSignature");
        check(hasMethod(c, "visitVoidDescriptor"), "method visitVoidDescriptor");
        check(hasMethod(c, "visitWildcard"), "method visitWildcard");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
