# java.beans.beancontext.BeanContextServices — 设计案

## 1. API 功能
服务化 BeanContext 接口（extends BeanContext, BeanContextServicesListener）。addService/revokeService/hasService/getService/releaseService/getCurrentServiceClasses/getCurrentServiceSelectors/add/removeBeanContextServicesListener。

## 2. Java 用户如何使用
BeanContextServicesSupport 实现之；子 Bean 通过 getService 请求服务。

## 3. 开源代码实际用法调研
JDK 源码 `BeanContextServices.java`。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + extends BeanContext。黑盒（`testBlackBox`）：addService/hasService/getCurrentServiceClasses/revokeService。

## 5. 深化追加（批次 beansI，2026-11）
移除了错误的 `@key headful`（本测试纯 headless）。追加深度（8→131 断言）：接口闭包与 9 方法精确签名（含 getService 的 TooManyListenersException 子句）；BCSS 生命周期全矩阵——重复 addService false 且不替换不 fire、BCSIterator.remove() 静默 no-op、selectors 活委托、未注册类 null；getService/releaseService 四参身份透传 + 计数、TMLE 冲突回滚（get+release 各 1）、非 child IAE、四 null NPE、never-seen release no-op、null yield 无记账；revoke(false) 保留引用（显式 release 仍达 provider）/revoke(true) 清引用（迟到 release 不达）+ provider 全程零回调 + 持引用换 provider UOE（新 provider get+release 回滚）；监听器矩阵（同一事件实例、null NPE、no-op 移除、重复 addService 无事件）+ 嵌套委托（子 BCSS 收父 serviceAvailable、inner.hasService/getService/releaseService 穿透父 provider）；JDK-27 侧加 @Deprecated(forRemoval=true,since=23) 结构断言。双侧（jdk8u372-b07/27.0.1）行为实测一致。
