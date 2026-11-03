# java.security.PrivilegedActionException 黑盒行为测试设计

## 1. API 功能
- 构造：`PrivilegedActionException(Exception)`；查询 `getException()`（=getCause）、toString（追加 cause）

## 2. Java 用户如何使用
doPrivileged(PrivilegedExceptionAction) 受检异常的统一包装；JDK-27 getMessage() 恒 null，toString 含 cause。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/PrivilegedActionException.java`（super(null, cause)，无 message）
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/security/PrivilegedActionException/PrintWrappedException.java`、`/data/yansendao/git/jdk25u-dev/test/jdk/java/lang/Throwable/LegacyChainedExceptionSerialization.java`

## 4. 测试用例设计 + 自动化实现
- 正常：getException/getCause 同一实例、toString 含异常类名与 cause 消息
- 组合：doPrivileged 真实产生 PAE 全链路断言
- 边界：JDK-27 接受 null exception（getException()==null）；getMessage()==null（不镜像 cause 消息，JDK-17+ 行为）

断言入口：`PrivilegedActionExceptionTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
