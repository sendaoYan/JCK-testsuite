# java.beans.beancontext.BeanContextServiceProvider — 设计案（JDK-8 侧）

## 一、API 功能
服务提供者接口。恰 3 个方法：getService(BeanContextServices,Object,Class,Object)→Object、releaseService(BeanContextServices,Object,Object)→void、getCurrentServiceSelectors(BeanContextServices,Class)→Iterator。无父接口、非 Serializable。**没有** getServiceBeanInfo/getServicesBeanInfo——provider 的元数据走并行的 BeanContextServiceProviderBeanInfo 约定，接口层面无关联。

## 二、使用方式
实现本接口并经 BeanContextServices.addService(Class, provider) 注册；上下文在每次 getService 请求时调用 provider.getService（参数原样透传），每次 releaseService 时调用 provider.releaseService；revoke 时上下文不调用 provider 任何方法（撤销是上下文侧的簿记+事件）。

## 三、参考来源
- Oracle javadoc，Java SE 8, java.beans.beancontext.BeanContextServiceProvider
- JavaBeans 规范 1.01-A（服务提供者章节）
- OpenJDK 8u 源码 `BeanContextServicesSupport.java`（getService 的调用/回滚路径：TMLE 与 UOE 都先 get 再 release 回滚；provider 异常直接冒泡）；`~/git/jdk` JDK-27 主线行为一致
- 双侧（jdk8u372-b07 / 27.0.1）探针实测一致

## 四、测试用例设计 + 易错规范细节
测试方法与断言要点（目标 ≥35，实际 51）：
1. testWhiteBox（原 5 断言保留 + 扩展）：接口/无超类/3 个 hasMethod（原样保留）；方法名集合恰 3 个；**无 getServiceBeanInfo**（记录型断言）；非 BeanInfo 子类型；无父接口；全部 public abstract 且无 checked 异常；3 个签名精确（参数/返回型）。
2. testBlackBox（原 3 断言保留 + 扩展）：匿名实现直接调用 getService/releaseService/getCurrentServiceSelectors。
3. testLifecycleCounters（经真实 BCSS 生命周期断言计数器）：注册时 0 回调；每次请求恰一次 getService；requestor/selector/serviceClass/bcs 四参**按身份透传**（bcs 即请求它的上下文本身）；同 requestor+同 listener 重复请求再次计费（引用计数）；每次 release 恰一次 releaseService 且服务实例按身份透传；TMLE 冲突路径 = 一次 get + 一次回滚 release；revoke 时 provider **零回调**；revoke(true) 后迟到 release 不达 provider；持引用换 provider → UOE 且新 provider get+release 各一次（回滚）。
4. testProviderBehaviorVariants：provider.getService 抛 IllegalStateException → 原样传播到 getService 调用方（消息身份断言）且无回滚 release；provider 返回 null → 上下文返回 null 且无记账/无 release；选择器活迭代：每次 getCurrentServiceSelectors 都新建 provider 迭代器（顺序 alpha/beta）、未注册类 null；同一 provider 可注册多个服务类（计数器跨类累计、lastClass 覆盖断言）。
易错规范细节：① revoke 不给 provider 任何通知——服务撤销语义完全由上下文与监听器承担；② TMLE/UOE 两条失败路径都会**回滚**（get 后紧跟 release），但 provider 自身抛异常时不回滚（还没记账）；③ provider 返回 null 是合法的"无服务"信号；④ 3 方法之外无 BeanInfo 表面——与 BeanContextServiceProviderBeanInfo 只是命名约定关联。
