# java.beans.beancontext.BeanContextServiceProvider — 设计案

## 1. API 功能
服务提供者接口。getService/releaseService/getCurrentServiceSelectors。

## 2. Java 用户如何使用
实现该接口向 BeanContextServices 注册服务，供子 Bean 请求。

## 3. 开源代码实际用法调研
JDK 源码 `BeanContextServiceProvider.java`。

## 4. 测试用例设计 + 自动化实现
白盒：接口。黑盒（`testBlackBox`）：匿名实现 getService/releaseService/getCurrentServiceSelectors。

## 5. 深化追加（批次 beansI，2026-11）
移除了错误的 `@key headful`。追加深度（8→54 断言）：3 方法精确清单+签名；**无 getServiceBeanInfo**（元数据走 BeanContextServiceProviderBeanInfo 约定，记录型断言）；经真实 BCSS 生命周期断言计数器——每请求恰一次 getService（requestor/selector/serviceClass/bcs 四参身份透传）、每 release 恰一次 releaseService（服务实例透传）、同 requestor+同 listener 重复请求再计费、TMLE 冲突= get+回滚 release、**revoke 时 provider 零回调**、revoke(true) 后迟到 release 不达、持引用换 provider → UOE + 新 provider get/release 各一次；变体——provider 抛 IllegalStateException 原样传播（无回滚）、null yield 无记账、selectors 活迭代每调新建、同一 provider 多服务类；JDK-27 侧 @Deprecated(forRemoval=true,since=23) 断言。双侧行为实测一致。
