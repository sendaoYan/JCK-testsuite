/*
 * @test
 * @summary sun.jvm.hotspot.debugger.win32.coff.COFFSymbolConstants conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.win32.coff:+open
 * @run main COFFSymbolConstantsTest
 */
import java.lang.reflect.*;

public class COFFSymbolConstantsTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.win32.coff.COFFSymbolConstants", false, COFFSymbolConstantsTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasField(c, "IMAGE_SYM_UNDEFINED"), "field IMAGE_SYM_UNDEFINED");
        check(hasField(c, "IMAGE_SYM_ABSOLUTE"), "field IMAGE_SYM_ABSOLUTE");
        check(hasField(c, "IMAGE_SYM_DEBUG"), "field IMAGE_SYM_DEBUG");
        check(hasField(c, "IMAGE_SYM_TYPE_NULL"), "field IMAGE_SYM_TYPE_NULL");
        check(hasField(c, "IMAGE_SYM_TYPE_VOID"), "field IMAGE_SYM_TYPE_VOID");
        check(hasField(c, "IMAGE_SYM_TYPE_CHAR"), "field IMAGE_SYM_TYPE_CHAR");
        check(hasField(c, "IMAGE_SYM_TYPE_SHORT"), "field IMAGE_SYM_TYPE_SHORT");
        check(hasField(c, "IMAGE_SYM_TYPE_INT"), "field IMAGE_SYM_TYPE_INT");
        check(hasField(c, "IMAGE_SYM_TYPE_LONG"), "field IMAGE_SYM_TYPE_LONG");
        check(hasField(c, "IMAGE_SYM_TYPE_FLOAT"), "field IMAGE_SYM_TYPE_FLOAT");
        check(hasField(c, "IMAGE_SYM_TYPE_DOUBLE"), "field IMAGE_SYM_TYPE_DOUBLE");
        check(hasField(c, "IMAGE_SYM_TYPE_STRUCT"), "field IMAGE_SYM_TYPE_STRUCT");
        check(hasField(c, "IMAGE_SYM_TYPE_UNION"), "field IMAGE_SYM_TYPE_UNION");
        check(hasField(c, "IMAGE_SYM_TYPE_ENUM"), "field IMAGE_SYM_TYPE_ENUM");
        check(hasField(c, "IMAGE_SYM_TYPE_MOE"), "field IMAGE_SYM_TYPE_MOE");
        check(hasField(c, "IMAGE_SYM_TYPE_BYTE"), "field IMAGE_SYM_TYPE_BYTE");
        check(hasField(c, "IMAGE_SYM_TYPE_WORD"), "field IMAGE_SYM_TYPE_WORD");
        check(hasField(c, "IMAGE_SYM_TYPE_UINT"), "field IMAGE_SYM_TYPE_UINT");
        check(hasField(c, "IMAGE_SYM_TYPE_DWORD"), "field IMAGE_SYM_TYPE_DWORD");
        check(hasField(c, "IMAGE_SYM_DTYPE_NULL"), "field IMAGE_SYM_DTYPE_NULL");
        check(hasField(c, "IMAGE_SYM_DTYPE_POINTER"), "field IMAGE_SYM_DTYPE_POINTER");
        check(hasField(c, "IMAGE_SYM_DTYPE_FUNCTION"), "field IMAGE_SYM_DTYPE_FUNCTION");
        check(hasField(c, "IMAGE_SYM_DTYPE_ARRAY"), "field IMAGE_SYM_DTYPE_ARRAY");
        check(hasField(c, "IMAGE_SYM_CLASS_END_OF_FUNCTION"), "field IMAGE_SYM_CLASS_END_OF_FUNCTION");
        check(hasField(c, "IMAGE_SYM_CLASS_NULL"), "field IMAGE_SYM_CLASS_NULL");
        check(hasField(c, "IMAGE_SYM_CLASS_AUTOMATIC"), "field IMAGE_SYM_CLASS_AUTOMATIC");
        check(hasField(c, "IMAGE_SYM_CLASS_EXTERNAL"), "field IMAGE_SYM_CLASS_EXTERNAL");
        check(hasField(c, "IMAGE_SYM_CLASS_STATIC"), "field IMAGE_SYM_CLASS_STATIC");
        check(hasField(c, "IMAGE_SYM_CLASS_REGISTER"), "field IMAGE_SYM_CLASS_REGISTER");
        check(hasField(c, "IMAGE_SYM_CLASS_EXTERNAL_DEF"), "field IMAGE_SYM_CLASS_EXTERNAL_DEF");
        check(hasField(c, "IMAGE_SYM_CLASS_LABEL"), "field IMAGE_SYM_CLASS_LABEL");
        check(hasField(c, "IMAGE_SYM_CLASS_UNDEFINED_LABEL"), "field IMAGE_SYM_CLASS_UNDEFINED_LABEL");
        check(hasField(c, "IMAGE_SYM_CLASS_MEMBER_OF_STRUCT"), "field IMAGE_SYM_CLASS_MEMBER_OF_STRUCT");
        check(hasField(c, "IMAGE_SYM_CLASS_ARGUMENT"), "field IMAGE_SYM_CLASS_ARGUMENT");
        check(hasField(c, "IMAGE_SYM_CLASS_STRUCT_TAG"), "field IMAGE_SYM_CLASS_STRUCT_TAG");
        check(hasField(c, "IMAGE_SYM_CLASS_MEMBER_OF_UNION"), "field IMAGE_SYM_CLASS_MEMBER_OF_UNION");
        check(hasField(c, "IMAGE_SYM_CLASS_UNION_TAG"), "field IMAGE_SYM_CLASS_UNION_TAG");
        check(hasField(c, "IMAGE_SYM_CLASS_TYPE_DEFINITION"), "field IMAGE_SYM_CLASS_TYPE_DEFINITION");
        check(hasField(c, "IMAGE_SYM_CLASS_UNDEFINED_STATIC"), "field IMAGE_SYM_CLASS_UNDEFINED_STATIC");
        check(hasField(c, "IMAGE_SYM_CLASS_ENUM_TAG"), "field IMAGE_SYM_CLASS_ENUM_TAG");
        check(hasField(c, "IMAGE_SYM_CLASS_MEMBER_OF_ENUM"), "field IMAGE_SYM_CLASS_MEMBER_OF_ENUM");
        check(hasField(c, "IMAGE_SYM_CLASS_REGISTER_PARAM"), "field IMAGE_SYM_CLASS_REGISTER_PARAM");
        check(hasField(c, "IMAGE_SYM_CLASS_BIT_FIELD"), "field IMAGE_SYM_CLASS_BIT_FIELD");
        check(hasField(c, "IMAGE_SYM_CLASS_BLOCK"), "field IMAGE_SYM_CLASS_BLOCK");
        check(hasField(c, "IMAGE_SYM_CLASS_FUNCTION"), "field IMAGE_SYM_CLASS_FUNCTION");
        check(hasField(c, "IMAGE_SYM_CLASS_END_OF_STRUCT"), "field IMAGE_SYM_CLASS_END_OF_STRUCT");
        check(hasField(c, "IMAGE_SYM_CLASS_FILE"), "field IMAGE_SYM_CLASS_FILE");
        check(hasField(c, "IMAGE_SYM_CLASS_SECTION"), "field IMAGE_SYM_CLASS_SECTION");
        check(hasField(c, "IMAGE_SYM_CLASS_WEAK_EXTERNAL"), "field IMAGE_SYM_CLASS_WEAK_EXTERNAL");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
