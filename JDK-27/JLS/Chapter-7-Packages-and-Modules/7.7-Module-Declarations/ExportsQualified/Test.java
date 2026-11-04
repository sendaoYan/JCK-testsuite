/*
 * @test
 * @summary JLS 7.7.2: a qualified exports directive (with a to clause) makes the
 *          public and protected types of a package accessible solely to the
 *          modules named in the to clause (the friends).
 * @build provider/* friend/*
 * @run main friend/client.Main
 */
