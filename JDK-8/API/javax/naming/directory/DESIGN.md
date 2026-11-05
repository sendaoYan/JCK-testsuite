# javax.naming.directory 包 测试设计案

> 覆盖目标：**完整契约**（属性模型 + DirContext 门面 + 9 个异常）。headless 安全，无网络、无真实 LDAP。

## 1. API 功能
目录服务扩展：Attributes/Attribute 属性模型（有序/无序、忽略大小写）、DirContext（getAttributes 四重载、
bind/rebind 三参、modifyAttributes、search 四形态、schema 三族、createSubcontext 三参）、InitialDirContext 门面、
ModificationItem/SearchControls/SearchResult 值对象、9 个目录异常。

## 2. Java 用户如何使用
`InitialDirContext ctx = new InitialDirContext(env); ctx.bind("cn=x", obj, attrs); ctx.getAttributes("cn=x", new String[]{"cn"});`
`search` 用 Attributes 匹配或 RFC 2254 过滤器；`modifyAttributes` 用 ADD/REPLACE/REMOVE 三常量。

## 3. 参考来源
JDK javax.naming.directory javadoc；NamingManager/DirectoryManager 源码（getObjectInstance 分派）；
Harmony JNDI 目录测试的属性矩阵思路。

## 4. 测试用例设计（各类型子目录）
- **Attributes/BasicAttributes**：equals 含 ignoreCase 标志（hashCode 基数恰 +1）；**跨大小写 id 的 Attribute 级 equals 仍区分大小写**；put 不同大小写替换；clone/序列化。
- **Attribute/BasicAttribute**：有序/无序、get(int) 越界 AIOOBE、add(int,..) 有序插入、contains/allows null 值。
- **DirContext**：内存实现端到端 bind(三参)→getAttributes(ids=null **返回全部**)→modifyAttributes(ADD/REMOVE/REPLACE)→search(匹配/过滤器/过滤器参数/存在 `(attr=*)`)→rebind(null attrs) **替换属性集**；接口面无 getSchemaName（易错）。
- **InitialDirContext**：no-arg 构造 **lazy 不抛**，首次操作才 NoInitialContextException（消息含 java.naming.factory.initial）；工厂经 env 闭环；getEnvironment 委托底层上下文（工厂需透传 env）。
- **异常 9 个**：构造链 + 真实触发点（AttributeInUse 由已存在 id 触发等）。
- SearchControls：returningObjFlag/countLimit/timeLimit 默认值矩阵 + 越界负例。

## 5. JDK-8 侧注记
本目录类型在 JDK-8 与 JDK-27 行为一致处共享断言文本；差异点（如 ArrayList 越界异常类、ldap/spi 子包缺失）
在各类型设计第 4 节内联标注。JDK-8 无 ldap/spi 子包（LdapDnsProvider 为 JDK-9+ 新增）。
