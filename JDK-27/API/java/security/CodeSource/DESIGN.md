# java.security.CodeSource 黑盒行为测试设计

## 1. API 功能
- 构造：`CodeSource(URL, Certificate[])`、`CodeSource(URL, CodeSigner[])`
- 查询：`getLocation()`、`getCertificates()`（克隆）、`getCodeSigners()`、`implies(CodeSource)`、`toString/hashCode/equals`

## 2. Java 用户如何使用
保护域绑定：`new ProtectionDomain(cs, perms)`；implies 用于策略匹配，`/-` 与 `/*` 文件路径形式支持递归/单层蕴含。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/CodeSource.java`（matchCerts/matchLocation：JDK-27 普通目录 URL 不蕴含子文件）
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/io/FilePermission/FilePermissionCollection.java`（CodeSource + 权限）

## 4. 测试用例设计 + 自动化实现
- 正常：URL/证书构造，getCertificates 返回克隆；equals/hashCode 基于 URL+签名者
- 组合：implies 矩阵——精确匹配、`/-` 递归、`/*` 单层、普通目录不蕴含子文件、无证书不蕴含有证书（反向成立）、null location 恒真/恒假方向
- 异常：implies(null) → false

断言入口：`CodeSourceTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
