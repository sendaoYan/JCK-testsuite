/*
 * @test
 * @summary JLS 7.1 (Java SE 8): a package may not contain two members of the
 *          same name; a top level type "sub" in package p and a subpackage p.sub
 *          collide (class sub clashes with package of same name).
 * @compile/fail p/sub.java p/sub/C.java
 */
