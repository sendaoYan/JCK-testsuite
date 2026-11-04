# java.beans.beancontext.BeanContextServicesSupport.BCSSProxyServiceProvider — 设计案

## 一、API 功能
BCSS 的 protected 内嵌（非 static）类，`implements BeanContextServiceProvider,
BeanContextServiceRevokedListener`，direct subclass of Object，非 Serializable。
字段：private `nestingCtxt`（BeanContextServices，委托目标）+ this$0。方法恰 4 个
（全部 public）：`getService`（转发 nestingCtxt.getService，吞 TooManyListenersException
返回 null）、`releaseService`（转发）、`getCurrentServiceSelectors`（转发）、
`serviceRevoked`（对本 BCSS 的各 BCSSChild 以 delegated=true 重发撤销）。构造器包私有
`(BeanContextServicesSupport, BeanContextServices)`。

## 二、使用方式
不可直接使用（protected + 包私有构造器）；BCSS 被 setBeanContext 嵌入 BeanContextServices
父上下文时由 initializeBeanContextResources 创建，父级服务经其委托。

## 三、参考来源
- Oracle Java Platform, Standard Edition 8 API Specification（BeanContextServicesSupport.BCSSProxyServiceProvider 嵌套类页）。
- JavaBeans Specification 1.01-A（第 11.3 节：嵌套上下文服务委托）。
- OpenJDK 8 `BeanContextServicesSupport.java` 中 BCSSProxyServiceProvider 与 initializeBeanContextResources/releaseBeanContextResources 源码（proxy 生命周期与 children.isEmpty() 早退细节）。

## 四、测试用例设计 + 易错规范细节（BCSSProxyServiceProviderTest，59 断言）
结构（反射）：保留原有 7 条断言 + 扩展：protected、非 static、成员类、enclosing 类、
superclass=Object、非 Serializable、接口恰 2 个且为上述两个；方法恰 4 个（public、
非 static、具体），逐个签名断言（getService 4 参首参 BeanContextServices、
getCurrentServiceSelectors 2 参、serviceRevoked 1 参返回 void 等）；字段恰 2 个
（nestingCtxt + 外部引用），nestingCtxt private 且类型 BeanContextServices；构造器
唯一、包私有、2 参（外部引用 + nesting context）。
生命周期（ProbingBCSS 子类读 protected proxy 字段，无需反射 setAccessible）：
初始无 proxy；嵌入 BeanContextServices 父 → 创建且类名恰为 BCSSProxyServiceProvider；
有成员时 unnest → 清空；实测记录源码 quirk：无成员时 releaseBeanContextResources 因
`children.isEmpty()` 提前 return，proxy 不清空（stale）；嵌入普通 BeanContextSupport
（非服务上下文）父 → 不创建；直接 setBeanContext(parent) 亦创建。
委托行为：子的成员 getService 得到父 provider 单例（父计数 +1）；releaseService
转发（父 release 计数 +1）；父 revokeService(invalidate=true) → proxy.serviceRevoked
在子侧重发事件（serviceClass 保真、isCurrentServiceInvalidNow=true、source=子上下文）、
子 hasService 随之失明。实测记录：委托引用已 release 后再 revoke 不会产生事件
（账目已清）。
易错点：观察 proxy 字段必须经子类 `this.proxy`（同包不可达）；先 release 后 revoke
的时序会清掉撤销分发所需 bookkeeping。
