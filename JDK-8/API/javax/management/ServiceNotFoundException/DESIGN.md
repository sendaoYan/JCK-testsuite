# javax.management.ServiceNotFoundException 契约测试设计（新建 + 深化 2026-11）

## 1. API 功能
- 受检异常：`() ` / `(String)` 两个构造器；直接父类 `OperationsException`（→ JMException → Exception）。
- 无特有 getter（消息即全部状态）。

## 2. Java 用户如何使用
```java
try { mbs.getAttribute(name, attr); }
catch (AttributeNotFoundException e) { ... }
```

## 3. 参考来源
- Oracle javadoc javax.management.ServiceNotFoundException；javap 双侧对照（JDK-8/JDK-27 公开方法集一致）。
- 实现权威：`src/java.management/share/classes/javax/management/ServiceNotFoundException.java`（serialVersionUID 声明值与 ObjectStreamClass 实测一致）。

## 4. 测试用例设计 + 易错规范细节
- 构造链：`() ` message/cause null、`(String)` 精确保留、`(String)null` → null；toString 含类名。
- 层级：OperationsException/JMException/Exception/Serializable，受检非运行时。
- serialVersionUID = `-3990675661956646827L`（两侧同值）；序列化 round-trip（消息保真、cause null）。
- 真实触发（仅 JDK-8）：注册 MLet 后 getMBeansFromURL(缺失文件) → 本异常；JDK-27 已移除 MLet，触发点不可构造（版本差异）。
