/*
 * @test
 * @summary java.lang.constant.DynamicCallSiteDesc conformance: white-box + black-box behavior.
 * @run main DynamicCallSiteDescTest
 */

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.DynamicCallSiteDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;

public class DynamicCallSiteDescTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // real bootstrap methods used by resolveCallSiteDesc
    public static CallSite nameBs(MethodHandles.Lookup lookup, String name, MethodType type)
            throws NoSuchMethodException, IllegalAccessException {
        check(name.equals("target"), "bootstrap receives invocation name");
        return new ConstantCallSite(lookup.findStatic(lookup.lookupClass(), "target", type));
    }

    static int target(String s) { return s.length(); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.constant.DynamicCallSiteDesc");
        check(Modifier.isFinal(c.getModifiers()), "final class");
        int pubCtors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (Modifier.isPublic(ct.getModifiers())) pubCtors++;
        }
        check(pubCtors == 0, "no public constructor");
        for (String m : new String[]{"of", "withArgs", "withNameAndType", "invocationName",
                                      "invocationType", "bootstrapMethod", "bootstrapArgs",
                                      "resolveCallSiteDesc", "equals", "hashCode", "toString"}) {
            boolean found = false;
            for (java.lang.reflect.Method mm : c.getDeclaredMethods())
                if (mm.getName().equals(m)) found = true;
            check(found, "declares " + m);
        }
        check(Modifier.isStatic(c.getDeclaredMethod("of", DirectMethodHandleDesc.class,
                String.class, MethodTypeDesc.class, java.lang.constant.ConstantDesc[].class)
                .getModifiers()), "static of(bsm, name, type, args...)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        ClassDesc self = ClassDesc.of("DynamicCallSiteDescTest");
        DirectMethodHandleDesc bs = MethodHandleDesc.ofMethod(
                DirectMethodHandleDesc.Kind.STATIC, self, "nameBs",
                MethodTypeDesc.ofDescriptor(
                        "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;"));
        MethodTypeDesc type = MethodTypeDesc.of(ConstantDescs.CD_int, ConstantDescs.CD_String);

        // of(bsm, name, type, args...) / withers / accessors
        DynamicCallSiteDesc d = DynamicCallSiteDesc.of(bs, "target", type,
                Integer.valueOf(7), "extra");
        check(d.invocationName().equals("target"), "invocationName");
        check(d.invocationType().descriptorString().equals("(Ljava/lang/String;)I"),
              "invocationType");
        check(d.bootstrapMethod() == bs, "bootstrapMethod identity");
        check(d.bootstrapArgs().length == 2, "bootstrapArgs length");
        check(d.bootstrapArgs()[0].equals(7) && d.bootstrapArgs()[1].equals("extra"),
              "bootstrapArgs values");

        // of(bsm, type): default name "_", no args
        DynamicCallSiteDesc bare = DynamicCallSiteDesc.of(bs, type);
        check(bare.invocationName().equals("_"), "default name is _");
        check(bare.bootstrapArgs().length == 0, "default no args");

        // withArgs replaces the bootstrap arguments wholesale
        DynamicCallSiteDesc w1 = d.withArgs("only");
        check(w1.bootstrapArgs().length == 1 && w1.bootstrapArgs()[0].equals("only"),
              "withArgs replaces");
        check(w1.invocationName().equals(d.invocationName()), "withArgs keeps name");
        check(w1.bootstrapMethod().equals(d.bootstrapMethod()), "withArgs keeps bsm");

        // withNameAndType
        DynamicCallSiteDesc w2 = d.withNameAndType("other",
                MethodTypeDesc.ofDescriptor("(J)V"));
        check(w2.invocationName().equals("other"), "withNameAndType name");
        check(w2.invocationType().descriptorString().equals("(J)V"), "withNameAndType type");

        // value semantics
        check(d.equals(DynamicCallSiteDesc.of(bs, "target", type,
                Integer.valueOf(7), "extra")), "equals by value");
        check(d.hashCode() == DynamicCallSiteDesc.of(bs, "target", type,
                Integer.valueOf(7), "extra").hashCode(), "hashCode");
        check(!d.equals(bare), "!equals different name/args");
        check(!d.equals(null), "!equals null");

        // toString mentions the bootstrap and name
        String s = d.toString();
        check(s.contains("nameBs") && s.contains("target"), "toString content: " + s);

        // resolveCallSiteDesc: invokes the real bootstrap and returns a usable CallSite
        CallSite cs = DynamicCallSiteDesc.of(bs, "target", type).resolveCallSiteDesc(lookup);
        check(cs != null, "resolveCallSiteDesc returns a CallSite");
        check(cs.type().toMethodDescriptorString().equals("(Ljava/lang/String;)I"),
              "call site type matches invocation type");
        check((int) cs.getTarget().invokeExact((String) "abcd") == 4,
              "target invokes through the call site");
    }

    // ---- Part 3: deep contract (invokedynamic resolution closure + null matrix) ----
    static void testDeepContract() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        // null factory matrix
        try {
            DynamicCallSiteDesc.of(null, "n", MethodTypeDesc.ofDescriptor("()V"));
            check(false, "of(null bsm) rejected");
        } catch (NullPointerException expected) { check(true, "of(null bsm) -> NPE"); }
        try {
            DynamicCallSiteDesc.of(ConstantDescs.BSM_INVOKE, null);
            check(false, "of(bsm, null type) rejected");
        } catch (NullPointerException expected) { check(true, "of(bsm, null type) -> NPE"); }

        // real invokedynamic-style resolution: StringConcatFactory.makeConcat builds a
        // working call site whose target concatenates its two String arguments
        DirectMethodHandleDesc concatBsm = MethodHandleDesc.ofMethod(
                DirectMethodHandleDesc.Kind.STATIC,
                ClassDesc.of("java.lang.invoke.StringConcatFactory"), "makeConcat",
                MethodTypeDesc.ofDescriptor(
                        "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;"
                                + "Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;"));
        DynamicCallSiteDesc concat = DynamicCallSiteDesc.of(concatBsm, "x",
                MethodTypeDesc.ofDescriptor("(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"));
        CallSite cs = concat.resolveCallSiteDesc(lookup);
        check(cs != null, "makeConcat call site resolves");
        check(cs.getTarget().invoke("a", "b").equals("ab"),
              "makeConcat target concatenates \"a\"+\"b\" == \"ab\"");

        // withArgs/withNameAndType do not mutate the receiver (value semantics)
        DirectMethodHandleDesc bsm = ConstantDescs.BSM_INVOKE;
        MethodTypeDesc type = MethodTypeDesc.ofDescriptor("(I)V");
        DynamicCallSiteDesc base = DynamicCallSiteDesc.of(bsm, "orig", type, 7, "extra");
        base.withArgs("only");
        base.withNameAndType("other", MethodTypeDesc.ofDescriptor("(J)V"));
        check(base.invocationName().equals("orig"), "with* leaves receiver name");
        check(base.invocationType().descriptorString().equals("(I)V"),
              "with* leaves receiver type");
        check(base.bootstrapArgs().length == 2, "with* leaves receiver args");

        // bootstrapArgs array is defensive or value-equal on reconstruction
        DynamicCallSiteDesc same = DynamicCallSiteDesc.of(
                (DirectMethodHandleDesc) base.bootstrapMethod(), base.invocationName(),
                base.invocationType(), base.bootstrapArgs());
        check(base.equals(same) && base.hashCode() == same.hashCode(),
              "reconstruction from accessors is equals");

        // generic Object bootstrap path: ConstantBootstraps.invoke-style descriptor resolves
        check(concat.toString().contains("makeConcat"), "toString names the bootstrap");
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
