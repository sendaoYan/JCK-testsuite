# java.security.PermissionCollection 黑盒行为测试设计

## 1. API 功能
- 抽象：`implies`、`elements`、`add`；钩子 `setReadOnly`/`isReadOnly`、`toString`

## 2. Java 用户如何使用
权限按类型分组存放；readOnly 后 add 抛 SecurityException（JDK-27）。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/PermissionCollection.java`
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/security/PermissionCollection/AddToReadOnlyPermissionCollection.java`、`Concurrent.java`

## 4. 测试用例设计 + 自动化实现
- 黑盒：子类实现 add/implies/elements；setReadOnly → isReadOnly=true 且 add 抛 SecurityException；elements 迭代顺序与加入顺序一致（ArrayList 实现，排序后断言）

断言入口：`PermissionCollectionTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
