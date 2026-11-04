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

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
