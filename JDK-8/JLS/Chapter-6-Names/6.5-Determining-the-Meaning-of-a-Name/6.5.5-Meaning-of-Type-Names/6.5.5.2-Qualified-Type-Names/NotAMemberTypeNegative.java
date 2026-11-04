/*
 * @test
 * @summary JLS 6.5.5.2 (Java SE 8): a qualified type name Q.Id must denote an
 *          accessible member type of the type Q or top-level type of package Q;
 *          a name that denotes no such type is a compile-time error.
 * @compile/fail NotAMemberTypeNegative.java
 */

class NotAMemberTypeNegative {
    java.util.Date.NoSuchMember x;   // error: cannot find symbol NoSuchMember
}
