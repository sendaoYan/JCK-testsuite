/*
 * @test
 * @summary sun.jvm.hotspot.debugger.cdbg.TypeVisitor conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.cdbg:+open
 * @run main TypeVisitorTest
 */
import java.lang.reflect.*;

public class TypeVisitorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasField(Class<?> c, String n) {
        for (Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.cdbg.TypeVisitor", false, TypeVisitorTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "doBitType"), "method doBitType");
        check(hasMethod(c, "doIntType"), "method doIntType");
        check(hasMethod(c, "doEnumType"), "method doEnumType");
        check(hasMethod(c, "doFloatType"), "method doFloatType");
        check(hasMethod(c, "doDoubleType"), "method doDoubleType");
        check(hasMethod(c, "doPointerType"), "method doPointerType");
        check(hasMethod(c, "doArrayType"), "method doArrayType");
        check(hasMethod(c, "doRefType"), "method doRefType");
        check(hasMethod(c, "doCompoundType"), "method doCompoundType");
        check(hasMethod(c, "doFunctionType"), "method doFunctionType");
        check(hasMethod(c, "doMemberFunctionType"), "method doMemberFunctionType");
        check(hasMethod(c, "doVoidType"), "method doVoidType");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
