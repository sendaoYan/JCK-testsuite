/*
 * @test
 * @summary JLS 8.2: fields, methods, member classes, and member interfaces of a
 *          class may have the same name; they are disambiguated by context.
 * @compile SameNameMemberKinds.java
 */

class SameNameMemberKinds {
    int item = 1;
    int item() { return 2; }
    class item {}
    interface Other {}
}
