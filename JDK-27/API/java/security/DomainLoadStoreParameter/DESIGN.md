# java.security.DomainLoadStoreParameter 黑盒行为测试设计

## 1. API 功能
- 构造：`DomainLoadStoreParameter(URI, Map<String, ProtectionParameter>)`
- 查询：`getConfiguration()`、`getProtectionParameter()`（恒 null）、`getProtectionParams()`（不可变视图）

## 2. Java 用户如何使用
DKS（Domain KeyStore）load/store 时传入：`ks.load(new DomainLoadStoreParameter(uri, Map.of(...)))`。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/DomainLoadStoreParameter.java`
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/sun/security/provider/KeyStore/DKSTest.java`、`DksWithEmptyKeystore.java`（DKS 全链路）

## 4. 测试用例设计 + 自动化实现
- 正常：getConfiguration 返回同一 URI；getProtectionParameter 恒 null（规范如此，keystore 条目在 params 里）
- 组合：getProtectionParams 内容与顺序、空 map
- 异常：返回 map 不可修改——put 抛 UnsupportedOperationException；null URI/构造 NPE

断言入口：`DomainLoadStoreParameterTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。

## 5. 追加契约维度（深化批）
- 不可写视图全通道：remove/putAll/clear 亦 UOE。
- 构造快照语义：源 Map 构造后增删不可见（防御拷贝）。
- identity equals（同内容不同实例不等）；恰 3 getter；类 final。
