/*
 * @test
 * @summary java.lang.invoke.VarHandle.VarHandleDesc conformance: white-box + black-box behavior.
 * @run main VarHandleDescTest
 */

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.invoke.VarHandle.VarHandleDesc;

public class VarHandleDescTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static class Holder {
        public int x = 5;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.invoke.VarHandle$VarHandleDesc");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        check(c.getSuperclass() == Class.forName("java.lang.constant.DynamicConstantDesc"),
              "extends DynamicConstantDesc<VarHandle>");
        int pubCtors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPublic(ct.getModifiers())) pubCtors++;
        }
        check(pubCtors == 0, "no public constructor (factories only)");
        for (String n : new String[]{"ofField", "ofStaticField", "ofArray", "varType",
                "resolveConstantDesc", "toString"}) {
            boolean found = false;
            for (java.lang.reflect.Method mm : c.getDeclaredMethods())
                if (mm.getName().equals(n)) found = true;
            check(found, "declares " + n);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        ClassDesc holder = ClassDesc.of("VarHandleDescTest").nested("Holder");

        // ofField: nominal form of an instance field VarHandle
        VarHandleDesc fd = VarHandleDesc.ofField(holder, "x", ConstantDescs.CD_int);
        check(fd.varType().descriptorString().equals("I"), "ofField varType");
        check(fd.toString().contains("x"), "toString names the field: " + fd);
        VarHandle resolved = fd.resolveConstantDesc(lookup);
        check(resolved.varType() == int.class, "resolved varType");
        check(resolved.coordinateTypes().equals(java.util.List.of(Holder.class)),
              "resolved coordinates");
        Holder h = new Holder();
        check((int) resolved.get(h) == 5, "resolved field VarHandle readable");
        resolved.set(h, 8);
        check(h.x == 8, "resolved field VarHandle writable");

        // equality with the desc produced by a live VarHandle
        VarHandle live = lookup.findVarHandle(Holder.class, "x", int.class);
        check(live.describeConstable().isPresent(), "live describeConstable present");
        check(fd.equals(live.describeConstable().orElseThrow()),
              "ofField matches describeConstable");

        // ofStaticField
        VarHandleDesc sd = VarHandleDesc.ofStaticField(
                ClassDesc.of("VarHandleDescTest"), "stat", ConstantDescs.CD_int);
        VarHandle sResolved = sd.resolveConstantDesc(lookup);
        check(sResolved.coordinateTypes().isEmpty(), "static resolved coordinates");
        check((int) sResolved.get() == 77, "static resolved readable");
        sResolved.set(78);
        check(stat == 78, "static resolved writable");

        // ofArray: nominal form of an array element VarHandle
        VarHandleDesc ad = VarHandleDesc.ofArray(ClassDesc.of("java.lang.String").arrayType());
        check(ad.varType().descriptorString().equals("Ljava/lang/String;"), "ofArray varType");
        VarHandle aResolved = ad.resolveConstantDesc(lookup);
        String[] strs = {"a", "b"};
        check("b".equals((String) aResolved.get(strs, 1)), "array resolved readable");
        aResolved.set(strs, 1, "z");
        check(strs[1].equals("z"), "array resolved writable");
        check(aResolved.coordinateTypes().equals(java.util.List.of(String[].class, int.class)),
              "array resolved coordinates");

        // value semantics
        check(fd.equals(VarHandleDesc.ofField(holder, "x", ConstantDescs.CD_int)),
              "equals by value");
        check(!fd.equals(VarHandleDesc.ofField(holder, "y", ConstantDescs.CD_int)),
              "field name participates");
        check(!fd.equals(sd), "!equals other factory");
    }

    static int stat = 77;

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
