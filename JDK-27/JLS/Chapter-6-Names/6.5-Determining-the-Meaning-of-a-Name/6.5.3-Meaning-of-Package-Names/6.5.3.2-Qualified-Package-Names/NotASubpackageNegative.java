/*
 * @test
 * @summary JLS 6.5.3.2 (Java SE 27): a qualified name Q.Id is a package name only
 *          if Q is a package name and Id is a subpackage of Q; otherwise the
 *          package does not exist and a compile-time error occurs.
 * @compile/fail NotASubpackageNegative.java
 */

class NotASubpackageNegative {
    java.util.nosuchsub.C c;   // error: package java.util.nosuchsub does not exist
}
