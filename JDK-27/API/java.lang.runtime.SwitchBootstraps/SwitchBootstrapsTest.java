/*
 * @test
 * @summary java.lang.runtime.SwitchBootstraps conformance: final class and the
 *          typeSwitch/enumSwitch bootstrap entry points.
 * @run main SwitchBootstrapsTest
 */

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.SwitchBootstraps;

public class SwitchBootstrapsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(!SwitchBootstraps.class.isInterface(), "SwitchBootstraps is a final class");

        java.lang.reflect.Method type = SwitchBootstraps.class.getMethod("typeSwitch",
                MethodHandles.Lookup.class, String.class, MethodType.class, Object[].class);
        check(type != null, "typeSwitch(Lookup,String,MethodType,Object[])");
        check(CallSite.class.equals(type.getReturnType()), "typeSwitch returns CallSite");

        java.lang.reflect.Method en = SwitchBootstraps.class.getMethod("enumSwitch",
                MethodHandles.Lookup.class, String.class, MethodType.class, Object[].class);
        check(en != null, "enumSwitch(Lookup,String,MethodType,Object[])");
        check(CallSite.class.equals(en.getReturnType()), "enumSwitch returns CallSite");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
