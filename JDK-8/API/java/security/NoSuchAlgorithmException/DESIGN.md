# java.security.NoSuchAlgorithmException — 设计案

## 1. API 功能
GeneralSecurityException 子类异常（serialVersionUID=-7443947487218346562）。公共构造器：4（()/(String)/(String,Throwable)/(Throwable)）。继承链：NoSuchAlgorithmException → GeneralSecurityException → … → Exception/RuntimeException。

## 2. 使用方式
所有引擎工厂 getInstance 收到未知算法名时抛出，message 包含所请求的算法名（MessageDigest/Signature/KeyPairGenerator/KeyFactory/SecureRandom）。

## 3. 参考来源
- Oracle javadoc `java.security.NoSuchAlgorithmException`（构造器/层级/抛出场景）。
- OpenJDK `test/jdk/java/security/`（各引擎负例回归中该异常的真实抛出路径）。
- JCK runtime-8b `api/java_security/`（异常构造矩阵与序列化稳定性）。

## 4. 测试用例设计 + 易错规范细节
- 构造矩阵：全构造器 + getMessage/getCause 精确往返；(Throwable) 单参时 message == cause.toString()、cause 为同一对象。
- 异常链不变量：二次 initCause → IllegalStateException；initCause(this) → IllegalArgumentException（PAE 例外见下）；fillInStackTrace() 返回 this。
- 栈帧与抑制：setStackTrace/getStackTrace 往返 + 返回防御拷贝；addSuppressed/getSuppressed 往返；addSuppressed(null) → NPE。
- 形态：toString（null 消息裸类名 / 有消息 "类名: 消息"）；printStackTrace 流输出含类名；equals 为身份语义（同消息不同实例不等）。
- 序列化：serialVersionUID 精确锁定（本类 -7443947487218346562，双侧一致）+ round-trip 保持 message/cause/suppressed。
- 层级：继承链逐级 isAssignableFrom 断言。
- 五个引擎工厂（MessageDigest/Signature/KeyPairGenerator/KeyFactory/SecureRandom）bogus 名均断言 message contains 算法名（消息格式各引擎不同）。
