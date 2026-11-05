# jdk.security.jarsigner.JarSigner.Builder 契约设计

## 1. API 功能
`JarSigner.Builder` 配置并产出 `JarSigner`：构造器接收 (PrivateKey, CertPath)（自签链最小 1 证书）或 KeyStore 三参形态。配置项：`digestAlgorithm`、`signatureAlgorithm`（须与 key 算法兼容）、`signerName`（8 字符内字母数字 - _，输出大写）、`tsa(URI)`、`eventHandler(BiConsumer<String,String>)`、`setProperty(internalsf|sectionsonly|tsadigestalg|tsapolicyid)`。`build()` 可多次调用产出独立实例。

## 2. Java 用户如何使用
链式调用后 build：`new JarSigner.Builder(pk, certPath).digestAlgorithm("SHA-256").signerName("unittest").eventHandler((a,f)->log).build()`。默认摘要 SHA-384，默认签名算法按 key 类型分派（RSA→SHA384withRSA，EC→SHA384withECDSA，DSA→SHA256withDSA）。

## 3. 开源代码实际用法调研
- OpenJDK jarsigner 工具的签名引擎就是本 Builder 的一层 CLI 壳（JarsignerTool）。
- Gradle SignPlugin / Maven Jarsigner Plugin 走命令行而非本 API；直接消费方是 Conscrypt/IBM JDK 互操作测试与 pack200 替代实现。
- 默认签名名 "SIGNER"（未显式设置时的内部回退值，实测 JDK-27）。

## 4. 测试用例设计+自动化实现
- 真实密钥闭环：keytool 子进程 genkeypair RSA-2048（PKCS12）→ KeyStore 读出 pk+CertPath。
- 链式与实例语义：每个 setter 返回 Builder、build() 两次得到不同实例、tsa/signerName 配置回读（大写化 "N"）。
- 负例精确消息表：signerName "bad name!"/空 → IAE "Name too long"；digestAlgorithm("NOTADIGEST") → NoSuchAlgorithmException "NOTADIGEST MessageDigest not available"；signatureAlgorithm("NOTASIG") → "NOTASIG Signature not available"；RSA key + SHA256withECDSA → IAE "key algorithm not compatible with signature algorithm"；setProperty("bogus") → UOE "Unsupported key bogus"；tsa(null)/构造器 null pk/certPath → NPE。
- setProperty 正向回环：internalsf/sectionsonly "true" 经 build().getProperty() 读回。
- 自动化：jtreg `run main` + `check()`；keytool 输出全部吞掉只看 exit 0。
