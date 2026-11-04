# java.beans.beancontext.BeanContextServicesListener — 设计案（JDK-8 侧）

## 一、API 功能
服务监听接口。仅声明 serviceAvailable(BeanContextServiceAvailableEvent)；直接父接口恰为 BeanContextServiceRevokedListener（→ java.util.EventListener），因此实现类必须同时提供 serviceRevoked。非 Serializable。

## 二、使用方式
经 BeanContextServicesSupport.addBeanContextServicesListener 注册，接收 addService 的 serviceAvailable 与 revokeService 的 serviceRevoked；子 BeanContextServices 上下文作为 child 时也按此接口接收父级注册事件（revoke 不走此路：父级 revokeService 不向子上下文自身注册的监听器扇出，仅委托请求链上的 per-requestor 监听器收到）。

## 三、参考来源
- Oracle javadoc，Java SE 8, java.beans.beancontext.BeanContextServicesListener
- JavaBeans 规范 1.01-A（服务事件模型）
- OpenJDK 8u 源码 `BeanContextServicesSupport.java` 的 fireServiceAdded/fireServiceRevoked/addService/revokeService（同事件实例逐监听器分发、异常不吞）；`~/git/jdk` JDK-27 主线同名文件行为一致
- 双侧（jdk8u372-b07 / 27.0.1）探针实测，异常与分发行为一致

## 四、测试用例设计 + 易错规范细节
测试方法与断言要点（目标 ≥30，实际 47）：
1. testWhiteBox（原 4 断言保留 + 扩展）：接口/无超类/extends BeanContextServiceRevokedListener/hasMethod serviceAvailable（原样保留）；方法名集合恰 [serviceAvailable]、public abstract、void 返回、单参 BeanContextServiceAvailableEvent、无 checked 异常；直接父接口恰 1 个；传递 EventListener；非 BeanContext、非 Serializable；BCSS instanceof 两个接口；BCSS 可注册为自身监听器（自引用注册/移除不报错）。
2. testBlackBox（原 dispatch 断言保留 + 扩展）：匿名实现双方法分发计数；serviceRevoked 可独立重复分发。
3. testWiring：注册两个监听器 + 重复注册同一监听器（no-op）；addService → 两监听器各 1 次、**同一事件实例**（==）、source==上下文、serviceClass 精确；revoke(false)/revoke(true) 均分发且 invalidate 旗标分别 false/true；移除监听器后停送（另一监听器继续）；重新注册恢复接收。计数矩阵覆盖中途 addService 的累计值。
4. testNestedDelivery：子 BCSS 的监听器收到父级 addService 事件（source==父）；**父级 revokeService 不通知子上下文自身注册的监听器**（revokedCount 保持 0 —— 实测契约）；叶子经子上下文委托请求后，父 revoke(true) → 叶子的 per-requestor 监听器收 invalidate=true；inner.hasService 委托 false 化；委托再请求 → null。
5. testExceptionPropagation：serviceAvailable 抛 RuntimeException → 传播出 addService 且服务仍注册（fire 循环不吞异常）；serviceRevoked 抛 → 传播出 revokeService 且服务仍已注销；per-requestor serviceRevoked 抛 → 传播出 revokeService。
易错规范细节：① fire 用 toArray 快照逐个调用，监听器异常直接冒泡，状态变更已完成；② 事件实例对所有监听器共享（身份相等）；③ 向下传播不对称：addService 主动通知 child BCSS，revokeService 不通知（只有委托请求链上的 per-requestor 监听器）；④ 移除监听器后可重注册恢复。
