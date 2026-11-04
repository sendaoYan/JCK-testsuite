# java.beans.beancontext.BeanContextServicesSupport.BCSSProxyServiceProvider — 设计案

## 1. API 功能
BCSSProxyServiceProvider 嵌套类（implements BeanContextServiceProvider, BeanContextServiceRevokedListener），把服务请求代理到嵌套的父 BeanContextServices（包私有构造器）。

## 2. Java 用户如何使用
不直接使用；BeanContextServicesSupport 嵌套时内部创建。

## 3. 开源代码实际用法调研
JDK 源码 `BeanContextServicesSupport.java` 内部类。

## 4. 测试用例设计 + 自动化实现
白盒：public + 两个接口 + 声明服务方法。

## 5. 深化补充（beansJ 批次，57 断言）
原有 testWhiteBox/testLegacyBlackBox 断言全部保留，追加：完整结构断言（protected 非 static、superclass=Object、非 Serializable、接口恰 2、方法恰 4 个 public 且逐个签名断言、字段恰 nestingCtxt+this$0、构造器唯一包私有 2 参）；生命周期（ProbingBCSS 读 protected proxy 字段）：嵌套创建（类名精确）、有成员 unnest 清空、无成员 unnest 保留 stale proxy（源码 children.isEmpty() 早退 quirk，实测记录）、普通 BeanContextSupport 父不创建、setBeanContext 直达路径创建；委托行为：get/release 转发到父 provider（计数）、父 revoke 后 proxy.serviceRevoked 在子侧重发事件（字段保真、source=子上下文）、已 release 的引用不参与撤销分发（实测记录）。@test 头原有 `@key headful` 行已删除。
