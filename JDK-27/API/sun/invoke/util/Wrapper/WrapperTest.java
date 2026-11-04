/*
 * @test
 * @summary sun.invoke.util.Wrapper conformance: internal package reflection.
 * @modules java.base/sun.invoke.util:+open
 * @run main WrapperTest
 */
public class WrapperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.invoke.util.Wrapper", false, WrapperTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "arrayType"), "method arrayType");
        check(hasMethod(c, "asPrimitiveType"), "method asPrimitiveType");
        check(hasMethod(c, "asWrapperType"), "method asWrapperType");
        check(hasMethod(c, "basicClassDescriptor"), "method basicClassDescriptor");
        check(hasMethod(c, "basicTypeChar"), "method basicTypeChar");
        check(hasMethod(c, "basicTypeString"), "method basicTypeString");
        check(hasMethod(c, "bitWidth"), "method bitWidth");
        check(hasMethod(c, "cast"), "method cast");
        check(hasMethod(c, "convert"), "method convert");
        check(hasMethod(c, "copyArrayBoxing"), "method copyArrayBoxing");
        check(hasMethod(c, "copyArrayUnboxing"), "method copyArrayUnboxing");
        check(hasMethod(c, "detailString"), "method detailString");
        check(hasMethod(c, "forBasicType"), "method forBasicType");
        check(hasMethod(c, "forPrimitiveType"), "method forPrimitiveType");
        check(hasMethod(c, "forWrapperType"), "method forWrapperType");
        check(hasMethod(c, "isConvertibleFrom"), "method isConvertibleFrom");
        check(hasMethod(c, "isDoubleWord"), "method isDoubleWord");
        check(hasMethod(c, "isFloating"), "method isFloating");
        check(hasMethod(c, "isIntegral"), "method isIntegral");
        check(hasMethod(c, "isNumeric"), "method isNumeric");
        check(hasMethod(c, "isOther"), "method isOther");
        check(hasMethod(c, "isPrimitiveType"), "method isPrimitiveType");
        check(hasMethod(c, "isSigned"), "method isSigned");
        check(hasMethod(c, "isSingleWord"), "method isSingleWord");
        check(hasMethod(c, "isStrictSubRangeOf"), "method isStrictSubRangeOf");
        check(hasMethod(c, "isSubwordOrInt"), "method isSubwordOrInt");
        check(hasMethod(c, "isUnsigned"), "method isUnsigned");
        check(hasMethod(c, "isWrapperNumericOrBooleanType"), "method isWrapperNumericOrBooleanType");
        check(hasMethod(c, "isWrapperType"), "method isWrapperType");
        check(hasMethod(c, "makeArray"), "method makeArray");
        check(hasMethod(c, "primitiveSimpleName"), "method primitiveSimpleName");
        check(hasMethod(c, "primitiveType"), "method primitiveType");
        check(hasMethod(c, "stackSlots"), "method stackSlots");
        check(hasMethod(c, "valueOf"), "method valueOf");
        check(hasMethod(c, "values"), "method values");
        check(hasMethod(c, "wrap"), "method wrap");
        check(hasMethod(c, "wrapperClassDescriptor"), "method wrapperClassDescriptor");
        check(hasMethod(c, "wrapperSimpleName"), "method wrapperSimpleName");
        check(hasMethod(c, "wrapperType"), "method wrapperType");
        check(hasMethod(c, "zero"), "method zero");
        check(hasField(c, "$VALUES"), "field $VALUES");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "BOOLEAN"), "field BOOLEAN");
        check(hasField(c, "BYTE"), "field BYTE");
        check(hasField(c, "CHAR"), "field CHAR");
        check(hasField(c, "COUNT"), "field COUNT");
        check(hasField(c, "DOUBLE"), "field DOUBLE");
        check(hasField(c, "DOUBLE_ZERO"), "field DOUBLE_ZERO");
        check(hasField(c, "FLOAT"), "field FLOAT");
        check(hasField(c, "FLOAT_ZERO"), "field FLOAT_ZERO");
        check(hasField(c, "FROM_CHAR"), "field FROM_CHAR");
        check(hasField(c, "INT"), "field INT");
        check(hasField(c, "LONG"), "field LONG");
        check(hasField(c, "OBJECT"), "field OBJECT");
        check(hasField(c, "SHORT"), "field SHORT");
        check(hasField(c, "VOID"), "field VOID");
        check(hasField(c, "basicClassDesc"), "field basicClassDesc");
        check(hasField(c, "basicTypeChar"), "field basicTypeChar");
        check(hasField(c, "basicTypeString"), "field basicTypeString");
        check(hasField(c, "emptyArray"), "field emptyArray");
        check(hasField(c, "format"), "field format");
        check(hasField(c, "numericClass"), "field numericClass");
        check(hasField(c, "primitiveSimpleName"), "field primitiveSimpleName");
        check(hasField(c, "primitiveType"), "field primitiveType");
        check(hasField(c, "superClasses"), "field superClasses");
        check(hasField(c, "wrapperClassDesc"), "field wrapperClassDesc");
        check(hasField(c, "wrapperSimpleName"), "field wrapperSimpleName");
        check(hasField(c, "wrapperType"), "field wrapperType");
        check(hasEnumConstant(c, "BOOLEAN"), "enum BOOLEAN");
        check(hasEnumConstant(c, "BYTE"), "enum BYTE");
        check(hasEnumConstant(c, "CHAR"), "enum CHAR");
        check(hasEnumConstant(c, "DOUBLE"), "enum DOUBLE");
        check(hasEnumConstant(c, "FLOAT"), "enum FLOAT");
        check(hasEnumConstant(c, "INT"), "enum INT");
        check(hasEnumConstant(c, "LONG"), "enum LONG");
        check(hasEnumConstant(c, "OBJECT"), "enum OBJECT");
        check(hasEnumConstant(c, "SHORT"), "enum SHORT");
        check(hasEnumConstant(c, "VOID"), "enum VOID");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
