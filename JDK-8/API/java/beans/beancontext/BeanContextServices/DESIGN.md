# java.beans.beancontext.BeanContextServices — 设计案（JDK-8 侧）

## 一、API 功能
服务化 BeanContext 接口。extends BeanContext（→ BeanContextChild/Collection/DesignMode/Visibility）+ BeanContextServicesListener（→ BeanContextServiceRevokedListener → EventListener）。恰声明 9 个方法：addService(Class,BeanContextServiceProvider)→boolean、revokeService(Class,BeanContextServiceProvider,boolean)、hasService(Class)→boolean、getService(BeanContextChild,Object,Class,Object,BeanContextServiceRevokedListener)→Object（唯一 checked 异常 TooManyListenersException）、releaseService(BeanContextChild,Object,Object)、getCurrentServiceClasses()→Iterator、getCurrentServiceSelectors(Class)→Iterator、add/removeBeanContextServicesListener(BeanContextServicesListener)。

## 二、使用方式
通过 BeanContextServicesSupport（本批的语义中枢）实现；子 Bean 先 add 进上下文，再以 (child, requestor, serviceClass, selector, revokedListener) 五元组请求服务；同一 requestor 绑定同一 revokedListener（换监听器 → TooManyListenersException），同一服务类换 provider 且引用未释放 → UnsupportedOperationException。嵌套子上下文经 BCSSProxyServiceProvider 向父级委托 getService/releaseService/hasService。

## 三、参考来源
- Oracle javadoc，Java SE 8, java.beans.beancontext.BeanContextServices
- JavaBeans 规范 1.01-A（BeanContext 服务模型章节）
- OpenJDK 8u 源码 `jdk/src/share/classes/java/beans/beancontext/BeanContextServicesSupport.java`（addService/revokeService/getService/usingService/BCSIterator 逐行比对；`~/git/jdk` JDK-27 主线同名文件行为一致）
- 负例异常类型以 JDK-8（jdk8u372-b07）与 JDK-27（27.0.1）双侧探针实测为准（两侧结果一致）。

## 四、测试用例设计 + 易错规范细节
测试方法与断言要点（目标 ≥30，实际 123）：
1. testWhiteBoxStructure（原 8 断言保留 + 扩展）：接口/无超类/extends 两个父接口（原有 4 条 + hasMethod 3 条原样保留）；直接父接口恰为 {BeanContext, BeanContextServicesListener}；传递闭包（BeanContextChild/BeanContextServiceRevokedListener/EventListener/DesignMode/Visibility/Collection）；恰 9 方法名集合精确比对 + 全部 public abstract；9 个签名逐一（参数/返回型/TooManyListenersException 子句）；非 Serializable；BCSS instanceof 全闭包。
2. testServiceLifecycle：hasService false→true→false；addService 首次 true、重复 false（不换 provider，不 fire 事件）；getCurrentServiceClasses 含全部注册类、空上下文为空迭代器；BCSIterator.remove() 是**静默 no-op**（不抛、不注销）；getCurrentServiceSelectors 活委托 provider（内容逐个断言）、未注册类 → null；addService(null,sp)/(cls,null)/hasService(null) → NPE（"serviceClass"/"bcsp"）；revokeService provider 不匹配 → IAE("service provider mismatch")、null 参数 → NPE、未注册类静默 no-op。
3. testGetServiceRelease：未注册服务 → 返回 null（不抛）；非 child → IAE("not a child of this context")；child/requestor/serviceClass/bcsrl 四个 null → 各自 NPE；成功请求后 provider 计数 getService=1 且 requestor/selector/serviceClass/bcs 四参**按身份透传**；同 requestor+同 listener 二次请求允许（引用计数）；每次 releaseService 恰好转发一次 provider.releaseService；TMLE 冲突路径回滚（getC+1 且 relC+1）；releaseService 非 child → IAE、null → NPE、从未获取的服务对象 → 静默 no-op；provider 返回 null → getService null 且无记账。
4. testRevokeSemantics：revoke(false) → per-requestor listener 收 invalidate=false 事件 + 已注册 services listener 同旗标；provider 在 revoke 时不收任何回调；revoke(false) 后显式 releaseService 仍达 provider（引用保留）；re-add 同 provider 后同 requestor/listener 重新获取 OK；revoke(true) → invalidate=true、事后 releaseService 不再达 provider（引用已清）；持引用换 provider → UOE 且新 provider get+rollback release 各一次。
5. testListenerMatrix：add/removeBeanContextServicesListener(null) → NPE；移除未注册 → no-op；addService → 每个监听器收到**同一事件实例**（身份相等）且 source==上下文、serviceClass 精确；重复 addService 不 fire；移除后停送；嵌套：子 BCSS 收到父注册的 serviceAvailable（source==父）；inner.hasService 委托 true；inner.getService 得到父 provider 实例；inner.releaseService 达父 provider（relC=1）。
易错规范细节：① 重复 addService 返回 false 但**不替换** provider、不 fire；② BCSIterator.remove() 不抛 UOE 而是 no-op；③ revoke 只通知 per-requestor 与 addBeanContextServicesListener 注册的监听器，provider 无回调；④ revoke(false) 保留引用（后续换 provider 会 UOE），revoke(true) 清引用（后续 release 无副作用）；⑤ 嵌套委托的 hasService/getService/releaseService 链路穿透到父 provider。
