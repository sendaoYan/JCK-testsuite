/*
 * @test
 * @summary javax.crypto.ExemptionMechanism conformance: no-provider getInstance throws and class shape.
 * @run main ExemptionMechanismTest
 */

import javax.crypto.*;
import java.lang.reflect.*;
import java.security.*;

public class ExemptionMechanismTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.crypto.ExemptionMechanism");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "public concrete class");
        boolean nsa = false;
        try { ExemptionMechanism.getInstance("NoSuchMechanism"); } catch (NoSuchAlgorithmException e) { nsa = true; }
        check(nsa, "getInstance throws NoSuchAlgorithmException");
        check(c.getConstructors().length == 0, "no public constructor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
