# com.sun.security.jgss.ExtendedGSSCredential 测试设计

## 1. API 功能
- 类型：`public interface ExtendedGSSCredential extends org.ietf.jgss.GSSCredential`（模块 `jdk.security.jgss`）。
- 新增方法：`GSSCredential impersonate(GSSName name) throws GSSException`——模拟（impersonate）某主体，Kerberos 中可用 S4U2self 实现；失败时抛 `GSSException.NO_CRED`/`FAILURE`。
- 继承 `GSSCredential` 的全部抽象方法（dispose/getName/getRemainingLifetime/getUsage/getMechs/add/equals/hashCode 等，无 default 方法）。

## 2. Java 用户如何使用
```java
GSSCredential cred = manager.createCredential(...);
if (cred instanceof ExtendedGSSCredential) {
    GSSCredential imp = ((ExtendedGSSCredential) cred).impersonate(someName);
}
```

## 3. 开源代码实际用法调研
- JDK 源码定义：`~/git/jdk25u-dev/src/jdk.security.jgss/share/classes/com/sun/security/jgss/ExtendedGSSCredential.java`（`impersonate` 的 Javadoc 说明 S4U2self 与异常码）。
- JDK 自带测试：`~/git/jdk25u-dev/test/jdk/sun/security/krb5/auto/S4U2selfGSS.java`、`S4U2selfNotF.java` 使用 `ExtendedGSSCredential.impersonate` 验证 S4U2self 委派。
- 第三方框架基本通过标准 `GSSCredential` 使用，扩展接口主要被 JDK krb5 测试使用。

## 4. 测试用例设计 + 自动化实现
采用 `java.lang.reflect.Proxy` 动态实现 `ExtendedGSSCredential`（`GSSCredential` 抽象方法较多，Proxy 的 InvocationHandler 对 `impersonate` 返回 null、其余按返回类型给默认值）。
黑盒部分（`ExtendedGSSCredentialTest.testBlackBox()`）覆盖：
- proxy `instanceof GSSCredential`。
- `impersonate(null)` 可调用并返回 null stub，不抛异常。
白盒部分补齐：isInterface/extends GSSCredential/存在 impersonate 方法。
