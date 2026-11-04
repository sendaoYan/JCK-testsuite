/*
 * @test
 * @summary sun.jvm.hotspot.debugger.win32.coff.DebugVC50TypeLeafIndices conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.win32.coff:+open
 * @run main DebugVC50TypeLeafIndicesTest
 */
import java.lang.reflect.*;

public class DebugVC50TypeLeafIndicesTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.win32.coff.DebugVC50TypeLeafIndices", false, DebugVC50TypeLeafIndicesTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasField(c, "LF_MODIFIER"), "field LF_MODIFIER");
        check(hasField(c, "LF_POINTER"), "field LF_POINTER");
        check(hasField(c, "LF_ARRAY"), "field LF_ARRAY");
        check(hasField(c, "LF_CLASS"), "field LF_CLASS");
        check(hasField(c, "LF_STRUCTURE"), "field LF_STRUCTURE");
        check(hasField(c, "LF_UNION"), "field LF_UNION");
        check(hasField(c, "LF_ENUM"), "field LF_ENUM");
        check(hasField(c, "LF_PROCEDURE"), "field LF_PROCEDURE");
        check(hasField(c, "LF_MFUNCTION"), "field LF_MFUNCTION");
        check(hasField(c, "LF_VTSHAPE"), "field LF_VTSHAPE");
        check(hasField(c, "LF_COBOL0"), "field LF_COBOL0");
        check(hasField(c, "LF_COBOL1"), "field LF_COBOL1");
        check(hasField(c, "LF_BARRAY"), "field LF_BARRAY");
        check(hasField(c, "LF_LABEL"), "field LF_LABEL");
        check(hasField(c, "LF_NULL"), "field LF_NULL");
        check(hasField(c, "LF_NOTTRAN"), "field LF_NOTTRAN");
        check(hasField(c, "LF_DIMARRAY"), "field LF_DIMARRAY");
        check(hasField(c, "LF_VFTPATH"), "field LF_VFTPATH");
        check(hasField(c, "LF_PRECOMP"), "field LF_PRECOMP");
        check(hasField(c, "LF_ENDPRECOMP"), "field LF_ENDPRECOMP");
        check(hasField(c, "LF_OEM"), "field LF_OEM");
        check(hasField(c, "LF_TYPESERVER"), "field LF_TYPESERVER");
        check(hasField(c, "LF_SKIP"), "field LF_SKIP");
        check(hasField(c, "LF_ARGLIST"), "field LF_ARGLIST");
        check(hasField(c, "LF_DEFARG"), "field LF_DEFARG");
        check(hasField(c, "LF_FIELDLIST"), "field LF_FIELDLIST");
        check(hasField(c, "LF_DERIVED"), "field LF_DERIVED");
        check(hasField(c, "LF_BITFIELD"), "field LF_BITFIELD");
        check(hasField(c, "LF_METHODLIST"), "field LF_METHODLIST");
        check(hasField(c, "LF_DIMCONU"), "field LF_DIMCONU");
        check(hasField(c, "LF_DIMCONLU"), "field LF_DIMCONLU");
        check(hasField(c, "LF_DIMVARU"), "field LF_DIMVARU");
        check(hasField(c, "LF_DIMVARLU"), "field LF_DIMVARLU");
        check(hasField(c, "LF_REFSYM"), "field LF_REFSYM");
        check(hasField(c, "LF_BCLASS"), "field LF_BCLASS");
        check(hasField(c, "LF_VBCLASS"), "field LF_VBCLASS");
        check(hasField(c, "LF_IVBCLASS"), "field LF_IVBCLASS");
        check(hasField(c, "LF_ENUMERATE"), "field LF_ENUMERATE");
        check(hasField(c, "LF_FRIENDFCN"), "field LF_FRIENDFCN");
        check(hasField(c, "LF_INDEX"), "field LF_INDEX");
        check(hasField(c, "LF_MEMBER"), "field LF_MEMBER");
        check(hasField(c, "LF_STMEMBER"), "field LF_STMEMBER");
        check(hasField(c, "LF_METHOD"), "field LF_METHOD");
        check(hasField(c, "LF_NESTTYPE"), "field LF_NESTTYPE");
        check(hasField(c, "LF_VFUNCTAB"), "field LF_VFUNCTAB");
        check(hasField(c, "LF_FRIENDCLS"), "field LF_FRIENDCLS");
        check(hasField(c, "LF_ONEMETHOD"), "field LF_ONEMETHOD");
        check(hasField(c, "LF_VFUNCOFF"), "field LF_VFUNCOFF");
        check(hasField(c, "LF_NESTTYPEEX"), "field LF_NESTTYPEEX");
        check(hasField(c, "LF_MEMBERMODIFY"), "field LF_MEMBERMODIFY");
        check(hasField(c, "LF_NUMERIC"), "field LF_NUMERIC");
        check(hasField(c, "LF_CHAR"), "field LF_CHAR");
        check(hasField(c, "LF_SHORT"), "field LF_SHORT");
        check(hasField(c, "LF_USHORT"), "field LF_USHORT");
        check(hasField(c, "LF_LONG"), "field LF_LONG");
        check(hasField(c, "LF_ULONG"), "field LF_ULONG");
        check(hasField(c, "LF_REAL32"), "field LF_REAL32");
        check(hasField(c, "LF_REAL64"), "field LF_REAL64");
        check(hasField(c, "LF_REAL80"), "field LF_REAL80");
        check(hasField(c, "LF_REAL128"), "field LF_REAL128");
        check(hasField(c, "LF_QUADWORD"), "field LF_QUADWORD");
        check(hasField(c, "LF_UQUADWORD"), "field LF_UQUADWORD");
        check(hasField(c, "LF_REAL48"), "field LF_REAL48");
        check(hasField(c, "LF_COMPLEX32"), "field LF_COMPLEX32");
        check(hasField(c, "LF_COMPLEX64"), "field LF_COMPLEX64");
        check(hasField(c, "LF_COMPLEX80"), "field LF_COMPLEX80");
        check(hasField(c, "LF_COMPLEX128"), "field LF_COMPLEX128");
        check(hasField(c, "LF_VARSTRING"), "field LF_VARSTRING");
        check(hasField(c, "LF_PAD0"), "field LF_PAD0");
        check(hasField(c, "LF_PAD1"), "field LF_PAD1");
        check(hasField(c, "LF_PAD2"), "field LF_PAD2");
        check(hasField(c, "LF_PAD3"), "field LF_PAD3");
        check(hasField(c, "LF_PAD4"), "field LF_PAD4");
        check(hasField(c, "LF_PAD5"), "field LF_PAD5");
        check(hasField(c, "LF_PAD6"), "field LF_PAD6");
        check(hasField(c, "LF_PAD7"), "field LF_PAD7");
        check(hasField(c, "LF_PAD8"), "field LF_PAD8");
        check(hasField(c, "LF_PAD9"), "field LF_PAD9");
        check(hasField(c, "LF_PAD10"), "field LF_PAD10");
        check(hasField(c, "LF_PAD11"), "field LF_PAD11");
        check(hasField(c, "LF_PAD12"), "field LF_PAD12");
        check(hasField(c, "LF_PAD13"), "field LF_PAD13");
        check(hasField(c, "LF_PAD14"), "field LF_PAD14");
        check(hasField(c, "LF_PAD15"), "field LF_PAD15");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
