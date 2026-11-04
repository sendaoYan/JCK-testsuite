# java.security.PEMDecoder 黑盒行为测试设计

## 1. API 功能
- 工厂：`of()`（共享实例）
- 操作：`decode(String)`/`decode(Reader)` → BinaryEncodable

## 2. Java 用户如何使用
按 "-----BEGIN X-----" 标签路由：CERTIFICATE→X509Certificate、PRIVATE KEY→PrivateKey、未知标签→PEM、垃圾输入→IAE。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/PEMDecoder.java`（preview）
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/security/PEMDecoder/`（各类型解码断言）

## 4. 测试用例设计 + 自动化实现
- 正常：固定自签根证书（内嵌 base64，keytool 预生成）解码为 X509Certificate 且与 CertificateFactory 结果 equals；PKCS#8 私钥解码 getFormat=PKCS#8 且模数与证书公钥一致；CRL 标签 → X509CRL
- 组合：PEMEncoder 产物再解码 equals
- 异常：非 PEM 文本 IAE；of() 返回共享实例（JDK-27 实测）

断言入口：`PEMDecoderTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。

## 5. 契约深化补充（本批）
- **实现方式**：PEM 族为 JDK-27 preview API，测试改为全反射访问（无 `--enable-preview` 依赖）。
- **fixture 修正**：原 ROOT_KEY_B64 内嵌串损坏（1625 字符，%4==1，`PEM.decode` 抛 base64 错误）；2026-11-05 用 JDK-27 keytool 重新生成 CN=jck-root 证书+配套 PKCS#8 私钥（1624/1072 字符，模数校验一致）后全部通过。
- 深化断言：decode(null String/null InputStream/null Class) → NPE；CRLF 帧接受；BEGIN/END 标签不匹配 IAE；缺 END IAE；**拼接多块时第一块生效**；宽松 base64 主体 content() 原样保留；未知类型保留原始 base64 且 decode() 给出 DER；`withDecryption`/`withFactoriesOf` 每次返回新实例而 `of()` 共享单例；ENCRYPTED PRIVATE KEY 无密码路径按容错处理（IAE 或忽略垃圾体均可，实测记录）。
