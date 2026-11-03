# java.security.Timestamp 黑盒行为测试设计

## 1. API 功能
- 构造：`Timestamp(Date, CertPath)`
- 查询：`getTimestamp()`、`getSignerCertPath()`、`equals/hashCode/toString`

## 2. Java 用户如何使用
CodeSigner 的时间戳分量；签名 jar 校验时参与 CodeSource equals。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/Timestamp.java`
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/util/jar/JarFile/`（签名块时间戳处理）

## 4. 测试用例设计 + 自动化实现
- 正常：固定 Date+CertPath（内嵌自签证书）构造，getter 精确往返、equals/hashCode 自反
- 组合：装入 CodeSigner/CodeSource 后读回同一实例
- 异常：null 入参 NPE；不同 Date 不等

断言入口：`TimestampTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
