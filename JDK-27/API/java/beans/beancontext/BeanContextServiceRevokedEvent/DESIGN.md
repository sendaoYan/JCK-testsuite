# java.beans.beancontext.BeanContextServiceRevokedEvent — 设计案

## 1. API 功能
服务撤销事件（extends BeanContextEvent）。构造器 `(BeanContextServices, Class, boolean)`；getSourceAsBeanContextServices/getServiceClass/isServiceClass/isCurrentServiceInvalidNow。

## 2. Java 用户如何使用
serviceRevoked 监听器接收该事件；isCurrentServiceInvalidNow 决定是否强制撤销。

## 3. 开源代码实际用法调研
JDK 源码 `BeanContextServiceRevokedEvent.java`。

## 4. 测试用例设计 + 自动化实现
白盒：extends BeanContextEvent。黑盒（`testBlackBox`）：构造器、isServiceClass、isCurrentServiceInvalidNow 两个值。

## 5. 深化追加（批次 beansI，2026-11）
移除了错误的 `@key headful`。追加深度（8→84 断言）：恰 1 公共构造器 (BeanContextServices, Class, boolean)、4 方法清单、serviceClass protected + invalidateRefs **private** boolean 均非 transient、serialVersionUID -1295543154724961754L；**isCurrentServiceInvalidNow 恰等于构造器旗标**；isServiceClass 矩阵——同类 true/异类 false/原始类 false/**null 参数 false（不抛）**/超类 false/旗标无关；真实 revoke 流——revoke(false) invalidate=false、revoke(true) invalidate=true、**revoke(false) 遗留在册条目下一轮再收事件（每在册 requestor 条目一个事件）**、事后 getService → null（无 ServiceUnavailableException）；null 矩阵——null source IAE、null serviceClass 存储、isServiceClass(任意) → **NPE**；序列化——旗标两值存活、isServiceClass 往返可用、source transient null；JDK-27 侧 @Deprecated(forRemoval=true,since=23) 断言。双侧行为实测一致。
