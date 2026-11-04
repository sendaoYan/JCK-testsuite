# java.beans.beancontext.BeanContextProxy — 设计案

## 1. API 功能
接口。`BeanContextChild getBeanContextProxy()` 返回 BeanContextChild 代理。

## 2. Java 用户如何使用
非 BeanContextChild 对象实现该接口暴露其 BeanContextChild 代理。

## 3. 开源代码实际用法调研
JDK 源码 `BeanContextProxy.java`。

## 4. 测试用例设计 + 自动化实现
白盒：接口。黑盒（`testBlackBox`）：匿名实现返回 BeanContextChildSupport 的 getBeanContextProxy。

## 5. 深化追加（批次 beansI，2026-11）
移除了错误的 `@key headful`。追加深度（8→46 断言）：单方法精确清单（**getBeanContextProxy 而非 getBeanContext**）、返回型恰 BeanContextChild；真实语义经 BeanContextSupport——add 代理组合后**代理与 peer 双双入册**（contains/size==2/成员事件含两者）、peer.setBeanContext 被路由、remove 任一齐删（移除事件含两者）；负例——getBeanContextProxy()==null 时 add → NPE("BeanContextPeer.getBeanContextProxy()")、BeanContextChild+BeanContextProxy 双实现 → IAE；记录：**BeanContextSupport/BCSS 自身不实现本接口**；peer 可独立作为 child；JDK-27 侧 @Deprecated(forRemoval=true,since=23) 断言。双侧行为实测一致。
