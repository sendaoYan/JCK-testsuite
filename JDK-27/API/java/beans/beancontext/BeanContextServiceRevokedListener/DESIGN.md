# java.beans.beancontext.BeanContextServiceRevokedListener — 设计案

## 1. API 功能
服务撤销监听器接口（extends EventListener）。serviceRevoked。

## 2. Java 用户如何使用
服务被撤销时接收通知。

## 3. 开源代码实际用法调研
JDK 源码 `BeanContextServiceRevokedListener.java`。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + extends EventListener。黑盒（`testBlackBox`）：匿名实现 serviceRevoked 分发。

## 5. 深化追加（批次 beansI，2026-11）
移除了错误的 `@key headful`。追加深度（8→41 断言）：单方法精确清单+签名、直接父接口恰 EventListener；真实流——请求本身不 fire、revoke(false) 恰 1 次 + serviceClass/invalidate/source 精确、revoke(true) invalidate=true、provider 全程零回调、**revoke 后 getService → null（本 API 无 ServiceUnavailableException 路径）**、两个在册 requestor 各收一个事件、**不为无关服务收事件**；监听器实例独立计数 + 同一事件实例跨监听器（==）；监听器抛 IllegalStateException → 冒泡出 revokeService 且撤销完成；JDK-27 侧 @Deprecated(forRemoval=true,since=23) 断言。双侧行为实测一致。
