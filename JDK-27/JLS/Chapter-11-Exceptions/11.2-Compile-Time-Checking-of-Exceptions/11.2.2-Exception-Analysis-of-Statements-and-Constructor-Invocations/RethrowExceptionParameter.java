/*
 * @test
 * @summary JLS 11.2.2: a throw statement whose thrown expression is a final or
 *          effectively final exception parameter performs precise rethrow
 *          analysis, so it need only declare the specific rethrown types.
 * @compile RethrowExceptionParameter.java
 */

import java.io.FileNotFoundException;

class RethrowExceptionParameter {
    void m() throws FileNotFoundException {
        try {
            throw new FileNotFoundException();
        } catch (Exception e) {
            throw e;   // legal: precise rethrow of the caught exception parameter
        }
    }
}
