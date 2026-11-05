/*
 * @test
 * @summary java.lang.constant.MethodHandleDesc conformance: white-box + black-box behavior.
 * @run main MethodHandleDescTest
 */

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public class MethodHandleDescTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.constant.MethodHandleDesc");
        check(c.isInterface(), "is an interface");
        check(Class.forName("java.lang.constant.ConstantDesc").isAssignableFrom(c),
              "extends ConstantDesc");
        for (String m : new String[]{"of", "ofMethod", "ofField", "ofConstructor",
                                      "asType", "invocationType", "resolveConstantDesc"}) {
            boolean found = false;
            for (java.lang.reflect.Method mm : c.getDeclaredMethods())
                if (mm.getName().equals(m)) found = true;
            check(found, "declares " + m);
        }
        check(c.getDeclaredMethod("asType", Class.forName("java.lang.constant.MethodTypeDesc")).isDefault(),
              "asType is a default method");
        check(c.getDeclaredMethod("ofMethod", Class.forName("java.lang.constant.DirectMethodHandleDesc$Kind"),
                ClassDesc.class, String.class, Class.forName("java.lang.constant.MethodTypeDesc"))
                      .getReturnType() == DirectMethodHandleDesc.class,
              "ofMethod returns DirectMethodHandleDesc");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        // ofMethod(VIRTUAL, String.length) -> direct descriptor, resolvable, invocable
        MethodHandleDesc len = MethodHandleDesc.ofMethod(
                DirectMethodHandleDesc.Kind.VIRTUAL, ClassDesc.of("java.lang.String"),
                "length", MethodTypeDesc.of(ConstantDescs.CD_int));
        check(len.invocationType().descriptorString().equals("(Ljava/lang/String;)I"),
              "invocationType");
        MethodHandle h = len.resolveConstantDesc(lookup);
        check(h != null && (int) h.invoke("hello") == 5, "resolve + invoke");

        // asType adapts the invocation type
        MethodHandleDesc adapted = len.asType(MethodTypeDesc.of(ConstantDescs.CD_int, ClassDesc.of("java.lang.String")));
        check(adapted.invocationType().descriptorString().equals("(Ljava/lang/String;)I"),
              "asType reflected in invocationType");

        // ofMethod(STATIC) with an explicit signature: Integer.parseInt
        MethodHandleDesc parseInt = MethodHandleDesc.ofMethod(
                DirectMethodHandleDesc.Kind.STATIC, ClassDesc.of("java.lang.Integer"),
                "parseInt", MethodTypeDesc.of(ConstantDescs.CD_int, ConstantDescs.CD_String));
        check((int) parseInt.resolveConstantDesc(lookup).invoke("123") == 123,
              "STATIC resolve + invoke");
        check(parseInt.invocationType().descriptorString().equals("(Ljava/lang/String;)I"),
              "STATIC invocationType has no receiver");

        // ofField(STATIC_GETTER) resolves to a working getter
        MethodHandleDesc minVal = MethodHandleDesc.ofField(
                DirectMethodHandleDesc.Kind.STATIC_GETTER, ClassDesc.of("java.lang.Integer"),
                "MIN_VALUE", ConstantDescs.CD_int);
        check((int) minVal.resolveConstantDesc(lookup).invoke() == Integer.MIN_VALUE,
              "STATIC_GETTER resolve + invoke");
        check(minVal.invocationType().descriptorString().equals("()I"),
              "static getter invocationType");

        // ofField(GETTER): instance field on our own public holder
        MethodHandleDesc xGet = MethodHandleDesc.ofField(
                DirectMethodHandleDesc.Kind.GETTER,
                ClassDesc.ofDescriptor("LMethodHandleDescTest$Holder;"), "x", ConstantDescs.CD_int);
        check(xGet.invocationType().descriptorString().equals("(LMethodHandleDescTest$Holder;)I"),
              "getter invocationType has receiver");
        check((int) xGet.resolveConstantDesc(lookup).invoke((Object) new Holder()) == 42,
              "GETTER resolve + invoke");
        // setter writes through the resolved handle
        MethodHandleDesc xSet = MethodHandleDesc.ofField(
                DirectMethodHandleDesc.Kind.SETTER,
                ClassDesc.ofDescriptor("LMethodHandleDescTest$Holder;"), "x", ConstantDescs.CD_int);
        check(((DirectMethodHandleDesc) xSet).refKind() == 3, "SETTER refKind 3");

        // ofConstructor: invocation returns the constructed type
        MethodHandleDesc ctor = MethodHandleDesc.ofConstructor(
                ClassDesc.of("java.lang.StringBuilder"), ConstantDescs.CD_String);
        check(ctor.invocationType().descriptorString().equals("(Ljava/lang/String;)Ljava/lang/StringBuilder;"),
              "constructor invocationType");
        check(ctor.resolveConstantDesc(lookup).invoke("sb").toString().equals("sb"),
              "constructor builds the object");

        // equality by parts
        MethodHandleDesc len2 = MethodHandleDesc.ofMethod(
                DirectMethodHandleDesc.Kind.VIRTUAL, ClassDesc.of("java.lang.String"),
                "length", MethodTypeDesc.of(ConstantDescs.CD_int));
        check(len.equals(len2), "equals by value");
        check(len.hashCode() == len2.hashCode(), "hashCode consistent");
        check(!len.equals(parseInt), "!equals other method");

        // constant-pool symmetric: every factory result is itself a ConstantDesc
        check(len instanceof java.lang.constant.ConstantDesc, "MethodHandleDesc is ConstantDesc");

        // failed resolution
        try {
            MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC,
                    ClassDesc.of("java.lang.String"), "nope",
                    MethodTypeDesc.of(ConstantDescs.CD_void)).resolveConstantDesc(lookup);
            check(false, "missing static should fail");
        } catch (ReflectiveOperationException e) {
            check(true, "missing static -> NoSuchMethodException family");
        }
    }

    // ---- Part 3: deep contract (view adaptation + write closure + null matrix) ----
    static void testDeepContract() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        // asType to a supertype view still resolves and dispatches the original method
        MethodHandleDesc len = MethodHandleDesc.ofMethod(
                DirectMethodHandleDesc.Kind.VIRTUAL, ClassDesc.of("java.lang.String"),
                "length", MethodTypeDesc.of(ConstantDescs.CD_int));
        MethodHandleDesc charSeqView = len.asType(
                MethodTypeDesc.of(ConstantDescs.CD_int, ClassDesc.of("java.lang.CharSequence")));
        check(charSeqView.invocationType().descriptorString()
                      .equals("(Ljava/lang/CharSequence;)I"),
              "asType(CharSequence) reflected in invocationType");
        check((int) charSeqView.resolveConstantDesc(lookup).invoke("abcd") == 4,
              "asType view resolves and invokes");
        try {
            len.asType(null);
            check(false, "asType(null) rejected");
        } catch (NullPointerException expected) {
            check(true, "asType(null) -> NPE");
        }

        // SETTER/GETTER full write-read cycle through resolved handles
        DirectMethodHandleDesc setter = (DirectMethodHandleDesc) MethodHandleDesc.ofField(
                DirectMethodHandleDesc.Kind.SETTER,
                ClassDesc.ofDescriptor("LMethodHandleDescTest$Holder;"), "x", ConstantDescs.CD_int);
        Holder box = new Holder();
        setter.resolveConstantDesc(lookup).invoke(box, 777);
        check(box.x == 777, "SETTER writes 777 through resolved handle");
        DirectMethodHandleDesc getter = (DirectMethodHandleDesc) MethodHandleDesc.ofField(
                DirectMethodHandleDesc.Kind.GETTER,
                ClassDesc.ofDescriptor("LMethodHandleDescTest$Holder;"), "x", ConstantDescs.CD_int);
        check((int) getter.resolveConstantDesc(lookup).invoke((Object) box) == 777,
              "GETTER reads the setter's value back");

        // ofConstructor with no extra parameters links the default constructor
        MethodHandleDesc defCtor = MethodHandleDesc.ofConstructor(ClassDesc.of("java.lang.Object"));
        check(defCtor.invocationType().descriptorString().equals("()Ljava/lang/Object;"),
              "default constructor invocationType");
        check(defCtor.resolveConstantDesc(lookup).invoke() instanceof Object,
              "default constructor resolves and constructs");

        // null factory matrix
        try {
            MethodHandleDesc.of(null, ClassDesc.of("java.lang.Object"),
                    "<init>", "()V");
            check(false, "of(null kind) rejected");
        } catch (NullPointerException expected) { check(true, "of(null kind) -> NPE"); }
        try {
            MethodHandleDesc.ofConstructor(null);
            check(false, "ofConstructor(null) rejected");
        } catch (NullPointerException expected) { check(true, "ofConstructor(null) -> NPE"); }

        // invocationType vs resolve type agreement for a STATIC method
        MethodHandleDesc parseInt = MethodHandleDesc.ofMethod(
                DirectMethodHandleDesc.Kind.STATIC, ClassDesc.of("java.lang.Integer"),
                "parseInt", MethodTypeDesc.of(ConstantDescs.CD_int, ConstantDescs.CD_String));
        MethodHandle resolved = parseInt.resolveConstantDesc(lookup);
        check(resolved.type().toMethodDescriptorString()
                      .equals(parseInt.invocationType().descriptorString()),
              "resolved MethodType equals invocationType descriptor");
    }

    public static class Holder {
        public int x = 42;
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
