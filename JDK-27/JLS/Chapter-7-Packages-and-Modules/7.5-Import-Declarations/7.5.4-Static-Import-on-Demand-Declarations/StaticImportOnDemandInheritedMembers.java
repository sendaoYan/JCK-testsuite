/*
 * @test
 * @summary JLS 7.5.4: a static-import-on-demand declaration imports
 *          inherited static members as well as declared static members.
 * @compile p/Base.java p/Sub.java StaticImportOnDemandInheritedMembers.java
 * @run main StaticImportOnDemandInheritedMembers
 */

import static p.Sub.*;

public class StaticImportOnDemandInheritedMembers {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(inherited == 5, "inherited static field via on-demand import");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
