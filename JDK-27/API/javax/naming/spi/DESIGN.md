# javax.naming.spi 包 测试设计案

> 覆盖目标：**完整契约**（服务提供者插拔点全闭环）。headless 安全，全部自造工厂，绝不起真网络服务。

## 1. API 功能
JNDI SPI：NamingManager（getInitialContext、getObjectInstance、getStateToBind、getURLContext、
setInitialContextFactoryBuilder 一次性）、DirectoryManager（DirObjectFactory/DirStateFactory 分派）、
InitialContextFactory(+Builder)、ObjectFactory(+Builder)、StateFactory、DirObjectFactory、DirStateFactory、
Resolver/ResolveResult。

## 2. Java 用户如何使用
实现 InitialContextFactory 并经 `java.naming.factory.initial` 属性（或 Builder）注入；Reference 里带 ObjectFactory
类名实现按引用还原；StateFactory 在 bind 时换状态；Resolver.resolveToClass 逐段解析。

## 3. 参考来源
JDK NamingManager/DirectoryManager 源码（FactoryEnumeration 反射加载：**工厂类必须 public**，
模块 java.naming 内的加载器无法访问包私有类）；getURLContext 的内置 dns/ldap URL 工厂事实。

## 4. 测试用例设计（各类型子目录）
- **NamingManager**：env 属性路由工厂闭环；builder 一次性（二次 set 抛 IllegalStateException）；URL 上下文
  `http→null`、**内置 dns/ldap→非 null**（java.naming 模块自带）；getObjectInstance 经 Reference 类名还原；
  getStateToBind 状态替换。
- **DirectoryManager**：DirObjectFactory 收到 attrs；DirStateFactory.getResult 返回 (obj,attrs) 对。
- **DirObjectFactory/DirStateFactory**：嵌套工厂类声明 public（否则 FactoryEnumeration IllegalAccessException）；null attrs 合法。
- **InitialLdapContext 闭环**（见 ldap/）：connCtls 传到底层。
- **Resolver/ResolveResult**：两重载分派、参数原样转发；**ResolveResult 两参构造的 String 剩余名不可为 null（NPE）**。
- **ResolveResult**：公共构造仅 (Object,String)/(Object,Name)，单参为 protected。
