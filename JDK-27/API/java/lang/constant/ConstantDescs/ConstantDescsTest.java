/*
 * @test
 * @summary java.lang.constant.ConstantDescs conformance: white-box + black-box behavior.
 * @run main ConstantDescsTest
 */

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.DynamicConstantDesc;
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

    // ---- Part 3: deep contract (canonical constant table + equivalence) ----
    static void testDeepContract() throws Exception {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        // full primitive descriptor table (9 primitive/void constants)
        String[][] prims = {
                {"CD_boolean", "Z"}, {"CD_byte", "B"}, {"CD_char", "C"}, {"CD_short", "S"},
                {"CD_int", "I"}, {"CD_long", "J"}, {"CD_float", "F"}, {"CD_double", "D"},
                {"CD_void", "V"},
        };
        for (String[] pair : prims)
            check(ds((ClassDesc) field(pair[0])).equals(pair[1]),
                  pair[0] + " descriptor " + pair[1]);

        // boxed + core reference constants (15 more canonical descriptors)
        String[][] refs = {
                {"CD_Object", "Ljava/lang/Object;"},
                {"CD_String", "Ljava/lang/String;"},
                {"CD_Integer", "Ljava/lang/Integer;"},
                {"CD_Boolean", "Ljava/lang/Boolean;"},
                {"CD_Byte", "Ljava/lang/Byte;"},
                {"CD_Character", "Ljava/lang/Character;"},
                {"CD_Short", "Ljava/lang/Short;"},
                {"CD_Long", "Ljava/lang/Long;"},
                {"CD_Float", "Ljava/lang/Float;"},
                {"CD_Double", "Ljava/lang/Double;"},
                {"CD_Number", "Ljava/lang/Number;"},
                {"CD_Exception", "Ljava/lang/Exception;"},
                {"CD_Throwable", "Ljava/lang/Throwable;"},
                {"CD_Collection", "Ljava/util/Collection;"},
                {"CD_Set", "Ljava/util/Set;"},
                {"CD_MethodHandle", "Ljava/lang/invoke/MethodHandle;"},
                {"CD_MethodType", "Ljava/lang/invoke/MethodType;"},
                {"CD_VarHandle", "Ljava/lang/invoke/VarHandle;"},
                {"CD_CallSite", "Ljava/lang/invoke/CallSite;"},
                {"CD_ConstantDesc", "Ljava/lang/constant/ConstantDesc;"},
                {"CD_ConstantBootstraps", "Ljava/lang/invoke/ConstantBootstraps;"},
        };
        for (String[] pair : refs)
            check(ds((ClassDesc) field(pair[0])).equals(pair[1]),
                  pair[0] + " descriptor exact");

        // canonical constants equal freshly-built descriptors by value (but are not the same instance)
        ClassDesc cdString = (ClassDesc) field("CD_String");
        ClassDesc built = ClassDesc.of("java.lang.String");
        check(cdString.equals(built), "CD_String equals ClassDesc.of(...)");
        check(cdString != built, "CD_String is not the same instance as of() (no interning)");
        check(cdString.hashCode() == built.hashCode(), "CD_String hashCode agrees with of()");
        check(ConstantDescs.CD_int.equals(ClassDesc.ofDescriptor("I")),
              "CD_int equals ofDescriptor(\"I\")");

        // canonical constants resolve
        check(((ClassDesc) field("CD_Number")).resolveConstantDesc(lookup) == Number.class,
              "CD_Number resolves");
        check(((ClassDesc) field("CD_Collection"))
                      .resolveConstantDesc(lookup) == java.util.Collection.class,
              "CD_Collection resolves");

        // every BSM constant is a resolvable STATIC method; most live on
        // ConstantBootstraps, the class-data pair lives on MethodHandles
        String[] bsmFields = {"BSM_PRIMITIVE_CLASS", "BSM_ENUM_CONSTANT", "BSM_GET_STATIC_FINAL",
                              "BSM_NULL_CONSTANT", "BSM_VARHANDLE_FIELD", "BSM_VARHANDLE_STATIC_FIELD",
                              "BSM_VARHANDLE_ARRAY", "BSM_INVOKE", "BSM_EXPLICIT_CAST"};
        for (String name : bsmFields) {
            DirectMethodHandleDesc d = (DirectMethodHandleDesc) field(name);
            check(d.kind() == DirectMethodHandleDesc.Kind.STATIC, name + " STATIC kind");
            check(d.owner().descriptorString().equals("Ljava/lang/invoke/ConstantBootstraps;"),
                  name + " owner ConstantBootstraps");
            String ts = d.toString();
            check(ts.contains("ConstantBootstraps"), name + " toString names owner");
            check(d.resolveConstantDesc(lookup) != null, name + " resolves to a MethodHandle");
        }
        for (String name : new String[]{"BSM_CLASS_DATA", "BSM_CLASS_DATA_AT"}) {
            DirectMethodHandleDesc d = (DirectMethodHandleDesc) field(name);
            check(d.kind() == DirectMethodHandleDesc.Kind.STATIC, name + " STATIC kind");
            check(d.owner().descriptorString().equals("Ljava/lang/invoke/MethodHandles;"),
                  name + " owner MethodHandles (class-data bootstraps)");
            check(d.resolveConstantDesc(lookup) != null, name + " resolves to a MethodHandle");
        }

        // MTD / dynamic-constant table precision
        check(ConstantDescs.MTD_void.equals(MethodTypeDesc.ofDescriptor("()V")),
              "MTD_void equals ofDescriptor(\"()V\")");
        check(ConstantDescs.TRUE.resolveConstantDesc(lookup).getClass() == Boolean.class,
              "TRUE resolves to a Boolean");
        check(ConstantDescs.NULL.equals(DynamicConstantDesc.ofNamed(
                      ConstantDescs.BSM_NULL_CONSTANT, "_", ConstantDescs.CD_Object)),
              "NULL equals an equivalent freshly-built dynamic constant");
    }

    static Object field(String name) throws Exception {
        Field f = ConstantDescs.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.get(null);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
