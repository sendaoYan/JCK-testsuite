# java.security.InvalidKeyException — 设计案

## 1. API 功能
KeyException 子类异常（serialVersionUID=5698479920593359816）。公共构造器：4（()/(String)/(String,Throwable)/(Throwable)）。继承链：InvalidKeyException → KeyException → … → Exception/RuntimeException。

## 2. 使用方式
`Signature.initSign/initVerify` 收到 null、类型不匹配或无 provider 支持的密钥时抛出；是 KeyException 在引擎层的直接落点。

## 3. 参考来源
- Oracle javadoc `java.security.InvalidKeyException`（构造器/层级/抛出场景）。
- OpenJDK `test/jdk/java/security/`（各引擎负例回归中该异常的真实抛出路径）。
- JCK runtime-8b `api/java_security/`（异常构造矩阵与序列化稳定性）。

## 4. 测试用例设计 + 易错规范细节
- 构造矩阵：全构造器 + getMessage/getCause 精确往返；(Throwable) 单参时 message == cause.toString()、cause 为同一对象。
- 异常链不变量：二次 initCause → IllegalStateException；initCause(this) → IllegalArgumentException（PAE 例外见下）；fillInStackTrace() 返回 this。
- 栈帧与抑制：setStackTrace/getStackTrace 往返 + 返回防御拷贝；addSuppressed/getSuppressed 往返；addSuppressed(null) → NPE。
- 形态：toString（null 消息裸类名 / 有消息 "类名: 消息"）；printStackTrace 流输出含类名；equals 为身份语义（同消息不同实例不等）。
- 序列化：serialVersionUID 精确锁定（本类 5698479920593359816，双侧一致）+ round-trip 保持 message/cause/suppressed。
- 层级：继承链逐级 isAssignableFrom 断言。
- 伪造密钥用静态命名类 `FakePrivKey implements PrivateKey`（匿名 Key 不能强转 PrivateKey，会先吃到 ClassCastException）；initVerify 需显式 `(PublicKey) null` 消除与 Certificate 重载的歧义。
