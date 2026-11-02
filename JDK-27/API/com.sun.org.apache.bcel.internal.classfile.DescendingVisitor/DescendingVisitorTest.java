/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.classfile.DescendingVisitor conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.bcel.internal.classfile:+open
 * @run main DescendingVisitorTest
 */
public class DescendingVisitorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.classfile.DescendingVisitor", false, DescendingVisitorTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.bcel.internal.classfile.DescendingVisitor"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "interface-count");
        check(ifs.contains("com.sun.org.apache.bcel.internal.classfile.Visitor"), "if-com.sun.org.apache.bcel.internal.classfile.Visitor");
        java.util.Set<String> ec = new java.util.HashSet<>();
        java.util.Set<String> fd = new java.util.HashSet<>();
        java.util.Set<String> md = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (f.isEnumConstant()) ec.add(f.getName());
            else if (java.lang.reflect.Modifier.isPublic(f.getModifiers()) || java.lang.reflect.Modifier.isProtected(f.getModifiers())) fd.add(f.getName());
        }
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (java.lang.reflect.Modifier.isPublic(m.getModifiers()) || java.lang.reflect.Modifier.isProtected(m.getModifiers())) md.add(m.getName());
        }
        check(ec.size() == 0, "enum-constant-count");
        check(fd.size() == 0, "field-count");
        check(md.size() == 63, "method-count");
        check(md.contains("current"), "method-current");
        check(md.contains("predecessor"), "method-predecessor");
        check(md.contains("visit"), "method-visit");
        check(md.contains("visitAnnotation"), "method-visitAnnotation");
        check(md.contains("visitAnnotationDefault"), "method-visitAnnotationDefault");
        check(md.contains("visitAnnotationEntry"), "method-visitAnnotationEntry");
        check(md.contains("visitBootstrapMethods"), "method-visitBootstrapMethods");
        check(md.contains("visitCode"), "method-visitCode");
        check(md.contains("visitCodeException"), "method-visitCodeException");
        check(md.contains("visitConstantClass"), "method-visitConstantClass");
        check(md.contains("visitConstantDouble"), "method-visitConstantDouble");
        check(md.contains("visitConstantDynamic"), "method-visitConstantDynamic");
        check(md.contains("visitConstantFieldref"), "method-visitConstantFieldref");
        check(md.contains("visitConstantFloat"), "method-visitConstantFloat");
        check(md.contains("visitConstantInteger"), "method-visitConstantInteger");
        check(md.contains("visitConstantInterfaceMethodref"), "method-visitConstantInterfaceMethodref");
        check(md.contains("visitConstantInvokeDynamic"), "method-visitConstantInvokeDynamic");
        check(md.contains("visitConstantLong"), "method-visitConstantLong");
        check(md.contains("visitConstantMethodHandle"), "method-visitConstantMethodHandle");
        check(md.contains("visitConstantMethodType"), "method-visitConstantMethodType");
        check(md.contains("visitConstantMethodref"), "method-visitConstantMethodref");
        check(md.contains("visitConstantModule"), "method-visitConstantModule");
        check(md.contains("visitConstantNameAndType"), "method-visitConstantNameAndType");
        check(md.contains("visitConstantPackage"), "method-visitConstantPackage");
        check(md.contains("visitConstantPool"), "method-visitConstantPool");
        check(md.contains("visitConstantString"), "method-visitConstantString");
        check(md.contains("visitConstantUtf8"), "method-visitConstantUtf8");
        check(md.contains("visitConstantValue"), "method-visitConstantValue");
        check(md.contains("visitDeprecated"), "method-visitDeprecated");
        check(md.contains("visitEnclosingMethod"), "method-visitEnclosingMethod");
        check(md.contains("visitExceptionTable"), "method-visitExceptionTable");
        check(md.contains("visitField"), "method-visitField");
        check(md.contains("visitInnerClass"), "method-visitInnerClass");
        check(md.contains("visitInnerClasses"), "method-visitInnerClasses");
        check(md.contains("visitJavaClass"), "method-visitJavaClass");
        check(md.contains("visitLineNumber"), "method-visitLineNumber");
        check(md.contains("visitLineNumberTable"), "method-visitLineNumberTable");
        check(md.contains("visitLocalVariable"), "method-visitLocalVariable");
        check(md.contains("visitLocalVariableTable"), "method-visitLocalVariableTable");
        check(md.contains("visitLocalVariableTypeTable"), "method-visitLocalVariableTypeTable");
        check(md.contains("visitMethod"), "method-visitMethod");
        check(md.contains("visitMethodParameter"), "method-visitMethodParameter");
        check(md.contains("visitMethodParameters"), "method-visitMethodParameters");
        check(md.contains("visitModule"), "method-visitModule");
        check(md.contains("visitModuleExports"), "method-visitModuleExports");
        check(md.contains("visitModuleMainClass"), "method-visitModuleMainClass");
        check(md.contains("visitModuleOpens"), "method-visitModuleOpens");
        check(md.contains("visitModulePackages"), "method-visitModulePackages");
        check(md.contains("visitModuleProvides"), "method-visitModuleProvides");
        check(md.contains("visitModuleRequires"), "method-visitModuleRequires");
        check(md.contains("visitNestHost"), "method-visitNestHost");
        check(md.contains("visitNestMembers"), "method-visitNestMembers");
        check(md.contains("visitParameterAnnotation"), "method-visitParameterAnnotation");
        check(md.contains("visitParameterAnnotationEntry"), "method-visitParameterAnnotationEntry");
        check(md.contains("visitRecord"), "method-visitRecord");
        check(md.contains("visitRecordComponent"), "method-visitRecordComponent");
        check(md.contains("visitSignature"), "method-visitSignature");
        check(md.contains("visitSourceFile"), "method-visitSourceFile");
        check(md.contains("visitStackMap"), "method-visitStackMap");
        check(md.contains("visitStackMapEntry"), "method-visitStackMapEntry");
        check(md.contains("visitStackMapType"), "method-visitStackMapType");
        check(md.contains("visitSynthetic"), "method-visitSynthetic");
        check(md.contains("visitUnknown"), "method-visitUnknown");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
