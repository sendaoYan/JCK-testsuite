/*
 * @test
 * @summary sun.jvm.hotspot.oops.InstanceKlass conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.oops:+open
 * @run main InstanceKlassTest
 */
import java.lang.reflect.*;

public class InstanceKlassTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.oops.InstanceKlass", false, InstanceKlassTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.oops.Klass".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "signature"), "method signature");
        check(hasMethod(c, "isInterface"), "method isInterface");
        check(hasMethod(c, "isAbstract"), "method isAbstract");
        check(hasMethod(c, "isFinal"), "method isFinal");
        check(hasMethod(c, "isPublic"), "method isPublic");
        check(hasMethod(c, "isSynthetic"), "method isSynthetic");
        check(hasMethod(c, "getMethods"), "method getMethods");
        check(hasMethod(c, "findMethod"), "method findMethod");
        check(hasMethod(c, "getAnnotations"), "method getAnnotations");
        check(hasMethod(c, "getNestMembers"), "method getNestMembers");
        check(hasMethod(c, "getGenericSignature"), "method getGenericSignature");
        check(hasMethod(c, "getSize"), "method getSize");
        check(hasMethod(c, "isLoaded"), "method isLoaded");
        check(hasMethod(c, "majorVersion"), "method majorVersion");
        check(hasMethod(c, "minorVersion"), "method minorVersion");
        check(hasMethod(c, "getFieldName"), "method getFieldName");
        check(hasMethod(c, "getHeaderSize"), "method getHeaderSize");
        check(hasMethod(c, "getAccessFlags"), "method getAccessFlags");
        check(hasMethod(c, "getAccessFlagsObj"), "method getAccessFlagsObj");
        check(hasMethod(c, "isSuper"), "method isSuper");
        check(hasMethod(c, "getJavaFieldsCount"), "method getJavaFieldsCount");
        check(hasMethod(c, "getAllFieldsCount"), "method getAllFieldsCount");
        check(hasMethod(c, "getFieldSignature"), "method getFieldSignature");
        check(hasMethod(c, "getInitStateAsInt"), "method getInitStateAsInt");
        check(hasMethod(c, "isLinked"), "method isLinked");
        check(hasMethod(c, "isInitialized"), "method isInitialized");
        check(hasMethod(c, "isInErrorState"), "method isInErrorState");
        check(hasMethod(c, "getSizeHelper"), "method getSizeHelper");
        check(hasMethod(c, "getItableLen"), "method getItableLen");
        check(hasMethod(c, "getNonstaticOopMapSize"), "method getNonstaticOopMapSize");
        check(hasMethod(c, "getConstants"), "method getConstants");
        check(hasMethod(c, "getFieldInfoStream"), "method getFieldInfoStream");
        check(hasMethod(c, "getSourceFileName"), "method getSourceFileName");
        check(hasMethod(c, "getInnerClasses"), "method getInnerClasses");
        check(hasMethod(c, "getTransitiveInterfaces"), "method getTransitiveInterfaces");
        check(hasMethod(c, "implementsInterface"), "method implementsInterface");
        check(hasMethod(c, "iterateFields"), "method iterateFields");
        check(hasMethod(c, "getFieldAccessFlags"), "method getFieldAccessFlags");
        check(hasMethod(c, "iterateNonStaticFields"), "method iterateNonStaticFields");
        check(hasMethod(c, "getLocalInterfaces"), "method getLocalInterfaces");
        check(hasMethod(c, "findLocalField"), "method findLocalField");
        check(hasMethod(c, "findInterfaceField"), "method findInterfaceField");
        check(hasMethod(c, "findField"), "method findField");
        check(hasMethod(c, "getFieldByIndex"), "method getFieldByIndex");
        check(hasMethod(c, "getImmediateFields"), "method getImmediateFields");
        check(hasMethod(c, "getMethodOrdering"), "method getMethodOrdering");
        check(hasMethod(c, "getArrayKlasses"), "method getArrayKlasses");
        check(hasMethod(c, "arrayKlassImpl"), "method arrayKlassImpl");
        check(hasMethod(c, "getClassAnnotations"), "method getClassAnnotations");
        check(hasMethod(c, "getClassTypeAnnotations"), "method getClassTypeAnnotations");
        check(hasMethod(c, "getFieldAnnotations"), "method getFieldAnnotations");
        check(hasMethod(c, "getFieldTypeAnnotations"), "method getFieldTypeAnnotations");
        check(hasMethod(c, "getInitState"), "method getInitState");
        check(hasMethod(c, "isNotInitialized"), "method isNotInitialized");
        check(hasMethod(c, "isBeingInitialized"), "method isBeingInitialized");
        check(hasMethod(c, "getClassStatus"), "method getClassStatus");
        check(hasMethod(c, "getObjectSize"), "method getObjectSize");
        check(hasMethod(c, "getFieldNameIndex"), "method getFieldNameIndex");
        check(hasMethod(c, "getSymbolFromIndex"), "method getSymbolFromIndex");
        check(hasMethod(c, "getFieldSignatureIndex"), "method getFieldSignatureIndex");
        check(hasMethod(c, "getFieldGenericSignatureIndex"), "method getFieldGenericSignatureIndex");
        check(hasMethod(c, "getFieldGenericSignature"), "method getFieldGenericSignature");
        check(hasMethod(c, "getFieldInitialValueIndex"), "method getFieldInitialValueIndex");
        check(hasMethod(c, "getFieldOffset"), "method getFieldOffset");
        check(hasMethod(c, "getDefaultMethods"), "method getDefaultMethods");
        check(hasMethod(c, "getSourceDebugExtension"), "method getSourceDebugExtension");
        check(hasMethod(c, "getNonstaticFieldSize"), "method getNonstaticFieldSize");
        check(hasMethod(c, "getStaticOopFieldCount"), "method getStaticOopFieldCount");
        check(hasMethod(c, "getNestHostIndex"), "method getNestHostIndex");
        check(hasMethod(c, "isInnerClassName"), "method isInnerClassName");
        check(hasMethod(c, "isInnerOrLocalClassName"), "method isInnerOrLocalClassName");
        check(hasMethod(c, "printValueOn"), "method printValueOn");
        check(hasMethod(c, "iterateStaticFields"), "method iterateStaticFields");
        check(hasMethod(c, "getJavaSuper"), "method getJavaSuper");
        check(hasMethod(c, "getStaticFields"), "method getStaticFields");
        check(hasMethod(c, "findFieldDbg"), "method findFieldDbg");
        check(hasMethod(c, "getAllFields"), "method getAllFields");
        check(hasMethod(c, "getImmediateMethods"), "method getImmediateMethods");
        check(hasMethod(c, "getDirectImplementedInterfaces"), "method getDirectImplementedInterfaces");
        check(hasMethod(c, "getBreakpoints"), "method getBreakpoints");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
