/*
 * @test
 * @summary JLS 9.1.5: an interface body may contain fields, methods, classes,
 *          and interfaces.
 * @compile InterfaceBodyMembers.java
 */

interface InterfaceBodyMembers {
    int CONSTANT = 1;            // field
    void method();               // method
    class MemberClass {}         // member class
    interface MemberInterface {} // member interface
}
