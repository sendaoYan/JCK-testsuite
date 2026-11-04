# java.beans.beancontext.BeanContextProxy — 设计案（JDK-8 侧）

## 一、API 功能
代理标记接口。仅声明 getBeanContextProxy()→BeanContextChild（**方法名是 getBeanContextProxy，不是 getBeanContext**——后者属 BeanContextChild）。无父接口、非 Serializable、非 EventListener；与 BeanContextChild 互不继承。

## 二、使用方式
自身不是 BeanContextChild 的对象实现本接口暴露其 BeanContextChild 代理；BeanContextSupport.add() 识别该接口：getBeanContextProxy() 结果（peer）与代理对象**双双**登记为成员，并对 peer 调用 setBeanContext；移除任一即移除两者。

## 三、参考来源
- Oracle javadoc，Java SE 8, java.beans.beancontext.BeanContextProxy
- JavaBeans 规范 1.01-A（代理与 containment 协议）
- OpenJDK 8u 源码 `BeanContextSupport.java` 的 add()/getChildBeanContextChild()（null peer → NPE("BeanContextPeer.getBeanContextProxy()")；双角色 → IAE；成员事件 children 含 proxy+peer 两者）；`~/git/jdk` JDK-27 主线一致
- 双侧（jdk8u372-b07 / 27.0.1）探针实测一致

## 四、测试用例设计 + 易错规范细节
测试方法与断言要点（目标 ≥30，实际 43）：
1. testWhiteBox（原 3 断言保留 + 扩展）：接口/无超类/hasMethod getBeanContextProxy（原样保留）；方法名集合恰 1 且**不含 getBeanContext**；无父接口；public abstract、返回型恰 BeanContextChild、无参无 checked 异常；非 EventListener/非 Serializable；与 BeanContextChild 双向不 assignable；**BeanContextSupport/BeanContextServicesSupport 自身并不实现本接口**（探针记录）。
2. testBlackBox（原断言保留 + 扩展）：匿名实现返回 peer 身份；重复调用稳定同实例；NullProxyBean 直接调用返回 null 合法（校验发生在 add 时）。
3. testCompositeMembership（真实语义，经 BeanContextSupport）：add(proxyBean) → contains 代理与 peer 两者、size==2；成员事件 children 恰含两者；peer.setBeanContext 被路由（peer.getBeanContext()==上下文）；remove(代理) → 两者齐删 + 移除事件含两者；重加后 remove(peer) 同样齐删。
4. testNegativeMatrix：add(NullProxyBean) → NPE（"BeanContextPeer.getBeanContextProxy()"）；add(DualRoleBean extends BeanContextChildSupport + implements BeanContextProxy) → IAE（"child cannot implement both..."）；普通 BeanContextChildSupport 不受代理规则影响（单成员）；peer 可脱离代理独立作为 child 加入另一上下文。
易错规范细节：① 双登记是核心语义——成员计数、contains、成员事件都同时包含代理对象与 peer；② null peer 的 NPE 在 add 时（不在接口调用时）；③ BeanContextChild+BeanContextProxy 双实现被明确禁止（IAE）；④ 内建上下文类都不是本接口的实现者。
