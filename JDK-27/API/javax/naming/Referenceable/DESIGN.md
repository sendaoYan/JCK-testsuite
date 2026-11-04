# javax.naming.Referenceable — 测试设计

## 1. API 功能
单方法接口：`Reference getReference() throws NamingException`；bind 时由 provider 调用得到 Reference 存储。

## 2. Java 用户如何使用
`class MyDs implements Referenceable { public Reference getReference() { return new Reference(getClass().getName(), new StringRefAddr("url", url)); } }`，随后 `ctx.bind("jdbc/ds", ds)` 存引用。

## 3. 开源代码实际用法调研
- JDK `javax.naming.Referenceable` javadoc：Reference 与 Referenceable 协作。
- com.sun.jndi NamingManager.getStateToBind/getObjectInstance 的两侧对接。

## 4. 测试用例设计（ReferenceableTest.java）
- 白盒：接口、方法签名与异常声明。
- 黑盒：实现类多次调用：每次返回非 null、className 指向实现类、addr 内容正确、连续调用结果相等且各次独立（计数验证）。
