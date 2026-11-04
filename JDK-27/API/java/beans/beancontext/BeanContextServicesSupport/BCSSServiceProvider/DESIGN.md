# java.beans.beancontext.BeanContextServicesSupport.BCSSServiceProvider — 设计案

## 1. API 功能
BCSSServiceProvider 嵌套类（implements Serializable），包装 BeanContextServiceProvider（包私有构造器，protected getServiceProvider）。

## 2. Java 用户如何使用
不直接使用；BeanContextServicesSupport.addService 时内部创建。

## 3. 开源代码实际用法调研
JDK 源码 `BeanContextServicesSupport.java` 内部类。

## 4. 测试用例设计 + 自动化实现
白盒：public + Serializable。黑盒（`testBlackBox`）：子类暴露 createBCSSServiceProvider 间接取得实例。

## 5. 深化补充（beansJ 批次，43 断言）
原有 testWhiteBox/testLegacyBlackBox 断言全部保留，追加：完整结构断言（protected static、UID=861278251667444782、字段恰 2 个且 serviceProvider 为 protected 非 transient、方法恰 1 个 getServiceProvider() protected 0 参、构造器唯一包私有 2 参、接口恰 Serializable）；实例行为（CapturingBCSS 捕获）：每注册一个包装器、类精确、互异、重复注册拒收不新增；包装内容：JDK-27 反射 setAccessible 被 java.desktop 模块拒绝（InaccessibleObjectException，实测记录，跳过身份断言），行为等价证据走 revokeService 匹配（原 provider 接受、异 provider IAE）；序列化：Serializable provider 的包装器 round-trip 类保真，非 Serializable provider 抛 NotSerializableException（实测）。@test 头原有 `@key headful` 行已删除。
