/*
 * @test
 * @summary JLS 8.4.8.3 (Java SE 8): an overriding method must provide at least as much
 *          access as the overridden method; a package-access method cannot be
 *          overridden with a private method.
 * @compile/fail OverridePackageAccessWithPrivate.java
 */
class Base { void m() { } }
class OverridePackageAccessWithPrivate extends Base {
    private void m() { }   // error: attempting to assign weaker access privileges
}
