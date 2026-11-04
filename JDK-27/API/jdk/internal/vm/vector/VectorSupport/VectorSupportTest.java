/*
 * @test
 * @summary jdk.internal.vm.vector.VectorSupport conformance: internal package reflection.
 * @modules java.base/jdk.internal.vm.vector:+open
 * @run main VectorSupportTest
 */
public class VectorSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.vm.vector.VectorSupport", false, VectorSupportTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "binaryOp"), "method binaryOp");
        check(hasMethod(c, "blend"), "method blend");
        check(hasMethod(c, "broadcastInt"), "method broadcastInt");
        check(hasMethod(c, "compare"), "method compare");
        check(hasMethod(c, "compressExpandOp"), "method compressExpandOp");
        check(hasMethod(c, "convert"), "method convert");
        check(hasMethod(c, "extract"), "method extract");
        check(hasMethod(c, "fromBitsCoerced"), "method fromBitsCoerced");
        check(hasMethod(c, "getCPUFeatures"), "method getCPUFeatures");
        check(hasMethod(c, "getMaxLaneCount"), "method getMaxLaneCount");
        check(hasMethod(c, "indexPartiallyInUpperRange"), "method indexPartiallyInUpperRange");
        check(hasMethod(c, "indexVector"), "method indexVector");
        check(hasMethod(c, "insert"), "method insert");
        check(hasMethod(c, "libraryBinaryOp"), "method libraryBinaryOp");
        check(hasMethod(c, "libraryUnaryOp"), "method libraryUnaryOp");
        check(hasMethod(c, "load"), "method load");
        check(hasMethod(c, "loadMasked"), "method loadMasked");
        check(hasMethod(c, "loadNativeLibrary"), "method loadNativeLibrary");
        check(hasMethod(c, "loadWithMap"), "method loadWithMap");
        check(hasMethod(c, "maskReductionCoerced"), "method maskReductionCoerced");
        check(hasMethod(c, "maybeRebox"), "method maybeRebox");
        check(hasMethod(c, "rearrangeOp"), "method rearrangeOp");
        check(hasMethod(c, "reductionCoerced"), "method reductionCoerced");
        check(hasMethod(c, "selectFromOp"), "method selectFromOp");
        check(hasMethod(c, "selectFromTwoVectorOp"), "method selectFromTwoVectorOp");
        check(hasMethod(c, "store"), "method store");
        check(hasMethod(c, "storeMasked"), "method storeMasked");
        check(hasMethod(c, "storeWithMap"), "method storeWithMap");
        check(hasMethod(c, "ternaryOp"), "method ternaryOp");
        check(hasMethod(c, "test"), "method test");
        check(hasMethod(c, "unaryOp"), "method unaryOp");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "BT_eq"), "field BT_eq");
        check(hasField(c, "BT_ge"), "field BT_ge");
        check(hasField(c, "BT_gt"), "field BT_gt");
        check(hasField(c, "BT_le"), "field BT_le");
        check(hasField(c, "BT_lt"), "field BT_lt");
        check(hasField(c, "BT_ne"), "field BT_ne");
        check(hasField(c, "BT_no_overflow"), "field BT_no_overflow");
        check(hasField(c, "BT_overflow"), "field BT_overflow");
        check(hasField(c, "BT_uge"), "field BT_uge");
        check(hasField(c, "BT_ugt"), "field BT_ugt");
        check(hasField(c, "BT_ule"), "field BT_ule");
        check(hasField(c, "BT_ult"), "field BT_ult");
        check(hasField(c, "BT_unsigned_compare"), "field BT_unsigned_compare");
        check(hasField(c, "LT_BYTE"), "field LT_BYTE");
        check(hasField(c, "LT_DOUBLE"), "field LT_DOUBLE");
        check(hasField(c, "LT_FLOAT"), "field LT_FLOAT");
        check(hasField(c, "LT_INT"), "field LT_INT");
        check(hasField(c, "LT_LONG"), "field LT_LONG");
        check(hasField(c, "LT_SHORT"), "field LT_SHORT");
        check(hasField(c, "MODE_BITS_COERCED_LONG_TO_MASK"), "field MODE_BITS_COERCED_LONG_TO_MASK");
        check(hasField(c, "MODE_BROADCAST"), "field MODE_BROADCAST");
        check(hasField(c, "U"), "field U");
        check(hasField(c, "VECTOR_OP_ABS"), "field VECTOR_OP_ABS");
        check(hasField(c, "VECTOR_OP_ACOS"), "field VECTOR_OP_ACOS");
        check(hasField(c, "VECTOR_OP_ADD"), "field VECTOR_OP_ADD");
        check(hasField(c, "VECTOR_OP_AND"), "field VECTOR_OP_AND");
        check(hasField(c, "VECTOR_OP_ASIN"), "field VECTOR_OP_ASIN");
        check(hasField(c, "VECTOR_OP_ATAN"), "field VECTOR_OP_ATAN");
        check(hasField(c, "VECTOR_OP_ATAN2"), "field VECTOR_OP_ATAN2");
        check(hasField(c, "VECTOR_OP_BIT_COUNT"), "field VECTOR_OP_BIT_COUNT");
        check(hasField(c, "VECTOR_OP_CAST"), "field VECTOR_OP_CAST");
        check(hasField(c, "VECTOR_OP_CBRT"), "field VECTOR_OP_CBRT");
        check(hasField(c, "VECTOR_OP_COMPRESS"), "field VECTOR_OP_COMPRESS");
        check(hasField(c, "VECTOR_OP_COMPRESS_BITS"), "field VECTOR_OP_COMPRESS_BITS");
        check(hasField(c, "VECTOR_OP_COS"), "field VECTOR_OP_COS");
        check(hasField(c, "VECTOR_OP_COSH"), "field VECTOR_OP_COSH");
        check(hasField(c, "VECTOR_OP_DIV"), "field VECTOR_OP_DIV");
        check(hasField(c, "VECTOR_OP_EXP"), "field VECTOR_OP_EXP");
        check(hasField(c, "VECTOR_OP_EXPAND"), "field VECTOR_OP_EXPAND");
        check(hasField(c, "VECTOR_OP_EXPAND_BITS"), "field VECTOR_OP_EXPAND_BITS");
        check(hasField(c, "VECTOR_OP_EXPM1"), "field VECTOR_OP_EXPM1");
        check(hasField(c, "VECTOR_OP_FMA"), "field VECTOR_OP_FMA");
        check(hasField(c, "VECTOR_OP_HYPOT"), "field VECTOR_OP_HYPOT");
        check(hasField(c, "VECTOR_OP_LOG"), "field VECTOR_OP_LOG");
        check(hasField(c, "VECTOR_OP_LOG10"), "field VECTOR_OP_LOG10");
        check(hasField(c, "VECTOR_OP_LOG1P"), "field VECTOR_OP_LOG1P");
        check(hasField(c, "VECTOR_OP_LROTATE"), "field VECTOR_OP_LROTATE");
        check(hasField(c, "VECTOR_OP_LSHIFT"), "field VECTOR_OP_LSHIFT");
        check(hasField(c, "VECTOR_OP_LZ_COUNT"), "field VECTOR_OP_LZ_COUNT");
        check(hasField(c, "VECTOR_OP_MASK_COMPRESS"), "field VECTOR_OP_MASK_COMPRESS");
        check(hasField(c, "VECTOR_OP_MASK_FIRSTTRUE"), "field VECTOR_OP_MASK_FIRSTTRUE");
        check(hasField(c, "VECTOR_OP_MASK_LASTTRUE"), "field VECTOR_OP_MASK_LASTTRUE");
        check(hasField(c, "VECTOR_OP_MASK_TOLONG"), "field VECTOR_OP_MASK_TOLONG");
        check(hasField(c, "VECTOR_OP_MASK_TRUECOUNT"), "field VECTOR_OP_MASK_TRUECOUNT");
        check(hasField(c, "VECTOR_OP_MATHLIB_FIRST"), "field VECTOR_OP_MATHLIB_FIRST");
        check(hasField(c, "VECTOR_OP_MATHLIB_LAST"), "field VECTOR_OP_MATHLIB_LAST");
        check(hasField(c, "VECTOR_OP_MAX"), "field VECTOR_OP_MAX");
        check(hasField(c, "VECTOR_OP_MIN"), "field VECTOR_OP_MIN");
        check(hasField(c, "VECTOR_OP_MUL"), "field VECTOR_OP_MUL");
        check(hasField(c, "VECTOR_OP_NEG"), "field VECTOR_OP_NEG");
        check(hasField(c, "VECTOR_OP_OR"), "field VECTOR_OP_OR");
        check(hasField(c, "VECTOR_OP_POW"), "field VECTOR_OP_POW");
        check(hasField(c, "VECTOR_OP_REINTERPRET"), "field VECTOR_OP_REINTERPRET");
        check(hasField(c, "VECTOR_OP_REVERSE"), "field VECTOR_OP_REVERSE");
        check(hasField(c, "VECTOR_OP_REVERSE_BYTES"), "field VECTOR_OP_REVERSE_BYTES");
        check(hasField(c, "VECTOR_OP_RROTATE"), "field VECTOR_OP_RROTATE");
        check(hasField(c, "VECTOR_OP_RSHIFT"), "field VECTOR_OP_RSHIFT");
        check(hasField(c, "VECTOR_OP_SADD"), "field VECTOR_OP_SADD");
        check(hasField(c, "VECTOR_OP_SIN"), "field VECTOR_OP_SIN");
        check(hasField(c, "VECTOR_OP_SINH"), "field VECTOR_OP_SINH");
        check(hasField(c, "VECTOR_OP_SQRT"), "field VECTOR_OP_SQRT");
        check(hasField(c, "VECTOR_OP_SSUB"), "field VECTOR_OP_SSUB");
        check(hasField(c, "VECTOR_OP_SUADD"), "field VECTOR_OP_SUADD");
        check(hasField(c, "VECTOR_OP_SUB"), "field VECTOR_OP_SUB");
        check(hasField(c, "VECTOR_OP_SUSUB"), "field VECTOR_OP_SUSUB");
        check(hasField(c, "VECTOR_OP_TAN"), "field VECTOR_OP_TAN");
        check(hasField(c, "VECTOR_OP_TANH"), "field VECTOR_OP_TANH");
        check(hasField(c, "VECTOR_OP_TZ_COUNT"), "field VECTOR_OP_TZ_COUNT");
        check(hasField(c, "VECTOR_OP_UCAST"), "field VECTOR_OP_UCAST");
        check(hasField(c, "VECTOR_OP_UMAX"), "field VECTOR_OP_UMAX");
        check(hasField(c, "VECTOR_OP_UMIN"), "field VECTOR_OP_UMIN");
        check(hasField(c, "VECTOR_OP_URSHIFT"), "field VECTOR_OP_URSHIFT");
        check(hasField(c, "VECTOR_OP_XOR"), "field VECTOR_OP_XOR");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
