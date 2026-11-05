/*
 * @test
 * @summary java.lang.constant.DirectMethodHandleDesc conformance: white-box + black-box behavior.
 * @run main DirectMethodHandleDescTest
 */

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public class DirectMethodHandleDescTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.constant.DirectMethodHandleDesc");
        check(c.isInterface(), "is an interface");
        check(Class.forName("java.lang.constant.MethodHandleDesc").isAssignableFrom(c),
              "extends MethodHandleDesc");
        for (String m : new String[]{"kind", "refKind", "isOwnerInterface", "owner",
                                      "methodName", "lookupDescriptor"}) {
            boolean found = false;
            for (java.lang.reflect.Method mm : c.getDeclaredMethods())
                if (mm.getName().equals(m)) found = true;
            check(found, "declares " + m);
        }
        check(c.getDeclaredMethod("kind").getReturnType()
                      == Class.forName("java.lang.constant.DirectMethodHandleDesc$Kind"),
              "kind() returns Kind");
        check(c.getDeclaredMethod("refKind").getReturnType() == int.class, "refKind int");
        check(c.getDeclaredMethod("isOwnerInterface").getReturnType() == boolean.class,
              "isOwnerInterface boolean");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        // virtual method: String.length()
        DirectMethodHandleDesc len = MethodHandleDesc.ofMethod(
                DirectMethodHandleDesc.Kind.VIRTUAL, ClassDesc.of("java.lang.String"),
                "length", MethodTypeDesc.ofDescriptor("()I"));
        check(len.kind() == DirectMethodHandleDesc.Kind.VIRTUAL, "kind VIRTUAL");
        check(len.refKind() == 5, "VIRTUAL refKind 5");
        check(!len.isOwnerInterface(), "String is not an interface owner");
        check(len.owner().descriptorString().equals("Ljava/lang/String;"), "owner");
        check(len.methodName().equals("length"), "methodName");
        check(len.lookupDescriptor().equals("()I"), "lookupDescriptor");
        check(len.invocationType().descriptorString().equals("(Ljava/lang/String;)I"),
              "invocationType inserts receiver");
        MethodHandle h = len.resolveConstantDesc(lookup);
        check((int) h.invoke("abcde") == 5, "resolves and invokes");

        // interface virtual: owner is an interface
        DirectMethodHandleDesc size = MethodHandleDesc.ofMethod(
                DirectMethodHandleDesc.Kind.INTERFACE_VIRTUAL, ClassDesc.of("java.util.List"),
                "size", MethodTypeDesc.ofDescriptor("()I"));
        check(size.isOwnerInterface(), "List owner is interface");
        check(size.refKind() == 9, "INTERFACE_VIRTUAL refKind 9");
        check(size.kind() == DirectMethodHandleDesc.Kind.INTERFACE_VIRTUAL, "kind");

        // constructor: methodName is <init>, invocation returns the new type
        DirectMethodHandleDesc ctor = MethodHandleDesc.ofConstructor(
                ClassDesc.of("java.lang.String"), ConstantDescs.CD_String);
        check(ctor.kind() == DirectMethodHandleDesc.Kind.CONSTRUCTOR, "CONSTRUCTOR kind");
        check(ctor.methodName().equals("<init>"), "constructor methodName <init>");
        check(ctor.lookupDescriptor().equals("(Ljava/lang/String;)V"),
              "constructor lookup returns void");
        check(ctor.invocationType().descriptorString().equals("(Ljava/lang/String;)Ljava/lang/String;"),
              "constructor invocation returns the class");
        check(ctor.resolveConstantDesc(lookup).invoke("zz").equals("zz"),
              "constructor resolves and invokes");

        // field access kinds
        DirectMethodHandleDesc minVal = MethodHandleDesc.ofField(
                DirectMethodHandleDesc.Kind.STATIC_GETTER, ClassDesc.of("java.lang.Integer"),
                "MIN_VALUE", ConstantDescs.CD_int);
        check(minVal.kind() == DirectMethodHandleDesc.Kind.STATIC_GETTER, "STATIC_GETTER kind");
        check(minVal.refKind() == 2, "STATIC_GETTER refKind 2");
        check((int) minVal.resolveConstantDesc(lookup).invoke() == Integer.MIN_VALUE,
              "static getter resolves and reads");
        DirectMethodHandleDesc getter = MethodHandleDesc.ofField(
                DirectMethodHandleDesc.Kind.GETTER, ClassDesc.of("java.lang.Integer"),
                "value", ConstantDescs.CD_int);
        check(getter.kind() == DirectMethodHandleDesc.Kind.GETTER, "GETTER kind");
        check(getter.refKind() == 1, "GETTER refKind 1");

        // value semantics: same parts -> equal descriptors
        DirectMethodHandleDesc len2 = MethodHandleDesc.ofMethod(
                DirectMethodHandleDesc.Kind.VIRTUAL, ClassDesc.of("java.lang.String"),
                "length", MethodTypeDesc.ofDescriptor("()I"));
        check(len.equals(len2), "equals by value");
        check(len.hashCode() == len2.hashCode(), "hashCode consistent");
        check(!len.equals(size), "!equals different method");
        check(!len.equals(null) && !len.equals("length"), "!equals unrelated");

        // well-known constants conform to the same accessors
        check(ConstantDescs.BSM_ENUM_CONSTANT.methodName().equals("enumConstant"),
              "BSM accessor agreement");

        // resolution failure for a missing member
        try {
            MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.VIRTUAL,
                    ClassDesc.of("java.lang.String"), "noSuchMethod",
                    MethodTypeDesc.ofDescriptor("()I")).resolveConstantDesc(lookup);
            check(false, "missing method must fail resolution");
        } catch (ReflectiveOperationException e) {
            check(true, "missing method -> NoSuchMethodException family");
        }
    }

    // ---- Part 3: deep contract (Kind matrix + field accessors + kind misuse) ----
    static final int FIELD_INT = 7;
    public int instanceField = FIELD_INT;

    static void testDeepContract() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        // complete Kind matrix: name -> (refKind, isInterface); refKinds follow MethodHandleInfo
        Object[][] matrix = {
                {"STATIC", 6, false}, {"INTERFACE_STATIC", 6, true},
                {"VIRTUAL", 5, false}, {"INTERFACE_VIRTUAL", 9, true},
                {"SPECIAL", 7, false}, {"INTERFACE_SPECIAL", 7, true},
                {"CONSTRUCTOR", 8, false},
                {"GETTER", 1, false}, {"SETTER", 3, false},
                {"STATIC_GETTER", 2, false}, {"STATIC_SETTER", 4, false},
        };
        check(DirectMethodHandleDesc.Kind.values().length == matrix.length,
              "Kind declares exactly " + matrix.length + " members");
        for (Object[] row : matrix) {
            DirectMethodHandleDesc.Kind k = DirectMethodHandleDesc.Kind.valueOf((String) row[0]);
            check(k.refKind == (Integer) row[1], (String) row[0] + " field refKind " + row[1]);
            check(k.isInterface == (Boolean) row[2], (String) row[0] + " field isInterface");
            check(k.toString().equals(row[0]), (String) row[0] + " toString is the name");
        }

        // Kind.valueOf(int) covers refKinds 1..9; 0 and 10 are rejected
        DirectMethodHandleDesc.Kind[] byRef = {
                null, DirectMethodHandleDesc.Kind.GETTER, DirectMethodHandleDesc.Kind.STATIC_GETTER,
                DirectMethodHandleDesc.Kind.SETTER, DirectMethodHandleDesc.Kind.STATIC_SETTER,
                DirectMethodHandleDesc.Kind.VIRTUAL, DirectMethodHandleDesc.Kind.STATIC,
                DirectMethodHandleDesc.Kind.SPECIAL, DirectMethodHandleDesc.Kind.CONSTRUCTOR,
                DirectMethodHandleDesc.Kind.INTERFACE_VIRTUAL,
        };
        for (int rk = 1; rk <= 9; rk++)
            check(DirectMethodHandleDesc.Kind.valueOf(rk) == byRef[rk],
                  "valueOf(" + rk + ") mapping");
        for (int bad : new int[]{0, 10, -1}) {
            try {
                DirectMethodHandleDesc.Kind.valueOf(bad);
                check(false, "valueOf(" + bad + ") rejected");
            } catch (IllegalArgumentException expected) {
                check(true, "valueOf(" + bad + ") -> IAE");
            }
        }
        // valueOf(refKind, isInterface) disambiguates the two shared refKinds
        check(DirectMethodHandleDesc.Kind.valueOf(6, false) == DirectMethodHandleDesc.Kind.STATIC
                      && DirectMethodHandleDesc.Kind.valueOf(6, true)
                                 == DirectMethodHandleDesc.Kind.INTERFACE_STATIC,
              "valueOf(6, iface) disambiguates STATIC family");
        check(DirectMethodHandleDesc.Kind.valueOf(7, true)
                      == DirectMethodHandleDesc.Kind.INTERFACE_SPECIAL,
              "valueOf(7, true) is INTERFACE_SPECIAL");

        // real field write closure: SETTER mutates, GETTER reads back
        ClassDesc owner = ClassDesc.of("DirectMethodHandleDescTest");
        DirectMethodHandleDesc setter = MethodHandleDesc.ofField(
                DirectMethodHandleDesc.Kind.SETTER, owner, "instanceField", ConstantDescs.CD_int);
        check(setter.refKind() == 3, "SETTER refKind 3 (putField)");
        check(setter.lookupDescriptor().equals("I"), "field lookupDescriptor is the field type I");
        check(setter.invocationType().descriptorString()
                      .equals("(LDirectMethodHandleDescTest;I)V"),
              "SETTER invocationType (owner,int)void");
        DirectMethodHandleDescTest host = new DirectMethodHandleDescTest();
        setter.resolveConstantDesc(lookup).invoke(host, 1234);
        check(host.instanceField == 1234, "SETTER resolved and wrote the field");
        DirectMethodHandleDesc getter = MethodHandleDesc.ofField(
                DirectMethodHandleDesc.Kind.GETTER, owner, "instanceField", ConstantDescs.CD_int);
        check(getter.invocationType().descriptorString()
                      .equals("(LDirectMethodHandleDescTest;)I"),
              "GETTER invocationType (owner)int");
        check((int) getter.resolveConstantDesc(lookup).invoke(host) == 1234,
              "GETTER resolved and read the written value");

        // kind misuse: VIRTUAL against a static method fails with IllegalAccessException
        DirectMethodHandleDesc misKind = MethodHandleDesc.ofMethod(
                DirectMethodHandleDesc.Kind.VIRTUAL, ConstantDescs.CD_String, "valueOf",
                MethodTypeDesc.ofDescriptor("(I)Ljava/lang/String;"));
        try {
            misKind.resolveConstantDesc(lookup);
            check(false, "VIRTUAL on static must fail");
        } catch (IllegalAccessException expected) {
            check(true, "VIRTUAL on static -> IllegalAccessException (kind is honored)");
        }

        // factory rejection matrix
        try {
            MethodHandleDesc.ofMethod(null, ConstantDescs.CD_String, "length",
                    MethodTypeDesc.ofDescriptor("()I"));
            check(false, "ofMethod(null kind) rejected");
        } catch (NullPointerException expected) { check(true, "ofMethod(null kind) -> NPE"); }
        try {
            MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.VIRTUAL, null, "length",
                    MethodTypeDesc.ofDescriptor("()I"));
            check(false, "ofMethod(null owner) rejected");
        } catch (NullPointerException expected) { check(true, "ofMethod(null owner) -> NPE"); }
        try {
            MethodHandleDesc.ofField(DirectMethodHandleDesc.Kind.GETTER, owner, "instanceField", null);
            check(false, "ofField(null type) rejected");
        } catch (NullPointerException expected) { check(true, "ofField(null type) -> NPE"); }
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
