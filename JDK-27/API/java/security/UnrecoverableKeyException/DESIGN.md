# java.security.UnrecoverableKeyException 黑盒行为测试设计

## 1. API 功能
- 构造：`()`、`(String)`

## 2. Java 用户如何使用
getKey(alias, wrongPassword) 失败的标准信号。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/UnrecoverableKeyException.java`
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/security/KeyStore/TestKeyStoreBasic.java`（错口令抛 UKE）

## 4. 测试用例设计 + 自动化实现
- 正常：2 构造器 message 精断、toString/getMessage 一致
- 组合：PKCS12 round-trip 后错口令 getKey 抛本异常、对 alias getEntry 抛 UnrecoverableEntryException 家族

断言入口：`UnrecoverableKeyExceptionTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。


## 5. 契约深化补充（本批）
在既有白盒/黑盒基础上追加 `testContractDeep()`：
- 构造矩阵补 (String,Throwable)/(Throwable)（本类适用的全部公共构造器）+ 消息/cause 精确往返。
- Throwable 通用契约：二次 initCause→ISE、fillInStackTrace==this、set/getStackTrace 防御拷贝、addSuppressed(+null→NPE)、suppressed 随序列化保持。
- 形态：toString 两种形态、printStackTrace 含类名、身份 equals。
- serialVersionUID 精确锁定（7275063078190151277，与 JDK-8 一致）+ 序列化 round-trip。
- 真实抛出路径（引擎级闭环）与多态捕获链。
- JDK-8 与 JDK-27 的错误口令消息文本不同（invalid stream header vs Bad padding）——只断异常类型；null protection 的消息双侧一致含 "password"。正确口令正例先行（getKey 非 null）再走负例。
