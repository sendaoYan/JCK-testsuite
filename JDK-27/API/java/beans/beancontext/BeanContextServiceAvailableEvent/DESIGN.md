# java.beans.beancontext.BeanContextServiceAvailableEvent — 设计案

## 1. API 功能
服务可用事件（extends BeanContextEvent）。构造器 `(BeanContextServices, Class)`；getSourceAsBeanContextServices/getServiceClass/getCurrentServiceSelectors。

## 2. Java 用户如何使用
serviceAvailable 监听器接收该事件，查询可用的服务与选择器。

## 3. 开源代码实际用法调研
JDK 源码 `BeanContextServiceAvailableEvent.java`。

## 4. 测试用例设计 + 自动化实现
白盒：extends BeanContextEvent。黑盒（`testBlackBox`）：构造器及 getSourceAsBeanContextServices/getServiceClass/getCurrentServiceSelectors。

## 5. 深化追加（批次 beansI，2026-11）
移除了错误的 `@key headful`。追加深度（8→64 断言）：恰 1 公共构造器 (BeanContextServices, Class)、3 方法清单、serviceClass protected 非 transient、serialVersionUID -5333985775656400778L 精确断言；真实 addService 流捕获事件——三 source 访问器身份相等、serviceClass 精确；**getCurrentServiceSelectors 活委托**——注册期间返回 provider 的 alpha/beta、revoke 后同一事件 → null（非快照，实测）、未注册类 null；null 矩阵——null source → IAE("null source")（EventObject 契约）、null serviceClass 合法存储（selectors 经 source → null、toString 不抛）；传播三态 + toString 前缀断言；序列化——serviceClass/propagatedFrom 存活、source transient → 三访问器 null、**getCurrentServiceSelectors 往返后 → NPE**；JDK-27 侧 @Deprecated(forRemoval=true,since=23) 断言。双侧行为实测一致。
