# java.beans.beancontext.BeanContextServicesSupport.BCSSServiceProvider — 设计案

## 一、API 功能
BCSS 的 protected static 嵌套类，`implements Serializable`，direct subclass of Object。
每注册一个服务类创建一个实例，包装对应 `BeanContextServiceProvider`：protected 字段
`serviceProvider`（非 transient，随包装器序列化）、protected 方法
`getServiceProvider()`（无参返回 provider）、serialVersionUID = 861278251667444782。
构造器包私有 `(Class, BeanContextServiceProvider)`。

## 二、使用方式
不可直接构造（包私有构造器）；`addService` 经 `createBCSSServiceProvider` 创建，
子类可覆写该方法捕获/替换包装器。公开 API 无任何路径返回该类型（事件类也不暴露），
只能经子类覆写或反射观察。

## 三、参考来源
- Oracle Java Platform, Standard Edition 8 API Specification（BeanContextServicesSupport.BCSSServiceProvider 嵌套类页）。
- JavaBeans Specification 1.01-A（第 11.2 节：服务注册项）。
- OpenJDK 8 `BeanContextServicesSupport.java` 中 BCSSServiceProvider/addService/revokeService 源码（revokeService 用 wrapper 内 provider equals 匹配；bcsPreSerializationHook 仅写 Serializable provider 的 wrapper）。

## 四、测试用例设计 + 易错规范细节（BCSSServiceProviderTest，42 断言）
结构（反射）：保留原有 3 条断言 + 扩展：protected static、成员类、enclosing 类、
superclass=Object、ObjectStreamClass UID 稳定值；字段恰 2 个（serialVersionUID +
serviceProvider），serviceProvider 为 protected、非 transient、非 static、类型
BeanContextServiceProvider；方法恰 1 个 getServiceProvider()（protected、非 static、
0 参、返回 BeanContextServiceProvider）；构造器唯一、包私有、2 参
（Class + provider）；接口恰 Serializable 一个。
实例行为（CapturingBCSS 覆写 createBCSSServiceProvider 捕获）：每次注册恰一个包装器、
类精确、Serializable、UID 稳定；不同服务类的包装器互异；重复注册被拒时不产生新包装器。
包装内容观察：JDK-8 反射 setAccessible + invoke 断言 getServiceProvider() 恰为注册的
provider（两个 wrapper 各自对应）；revokeService 的行为等价证据——用原 provider 撤销
成功、换 provider 撤销抛 IllegalArgumentException（wrapper 持有的 provider equals 匹配）。
序列化：包装器（含 Serializable provider）round-trip 类保真；非 Serializable provider
的包装器 writeObject 抛 NotSerializableException（实测记录）。
易错点：无公开路径暴露实例（BeanContextServiceAvailableEvent 不携带 wrapper），
故实例来源只能靠子类覆写；serviceProvider 非 transient 是 wrapper 可序列化的前提。
