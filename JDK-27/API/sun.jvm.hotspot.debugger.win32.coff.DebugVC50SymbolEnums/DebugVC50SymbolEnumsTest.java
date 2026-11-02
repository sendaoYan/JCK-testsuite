/*
 * @test
 * @summary sun.jvm.hotspot.debugger.win32.coff.DebugVC50SymbolEnums conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.win32.coff:+open
 * @run main DebugVC50SymbolEnumsTest
 */
import java.lang.reflect.*;

public class DebugVC50SymbolEnumsTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.win32.coff.DebugVC50SymbolEnums", false, DebugVC50SymbolEnumsTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasField(c, "MACHTYPE_INTEL_8080"), "field MACHTYPE_INTEL_8080");
        check(hasField(c, "MACHTYPE_INTEL_8086"), "field MACHTYPE_INTEL_8086");
        check(hasField(c, "MACHTYPE_INTEL_80286"), "field MACHTYPE_INTEL_80286");
        check(hasField(c, "MACHTYPE_INTEL_80386"), "field MACHTYPE_INTEL_80386");
        check(hasField(c, "MACHTYPE_INTEL_80486"), "field MACHTYPE_INTEL_80486");
        check(hasField(c, "MACHTYPE_INTEL_PENTIUM"), "field MACHTYPE_INTEL_PENTIUM");
        check(hasField(c, "MACHTYPE_INTEL_PENTIUM_PRO"), "field MACHTYPE_INTEL_PENTIUM_PRO");
        check(hasField(c, "MACHTYPE_MIPS_R4000"), "field MACHTYPE_MIPS_R4000");
        check(hasField(c, "MACHTYPE_MIPS_RESERVED"), "field MACHTYPE_MIPS_RESERVED");
        check(hasField(c, "MACHTYPE_MIPS_RESERVED2"), "field MACHTYPE_MIPS_RESERVED2");
        check(hasField(c, "MACHTYPE_MC68000"), "field MACHTYPE_MC68000");
        check(hasField(c, "MACHTYPE_MC68010"), "field MACHTYPE_MC68010");
        check(hasField(c, "MACHTYPE_MC68020"), "field MACHTYPE_MC68020");
        check(hasField(c, "MACHTYPE_MC68030"), "field MACHTYPE_MC68030");
        check(hasField(c, "MACHTYPE_MC68040"), "field MACHTYPE_MC68040");
        check(hasField(c, "MACHTYPE_ALPHA"), "field MACHTYPE_ALPHA");
        check(hasField(c, "MACHTYPE_PPC601"), "field MACHTYPE_PPC601");
        check(hasField(c, "MACHTYPE_PPC603"), "field MACHTYPE_PPC603");
        check(hasField(c, "MACHTYPE_PPC604"), "field MACHTYPE_PPC604");
        check(hasField(c, "MACHTYPE_PPC620"), "field MACHTYPE_PPC620");
        check(hasField(c, "COMPFLAG_LANGUAGE_MASK"), "field COMPFLAG_LANGUAGE_MASK");
        check(hasField(c, "COMPFLAG_LANGUAGE_SHIFT"), "field COMPFLAG_LANGUAGE_SHIFT");
        check(hasField(c, "COMPFLAG_LANGUAGE_C"), "field COMPFLAG_LANGUAGE_C");
        check(hasField(c, "COMPFLAG_LANGUAGE_CPP"), "field COMPFLAG_LANGUAGE_CPP");
        check(hasField(c, "COMPFLAG_LANGUAGE_FORTRAN"), "field COMPFLAG_LANGUAGE_FORTRAN");
        check(hasField(c, "COMPFLAG_LANGUAGE_MASM"), "field COMPFLAG_LANGUAGE_MASM");
        check(hasField(c, "COMPFLAG_LANGUAGE_PASCAL"), "field COMPFLAG_LANGUAGE_PASCAL");
        check(hasField(c, "COMPFLAG_LANGUAGE_BASIC"), "field COMPFLAG_LANGUAGE_BASIC");
        check(hasField(c, "COMPFLAG_LANGUAGE_COBOL"), "field COMPFLAG_LANGUAGE_COBOL");
        check(hasField(c, "COMPFLAG_PCODE_PRESENT_MASK"), "field COMPFLAG_PCODE_PRESENT_MASK");
        check(hasField(c, "COMPFLAG_FLOAT_PRECISION_MASK"), "field COMPFLAG_FLOAT_PRECISION_MASK");
        check(hasField(c, "COMPFLAG_FLOAT_PRECISION_SHIFT"), "field COMPFLAG_FLOAT_PRECISION_SHIFT");
        check(hasField(c, "COMPFLAG_FLOAT_PRECISION_ANSI_C"), "field COMPFLAG_FLOAT_PRECISION_ANSI_C");
        check(hasField(c, "COMPFLAG_FLOAT_PACKAGE_MASK"), "field COMPFLAG_FLOAT_PACKAGE_MASK");
        check(hasField(c, "COMPFLAG_FLOAT_PACKAGE_SHIFT"), "field COMPFLAG_FLOAT_PACKAGE_SHIFT");
        check(hasField(c, "COMPFLAG_FLOAT_PACKAGE_HARDWARE"), "field COMPFLAG_FLOAT_PACKAGE_HARDWARE");
        check(hasField(c, "COMPFLAG_FLOAT_PACKAGE_EMULATOR"), "field COMPFLAG_FLOAT_PACKAGE_EMULATOR");
        check(hasField(c, "COMPFLAG_FLOAT_PACKAGE_ALTMATH"), "field COMPFLAG_FLOAT_PACKAGE_ALTMATH");
        check(hasField(c, "COMPFLAG_AMBIENT_DATA_MASK"), "field COMPFLAG_AMBIENT_DATA_MASK");
        check(hasField(c, "COMPFLAG_AMBIENT_DATA_SHIFT"), "field COMPFLAG_AMBIENT_DATA_SHIFT");
        check(hasField(c, "COMPFLAG_AMBIENT_CODE_MASK"), "field COMPFLAG_AMBIENT_CODE_MASK");
        check(hasField(c, "COMPFLAG_AMBIENT_CODE_SHIFT"), "field COMPFLAG_AMBIENT_CODE_SHIFT");
        check(hasField(c, "COMPFLAG_AMBIENT_MODEL_NEAR"), "field COMPFLAG_AMBIENT_MODEL_NEAR");
        check(hasField(c, "COMPFLAG_AMBIENT_MODEL_FAR"), "field COMPFLAG_AMBIENT_MODEL_FAR");
        check(hasField(c, "COMPFLAG_AMBIENT_MODEL_HUGE"), "field COMPFLAG_AMBIENT_MODEL_HUGE");
        check(hasField(c, "COMPFLAG_MODE32_MASK"), "field COMPFLAG_MODE32_MASK");
        check(hasField(c, "FUNCRET_VARARGS_LEFT_TO_RIGHT_MASK"), "field FUNCRET_VARARGS_LEFT_TO_RIGHT_MASK");
        check(hasField(c, "FUNCRET_RETURNEE_STACK_CLEANUP_MASK"), "field FUNCRET_RETURNEE_STACK_CLEANUP_MASK");
        check(hasField(c, "FUNCRET_VOID"), "field FUNCRET_VOID");
        check(hasField(c, "FUNCRET_IN_REGISTERS"), "field FUNCRET_IN_REGISTERS");
        check(hasField(c, "FUNCRET_INDIRECT_CALLER_NEAR"), "field FUNCRET_INDIRECT_CALLER_NEAR");
        check(hasField(c, "FUNCRET_INDIRECT_CALLER_FAR"), "field FUNCRET_INDIRECT_CALLER_FAR");
        check(hasField(c, "FUNCRET_INDIRECT_RETURNEE_NEAR"), "field FUNCRET_INDIRECT_RETURNEE_NEAR");
        check(hasField(c, "FUNCRET_INDIRECT_RETURNEE_FAR"), "field FUNCRET_INDIRECT_RETURNEE_FAR");
        check(hasField(c, "PROCFLAGS_FRAME_POINTER_OMITTED"), "field PROCFLAGS_FRAME_POINTER_OMITTED");
        check(hasField(c, "PROCFLAGS_INTERRUPT_ROUTINE"), "field PROCFLAGS_INTERRUPT_ROUTINE");
        check(hasField(c, "PROCFLAGS_FAR_RETURN"), "field PROCFLAGS_FAR_RETURN");
        check(hasField(c, "PROCFLAGS_NEVER_RETURN"), "field PROCFLAGS_NEVER_RETURN");
        check(hasField(c, "THUNK_NO_TYPE"), "field THUNK_NO_TYPE");
        check(hasField(c, "THUNK_ADJUSTOR"), "field THUNK_ADJUSTOR");
        check(hasField(c, "THUNK_VCALL"), "field THUNK_VCALL");
        check(hasField(c, "THUNK_PCODE"), "field THUNK_PCODE");
        check(hasField(c, "EXMODEL_NOT_CODE"), "field EXMODEL_NOT_CODE");
        check(hasField(c, "EXMODEL_JUMP_TABLE"), "field EXMODEL_JUMP_TABLE");
        check(hasField(c, "EXMODEL_PADDING"), "field EXMODEL_PADDING");
        check(hasField(c, "EXMODEL_NATIVE"), "field EXMODEL_NATIVE");
        check(hasField(c, "EXMODEL_MICROFOCUS_COBOL"), "field EXMODEL_MICROFOCUS_COBOL");
        check(hasField(c, "EXMODEL_PADDING_FOR_ALIGNMENT"), "field EXMODEL_PADDING_FOR_ALIGNMENT");
        check(hasField(c, "EXMODEL_CODE"), "field EXMODEL_CODE");
        check(hasField(c, "EXMODEL_PCODE"), "field EXMODEL_PCODE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
