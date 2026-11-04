/*
 * @test
 * @summary sun.jvm.hotspot.tools.jcore.ClassWriter conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.tools.jcore:+open
 * @run main ClassWriterTest
 */
import java.lang.reflect.*;

public class ClassWriterTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.tools.jcore.ClassWriter", false, ClassWriterTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.runtime.ClassConstants"), "interface sun.jvm.hotspot.runtime.ClassConstants");
        check(hasMethod(c, "isSynthetic"), "method isSynthetic");
        check(hasMethod(c, "writeFields"), "method writeFields");
        check(hasMethod(c, "write"), "method write");
        check(hasMethod(c, "writeIndex"), "method writeIndex");
        check(hasMethod(c, "extractHighShortFromInt"), "method extractHighShortFromInt");
        check(hasMethod(c, "extractLowShortFromInt"), "method extractLowShortFromInt");
        check(hasMethod(c, "debugMessage"), "method debugMessage");
        check(hasMethod(c, "writeVersion"), "method writeVersion");
        check(hasMethod(c, "writeConstantPool"), "method writeConstantPool");
        check(hasMethod(c, "writeClassAccessFlags"), "method writeClassAccessFlags");
        check(hasMethod(c, "writeThisClass"), "method writeThisClass");
        check(hasMethod(c, "writeSuperClass"), "method writeSuperClass");
        check(hasMethod(c, "writeInterfaces"), "method writeInterfaces");
        check(hasMethod(c, "writeMethods"), "method writeMethods");
        check(hasMethod(c, "writeClassAttributes"), "method writeClassAttributes");
        check(hasMethod(c, "hasSyntheticAttribute"), "method hasSyntheticAttribute");
        check(hasMethod(c, "writeSynthetic"), "method writeSynthetic");
        check(hasMethod(c, "writeAnnotationAttribute"), "method writeAnnotationAttribute");
        check(hasMethod(c, "writeMethod"), "method writeMethod");
        check(hasMethod(c, "rewriteByteCode"), "method rewriteByteCode");
        check(hasMethod(c, "writeGenericSignature"), "method writeGenericSignature");
        check(hasField(c, "DEBUG"), "field DEBUG");
        check(hasField(c, "klass"), "field klass");
        check(hasField(c, "dos"), "field dos");
        check(hasField(c, "cpool"), "field cpool");
        check(hasField(c, "classToIndex"), "field classToIndex");
        check(hasField(c, "utf8ToIndex"), "field utf8ToIndex");
        check(hasField(c, "_sourceFileIndex"), "field _sourceFileIndex");
        check(hasField(c, "_innerClassesIndex"), "field _innerClassesIndex");
        check(hasField(c, "_nestHostIndex"), "field _nestHostIndex");
        check(hasField(c, "_nestMembersIndex"), "field _nestMembersIndex");
        check(hasField(c, "_syntheticIndex"), "field _syntheticIndex");
        check(hasField(c, "_deprecatedIndex"), "field _deprecatedIndex");
        check(hasField(c, "_constantValueIndex"), "field _constantValueIndex");
        check(hasField(c, "_codeIndex"), "field _codeIndex");
        check(hasField(c, "_exceptionsIndex"), "field _exceptionsIndex");
        check(hasField(c, "_stackMapTableIndex"), "field _stackMapTableIndex");
        check(hasField(c, "_lineNumberTableIndex"), "field _lineNumberTableIndex");
        check(hasField(c, "_localVariableTableIndex"), "field _localVariableTableIndex");
        check(hasField(c, "_signatureIndex"), "field _signatureIndex");
        check(hasField(c, "_bootstrapMethodsIndex"), "field _bootstrapMethodsIndex");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
