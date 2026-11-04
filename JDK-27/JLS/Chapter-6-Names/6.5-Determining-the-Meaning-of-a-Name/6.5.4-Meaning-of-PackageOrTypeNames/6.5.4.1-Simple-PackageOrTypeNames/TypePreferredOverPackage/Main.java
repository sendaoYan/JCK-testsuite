/*
 * @test
 * @summary JLS 6.5.4.1 (Java SE 27): a simple PackageOrTypeName that could be a
 *          type or a package resolves to the type; so p.q.T fails even though
 *          package p.q.T exists, because p is the type p and q is its member
 *          type (which has no member T).
 * @compile T.java p.java
 * @compile/fail Main.java
 */

class Main {
    p.q.T x;   // error: cannot find symbol T (p resolves to the type, not the package)
}
