/*
 * @test
 * @summary sun.jvm.hotspot.oops.ConstantPool conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.oops:+open
 * @run main ConstantPoolTest
 */
import java.lang.reflect.*;

public class ConstantPoolTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.oops.ConstantPool", false, ConstantPoolTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.oops.Metadata".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.runtime.ClassConstants"), "interface sun.jvm.hotspot.runtime.ClassConstants");
        check(hasMethod(c, "getLength"), "method getLength");
        check(hasMethod(c, "getGenericSignature"), "method getGenericSignature");
        check(hasMethod(c, "getSize"), "method getSize");
        check(hasMethod(c, "getNameAndTypeRefIndexAt"), "method getNameAndTypeRefIndexAt");
        check(hasMethod(c, "getIntAt"), "method getIntAt");
        check(hasMethod(c, "getLongAt"), "method getLongAt");
        check(hasMethod(c, "getFloatAt"), "method getFloatAt");
        check(hasMethod(c, "getDoubleAt"), "method getDoubleAt");
        check(hasMethod(c, "getTagAt"), "method getTagAt");
        check(hasMethod(c, "writeBytes"), "method writeBytes");
        check(hasMethod(c, "getCache"), "method getCache");
        check(hasMethod(c, "majorVersion"), "method majorVersion");
        check(hasMethod(c, "minorVersion"), "method minorVersion");
        check(hasMethod(c, "getSymbolAt"), "method getSymbolAt");
        check(hasMethod(c, "getSourceFileName"), "method getSourceFileName");
        check(hasMethod(c, "getKlassNameAt"), "method getKlassNameAt");
        check(hasMethod(c, "iterateFields"), "method iterateFields");
        check(hasMethod(c, "printValueOn"), "method printValueOn");
        check(hasMethod(c, "getPoolHolder"), "method getPoolHolder");
        check(hasMethod(c, "getKlassAt"), "method getKlassAt");
        check(hasMethod(c, "getNameRefAt"), "method getNameRefAt");
        check(hasMethod(c, "getSignatureRefAt"), "method getSignatureRefAt");
        check(hasMethod(c, "getFieldRefAt"), "method getFieldRefAt");
        check(hasMethod(c, "uncachedGetNameRefAt"), "method uncachedGetNameRefAt");
        check(hasMethod(c, "uncachedGetSignatureRefAt"), "method uncachedGetSignatureRefAt");
        check(hasMethod(c, "getMethodRefAt"), "method getMethodRefAt");
        check(hasMethod(c, "invokeDynamicNameAndTypeRefIndexAt"), "method invokeDynamicNameAndTypeRefIndexAt");
        check(hasMethod(c, "objectToCPIndex"), "method objectToCPIndex");
        check(hasMethod(c, "getUnresolvedStringAt"), "method getUnresolvedStringAt");
        check(hasMethod(c, "getMethodHandleIndexAt"), "method getMethodHandleIndexAt");
        check(hasMethod(c, "getMethodHandleRefKindAt"), "method getMethodHandleRefKindAt");
        check(hasMethod(c, "getMethodTypeIndexAt"), "method getMethodTypeIndexAt");
        check(hasMethod(c, "getResolvedReferences"), "method getResolvedReferences");
        check(hasMethod(c, "referenceMap"), "method referenceMap");
        check(hasMethod(c, "getTags"), "method getTags");
        check(hasMethod(c, "getAddressAtRaw"), "method getAddressAtRaw");
        check(hasMethod(c, "to_cp_index"), "method to_cp_index");
        check(hasMethod(c, "getNameRefIndexAt"), "method getNameRefIndexAt");
        check(hasMethod(c, "uncachedGetNameAndTypeRefIndexAt"), "method uncachedGetNameAndTypeRefIndexAt");
        check(hasMethod(c, "getSignatureRefIndexAt"), "method getSignatureRefIndexAt");
        check(hasMethod(c, "getKlassSlotAt"), "method getKlassSlotAt");
        check(hasMethod(c, "getResolvedKlasses"), "method getResolvedKlasses");
        check(hasMethod(c, "getSlotAt"), "method getSlotAt");
        check(hasMethod(c, "getFieldOrMethodAt"), "method getFieldOrMethodAt");
        check(hasMethod(c, "getFieldOrMethodKlassRefAt"), "method getFieldOrMethodKlassRefAt");
        check(hasMethod(c, "getNameAndTypeAt"), "method getNameAndTypeAt");
        check(hasMethod(c, "getBootstrapMethodArgsCount"), "method getBootstrapMethodArgsCount");
        check(hasMethod(c, "getBootstrapMethodAt"), "method getBootstrapMethodAt");
        check(hasMethod(c, "debugMessage"), "method debugMessage");
        check(hasMethod(c, "isConstantPool"), "method isConstantPool");
        check(hasMethod(c, "getBootstrapMethodsCount"), "method getBootstrapMethodsCount");
        check(hasMethod(c, "getBootstrapSpecifierAt"), "method getBootstrapSpecifierAt");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
