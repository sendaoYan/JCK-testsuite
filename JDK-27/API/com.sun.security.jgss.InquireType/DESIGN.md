# com.sun.security.jgss.InquireType 测试设计

## 1. API 功能
- 类型：`public enum InquireType`（模块 `jdk.security.jgss`），作为 `ExtendedGSSContext.inquireSecContext(InquireType)` 的参数，指定要查询的 Kerberos 属性。
- 枚举常量（声明序）：
  - `KRB5_GET_SESSION_KEY`（`@Deprecated`，被 `KRB5_GET_SESSION_KEY_EX` 取代）
  - `KRB5_GET_SESSION_KEY_EX`（返回 `javax.security.auth.kerberos.EncryptionKey`）
  - `KRB5_GET_TKT_FLAGS`（服务票据标志，boolean 数组）
  - `KRB5_GET_AUTHZ_DATA`（授权数据）
  - `KRB5_GET_AUTHTIME`（认证时间，String）
  - `KRB5_GET_KRB_CRED`（`javax.security.auth.kerberos.KerberosCredMessage`）
- 注意：**没有** `KRB5_GET_KEYTAB` 常量（任务简报中的该名字不准确）。
- 方法：`values()`、`valueOf(String)` 及 `Enum` 继承方法。

## 2. Java 用户如何使用
```java
import com.sun.security.jgss.*;
if (ctxt instanceof ExtendedGSSContext) {
    ExtendedGSSContext ex = (ExtendedGSSContext) ctxt;
    Object key = ex.inquireSecContext(InquireType.KRB5_GET_SESSION_KEY_EX);
}
for (InquireType t : InquireType.values()) { ... }
```

## 3. 开源代码实际用法调研
- JDK 源码定义：`~/git/jdk25u-dev/src/jdk.security.jgss/share/classes/com/sun/security/jgss/InquireType.java`，六个常量及各自 Javadoc（返回类型、`KRB5_GET_SESSION_KEY` 已 `@Deprecated`）。
- JDK 自带测试：`~/git/jdk25u-dev/test/jdk/sun/security/krb5/auto/NewInquireTypes.java`、`Context.java`（krb5 auto 框架）用 `InquireType.KRB5_GET_SESSION_KEY_EX/TKT_FLAGS/AUTHZ_DATA/AUTHTIME/KRB_CRED` 查询上下文属性。
- 第三方框架不直接使用，行为权威以 JDK 源码 + test/jdk 为准。

## 4. 测试用例设计 + 自动化实现
黑盒部分（`InquireTypeTest.testBlackBox()`）覆盖：
- `values()` 返回六个常量的声明序。
- 每个常量的 `ordinal()`、`toString()==name()`、`getDeclaringClass()`、`valueOf(name)` 恒等。
- `compareTo` 顺序；`valueOf("KRB5_GET_KEYTAB")` 抛 `IllegalArgumentException`（确认无该常量）；`valueOf(null)` 抛 `NullPointerException`。
白盒部分补齐：isEnum/final/extends Enum/六字段存在/6 常量。
