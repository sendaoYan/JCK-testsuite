# java.security.PrivilegedAction 黑盒行为测试设计

## 1. API 功能
- 抽象方法：`run()` 返回 T

## 2. Java 用户如何使用
lambda 传给 `AccessController.doPrivileged`；异常透传（不包装）。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/PrivilegedAction.java`（@FunctionalInterface）
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/lang/reflect/AccessibleObject/CanAccessTest.java`（doPrivileged+PrivilegedAction 组合）

## 4. 测试用例设计 + 自动化实现
- 黑盒：lambda 作为 PrivilegedAction 直接 run 返回固定值；doPrivileged 透传返回值；run 中抛 RuntimeException 原样向外抛（不被包装）；方法引用形态可用

断言入口：`PrivilegedActionTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
