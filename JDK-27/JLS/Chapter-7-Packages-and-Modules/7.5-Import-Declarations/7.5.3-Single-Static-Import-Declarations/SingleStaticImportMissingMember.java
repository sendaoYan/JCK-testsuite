/*
 * @test
 * @summary JLS 7.5.3: it is a compile-time error if the named class or interface
 *          has no static member of the imported name.
 * @compile/fail SingleStaticImportMissingMember.java
 */

import static java.lang.Math.NO_SUCH_MEMBER;

class SingleStaticImportMissingMember {
    void m() {
        double d = NO_SUCH_MEMBER;   // error: no such static member
    }
}
