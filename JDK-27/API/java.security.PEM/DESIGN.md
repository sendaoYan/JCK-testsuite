# java.security.PEM 黑盒行为测试设计

## 1. API 功能
- 构造：`PEM(String type, String base64Content)`、`PEM(String type, byte[] content)`
- 查询：`type()`、`subtype()`、`content()`、`encodableName()`

## 2. Java 用户如何使用
统一 PEM 表达：cert/key/CRL 以 (type, DER bytes) 记录，与 PEMEncoder/PEMDecoder 组成 round-trip。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/PEM.java`（preview）
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/security/PEMDecoder/` 系列预览测试（cert/key/CRL 解码矩阵）

## 4. 测试用例设计 + 自动化实现
- 正常：type/content 透传、equals/hashCode 基于两字段
- 组合：PEM → PEMEncoder.encodeToString → PEMDecoder.decode → equals 往返
- 异常：IAE（坏 base64）；null content NPE

断言入口：`PEMTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
