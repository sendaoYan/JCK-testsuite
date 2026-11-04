# java.security.AccessControlException 黑盒行为测试设计

## 1. API 功能
- 构造：`AccessControlException(String)`、`AccessControlException(String, Permission)`
- 查询：`getPermission()`

## 2. Java 用户如何使用
安全检查失败时被抛出；用户捕获后取 `getPermission()` 定位被拒权限。链路：`catch (AccessControlException e) → e.getPermission() → 处理/降级`。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/AccessControlException.java`
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/lang/System/SecurityManagerWarnings.java`、`/data/yansendao/git/jdk25u-dev/test/jdk/java/lang/SecurityManager/Basic.java`（断言 SecurityManager 相关操作抛出的异常类型）

## 4. 测试用例设计 + 自动化实现
- 正常：单参构造 message 精确等于入参、双参构造 getPermission 返回同一实例
- 组合：由 AccessController.checkPermission 真实抛出并捕获，getPermission 为传入权限
- 边界：null permission 构造后 getPermission()==null；序列化字段保持（toString 包含 message）

断言入口：`AccessControlExceptionTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
