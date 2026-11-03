# javax.naming.spi.InitialContextFactory — 测试设计

## 1. API 功能
- 服务提供者接口。
- 单一方法：`Context getInitialContext(Hashtable<?, ?>)`（抛 `NamingException`）。

## 2. Java 用户如何使用
- 提供者实现该接口，返回初始上下文；由 `InitialContext` 或 `NamingManager.getInitialContext` 调用。
- 组合链路：`NamingManager.getInitialContext(env)` → `factory.getInitialContext(env)` → 返回 `Context`。

## 3. 开源代码实际用法调研
来源：`~/git/jdk25u-dev`。
- `javax/naming/spi/NamingManager.getInitialContext`：`return factory.getInitialContext(env);`。
- `com/sun/jndi/ldap/LdapCtxFactory` 是内置实现，`getInitialContext` 根据环境创建 LDAP 上下文。
- `com/sun/jndi/url/ldap/ldapURLContextFactory` 实现 URL 上下文工厂。

## 4. 测试用例设计
黑盒断言（`InitialContextFactoryTest.testBlackBox`）：
- 匿名实现验证 `getInitialContext` 收到同一环境对象，返回 null 被原样返回。
白盒断言（`testWhiteBox`）：public/interface、无超类、方法签名。
