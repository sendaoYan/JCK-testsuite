# java.security.URIParameter 黑盒行为测试设计

## 1. API 功能
- 构造：`URIParameter(URI)`；查询 `getURI()`

## 2. Java 用户如何使用
Policy/Configuration getInstance 的参数载体。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/URIParameter.java`
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/javax/security/auth/login/Configuration/GetInstance.java`、`ConfigFileWithBlank.java`（URI 参数传入 getInstance）

## 4. 测试用例设计 + 自动化实现
- 正常：getURI 返回同一实例
- 组合：配合 Policy.getInstance("JavaPolicy", new URIParameter(...)) 走 NoSuchAlgorithmException（JDK-27 无实现）
- 异常：null URI → NPE

断言入口：`URIParameterTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。

## 5. 追加契约维度（深化批）
- getURI 重复调用同实例；非 AlgorithmParameterSpec（与 spec/ 家族区分）。
- identity equals/hashCode；opaque/相对/空 URI 全收。
- 恰 1 getter；**非 final**（实测）；非 Serializable。
