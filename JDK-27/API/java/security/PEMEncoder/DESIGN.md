# java.security.PEMEncoder 黑盒行为测试设计

## 1. API 功能
- 工厂：`of()`（共享实例）
- 操作：`encodeToString(BinaryEncodable)`、`encode(BinaryEncodable, Writer)`

## 2. Java 用户如何使用
证书/私钥/PEM → `-----BEGIN/END...-----` 文本，76 列 MIME base64。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/PEMEncoder.java`（preview）
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/security/PEMEncoder/`

## 4. 测试用例设计 + 自动化实现
- 正常：X509Certificate 直接 encodeToString 产出 BEGIN CERTIFICATE 头尾；PEM(X509 CRL, der) 产出 BEGIN X509 CRL
- 组合：encode → decode equals 往返
- 边界：of() 共享实例（JDK-27 实测）；多段内容行结构（首行 BEGIN、末行 END）

断言入口：`PEMEncoderTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。

## 5. 契约深化补充（本批）
- **实现方式**：PEM 族为 JDK-27 preview API，测试改为全反射访问（无 `--enable-preview` 依赖）。
- 深化断言：encodeToString(null)/encode(null)/withEncryption(null) → NPE，withEncryption(空数组) 合法；精确规范形 CRLF+尾 CRLF；byte[] 与 String 形式逐字节一致；同对象两次编码相同且等于 `PEM.toString()`；长 body 单行不重排；经 decoder 往返后 type/decode 字节保持且再编码稳定；证书重复编码确定。
