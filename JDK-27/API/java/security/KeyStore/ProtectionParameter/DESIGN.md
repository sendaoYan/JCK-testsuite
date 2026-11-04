# java.security.KeyStore.ProtectionParameter 黑盒行为测试设计

## 1. API 功能
- 标记接口（无方法）；实现：PasswordProtection、CallbackHandlerProtection

## 2. Java 用户如何使用
为不同条目声明保护方式。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/KeyStore.java`
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/security/KeyStore/EntryMethods.java`

## 4. 测试用例设计 + 自动化实现
- 正常：两个 JDK 实现 instanceof 满足；匿名实现合法
- 组合：默认类型空装载端到端走通

断言入口：`ProtectionParameterTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。


## 5. 契约深化补充（testContractDeep）
- 0 个声明方法（纯标记）；匿名实现 identity 相等。
- ProtectionParameter[] 协变装载两种内置实现。
- DomainLoadStoreParameter 的保护 map 以本接口为值类型；PasswordProtection 经 Destroyable 引用 destroy 生效。
- 补充：自定义匿名 ProtectionParameter 取 key 条目 → UnsupportedOperationException；Builder 原样接受并返回匿名标记值；DLSP 快照视图 put → UOE；PasswordProtection destroy 幂等。
