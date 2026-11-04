# java.security.AccessController 黑盒行为测试设计

## 1. API 功能
- 操作：`doPrivileged(PrivilegedAction)`、`doPrivileged(PrivilegedAction, AccessControlContext)`、`doPrivileged(PrivilegedExceptionAction[, AccessControlContext])`
- 查询：`getContext()`
- 检查：`checkPermission(Permission)`（JDK-27 固定抛 AccessControlException）

## 2. Java 用户如何使用
链路：`getContext() 快照 → doPrivileged(() -> 受限代码, acc)`；PrivilegedExceptionAction 抛受检异常时被包成 PrivilegedActionException 再抛。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/AccessController.java`（JDK-24 起 checkPermission 固定拒绝）
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/lang/Throwable/LegacyChainedExceptionSerialization.java`（PAE 链）
- 开源：RMI 测试库 `/data/yansendao/git/jdk25u-dev/test/jdk/java/rmi/testlibrary/TestLibrary.java` 用 doPrivileged 读系统属性

## 4. 测试用例设计 + 自动化实现
- 正常：doPrivileged 两种 action 返回值原样透传
- 组合：PrivilegedExceptionAction 抛受检异常 → 捕获 PrivilegedActionException，getCause/getException 指向原异常
- 异常：checkPermission 抛 AccessControlException；getContext 返回非 null ACC

断言入口：`AccessControllerTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
