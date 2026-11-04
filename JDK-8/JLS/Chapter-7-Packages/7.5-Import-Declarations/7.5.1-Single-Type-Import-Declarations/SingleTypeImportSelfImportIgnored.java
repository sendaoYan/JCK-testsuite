/*
 * @test
 * @summary JLS 7.5.1 (Java SE 8): if the type imported by a single-type-import
 *          declaration is declared in the same compilation unit, the import is
 *          ignored.
 * @run main p.SingleTypeImportSelfImportIgnored
 */
package p;

import p.C;

public class SingleTypeImportSelfImportIgnored {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        C c = new C();
        check(c.id == 7, "self-import ignored; local type C used");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class C {
    int id = 7;
}
