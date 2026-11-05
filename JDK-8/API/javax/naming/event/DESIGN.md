# javax.naming.event 包 测试设计案

> 覆盖目标：**完整契约**（事件对象 + 监听器接口 + 注册面）。headless 安全。

## 1. API 功能
命名事件通知：NamingEvent（4 事件类型常量 0..3 + dispatch 分派表）、NamingExceptionEvent、
EventContext/EventDirContext（addNamingListener 三 scope + targetMustExist）、
NamespaceChangeListener/ObjectChangeListener/NamingListener 接口。

## 2. Java 用户如何使用
`ctx.addNamingListener(target, EventContext.SUBTREE_SCOPE, listener)` 注册；服务端变化触发
`event.dispatch(listener)` 按类型分派到 objectAdded/Removed/Renamed/Changed；异常经 NamingExceptionEvent。

## 3. 参考来源
JDK javax.naming.event javadoc；NamingEvent.dispatch 的 switch 无 default 分支（未知类型静默）这一源码事实。

## 4. 测试用例设计（各类型子目录）
- **NamingEvent**：常量 0..3；dispatch 表（0→added/1→removed/2→renamed/3→changed，同步调用）；
  **类型与监听器不匹配抛 CCE**；未知类型 dispatch 无操作；null source IAE。
- **NamingExceptionEvent**：toString 为 EventObject 形态（**不含异常上下文**，须经 getException）；null source IAE。
- **EventContext**：OBJECT_SCOPE/ONELEVEL_SCOPE/SUBTREE_SCOPE=0/1/2；同一 listener 两次注册记两次、remove 清全部；targetRemovedFromScope 路由到 namingExceptionThrown。
- **EventDirContext**：**声明面恰 4 个 filter 版 addNamingListener**（Name/String × 过滤器/过滤器参数）。
- 监听器接口：方法集精确匹配（javap 对照）。

## 5. JDK-8 侧注记
本目录类型在 JDK-8 与 JDK-27 行为一致处共享断言文本；差异点（如 ArrayList 越界异常类、ldap/spi 子包缺失）
在各类型设计第 4 节内联标注。JDK-8 无 ldap/spi 子包（LdapDnsProvider 为 JDK-9+ 新增）。
