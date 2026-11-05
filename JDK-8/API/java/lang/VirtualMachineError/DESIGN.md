# VirtualMachineError — 设计方案

## 1. API 功能
`java.lang.VirtualMachineError` 继承自 `Error`，表示 JVM 自身损坏或资源耗尽的抽象基类（"Thrown to indicate that the JVM is broken or has run out of resources"）。
它是 abstract 类：不能直接实例化（编译期禁止 `new`，反射抛 `InstantiationException`），但向子类暴露 4 个 public 构造器：`()`、`(String)`、`(String, Throwable)`、`(Throwable)`（JDK-8 javap 实测面）。
具体子类：`InternalError`、`OutOfMemoryError`、`StackOverflowError`、`UnknownError`。
Serializable（经 Error），`serialVersionUID = 4161983926571568670L`（ObjectStreamClass.lookup 实测锁定）。
继承 Throwable 通用契约：`getMessage/getLocalizedMessage/getCause/initCause/addSuppressed/getSuppressed/printStackTrace/getStackTrace/fillInStackTrace/toString`。

## 2. Java 用户如何使用
以家族句柄统一兜底四种 JVM 级故障，再按具体子类分流：
```java
try {
    ...
} catch (VirtualMachineError v) {
    log(v.getClass().getSimpleName(), v.getMessage(), v.getCause());
}
```
子类化（自造诊断错误类型）时经 4 个构造器委托：`class MyVme extends VirtualMachineError { MyVme(Throwable c) { super(c); } }`。

## 3. 开源代码实际用法调研
用户代码极少主动 new；主要由 JVM 抛出、被框架顶层 handler 捕获（如 Netty 的 OOM 分流、测试框架对 SOE 的重试）。行为集中在构造/消息/原因/抑制异常/序列化，无额外业务方法（javap：无自有成员）。

## 4. 测试用例设计 + 易错规范细节
- 白盒：`Modifier.isAbstract` 为真；4 个构造器存在且全 public（abstract 不妨碍构造器可枚举）；`getSuperclass()==Error`；Serializable + suid 锁定。
- 反射负例：4 个构造器逐一 `newInstance`（null 实参匹配）全部 `InstantiationException`；编译期证据 = 本测试文件不含无体 `new VirtualMachineError(...)` 且可编译。
- 子类通道：匿名子类与具名静态嵌套 `SubVme` 走全部 4 构造器；`(Throwable)` 的 message 默认 `cause.toString()`；`(Throwable)null` 双 null。
- 家族归属：4 个具体子类 instanceof + 直系父断言 + 逐个按 VirtualMachineError 句柄抛/捕（同一对象）+ 家族数组多态 getMessage。
- Throwable 契约：initCause 一次写/二次 ISE/自反精确 IAE `"Self-causation not permitted"`（JDK-8/27 逐字一致）；suppressed 默认空；printStackTrace 首行带具体子类名；SubVme 经抽象父序列化往返保持 message/cause/suppressed。
- 易错：abstract 类 getDeclaredConstructors 照常返回 4 个——不能由"可枚举"推"可实例化"；序列化往返的 cause 是图拷贝。
