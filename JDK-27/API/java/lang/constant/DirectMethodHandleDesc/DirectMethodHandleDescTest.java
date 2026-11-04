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

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
