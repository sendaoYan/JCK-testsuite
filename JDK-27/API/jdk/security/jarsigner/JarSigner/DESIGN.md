# jdk.security.jarsigner.JarSigner 设计案

## 1. API 功能
JAR 签名器（final，Builder 构建）。

## 2. Java 用户如何使用
JarSigner.Builder(PrivateKey, CertPath).build()。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.security.jarsigner/share/classes/jdk/security/jarsigner/JarSigner.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒 Builder 静态默认算法。

## 5. 契约深化（JDK-27）

- keytool 子进程真实密钥链：genkeypair RSA-2048 PKCS12 → KeyStore 读 pk + X.509 CertPath（单证书自签链）。
- 默认算法分派表：getDefaultDigestAlgorithm()=="SHA-384"；getDefaultSignatureAlgorithm 按 key 分派 RSA→SHA384withRSA、EC→SHA384withECDSA、DSA→SHA256withDSA。
- 配置回读：digest/signature 算法、signerName 大写化（"unittest"→"UNITTEST"）、tsa 默认 null。
- getProperty 四键白名单：internalsf/sectionsonly 默认 "false"、tsadigestalg 默认 "SHA-384"、tsapolicyid 默认 null；bogus 键 UOE "Unsupported key bogus"、null 键 NPE。
- 真实签名闭环：sign(ZipFile, OutputStream) 后 jar 变大；META-INF/MANIFEST.MF、META-INF/UNITTEST.SF、META-INF/UNITTEST.RSA 齐全；业务 entry 字节不变；事件序列恰 4 条且首条 adding:META-INF/MANIFEST.MF；JarFile(verify) 读 entry 后 codeSigners 恰 1。
