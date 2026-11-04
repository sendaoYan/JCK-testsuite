/*
 * @test
 * @summary sun.jvm.hotspot.debugger.win32.coff.DebugVC50TypeEnums conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.win32.coff:+open
 * @run main DebugVC50TypeEnumsTest
 */
import java.lang.reflect.*;

public class DebugVC50TypeEnumsTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.win32.coff.DebugVC50TypeEnums", false, DebugVC50TypeEnumsTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasField(c, "MODIFIER_CONST_MASK"), "field MODIFIER_CONST_MASK");
        check(hasField(c, "MODIFIER_VOLATILE_MASK"), "field MODIFIER_VOLATILE_MASK");
        check(hasField(c, "MODIFIER_UNALIGNED_MASK"), "field MODIFIER_UNALIGNED_MASK");
        check(hasField(c, "POINTER_PTRTYPE_MASK"), "field POINTER_PTRTYPE_MASK");
        check(hasField(c, "POINTER_PTRTYPE_SHIFT"), "field POINTER_PTRTYPE_SHIFT");
        check(hasField(c, "POINTER_PTRTYPE_NEAR"), "field POINTER_PTRTYPE_NEAR");
        check(hasField(c, "POINTER_PTRTYPE_FAR"), "field POINTER_PTRTYPE_FAR");
        check(hasField(c, "POINTER_PTRTYPE_HUGE"), "field POINTER_PTRTYPE_HUGE");
        check(hasField(c, "POINTER_PTRTYPE_BASED_ON_SEGMENT"), "field POINTER_PTRTYPE_BASED_ON_SEGMENT");
        check(hasField(c, "POINTER_PTRTYPE_BASED_ON_VALUE"), "field POINTER_PTRTYPE_BASED_ON_VALUE");
        check(hasField(c, "POINTER_PTRTYPE_BASED_ON_SEGMENT_OF_VALUE"), "field POINTER_PTRTYPE_BASED_ON_SEGMENT_OF_VALUE");
        check(hasField(c, "POINTER_PTRTYPE_BASED_ON_ADDRESS_OF_SYMBOL"), "field POINTER_PTRTYPE_BASED_ON_ADDRESS_OF_SYMBOL");
        check(hasField(c, "POINTER_PTRTYPE_BASED_ON_SEGMENT_OF_SYMBOL_ADDRESS"), "field POINTER_PTRTYPE_BASED_ON_SEGMENT_OF_SYMBOL_ADDRESS");
        check(hasField(c, "POINTER_PTRTYPE_BASED_ON_TYPE"), "field POINTER_PTRTYPE_BASED_ON_TYPE");
        check(hasField(c, "POINTER_PTRTYPE_BASED_ON_SELF"), "field POINTER_PTRTYPE_BASED_ON_SELF");
        check(hasField(c, "POINTER_PTRTYPE_NEAR_32_BIT"), "field POINTER_PTRTYPE_NEAR_32_BIT");
        check(hasField(c, "POINTER_PTRTYPE_FAR_32_BIT"), "field POINTER_PTRTYPE_FAR_32_BIT");
        check(hasField(c, "POINTER_PTRTYPE_64_BIT"), "field POINTER_PTRTYPE_64_BIT");
        check(hasField(c, "POINTER_PTRMODE_MASK"), "field POINTER_PTRMODE_MASK");
        check(hasField(c, "POINTER_PTRMODE_SHIFT"), "field POINTER_PTRMODE_SHIFT");
        check(hasField(c, "POINTER_PTRMODE_POINTER"), "field POINTER_PTRMODE_POINTER");
        check(hasField(c, "POINTER_PTRMODE_REFERENCE"), "field POINTER_PTRMODE_REFERENCE");
        check(hasField(c, "POINTER_PTRMODE_PTR_TO_DATA_MEMBER"), "field POINTER_PTRMODE_PTR_TO_DATA_MEMBER");
        check(hasField(c, "POINTER_PTRMODE_PTR_TO_METHOD"), "field POINTER_PTRMODE_PTR_TO_METHOD");
        check(hasField(c, "POINTER_ISFLAT32_MASK"), "field POINTER_ISFLAT32_MASK");
        check(hasField(c, "POINTER_VOLATILE_MASK"), "field POINTER_VOLATILE_MASK");
        check(hasField(c, "POINTER_CONST_MASK"), "field POINTER_CONST_MASK");
        check(hasField(c, "POINTER_UNALIGNED_MASK"), "field POINTER_UNALIGNED_MASK");
        check(hasField(c, "POINTER_RESTRICT_MASK"), "field POINTER_RESTRICT_MASK");
        check(hasField(c, "PTR_FORMAT_DATA_NVF_NVB"), "field PTR_FORMAT_DATA_NVF_NVB");
        check(hasField(c, "PTR_FORMAT_DATA_VB"), "field PTR_FORMAT_DATA_VB");
        check(hasField(c, "PTR_FORMAT_METHOD_NVB_SAP"), "field PTR_FORMAT_METHOD_NVB_SAP");
        check(hasField(c, "PTR_FORMAT_METHOD_NVB_MAP"), "field PTR_FORMAT_METHOD_NVB_MAP");
        check(hasField(c, "PTR_FORMAT_METHOD_VB"), "field PTR_FORMAT_METHOD_VB");
        check(hasField(c, "PROPERTY_PACKED"), "field PROPERTY_PACKED");
        check(hasField(c, "PROPERTY_CTOR"), "field PROPERTY_CTOR");
        check(hasField(c, "PROPERTY_OVEROPS"), "field PROPERTY_OVEROPS");
        check(hasField(c, "PROPERTY_ISNESTED"), "field PROPERTY_ISNESTED");
        check(hasField(c, "PROPERTY_CNESTED"), "field PROPERTY_CNESTED");
        check(hasField(c, "PROPERTY_OPASSIGN"), "field PROPERTY_OPASSIGN");
        check(hasField(c, "PROPERTY_OPCAST"), "field PROPERTY_OPCAST");
        check(hasField(c, "PROPERTY_FWDREF"), "field PROPERTY_FWDREF");
        check(hasField(c, "PROPERTY_SCOPED"), "field PROPERTY_SCOPED");
        check(hasField(c, "CALLCONV_NEAR_C"), "field CALLCONV_NEAR_C");
        check(hasField(c, "CALLCONV_FAR_C"), "field CALLCONV_FAR_C");
        check(hasField(c, "CALLCONV_NEAR_PASCAL"), "field CALLCONV_NEAR_PASCAL");
        check(hasField(c, "CALLCONV_FAR_PASCAL"), "field CALLCONV_FAR_PASCAL");
        check(hasField(c, "CALLCONV_NEAR_FASTCALL"), "field CALLCONV_NEAR_FASTCALL");
        check(hasField(c, "CALLCONV_FAR_FASTCALL"), "field CALLCONV_FAR_FASTCALL");
        check(hasField(c, "CALLCONV_RESERVED"), "field CALLCONV_RESERVED");
        check(hasField(c, "CALLCONV_NEAR_STDCALL"), "field CALLCONV_NEAR_STDCALL");
        check(hasField(c, "CALLCONV_FAR_STDCALL"), "field CALLCONV_FAR_STDCALL");
        check(hasField(c, "CALLCONV_NEAR_SYSCALL"), "field CALLCONV_NEAR_SYSCALL");
        check(hasField(c, "CALLCONV_FAR_SYSCALL"), "field CALLCONV_FAR_SYSCALL");
        check(hasField(c, "CALLCONV_THIS_CALL"), "field CALLCONV_THIS_CALL");
        check(hasField(c, "CALLCONV_MIPS_CALL"), "field CALLCONV_MIPS_CALL");
        check(hasField(c, "CALLCONV_GENERIC"), "field CALLCONV_GENERIC");
        check(hasField(c, "VTENTRY_NEAR"), "field VTENTRY_NEAR");
        check(hasField(c, "VTENTRY_FAR"), "field VTENTRY_FAR");
        check(hasField(c, "VTENTRY_THIN"), "field VTENTRY_THIN");
        check(hasField(c, "VTENTRY_ADDRESS_PT_DISP"), "field VTENTRY_ADDRESS_PT_DISP");
        check(hasField(c, "VTENTRY_FAR_PTR_TO_METACLASS"), "field VTENTRY_FAR_PTR_TO_METACLASS");
        check(hasField(c, "VTENTRY_NEAR_32"), "field VTENTRY_NEAR_32");
        check(hasField(c, "VTENTRY_FAR_32"), "field VTENTRY_FAR_32");
        check(hasField(c, "LABEL_ADDR_MODE_NEAR"), "field LABEL_ADDR_MODE_NEAR");
        check(hasField(c, "LABEL_ADDR_MODE_FAR"), "field LABEL_ADDR_MODE_FAR");
        check(hasField(c, "RESERVED_TYPE_MASK"), "field RESERVED_TYPE_MASK");
        check(hasField(c, "RESERVED_TYPE_SPECIAL"), "field RESERVED_TYPE_SPECIAL");
        check(hasField(c, "RESERVED_TYPE_SIGNED_INT"), "field RESERVED_TYPE_SIGNED_INT");
        check(hasField(c, "RESERVED_TYPE_UNSIGNED_INT"), "field RESERVED_TYPE_UNSIGNED_INT");
        check(hasField(c, "RESERVED_TYPE_BOOLEAN"), "field RESERVED_TYPE_BOOLEAN");
        check(hasField(c, "RESERVED_TYPE_REAL"), "field RESERVED_TYPE_REAL");
        check(hasField(c, "RESERVED_TYPE_COMPLEX"), "field RESERVED_TYPE_COMPLEX");
        check(hasField(c, "RESERVED_TYPE_SPECIAL2"), "field RESERVED_TYPE_SPECIAL2");
        check(hasField(c, "RESERVED_TYPE_REALLY_INT"), "field RESERVED_TYPE_REALLY_INT");
        check(hasField(c, "RESERVED_MODE_MASK"), "field RESERVED_MODE_MASK");
        check(hasField(c, "RESERVED_MODE_DIRECT"), "field RESERVED_MODE_DIRECT");
        check(hasField(c, "RESERVED_MODE_NEAR_PTR"), "field RESERVED_MODE_NEAR_PTR");
        check(hasField(c, "RESERVED_MODE_FAR_PTR"), "field RESERVED_MODE_FAR_PTR");
        check(hasField(c, "RESERVED_MODE_HUGE_PTR"), "field RESERVED_MODE_HUGE_PTR");
        check(hasField(c, "RESERVED_MODE_NEAR_32_PTR"), "field RESERVED_MODE_NEAR_32_PTR");
        check(hasField(c, "RESERVED_MODE_FAR_32_PTR"), "field RESERVED_MODE_FAR_32_PTR");
        check(hasField(c, "RESERVED_MODE_NEAR_64_PTR"), "field RESERVED_MODE_NEAR_64_PTR");
        check(hasField(c, "RESERVED_SIZE_MASK"), "field RESERVED_SIZE_MASK");
        check(hasField(c, "RESERVED_SIZE_SPECIAL_NO_TYPE"), "field RESERVED_SIZE_SPECIAL_NO_TYPE");
        check(hasField(c, "RESERVED_SIZE_SPECIAL_ABSOLUTE_SYMBOL"), "field RESERVED_SIZE_SPECIAL_ABSOLUTE_SYMBOL");
        check(hasField(c, "RESERVED_SIZE_SPECIAL_SEGMENT"), "field RESERVED_SIZE_SPECIAL_SEGMENT");
        check(hasField(c, "RESERVED_SIZE_SPECIAL_VOID"), "field RESERVED_SIZE_SPECIAL_VOID");
        check(hasField(c, "RESERVED_SIZE_SPECIAL_BASIC_8_BYTE"), "field RESERVED_SIZE_SPECIAL_BASIC_8_BYTE");
        check(hasField(c, "RESERVED_SIZE_SPECIAL_NEAR_BASIC_STRING"), "field RESERVED_SIZE_SPECIAL_NEAR_BASIC_STRING");
        check(hasField(c, "RESERVED_SIZE_SPECIAL_FAR_BASIC_STRING"), "field RESERVED_SIZE_SPECIAL_FAR_BASIC_STRING");
        check(hasField(c, "RESERVED_SIZE_SPECIAL_UNTRANSLATED"), "field RESERVED_SIZE_SPECIAL_UNTRANSLATED");
        check(hasField(c, "RESERVED_SIZE_INT_1_BYTE"), "field RESERVED_SIZE_INT_1_BYTE");
        check(hasField(c, "RESERVED_SIZE_INT_2_BYTE"), "field RESERVED_SIZE_INT_2_BYTE");
        check(hasField(c, "RESERVED_SIZE_INT_4_BYTE"), "field RESERVED_SIZE_INT_4_BYTE");
        check(hasField(c, "RESERVED_SIZE_INT_8_BYTE"), "field RESERVED_SIZE_INT_8_BYTE");
        check(hasField(c, "RESERVED_SIZE_REAL_32_BIT"), "field RESERVED_SIZE_REAL_32_BIT");
        check(hasField(c, "RESERVED_SIZE_REAL_64_BIT"), "field RESERVED_SIZE_REAL_64_BIT");
        check(hasField(c, "RESERVED_SIZE_REAL_80_BIT"), "field RESERVED_SIZE_REAL_80_BIT");
        check(hasField(c, "RESERVED_SIZE_REAL_128_BIT"), "field RESERVED_SIZE_REAL_128_BIT");
        check(hasField(c, "RESERVED_SIZE_REAL_48_BIT"), "field RESERVED_SIZE_REAL_48_BIT");
        check(hasField(c, "RESERVED_SIZE_SPECIAL2_BIT"), "field RESERVED_SIZE_SPECIAL2_BIT");
        check(hasField(c, "RESERVED_SIZE_SPECIAL2_PASCAL_CHAR"), "field RESERVED_SIZE_SPECIAL2_PASCAL_CHAR");
        check(hasField(c, "RESERVED_SIZE_REALLY_INT_CHAR"), "field RESERVED_SIZE_REALLY_INT_CHAR");
        check(hasField(c, "RESERVED_SIZE_REALLY_INT_WCHAR"), "field RESERVED_SIZE_REALLY_INT_WCHAR");
        check(hasField(c, "RESERVED_SIZE_REALLY_INT_2_BYTE"), "field RESERVED_SIZE_REALLY_INT_2_BYTE");
        check(hasField(c, "RESERVED_SIZE_REALLY_INT_2_BYTE_U"), "field RESERVED_SIZE_REALLY_INT_2_BYTE_U");
        check(hasField(c, "RESERVED_SIZE_REALLY_INT_4_BYTE"), "field RESERVED_SIZE_REALLY_INT_4_BYTE");
        check(hasField(c, "RESERVED_SIZE_REALLY_INT_4_BYTE_U"), "field RESERVED_SIZE_REALLY_INT_4_BYTE_U");
        check(hasField(c, "RESERVED_SIZE_REALLY_INT_8_BYTE"), "field RESERVED_SIZE_REALLY_INT_8_BYTE");
        check(hasField(c, "RESERVED_SIZE_REALLY_INT_8_BYTE_U"), "field RESERVED_SIZE_REALLY_INT_8_BYTE_U");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
