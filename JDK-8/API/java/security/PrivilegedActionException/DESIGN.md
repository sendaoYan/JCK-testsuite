# java.security.PrivilegedActionException — 设计案

## 1. API 功能
Exception 子类异常（serialVersionUID=4724086851538908602）。公共构造器：1（(Exception)——唯一构造器）。继承链：PrivilegedActionException → Exception → Throwable（顶层父类，非 GeneralSecurityException 族）。

## 2. 使用方式
`AccessController.doPrivileged(PrivilegedExceptionAction)` 中受检异常的统一包装；getException()/getCause() 返回同一被包装异常；非受检异常与 Error 不包装直通。

## 3. 参考来源
- Oracle javadoc `java.security.PrivilegedActionException`（构造器/层级/抛出场景）。
- OpenJDK `test/jdk/java/security/`（各引擎负例回归中该异常的真实抛出路径）。
- JCK runtime-8b `api/java_security/`（异常构造矩阵与序列化稳定性）。

## 4. 测试用例设计 + 易错规范细节
- 构造矩阵：全构造器 + getMessage/getCause 精确往返；(Throwable) 单参时 message == cause.toString()、cause 为同一对象。
- 异常链不变量：二次 initCause → IllegalStateException；initCause(this) → IllegalArgumentException（PAE 例外见下）；fillInStackTrace() 返回 this。
- 栈帧与抑制：setStackTrace/getStackTrace 往返 + 返回防御拷贝；addSuppressed/getSuppressed 往返；addSuppressed(null) → NPE。
- 形态：toString（null 消息裸类名 / 有消息 "类名: 消息"）；printStackTrace 流输出含类名；equals 为身份语义（同消息不同实例不等）。
- 序列化：serialVersionUID 精确锁定（本类 4724086851538908602，双侧一致）+ round-trip 保持 message/cause/suppressed。
- 层级：继承链逐级 isAssignableFrom 断言。
- **cause 槽始终被构造器初始化**（经 super(exception)，即使参数为 null）→ initCause 一律 ISE（与普通异常可 initCause(self)→IAE 不同）；getMessage() 恒 null（不镜像被包装异常消息）；RuntimeException/Error 直通不包装；getException() 双侧均未标 @Deprecated；SUID=4724086851538908602。
