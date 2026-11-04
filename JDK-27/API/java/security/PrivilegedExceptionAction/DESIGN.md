# java.security.PrivilegedExceptionAction 黑盒行为测试设计

## 1. API 功能
- 抽象方法：`run()` throws Exception

## 2. Java 用户如何使用
doPrivileged 包裹后受检异常被包成 PrivilegedActionException；getCause/getException 还原。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/PrivilegedExceptionAction.java`
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/security/PrivilegedActionException/PrintWrappedException.java`（包装/还原链路）

## 4. 测试用例设计 + 自动化实现
- 正常：run 返回值透传
- 组合：run 抛 CloneNotSupportedException → doPrivileged 抛 PAE 且 getException()==原异常、getMessage() 为 null（JDK-27 构造器用 super(null, cause)）
- 异常：run 抛 RuntimeException 不包装直接外抛

断言入口：`PrivilegedExceptionActionTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
