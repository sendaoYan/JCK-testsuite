/*
 * @test
 * @summary java.lang.constant.DirectMethodHandleDesc.Kind conformance: the
 *          11 constants, refKind, isInterface, and valueOf lookups.
 * @run main KindTest
 */

import java.lang.constant.DirectMethodHandleDesc;

public class KindTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        DirectMethodHandleDesc.Kind[] all = DirectMethodHandleDesc.Kind.values();
        check(all.length == 11, "11 kind constants");

        check(DirectMethodHandleDesc.Kind.valueOf("STATIC") ==
              DirectMethodHandleDesc.Kind.STATIC, "valueOf(String) STATIC");
        check(DirectMethodHandleDesc.Kind.valueOf(6) == DirectMethodHandleDesc.Kind.STATIC,
              "valueOf(6) STATIC");
        check(DirectMethodHandleDesc.Kind.valueOf(6, true) ==
              DirectMethodHandleDesc.Kind.INTERFACE_STATIC, "valueOf(6,true) INTERFACE_STATIC");
        check(DirectMethodHandleDesc.Kind.valueOf(9) ==
              DirectMethodHandleDesc.Kind.INTERFACE_VIRTUAL, "valueOf(9) INTERFACE_VIRTUAL");

        check(DirectMethodHandleDesc.Kind.STATIC.refKind == 6, "STATIC refKind 6");
        check(!DirectMethodHandleDesc.Kind.STATIC.isInterface, "STATIC not interface");
        check(DirectMethodHandleDesc.Kind.INTERFACE_STATIC.isInterface,
              "INTERFACE_STATIC isInterface");
        check(DirectMethodHandleDesc.Kind.VIRTUAL.refKind == 5, "VIRTUAL refKind 5");
        check(DirectMethodHandleDesc.Kind.CONSTRUCTOR.refKind == 8, "CONSTRUCTOR refKind 8");
        check(DirectMethodHandleDesc.Kind.GETTER.refKind == 1, "GETTER refKind 1");
        check(DirectMethodHandleDesc.Kind.SETTER.refKind == 3, "SETTER refKind 3");
        check(DirectMethodHandleDesc.Kind.STATIC_GETTER.refKind == 2,
              "STATIC_GETTER refKind 2");
        check(DirectMethodHandleDesc.Kind.STATIC_SETTER.refKind == 4,
              "STATIC_SETTER refKind 4");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
