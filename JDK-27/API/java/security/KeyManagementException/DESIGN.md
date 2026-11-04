# java.security.KeyManagementException 黑盒行为测试设计

## 1. API 功能
- 构造：`()`、`(String)`、`(String, Throwable)`、`(Throwable)`

## 2. Java 用户如何使用
密钥/证书管理操作失败的通用信号；JDK-27 中 Identity.addCertificate 等遗留 API 声明抛出。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/KeyManagementException.java`
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/security/Identity/EqualsHashCodeContract.java`（Identity API 抛 KME）

## 4. 测试用例设计 + 自动化实现
- 正常：4 构造器 message/cause 精确断言（null 保持 null）
- 组合：catch 后 getCause/initCause 语义；toString/getMessage 一致
- 行为差异：JDK-27 Identity.setPublicKey 实际不再抛 KME（仅声明）

断言入口：`KeyManagementExceptionTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。


## 5. 契约深化补充（本批）
在既有白盒/黑盒基础上追加 `testContractDeep()`：
- 构造矩阵补 (String,Throwable)/(Throwable)（本类适用的全部公共构造器）+ 消息/cause 精确往返。
- Throwable 通用契约：二次 initCause→ISE、fillInStackTrace==this、set/getStackTrace 防御拷贝、addSuppressed(+null→NPE)、suppressed 随序列化保持。
- 形态：toString 两种形态、printStackTrace 含类名、身份 equals。
- serialVersionUID 精确锁定（947674216157062695，与 JDK-8 一致）+ 序列化 round-trip。
- 真实抛出路径（引擎级闭环）与多态捕获链。
- 公开引擎无直接抛出点（Identity 体系已废弃）；按构造语义、序列化与族关系断言，不伪造引擎路径。
