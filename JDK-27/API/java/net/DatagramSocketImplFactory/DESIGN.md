# DatagramSocketImplFactory 设计文档

## 一、API 功能

工厂接口，创建 DatagramSocketImpl 实例供 DatagramSocket 使用。

## 二、Java 用户如何使用

用户实现 createDatagramSocketImpl() 并通过 DatagramSocket.setDatagramSocketImplFactory 注册。

## 三、开源代码实际用法调研

自定义 UDP 实现使用。

## 四、测试用例设计

验证为单抽象方法接口；实现并调用其 SAM 方法。

---

# 深化批次 G 追加（2026-11-05，~6 → 30 断言级契约覆盖）

## 覆盖缺口分析

缺：接口形状精确断言（成员数/返回类型/无受检异常/无字段/包名）、异常透明性、
多次调用语义、以及最关键的一次性安装契约（二次 → SocketException "factory already
defined"、null 返回使 new DatagramSocket NPE、安装即被咨询）。

## 新增测试方法

| 方法 | 覆盖 |
|---|---|
| testWhiteBoxDeep | 公开接口、无父接口、恰好 1 个声明方法、返回 DatagramSocketImpl、0 参数、public abstract、无受检异常、无字段、包 java.net、DatagramSocket 侧安装器存在 |
| testImplementationsDeep | JDK-27 形态 StubImpl（13 抽象成员、无 setTTL/getTTL）经 lambda 工厂产出：类型/共享实例/二次调用一致 |
| testMethodContract | 工厂抛 RuntimeException 原样透传（同一对象）；计数工厂每次调用各产出新实例 |
| testFactoryLifecycle（文件尾） | 首次 setDatagramSocketImplFactory 成功；new DatagramSocket(0) 咨询工厂（计数≥1）、null 返回 → NPE（JDK-27 消息点名 factory，JDK-8 为裸 "null"，仅断类型）；二次 set（含 null）→ SocketException "factory already defined"（精确） |

## 版本差异

- null 返回的 NPE 消息：JDK-27 "Implementation returned by installed DatagramSocketImplFactory is null" vs JDK-8 "null" → 只断 NPE 类型。
- 其余（单方法接口形状、"factory already defined" 精确消息、二次拒绝含 null 实参）双侧一致。
