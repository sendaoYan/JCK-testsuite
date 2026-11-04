/*
 * @test
 * @summary sun.jvm.hotspot.oops.Field conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.oops:+open
 * @run main FieldTest
 */
import java.lang.reflect.*;

public class FieldTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.oops.Field", false, FieldTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "isStatic"), "method isStatic");
        check(hasMethod(c, "isFinal"), "method isFinal");
        check(hasMethod(c, "isPublic"), "method isPublic");
        check(hasMethod(c, "isSynthetic"), "method isSynthetic");
        check(hasMethod(c, "getFields"), "method getFields");
        check(hasMethod(c, "isProtected"), "method isProtected");
        check(hasMethod(c, "getGenericSignature"), "method getGenericSignature");
        check(hasMethod(c, "isTransient"), "method isTransient");
        check(hasMethod(c, "isVolatile"), "method isVolatile");
        check(hasMethod(c, "isEnumConstant"), "method isEnumConstant");
        check(hasMethod(c, "getSignature"), "method getSignature");
        check(hasMethod(c, "getFieldType"), "method getFieldType");
        check(hasMethod(c, "isPrivate"), "method isPrivate");
        check(hasMethod(c, "isGeneric"), "method isGeneric");
        check(hasMethod(c, "getOffset"), "method getOffset");
        check(hasMethod(c, "getID"), "method getID");
        check(hasMethod(c, "getAccessFlags"), "method getAccessFlags");
        check(hasMethod(c, "getAccessFlagsObj"), "method getAccessFlagsObj");
        check(hasMethod(c, "isInitialized"), "method isInitialized");
        check(hasMethod(c, "getNameIndex"), "method getNameIndex");
        check(hasMethod(c, "getSignatureIndex"), "method getSignatureIndex");
        check(hasMethod(c, "getGenericSignatureIndex"), "method getGenericSignatureIndex");
        check(hasMethod(c, "getInitialValueIndex"), "method getInitialValueIndex");
        check(hasMethod(c, "printOn"), "method printOn");
        check(hasMethod(c, "isVMField"), "method isVMField");
        check(hasMethod(c, "isInjected"), "method isInjected");
        check(hasMethod(c, "getFieldHolder"), "method getFieldHolder");
        check(hasMethod(c, "isNamedField"), "method isNamedField");
        check(hasMethod(c, "getFieldIndex"), "method getFieldIndex");
        check(hasMethod(c, "hasInitialValue"), "method hasInitialValue");
        check(hasMethod(c, "isPackagePrivate"), "method isPackagePrivate");
        check(hasMethod(c, "isStable"), "method isStable");
        check(hasMethod(c, "isContended"), "method isContended");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
