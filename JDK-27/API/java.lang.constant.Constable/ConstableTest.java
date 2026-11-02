/*
 * @test
 * @summary java.lang.constant.Constable conformance: describeConstable on
 *          the JDK's built-in Constable types.
 * @run main ConstableTest
 */

import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.lang.invoke.MethodHandles;
import java.util.Optional;

public class ConstableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(Constable.class.isInterface(), "Constable is an interface");

        Constable i = Integer.valueOf(5);
        Optional<? extends ConstantDesc> oi = i.describeConstable();
        check(oi.isPresent(), "Integer.describeConstable() present");

        Constable s = "hello";
        Optional<? extends ConstantDesc> os = s.describeConstable();
        check(os.isPresent(), "String.describeConstable() present");
        Object resolved = os.get().resolveConstantDesc(MethodHandles.lookup());
        check("hello".equals(resolved), "String constant resolves to itself");

        Constable l = Long.valueOf(99L);
        check(l.describeConstable().isPresent(), "Long.describeConstable() present");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
