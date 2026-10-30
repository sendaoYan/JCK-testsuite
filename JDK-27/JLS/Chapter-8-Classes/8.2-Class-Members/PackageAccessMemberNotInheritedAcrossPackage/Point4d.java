/*
 * @test
 * @summary JLS 8.2: only members declared protected or public are inherited by
 *          subclasses declared in a package other than the one in which the
 *          class is declared; package-access members are not inherited across
 *          packages.
 * @compile/fail points/Point.java points/Point3d.java Point4d.java
 */

import points.Point3d;

class Point4d extends Point3d {
    int w;
    void move(int dx, int dy, int dz, int dw) {
        x += dx; y += dy; z += dz; w += dw;   // error: x, y, z not inherited
    }
}
