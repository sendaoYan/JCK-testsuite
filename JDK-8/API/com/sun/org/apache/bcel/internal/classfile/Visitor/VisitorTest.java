/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.classfile.Visitor conformance: reflection.
 * @run main VisitorTest
 */

public class VisitorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.classfile.Visitor", false, VisitorTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("Visitor"), "simple name Visitor");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "visitCode"), "method visitCode");
        check(has(c, "visitCodeException"), "method visitCodeException");
        check(has(c, "visitConstantClass"), "method visitConstantClass");
        check(has(c, "visitConstantDouble"), "method visitConstantDouble");
        check(has(c, "visitConstantFieldref"), "method visitConstantFieldref");
        check(has(c, "visitConstantFloat"), "method visitConstantFloat");
        check(has(c, "visitConstantInteger"), "method visitConstantInteger");
        check(has(c, "visitConstantInterfaceMethodref"), "method visitConstantInterfaceMethodref");
        check(has(c, "visitConstantLong"), "method visitConstantLong");
        check(has(c, "visitConstantMethodref"), "method visitConstantMethodref");
        check(has(c, "visitConstantNameAndType"), "method visitConstantNameAndType");
        check(has(c, "visitConstantPool"), "method visitConstantPool");
        check(has(c, "visitConstantString"), "method visitConstantString");
        check(has(c, "visitConstantUtf8"), "method visitConstantUtf8");
        check(has(c, "visitConstantValue"), "method visitConstantValue");
        check(has(c, "visitDeprecated"), "method visitDeprecated");
        check(has(c, "visitExceptionTable"), "method visitExceptionTable");
        check(has(c, "visitField"), "method visitField");
        check(has(c, "visitInnerClass"), "method visitInnerClass");
        check(has(c, "visitInnerClasses"), "method visitInnerClasses");
        check(has(c, "visitJavaClass"), "method visitJavaClass");
        check(has(c, "visitLineNumber"), "method visitLineNumber");
        check(has(c, "visitLineNumberTable"), "method visitLineNumberTable");
        check(has(c, "visitLocalVariable"), "method visitLocalVariable");
        check(has(c, "visitLocalVariableTable"), "method visitLocalVariableTable");
        check(has(c, "visitLocalVariableTypeTable"), "method visitLocalVariableTypeTable");
        check(has(c, "visitMethod"), "method visitMethod");
        check(has(c, "visitSignature"), "method visitSignature");
        check(has(c, "visitSourceFile"), "method visitSourceFile");
        check(has(c, "visitSynthetic"), "method visitSynthetic");
        check(has(c, "visitUnknown"), "method visitUnknown");
        check(has(c, "visitStackMap"), "method visitStackMap");
        check(has(c, "visitStackMapEntry"), "method visitStackMapEntry");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
