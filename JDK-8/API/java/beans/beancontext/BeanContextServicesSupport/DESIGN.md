# java.beans.beancontext.BeanContextServicesSupport — 设计案

## 一、API 功能
服务化 BeanContext 实现（`extends BeanContextSupport implements BeanContextServices`，
同时是 `BeanContextServicesListener`/`BeanContextServiceRevokedListener`）。5 个构造器
`(peer, locale, designTime, okToUseGui)` 系列；服务注册/查找/释放/撤销：
`addService/revokeService/hasService/getService/releaseService/getCurrentServiceClasses/
getCurrentServiceSelectors`；监听器 `add/removeBeanContextServicesListener`；
`getBeanContextServicesPeer`、`serviceAvailable/serviceRevoked` 回调、`initialize`、
protected 钩子（`createBCSChild/createBCSSServiceProvider/childJustRemovedHook/
initialize·releaseBeanContextResources/fireServiceAdded·Revoked/bcsPre·DeserializationHook`）。
serialVersionUID = -8494482757288719206。

## 二、使用方式
作为带服务注册/查找的 Bean 容器：`addService(Svc.class, provider)` 注册，子 Bean
（需先 add 为成员）用 `getService(child, requestor, serviceClass, selector, revokedListener)`
取服务、`releaseService` 归还；BCSS 自身可嵌套进父 BCSS，形成服务委托链。

## 三、参考来源
- Oracle Java Platform, Standard Edition 8 API Specification（java.beans.beancontext 包，BeanContextServicesSupport 页）。
- JavaBeans Specification 1.01-A（第 5/8/11 章：BeanContext 服务模型）。
- OpenJDK 8 `java/beans/beancontext/BeanContextServicesSupport.java` 源码语义（addService 重复拒绝、getService/BCSSChild.usingService 引用计数、revokeService 双路径分发、bcsPreSerializationHook 仅序列化 Serializable provider）。

## 四、测试用例设计 + 易错规范细节（BeanContextServicesSupportTest，190 断言）
结构：原有 7 条反射断言保留（非接口/具体类/父类/接口/方法声明）+ 扩展（public 非 final、
监听器接口、Serializable、UID、23 个方法声明逐一、getService 5 参签名 + TooManyListenersException）。
构造器矩阵：5 个重载、peer 语义（无参=self、peer 构造器返回 peer、null peer=self）、
locale/designTime 传递。
服务注册矩阵：addService true；重复注册（同/不同 provider 均 false）；hasService 前后；
getCurrentServiceClasses 恰为注册集合（Set 比较不依赖顺序）；getCurrentServiceSelectors
未注册类 → null；provider 返回 null → 包装迭代器 hasNext() 抛 NPE（实测记录：BCSIterator
直接委托内层迭代器，无 null 防护）；provider 返回空迭代器 → 空遍历。
getService 生命周期（计数器 fixture）：返回 provider 单例；provider 每次请求都被调用
（无 per-requestor 缓存，实测记录）；bcs/requestor/selector/serviceClass 逐参透传；
同 requestor 换 revoked listener → TooManyListenersException 且 provider 先 getService
后被 releaseService 回退（计数 +1/+1）；同 listener 复用/换 requestor 均合法；
releaseService 按引用计数转发（3 次 getService 对应 3 次转发，第 4 次静默）；
负例矩阵：各参数 null → NPE、非成员 child → IAE（getService/releaseService）、
provider 返回 null → 得到 null 非异常。
revoke 矩阵：invalidate=false/true 与 isCurrentServiceInvalidNow 严格对应（实测记录）；
事件字段（serviceClass/isServiceClass/source=peer）精确；requestor 监听器与
bcsListeners 各收一次；撤销即移除注册（hasService false、getService 返回 null）；
撤销后可重新注册；provider 不匹配 → IAE；未注册类 → 静默；null → NPE。
监听器注册表：add/remove(null) → NPE；重复 add 折叠为一份（事件只收一次）；
serviceAvailable/serviceRevoked 透传同一事件对象；本上下文已提供该类时回调被抑制
（先注册后回调不触发）；remove 后不再通知。
父上下文委托：嵌套前后 hasService 变化（false→true）；子 getCurrentServiceClasses
只列自有服务（不含父服务）、父服务的 selectors → null；委托 getService 落到父
provider（计数）；委托 releaseService 转发（requestor 不匹配则静默）；父延迟注册 →
子的 services 监听器收 serviceAvailable（source=父）；父 revoke(invalidate=true) →
经 proxy 在子侧重发事件（serviceClass/isCurrent 保真、source=子上下文）；实测记录：
已 release 的引用不参与撤销分发（bookkeeping 已清，需重新 getService 才能观察）。
继承的 BeanContextSupport 集合契约：membership 监听器 childrenAdded/Removed（次数、
children、getBeanContext）；size/isEmpty/contains/containsAll/toArray/iterator；
remove 非成员 false 且无事件；add(null)/remove(null) → IAE、contains(null) false；
clear/addAll/removeAll/retainAll → UnsupportedOperationException。
序列化 round-trip：member 以拷贝存活（contains(orig)=false、类型保真）；Serializable
provider 的注册存活、非 Serializable provider 的注册丢失（实测记录：bcsPreSerializationHook
只写 Serializable provider）；deser 后 hasService/classes/selectors/getService 均可用
（经 provider 拷贝）；peer=self 保真。
易错点：所有行为断言基于双侧实测探针（JDK-8 与 JDK-27 结果逐条一致）；
getCurrentServiceClasses 迭代顺序不保证（用 Set 断言）。
