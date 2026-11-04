/*
 * @test
 * @summary JLS 7.5.2: it is not a compile-time error to name
 *          java.lang or the named package of the current compilation unit in a
 *          type-import-on-demand declaration; such a declaration is ignored.
 * @run main p.TypeImportOnDemandCurrentPackageIgnored
 */
package p;

import java.lang.*;
import p.*;

public class TypeImportOnDemandCurrentPackageIgnored {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        String s = "abc";                       // java.lang.String (implicit/on-demand)
        check(s.length() == 3, "java.lang on-demand ignored (String usable)");
        Local l = new Local();                  // p.Local via "import p.*"
        check(l.id == 9, "current package on-demand ignored");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class Local {
    int id = 9;
}
