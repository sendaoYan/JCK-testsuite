# javax.management.AttributeValueExp 黑盒行为测试设计

## 1. API 功能
- 实现 `ValueExp`。
- 方法：`getAttributeName()`、`apply(ObjectName)`、`setMBeanServer(MBeanServer)`、`toString()`。

## 2. Java 用户如何使用
与 `Query` 组合表达“属性名等于某值”的查询；直接 apply 会在当前 QueryEval 的 MBeanServer 上取该属性值。
```java
ValueExp e = new AttributeValueExp("Count");
ValueExp v = e.apply(objectName);
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/query/QueryFactory.java`：Query.attr 内部使用。
- `test/jdk/javax/management/query/SupportedQueryTypesTest.java`：apply 结果类型。
- 实现权威：`src/java.management/share/classes/javax/management/AttributeValueExp.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：getAttributeName/toString 返回属性名；apply 对 String 属性返回 StringValueExp、对 Number 属性返回 ValueExp；缺失属性因取到 null 抛 BadAttributeValueExpException（源码确认）。
- 白盒：public 类、implements ValueExp、含 apply/setMBeanServer。

## 深化（2026-11，批次 e）
新增维度：toString/命名 getter、**apply 值类型映射闭环**、失败模式矩阵、序列化。
- toString 返回裸属性名；无参构造 getAttributeName()=null；Query.attr(String) 与 Query.attr(class,name) 的 getAttributeName。
- **setMBeanServer 是 deprecated no-op**（实测：调用后 apply 仍 BadAttributeValueExpException——server 上下文只能经 QueryEval 注入）。
- apply 闭环（QueryEval 设 ThreadLocal + 真注册 MBean）：数值属性 → NumericValueExp（toString "5"）；boolean isIs 属性 → BooleanValueExp（toString "true"，包私有类型经 toString 锁定）；String 属性 → StringValueExp。
- 失败模式（两侧一致，全部 **BadAttributeValueExpException**，getCause/消息为 null——getAttribute 把一切异常吞成 null 再抛）：非 Number/String/Boolean 属性值（Object）、未知属性、未注册 ObjectName、上下文清理后。
- 序列化 round-trip 保属性名；serialVersionUID 锁定 **-7768025046539163385**（两侧一致）。
### 版本差异
- 无。
