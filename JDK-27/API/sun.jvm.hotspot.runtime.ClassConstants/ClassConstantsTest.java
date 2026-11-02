/*
 * @test
 * @summary sun.jvm.hotspot.runtime.ClassConstants conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.runtime:+open
 * @run main ClassConstantsTest
 */
import java.lang.reflect.*;

public class ClassConstantsTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.runtime.ClassConstants", false, ClassConstantsTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasField(c, "JVM_CONSTANT_Utf8"), "field JVM_CONSTANT_Utf8");
        check(hasField(c, "JVM_CONSTANT_Unicode"), "field JVM_CONSTANT_Unicode");
        check(hasField(c, "JVM_CONSTANT_Integer"), "field JVM_CONSTANT_Integer");
        check(hasField(c, "JVM_CONSTANT_Float"), "field JVM_CONSTANT_Float");
        check(hasField(c, "JVM_CONSTANT_Long"), "field JVM_CONSTANT_Long");
        check(hasField(c, "JVM_CONSTANT_Double"), "field JVM_CONSTANT_Double");
        check(hasField(c, "JVM_CONSTANT_Class"), "field JVM_CONSTANT_Class");
        check(hasField(c, "JVM_CONSTANT_String"), "field JVM_CONSTANT_String");
        check(hasField(c, "JVM_CONSTANT_Fieldref"), "field JVM_CONSTANT_Fieldref");
        check(hasField(c, "JVM_CONSTANT_Methodref"), "field JVM_CONSTANT_Methodref");
        check(hasField(c, "JVM_CONSTANT_InterfaceMethodref"), "field JVM_CONSTANT_InterfaceMethodref");
        check(hasField(c, "JVM_CONSTANT_NameAndType"), "field JVM_CONSTANT_NameAndType");
        check(hasField(c, "JVM_CONSTANT_MethodHandle"), "field JVM_CONSTANT_MethodHandle");
        check(hasField(c, "JVM_CONSTANT_MethodType"), "field JVM_CONSTANT_MethodType");
        check(hasField(c, "JVM_CONSTANT_Dynamic"), "field JVM_CONSTANT_Dynamic");
        check(hasField(c, "JVM_CONSTANT_InvokeDynamic"), "field JVM_CONSTANT_InvokeDynamic");
        check(hasField(c, "JVM_CONSTANT_Module"), "field JVM_CONSTANT_Module");
        check(hasField(c, "JVM_CONSTANT_Package"), "field JVM_CONSTANT_Package");
        check(hasField(c, "JVM_REF_getField"), "field JVM_REF_getField");
        check(hasField(c, "JVM_REF_getStatic"), "field JVM_REF_getStatic");
        check(hasField(c, "JVM_REF_putField"), "field JVM_REF_putField");
        check(hasField(c, "JVM_REF_putStatic"), "field JVM_REF_putStatic");
        check(hasField(c, "JVM_REF_invokeVirtual"), "field JVM_REF_invokeVirtual");
        check(hasField(c, "JVM_REF_invokeStatic"), "field JVM_REF_invokeStatic");
        check(hasField(c, "JVM_REF_invokeSpecial"), "field JVM_REF_invokeSpecial");
        check(hasField(c, "JVM_REF_newInvokeSpecial"), "field JVM_REF_newInvokeSpecial");
        check(hasField(c, "JVM_REF_invokeInterface"), "field JVM_REF_invokeInterface");
        check(hasField(c, "JVM_CONSTANT_Invalid"), "field JVM_CONSTANT_Invalid");
        check(hasField(c, "JVM_CONSTANT_UnresolvedClass"), "field JVM_CONSTANT_UnresolvedClass");
        check(hasField(c, "JVM_CONSTANT_ClassIndex"), "field JVM_CONSTANT_ClassIndex");
        check(hasField(c, "JVM_CONSTANT_StringIndex"), "field JVM_CONSTANT_StringIndex");
        check(hasField(c, "JVM_CONSTANT_UnresolvedClassInError"), "field JVM_CONSTANT_UnresolvedClassInError");
        check(hasField(c, "JVM_CONSTANT_MethodHandleInError"), "field JVM_CONSTANT_MethodHandleInError");
        check(hasField(c, "JVM_CONSTANT_MethodTypeInError"), "field JVM_CONSTANT_MethodTypeInError");
        check(hasField(c, "MAJOR_VERSION"), "field MAJOR_VERSION");
        check(hasField(c, "MINOR_VERSION"), "field MINOR_VERSION");
        check(hasField(c, "MAJOR_VERSION_OLD"), "field MAJOR_VERSION_OLD");
        check(hasField(c, "MINOR_VERSION_OLD"), "field MINOR_VERSION_OLD");
        check(hasField(c, "JVM_ACC_PUBLIC"), "field JVM_ACC_PUBLIC");
        check(hasField(c, "JVM_ACC_PRIVATE"), "field JVM_ACC_PRIVATE");
        check(hasField(c, "JVM_ACC_PROTECTED"), "field JVM_ACC_PROTECTED");
        check(hasField(c, "JVM_ACC_STATIC"), "field JVM_ACC_STATIC");
        check(hasField(c, "JVM_ACC_FINAL"), "field JVM_ACC_FINAL");
        check(hasField(c, "JVM_ACC_SYNCHRONIZED"), "field JVM_ACC_SYNCHRONIZED");
        check(hasField(c, "JVM_ACC_SUPER"), "field JVM_ACC_SUPER");
        check(hasField(c, "JVM_ACC_VOLATILE"), "field JVM_ACC_VOLATILE");
        check(hasField(c, "JVM_ACC_BRIDGE"), "field JVM_ACC_BRIDGE");
        check(hasField(c, "JVM_ACC_TRANSIENT"), "field JVM_ACC_TRANSIENT");
        check(hasField(c, "JVM_ACC_VARARGS"), "field JVM_ACC_VARARGS");
        check(hasField(c, "JVM_ACC_NATIVE"), "field JVM_ACC_NATIVE");
        check(hasField(c, "JVM_ACC_INTERFACE"), "field JVM_ACC_INTERFACE");
        check(hasField(c, "JVM_ACC_ABSTRACT"), "field JVM_ACC_ABSTRACT");
        check(hasField(c, "JVM_ACC_STRICT"), "field JVM_ACC_STRICT");
        check(hasField(c, "JVM_ACC_SYNTHETIC"), "field JVM_ACC_SYNTHETIC");
        check(hasField(c, "JVM_ACC_ANNOTATION"), "field JVM_ACC_ANNOTATION");
        check(hasField(c, "JVM_ACC_ENUM"), "field JVM_ACC_ENUM");
        check(hasField(c, "JVM_RECOGNIZED_CLASS_MODIFIERS"), "field JVM_RECOGNIZED_CLASS_MODIFIERS");
        check(hasField(c, "JVM_RECOGNIZED_FIELD_MODIFIERS"), "field JVM_RECOGNIZED_FIELD_MODIFIERS");
        check(hasField(c, "JVM_RECOGNIZED_METHOD_MODIFIERS"), "field JVM_RECOGNIZED_METHOD_MODIFIERS");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
