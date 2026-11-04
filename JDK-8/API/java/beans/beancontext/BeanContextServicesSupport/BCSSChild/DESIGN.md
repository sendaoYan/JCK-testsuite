# java.beans.beancontext.BeanContextServicesSupport.BCSSChild — 设计案

## 一、API 功能
BCSS 的 protected 内嵌（非 static）类，`extends BeanContextSupport.BCSChild`，
为每个成员 Bean 记录服务使用账目（serviceClass → provider/引用、requestor →
service → 引用计数）。字段：`serialVersionUID`、transient `serviceClasses`/`serviceRequestors`
+ 合成外部引用 this$0。方法（全部包私有）：`usingService/releaseService/revokeService/
cleanupReferences/revokeAllDelegatedServicesNow`。内嵌两个包私有引用持有类
`BCSSCServiceClassRef`/`BCSSCServiceRef`。构造器包私有
`(BeanContextServicesSupport, Object child, Object peer)`。
serialVersionUID = -3263851306889194873。

## 二、使用方式
不可直接使用（protected 内部类 + 包私有构造器）；由 `BeanContextServicesSupport.createBCSChild`
在 add 时创建，子类可覆写 createBCSChild 捕获/扩展。usingService 声明
TooManyListenersException 与 UnsupportedOperationException。

## 三、参考来源
- Oracle Java Platform, Standard Edition 8 API Specification（BeanContextServicesSupport.BCSSChild 嵌套类页）。
- JavaBeans Specification 1.01-A（第 5/11 章：per-child 服务状态）。
- OpenJDK 8 `BeanContextServicesSupport.java` 中 BCSSChild 源码（引用计数 addRef/releaseRef、cleanupReferences 在 childJustRemovedHook 中对每份引用各调一次 provider.releaseService）。

## 四、测试用例设计 + 易错规范细节（BCSSChildTest，56 断言）
结构（反射，不改访问性）：保留原有 3 条断言（非接口/具体类/父类名）+ 扩展：
protected 非 public、非 static（内部类，持有 this$0）、成员类、simpleName、
enclosing/declaring 类、Serializable（含父类）、ObjectStreamClass UID 稳定值；
字段恰为 4 个（serialVersionUID/serviceClasses/serviceRequestors/this$0）且两个
bookkeeping 字段 transient；方法恰为 5 个且全部包私有非 static；usingService 6 参
且声明 TooManyListenersException + UnsupportedOperationException；构造器唯一、
包私有、3 参；getDeclaredClasses 恰为 BCSSCServiceClassRef + BCSSCServiceRef 且包私有。
实例行为（CapturingBCSS 覆写 createBCSChild 捕获）：每次 add 产生一个精确类为
BCSSChild 的实例；两成员实例互异；remove+re-add 产生新实例；捕获实例独立序列化
round-trip 后类保真（外部 BCSS 可序列化，this$0 随之存活——实测通过）。
per-child 账目隔离（计数器 provider）：两个成员各持 1 份引用，remove 单个成员时
provider.releaseService 恰 +1（只释放该成员引用），再 remove 另一个 +1。
易错点：元素静态类型是 protected BCSChild，测试代码须先 widening 到 Object 再调
getClass()（否则编译错「定义于不可访问类」）；直接 getDeclaredMethods 的 modifier
含 synchronized 位，包私有断言须用 isPublic/isProtected/isPrivate 逐个取反。
