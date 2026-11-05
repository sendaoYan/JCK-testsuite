# javax.management.ValueExp 契约测试设计

## 1. API 功能
- 查询值表达式接口：apply(ObjectName)→ValueExp、setMBeanServer(MBeanServer)；extends Serializable。

## 2. Java 用户典型用法
```java
ValueExp v = Query.plus(Query.attr("Count"), Query.value(1));
```

## 3. 参考来源
- Oracle javadoc：javax.management.ValueExp。

## 4. 测试用例设计
- 既有：接口结构反射。

## 深化（2026-11，批次 e）
新增维度：接口契约、**自定义 ValueExp 组合进标准算子**、序列化 round-trip。
- 接口：apply(ObjectName)→ValueExp、setMBeanServer(MBeanServer)→void、extends Serializable。
- ConstExp（apply 返回常量 Query.value(3)）组合：eq/gt/lt 真求值；plus(自定义,2).apply(null) toString "5"；apply 计数证明每次求值调用一次；setMBeanServer 回调被观察到。
- 序列化：Query.value(5)/value("q")/plus(2,3) round-trip 后值保持、apply 可再执行。
### 版本差异
- 无。
