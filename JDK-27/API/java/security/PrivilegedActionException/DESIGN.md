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


## 5. 契约深化补充（本批）
在既有白盒/黑盒基础上追加 `testContractDeep()`：
- 构造矩阵补 (String,Throwable)/(Throwable)（本类适用的全部公共构造器）+ 消息/cause 精确往返。
- Throwable 通用契约：二次 initCause→ISE、fillInStackTrace==this、set/getStackTrace 防御拷贝、addSuppressed(+null→NPE)、suppressed 随序列化保持。
- 形态：toString 两种形态、printStackTrace 含类名、身份 equals。
- serialVersionUID 精确锁定（4724086851538908602，与 JDK-8 一致）+ 序列化 round-trip。
- 真实抛出路径（引擎级闭环）与多态捕获链。
- **cause 槽始终被构造器初始化**（经 super(exception)，即使参数为 null）→ initCause 一律 ISE（与普通异常可 initCause(self)→IAE 不同）；getMessage() 恒 null（不镜像被包装异常消息）；RuntimeException/Error 直通不包装；getException() 双侧均未标 @Deprecated；SUID=4724086851538908602。
