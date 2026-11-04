# java.security.UnrecoverableEntryException 黑盒行为测试设计

## 1. API 功能
- 构造：`()`、`(String)`；子类 UnrecoverableKeyException

## 2. Java 用户如何使用
KeyStore.getEntry 口令错误/条目损坏时抛出。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/UnrecoverableEntryException.java`
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/java/security/KeyStore/EntryMethods.java`（getEntry 错误口令路径）

## 4. 测试用例设计 + 自动化实现
- 正常：无参/带 message 构造精确断言
- 组合：PKCS12 内存库错口令 getEntry → UnrecoverableKeyException（子类）instanceof 本类
- 边界：子类多态 catch 顺序

断言入口：`UnrecoverableEntryExceptionTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。


## 5. 契约深化补充（本批）
在既有白盒/黑盒基础上追加 `testContractDeep()`：
- 构造矩阵补 (String,Throwable)/(Throwable)（本类适用的全部公共构造器）+ 消息/cause 精确往返。
- Throwable 通用契约：二次 initCause→ISE、fillInStackTrace==this、set/getStackTrace 防御拷贝、addSuppressed(+null→NPE)、suppressed 随序列化保持。
- 形态：toString 两种形态、printStackTrace 含类名、身份 equals。
- serialVersionUID 精确锁定（-4527142945246286535，与 JDK-8 一致）+ 序列化 round-trip。
- 真实抛出路径（引擎级闭环）与多态捕获链。
- 族根仅有 ()/(String) 两构造器；多态捕获链 UKE→UEE→GSE 三层断言。
