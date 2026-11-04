# java.security.IdentityScope 黑盒行为测试设计

## 1. API 功能
- 静态：`getSystemScope()`（JDK-27 恒 null）
- 抽象：`addIdentity/removeIdentity/identities/getIdentity(String|PublicKey)/size`

## 2. Java 用户如何使用
子类实现一组成员管理；JDK-27 不再安装系统 scope。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/IdentityScope.java`
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/security/Identity/EqualsHashCodeContract.java`（scope 参与 equals）

## 4. 测试用例设计 + 自动化实现
- 白盒：abstract、getSystemScope 恒 null（JDK-27 实测）
- 黑盒：子类实现 Identity 成员表（List 实现），add/size/getIdentity(name)/(PublicKey)/remove 全链路组合；getIdentity(null) 走 null 分支不抛

断言入口：`IdentityScopeTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
