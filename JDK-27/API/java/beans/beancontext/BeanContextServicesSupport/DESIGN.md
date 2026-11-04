# java.beans.beancontext.BeanContextServicesSupport — 设计案

## 1. API 功能
服务化 BeanContext 实现（extends BeanContextSupport, implements BeanContextServices）。构造器 `(peer, locale, designTime, okToUseGui)` 系列；addService/revokeService/hasService/getService/releaseService/getCurrentServiceClasses/getCurrentServiceSelectors/add/removeBeanContextServicesListener、getBeanContextServicesPeer、serviceAvailable/serviceRevoked。

## 2. Java 用户如何使用
作为带服务注册/查找的 Bean 容器；子 Bean 通过 getService 获取服务。

## 3. 开源代码实际用法调研
JDK 源码 `BeanContextServicesSupport.java`。

## 4. 测试用例设计 + 自动化实现
白盒：implements BeanContextServices + extends BeanContextSupport。黑盒（`testBlackBox`）：服务注册/查找/释放/撤销、服务类枚举、嵌套构造器。

## 5. 深化补充（beansJ 批次，195 断言）
原有 testWhiteBox/testBlackBox 断言全部保留（黑盒原 fixture 更名 LegacyProvider，check 调用不变），追加 9 个深化维度（与 JDK-8 侧同源）：
1. 结构：public 非 final、双监听器接口、Serializable、UID=-8494482757288719206、23 个方法声明逐一、getService 5 参签名 + TooManyListenersException。
2. 构造器矩阵：5 个重载、peer 语义、locale/designTime 传递。
3. 注册矩阵：重复注册（同/异 provider）false；服务类集合恰等；selectors：未注册 null、provider 返 null → 包装迭代器 hasNext() 抛 NPE（实测记录）、返空迭代器 → 空遍历。
4. getService 生命周期：无 per-requestor 缓存（provider 每次被调，实测记录）；逐参透传；同 requestor 换 revoked listener → TooManyListenersException 且 provider 回退（get+1/rel+1）；release 按 ref 计数转发、超量静默；null → NPE、非成员 → IAE、provider 返 null → null。
5. revoke 矩阵：isCurrentServiceInvalidNow == invalidate 参数（实测）；事件字段精确；双路分发（requestor 监听器 + bcsListeners 各一次）；撤销移除注册、后续 getService null；不匹配 provider IAE；未注册类静默。
6. 监听器注册表：NPE、重复折叠、事件对象同一性透传、本上下文已提供该类时回调抑制、remove 后失联。
7. 父委托：嵌套后 hasService 委托；子 getCurrentServiceClasses 只列自有；委托 get/release 落到父 provider（requestor 不匹配静默）；父延迟注册传播 serviceAvailable（source=父）；父 revoke 经 proxy 重发（source=子）；已 release 的引用不参与撤销分发（实测记录）。
8. 继承集合契约：membership 事件、contains/toArray/iterator、add(null)/remove(null) IAE、clear/addAll/removeAll/retainAll UOE。
9. 序列化 round-trip：member 拷贝存活；Serializable provider 注册存活、非 Serializable 丢失（实测）；deser 后服务可用。
另：本文件 @test 头原有的 `@key headful` 行已删除（纯 headless 可跑，保留会令 headless CI 漏跑）；java.beans.beancontext 在 JDK-23+ 整包 deprecated-for-removal 但功能完整，测试无需特殊处理。
