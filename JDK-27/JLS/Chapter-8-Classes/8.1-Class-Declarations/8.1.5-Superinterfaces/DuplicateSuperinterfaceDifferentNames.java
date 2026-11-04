/*
 * @test
 * @summary JLS 8.1.5: it is a compile-time error if the same interface is named
 *          by a direct superinterface type more than once, even if named in
 *          different ways.
 * @compile/fail DuplicateSuperinterfaceDifferentNames.java
 */
import java.io.Serializable;
class DuplicateSuperinterfaceDifferentNames implements java.io.Serializable, Serializable { }   // error: repeated interface
