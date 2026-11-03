# com.sun.security.jgss.ExtendedGSSContext 测试设计

## 1. API 功能
- 类型：`public interface ExtendedGSSContext extends org.ietf.jgss.GSSContext`（模块 `jdk.security.jgss`）。
- 新增方法：
  - `Object inquireSecContext(InquireType type) throws GSSException`——按 `InquireType` 查询机制特定属性（如会话密钥）。
  - `void requestDelegPolicy(boolean state) throws GSSException`——请求遵循委派策略（仅 initiator 侧、建立上下文前调用）。
  - `boolean getDelegPolicyState()`——返回委派策略应答状态。
- 继承 `GSSContext` 的全部抽象方法（约 40 个，无 default 方法）。

## 2. Java 用户如何使用
```java
GSSContext ctxt = manager.createContext(...);
if (ctxt instanceof ExtendedGSSContext) {
    ExtendedGSSContext ex = (ExtendedGSSContext) ctxt;
    ex.requestDelegPolicy(true);
    // ... 建立上下文 ...
    Object attr = ex.inquireSecContext(InquireType.KRB5_GET_SESSION_KEY);
    boolean policy = ex.getDelegPolicyState();
}
```

## 3. 开源代码实际用法调研
- JDK 源码定义：`~/git/jdk25u-dev/src/jdk.security.jgss/share/classes/com/sun/security/jgss/ExtendedGSSContext.java`（三方法 Javadoc 含异常/委派策略语义）。
- JDK 自带测试：`~/git/jdk25u-dev/test/jdk/sun/security/krb5/auto/NewInquireTypes.java`、`Context.java`（krb5 auto 测试框架）通过 `ExtendedGSSContext` 的 `inquireSecContext` 读取会话密钥/票据标志/授权数据等。
- 第三方框架基本通过 `org.ietf.jgss` 标准接口访问，扩展接口主要被 JDK 自身 krb5 测试使用。

## 4. 测试用例设计 + 自动化实现
采用 `java.lang.reflect.Proxy` 动态实现 `ExtendedGSSContext`（因为 `GSSContext` 有约 40 个抽象方法且无 default 方法，匿名实现等价于大量 stub；Proxy 的 InvocationHandler 对 `inquireSecContext` 返回 null、`requestDelegPolicy` 无操作、`getDelegPolicyState` 返回 false，其余按返回类型给默认值）。
黑盒部分（`ExtendedGSSContextTest.testBlackBox()`）覆盖：
- proxy `instanceof GSSContext`。
- `requestDelegPolicy(true/false)` 可调用不抛异常；`getDelegPolicyState()` 返回默认 false。
- 对每个 `InquireType` 常量调用 `inquireSecContext(t)` 返回 null；`inquireSecContext(null)` 返回 null。
白盒部分补齐：isInterface/extends GSSContext/三方法存在。
