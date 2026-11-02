/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.classfile.DescendingVisitor conformance: reflection.
 * @run main DescendingVisitorTest
 */

public class DescendingVisitorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.classfile.DescendingVisitor", false, DescendingVisitorTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("DescendingVisitor"), "simple name DescendingVisitor");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.classfile.Visitor"), "interface com.sun.org.apache.bcel.internal.classfile.Visitor");
        check(has(c, "predecessor"), "method predecessor");
        check(has(c, "current"), "method current");
        check(has(c, "visit"), "method visit");
        check(has(c, "visitJavaClass"), "method visitJavaClass");
        check(has(c, "visitField"), "method visitField");
        check(has(c, "visitConstantValue"), "method visitConstantValue");
        check(has(c, "visitMethod"), "method visitMethod");
        check(has(c, "visitExceptionTable"), "method visitExceptionTable");
        check(has(c, "visitCode"), "method visitCode");
        check(has(c, "visitCodeException"), "method visitCodeException");
        check(has(c, "visitLineNumberTable"), "method visitLineNumberTable");
        check(has(c, "visitLineNumber"), "method visitLineNumber");
        check(has(c, "visitLocalVariableTable"), "method visitLocalVariableTable");
        check(has(c, "visitLocalVariableTypeTable"), "method visitLocalVariableTypeTable");
        check(has(c, "visitStackMap"), "method visitStackMap");
        check(has(c, "visitStackMapEntry"), "method visitStackMapEntry");
        check(has(c, "visitLocalVariable"), "method visitLocalVariable");
        check(has(c, "visitConstantPool"), "method visitConstantPool");
        check(has(c, "visitConstantClass"), "method visitConstantClass");
        check(has(c, "visitConstantDouble"), "method visitConstantDouble");
        check(has(c, "visitConstantFieldref"), "method visitConstantFieldref");
        check(has(c, "visitConstantFloat"), "method visitConstantFloat");
        check(has(c, "visitConstantInteger"), "method visitConstantInteger");
        check(has(c, "visitConstantInterfaceMethodref"), "method visitConstantInterfaceMethodref");
        check(has(c, "visitConstantLong"), "method visitConstantLong");
        check(has(c, "visitConstantMethodref"), "method visitConstantMethodref");
        check(has(c, "visitConstantNameAndType"), "method visitConstantNameAndType");
        check(has(c, "visitConstantString"), "method visitConstantString");
        check(has(c, "visitConstantUtf8"), "method visitConstantUtf8");
        check(has(c, "visitInnerClasses"), "method visitInnerClasses");
        check(has(c, "visitInnerClass"), "method visitInnerClass");
        check(has(c, "visitDeprecated"), "method visitDeprecated");
        check(has(c, "visitSignature"), "method visitSignature");
        check(has(c, "visitSourceFile"), "method visitSourceFile");
        check(has(c, "visitSynthetic"), "method visitSynthetic");
        check(has(c, "visitUnknown"), "method visitUnknown");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
