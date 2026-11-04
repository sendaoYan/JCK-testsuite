# java.beans.beancontext.BeanContextServiceRevokedListener — 设计案（JDK-8 侧）

## 一、API 功能
服务撤销监听接口。仅声明 serviceRevoked(BeanContextServiceRevokedEvent)；直接父接口恰为 java.util.EventListener；非 Serializable。是 BeanContextServicesListener 的父接口（后者加 serviceAvailable）。

## 二、使用方式
作为 getService 的第 5 参（per-requestor 监听器）绑定到具体请求：revokeService 时上下文对**每个在册 requestor 条目**调用其监听器一次；也可经 BeanContextServicesListener 一并接收。

## 三、参考来源
- Oracle javadoc，Java SE 8, java.beans.beancontext.BeanContextServiceRevokedListener
- JavaBeans 规范 1.01-A（撤销通知）
- OpenJDK 8u 源码 `BeanContextServicesSupport.java`（BCSSChild.revokeService 的 cloneOfEntries 迭代：每条目一次、异常直接冒泡）；`~/git/jdk` JDK-27 主线一致
- 双侧（jdk8u372-b07 / 27.0.1）探针实测一致

## 四、测试用例设计 + 易错规范细节
测试方法与断言要点（目标 ≥30，实际 37）：
1. testWhiteBox（原 4 断言保留 + 扩展）：接口/无超类/extends EventListener/hasMethod serviceRevoked（原样保留）；方法名集合恰 1；public abstract、void、单参 BeanContextServiceRevokedEvent、无 checked 异常；直接父接口恰 EventListener；非 Serializable；被 BeanContextServicesListener 包含；BCSS instanceof 本接口。
2. testBlackBox（原 dispatch 断言保留 + 扩展）：匿名实现重复分发；两个实例独立计数；同一事件实例可交给多个监听器（== 断言）。
3. testRealFlow（真实流）：请求本身不 fire；revoke(false) → 恰 1 次、serviceClass 精确、invalidate=false、source==上下文、isServiceClass 命中；revoke(true) 后第二轮 invalidate=true；provider 全程 0 次 releaseService（撤销不给 provider 回调）；**revoke(now=true) 后 getService → null：本 API 没有 ServiceUnavailableException 路径**（探针记录）；两个在册 requestor（各自请求）→ revoke 一次各收一个事件（共 2）；**不为无关服务的 revoke 收事件**（只收自己持有的服务类）。
4. testExceptionPropagation：监听器抛 IllegalStateException → 传播出 revokeService 且撤销仍完成。
易错规范细节：① 通知粒度是 requestor 条目而非监听器实例——同一监听器若绑两个请求会收两个事件；② revoke 后再次 getService 返回 null（服务已从注册表移除），不抛任何服务不可用异常；③ 监听器异常冒泡到 revokeService 调用方，但状态变更（注销）已完成。
