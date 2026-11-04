# java.beans.beancontext.BeanContextServicesListener — 设计案

## 1. API 功能
服务监听器接口（extends BeanContextServiceRevokedListener）。serviceAvailable + serviceRevoked。

## 2. Java 用户如何使用
注册到 BeanContextServices 接收服务可用/撤销事件。

## 3. 开源代码实际用法调研
JDK 源码 `BeanContextServicesListener.java`。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + 父接口。黑盒（`testBlackBox`）：匿名实现 serviceAvailable/serviceRevoked 分发。

## 5. 深化追加（批次 beansI，2026-11）
移除了错误的 `@key headful`。追加深度（8→52 断言）：单方法精确清单+签名；真实接线——addService 后两监听器收**同一事件实例**、source/serviceClass 精确、重复注册 no-op、revoke(false)/(true) 的 invalidate 旗标分别 false/true、移除停送+重注册恢复（计数矩阵含中途 addService）；嵌套——子 BCSS 监听器收父注册事件（source==父），**父 revoke 不向子上下文自身监听器扇出**（实测契约），叶子经委托链请求后 per-requestor 监听器收 invalidate=true；异常传播——serviceAvailable/serviceRevoked/per-requestor 三路抛 RuntimeException 均冒泡到 addService/revokeService 调用方且状态变更保持；JDK-27 侧 @Deprecated(forRemoval=true,since=23) 断言。双侧行为实测一致。
