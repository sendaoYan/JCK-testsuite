/*
 * @test
 * @summary java.lang.constant.ConstantDescs conformance: white-box + black-box behavior.
 * @run main ConstantDescsTest
 */

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ConstantDescsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static String ds(ClassDesc cd) { return cd.descriptorString(); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.constant.ConstantDescs");
        check(Modifier.isFinal(c.getModifiers()), "final class");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        int pubCtors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (Modifier.isPublic(ct.getModifiers())) pubCtors++;
        }
        check(pubCtors == 0, "no public constructor (constants-only holder)");
        int constants = 0;
        for (Field f : c.getDeclaredFields()) {
            if (Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers())
                    && Modifier.isFinal(f.getModifiers())) constants++;
        }
        check(constants >= 60, "declares the full set of public constants: " + constants);
        check(Modifier.isStatic(c.getDeclaredMethod("ofCallsiteBootstrap",
                ClassDesc.class, String.class, ClassDesc.class, ClassDesc[].class).getModifiers()),
              "ofCallsiteBootstrap static");
        check(Modifier.isStatic(c.getDeclaredMethod("ofConstantBootstrap",
                ClassDesc.class, String.class, ClassDesc.class, ClassDesc[].class).getModifiers()),
              "ofConstantBootstrap static");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        // string constants
        check(ConstantDescs.DEFAULT_NAME.equals("_"), "DEFAULT_NAME is _");
        check(ConstantDescs.INIT_NAME.equals("<init>"), "INIT_NAME");
        check(ConstantDescs.CLASS_INIT_NAME.equals("<clinit>"), "CLASS_INIT_NAME");

        // reference-type ClassDesc constants: exact descriptor strings
        check(ds(ConstantDescs.CD_Object).equals("Ljava/lang/Object;"), "CD_Object");
        check(ds(ConstantDescs.CD_String).equals("Ljava/lang/String;"), "CD_String");
        check(ds(ConstantDescs.CD_Class).equals("Ljava/lang/Class;"), "CD_Class");
        check(ds(ConstantDescs.CD_Integer).equals("Ljava/lang/Integer;"), "CD_Integer");
        check(ds(ConstantDescs.CD_Enum).equals("Ljava/lang/Enum;"), "CD_Enum");
        check(ds(ConstantDescs.CD_List).equals("Ljava/util/List;"), "CD_List");
        check(ds(ConstantDescs.CD_Map).equals("Ljava/util/Map;"), "CD_Map");
        check(ds(ConstantDescs.CD_MethodHandles_Lookup)
                      .equals("Ljava/lang/invoke/MethodHandles$Lookup;"), "CD_MethodHandles_Lookup");
        check(ds(ConstantDescs.CD_DynamicConstantDesc)
                      .equals("Ljava/lang/constant/DynamicConstantDesc;"), "CD_DynamicConstantDesc");
        // and they resolve to the real runtime classes
        check(ConstantDescs.CD_String.resolveConstantDesc(lookup) == String.class,
              "CD_String resolves");
        check(ConstantDescs.CD_Object.resolveConstantDesc(lookup) == Object.class,
              "CD_Object resolves");

        // primitive ClassDesc constants
        check(ds(ConstantDescs.CD_int).equals("I"), "CD_int");
        check(ds(ConstantDescs.CD_long).equals("J"), "CD_long");
        check(ds(ConstantDescs.CD_float).equals("F"), "CD_float");
        check(ds(ConstantDescs.CD_double).equals("D"), "CD_double");
        check(ds(ConstantDescs.CD_void).equals("V"), "CD_void");
        check(ConstantDescs.CD_int.isPrimitive() && !ConstantDescs.CD_int.isArray(),
              "CD_int primitive");

        // MTD_void
        check(ConstantDescs.MTD_void.descriptorString().equals("()V"), "MTD_void descriptor");
        check(ConstantDescs.MTD_void.resolveConstantDesc(lookup)
                      == java.lang.invoke.MethodType.methodType(void.class),
              "MTD_void resolves");

        // bootstrap method constants: owner ConstantBootstraps, STATIC kind
        String[][] bsmNames = {
                {"BSM_PRIMITIVE_CLASS", "primitiveClass"},
                {"BSM_ENUM_CONSTANT", "enumConstant"},
                {"BSM_GET_STATIC_FINAL", "getStaticFinal"},
                {"BSM_NULL_CONSTANT", "nullConstant"},
                {"BSM_VARHANDLE_FIELD", "fieldVarHandle"},
                {"BSM_VARHANDLE_STATIC_FIELD", "staticFieldVarHandle"},
                {"BSM_VARHANDLE_ARRAY", "arrayVarHandle"},
                {"BSM_INVOKE", "invoke"},
                {"BSM_EXPLICIT_CAST", "explicitCast"},
        };
        for (String[] pair : bsmNames) {
            Field f = ConstantDescs.class.getDeclaredField(pair[0]);
            f.setAccessible(true);
            DirectMethodHandleDesc d = (DirectMethodHandleDesc) f.get(null);
            check(d.methodName().equals(pair[1]), pair[0] + " methodName");
            check(d.owner().descriptorString().equals("Ljava/lang/invoke/ConstantBootstraps;"),
                  pair[0] + " owner");
            check(d.kind() == DirectMethodHandleDesc.Kind.STATIC, pair[0] + " STATIC");
            check(d.refKind() == 6, pair[0] + " refKind 6");
            check(!d.isOwnerInterface(), pair[0] + " not interface");
            check(d.lookupDescriptor().startsWith("(Ljava/lang/invoke/MethodHandles$Lookup;"),
                  pair[0] + " lookup takes Lookup first");
        }
        // BSM_ENUM_CONSTANT's signature exactly
        check(ConstantDescs.BSM_ENUM_CONSTANT.lookupDescriptor()
                      .equals("(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Enum;"),
              "BSM_ENUM_CONSTANT signature");
        // bootstraps are resolvable to real method handles
        check(ConstantDescs.BSM_NULL_CONSTANT.resolveConstantDesc(lookup) != null,
              "BSM_NULL_CONSTANT resolves");

        // ofCallsiteBootstrap / ofConstantBootstrap build (Lookup, name, leading-type, extras...) type
        DirectMethodHandleDesc cs = ConstantDescs.ofCallsiteBootstrap(
                ConstantDescs.CD_Object, "bs", ConstantDescs.CD_Object);
        check(cs.lookupDescriptor().equals(
                "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/Object;"),
              "ofCallsiteBootstrap descriptor: " + cs.lookupDescriptor());
        DirectMethodHandleDesc cb = ConstantDescs.ofConstantBootstrap(
                ConstantDescs.CD_Object, "bs", ConstantDescs.CD_Integer, ConstantDescs.CD_String);
        check(cb.lookupDescriptor().equals(
                "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Integer;"),
              "ofConstantBootstrap descriptor: " + cb.lookupDescriptor());
        check(cb.kind() == DirectMethodHandleDesc.Kind.STATIC, "ofConstantBootstrap STATIC");
        check(cb.owner().equals(ConstantDescs.CD_Object), "ofConstantBootstrap owner");

        // resolution of the well-known dynamic constants
        check(ConstantDescs.NULL.resolveConstantDesc(lookup) == null, "NULL -> null");
        check(ConstantDescs.TRUE.resolveConstantDesc(lookup) == Boolean.TRUE, "TRUE -> true");
        check(ConstantDescs.FALSE.resolveConstantDesc(lookup) == Boolean.FALSE, "FALSE -> false");
        check(ConstantDescs.NULL instanceof ConstantDesc, "NULL is a ConstantDesc");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
