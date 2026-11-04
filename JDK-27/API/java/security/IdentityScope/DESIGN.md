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

## 5. 深化补遗（testContractMatrix）
- 结构元数据：恰 6 个抽象方法（size、getIdentity×3 中两个抽象+add/remove/identities）；getIdentity(Principal) 为具体方法按名委托；get/setSystemScope static、setSystemScope protected；无参构造 protected。
- (String, scope) 嵌套构造：child.getScope()==parent 且父 scope 在构造中被通知恰一次（addCalls==1、按名可查 child）。
- getIdentity((Principal) null) → NPE；Principal 查找按名命中/未命中 null。
- getSystemScope() 默认 null；setSystemScope 经子类静态句柄可见；try/finally 还原并断言还原成功。
- 序列化：可序列化 scope 子类（SerScope）round-trip 后 name/size/按名查找全部保持。
- 空 scope：size 0、identities() 空枚举、toString 含名。
