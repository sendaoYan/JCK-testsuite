/*
 * @test
 * @summary JLS 6.4.2 (Java SE 27): a type name obscures a package name, so a
 *          qualified name that begins with the type cannot reach members of the
 *          package.
 * @compile/fail TypeNameObscuresPackageNegative.java
 */

class java { }   // type named java obscures the package java

class TypeNameObscuresPackageNegative {
    java.lang.String s;   // error: cannot find symbol lang in class java
}
