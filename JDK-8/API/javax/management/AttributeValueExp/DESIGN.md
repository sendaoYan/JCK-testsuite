# javax.management.AttributeValueExp 契约测试设计

## 1. API 功能
- 属性值表达式：构造 (attr)/(无参 deprecated)、getAttributeName、apply（经 QueryEval.getMBeanServer() 取值并包装为 Number/String/Boolean 的 ValueExp）、toString（裸属性名）。

## 2. Java 用户典型用法
```java
new QueryEval() {}.setMBeanServer(server);
ValueExp v = new AttributeValueExp("Count").apply(name);  // "5"
```

## 3. 参考来源
- Oracle javadoc：javax.management.AttributeValueExp（setMBeanServer 已废弃无效果）。

## 4. 测试用例设计
- 既有：getAttributeName、null 构造。

## 深化（2026-11，批次 e）
新增维度：toString/命名 getter、**apply 值类型映射闭环**、失败模式矩阵、序列化。
- toString 返回裸属性名；无参构造 getAttributeName()=null；Query.attr(String) 与 Query.attr(class,name) 的 getAttributeName。
- **setMBeanServer 是 deprecated no-op**（实测：调用后 apply 仍 BadAttributeValueExpException——server 上下文只能经 QueryEval 注入）。
- apply 闭环（QueryEval 设 ThreadLocal + 真注册 MBean）：数值属性 → NumericValueExp（toString "5"）；boolean isIs 属性 → BooleanValueExp（toString "true"，包私有类型经 toString 锁定）；String 属性 → StringValueExp。
- 失败模式（两侧一致，全部 **BadAttributeValueExpException**，getCause/消息为 null——getAttribute 把一切异常吞成 null 再抛）：非 Number/String/Boolean 属性值（Object）、未知属性、未注册 ObjectName、上下文清理后。
- 序列化 round-trip 保属性名；serialVersionUID 锁定 **-7768025046539163385**（两侧一致）。
### 版本差异
- 无。
