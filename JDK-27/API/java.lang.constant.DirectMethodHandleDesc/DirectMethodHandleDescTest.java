/*
 * @test
 * @summary java.lang.constant.DirectMethodHandleDesc conformance: accessors
 *          (kind/refKind/owner/methodName/lookupDescriptor/isOwnerInterface).
 * @run main DirectMethodHandleDescTest
 */

import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandleInfo;

public class DirectMethodHandleDescTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(DirectMethodHandleDesc.class.isInterface(),
              "DirectMethodHandleDesc is an interface");
        check(MethodHandleDesc.class.isAssignableFrom(DirectMethodHandleDesc.class),
              "DirectMethodHandleDesc extends MethodHandleDesc");

        DirectMethodHandleDesc getter = MethodHandleDesc.ofField(
                DirectMethodHandleDesc.Kind.GETTER, ClassDesc.of("java.lang.System"),
                "out", ClassDesc.of("java.io.PrintStream"));
        check(getter.kind() == DirectMethodHandleDesc.Kind.GETTER, "kind() GETTER");
        check(getter.refKind() == MethodHandleInfo.REF_getField, "refKind getField");
        check("Ljava/lang/System;".equals(getter.owner().descriptorString()), "owner");
        check("out".equals(getter.methodName()), "methodName");
        check("Ljava/io/PrintStream;".equals(getter.lookupDescriptor()),
              "lookupDescriptor");
        check(!getter.isOwnerInterface(), "isOwnerInterface false for System");

        DirectMethodHandleDesc iface = MethodHandleDesc.ofMethod(
                DirectMethodHandleDesc.Kind.INTERFACE_VIRTUAL,
                ClassDesc.of("java.lang.CharSequence"), "length",
                MethodTypeDesc.of(ClassDesc.ofDescriptor("I")));
        check(iface.isOwnerInterface(), "isOwnerInterface true for CharSequence");
        check(iface.refKind() == MethodHandleInfo.REF_invokeInterface,
              "INTERFACE_VIRTUAL refKind");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
