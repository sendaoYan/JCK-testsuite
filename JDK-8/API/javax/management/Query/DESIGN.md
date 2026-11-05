# javax.management.Query 黑盒行为测试设计

## 1. API 功能
- 比较常量：GT/LT/GE/LE/EQ；算术常量：PLUS/MINUS/TIMES/DIV。
- 工厂方法：`eq/gt/lt/ge/leq/and/or/not/between/in/match/initialSubString/anySubString/finalSubString/isInstanceOf`，`attr/value/plus/minus/times/div/classattr`。

## 2. Java 用户如何使用
构造 QueryExp 交给 MBeanServer.queryNames/queryMBeans 做条件查询。
```java
QueryExp q = Query.and(Query.gt(Query.attr("Count"), Query.value(0)),
                       Query.attr("Name").equals(Query.value("c*")));
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/query/QueryFactory.java`、`QueryMatchTest.java`、`QuerySubstringTest.java`、`InstanceOfExpTest.java`、`SupportedQueryTypesTest.java`。
- 实现权威：`src/java.management/share/classes/javax/management/Query.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：各工厂返回非空 QueryExp/ValueExp；常量值正确；把组合查询用于已注册 StandardMBean，验证 eq/gt/lt/and/or/not/between/in/match/子串/isInstanceOf 的匹配结果。
- 白盒：public 类、public 常量、含各工厂方法。

## 深化（2026-11，批次 e）
新增维度：**真注册真过滤闭环**（两个 Counter 实例 count=5/6 + 一个 String 属性 MBean，走 server.queryNames 的 server 侧 apply）。
- 比较边界：eq(5)=只命中 count5；eq(6)=只命中 count6；gt(5)/ge(5)/lt(5)/leq(5) 各精确命中集（断言用 Set equals，无序安全）。
- 布尔组合：and(gt4,lt6)、or(eq5,eq6)、not(eq5)（not 只保留 eq 正常求值为 false 的 MBean；属性缺失求值异常的被 server 静默排除）、between(5,6) 双端闭包、in{5,7}。
- 大小写敏感：attr("count") 不命中。
- **attr(className, name) 第一参数是类名不是域**（实测 attr("jck.q","Count") 不命中；attr(QCounter.class.getName(),"Count") 命中；错类名不命中）。
- isInstanceOf(Counter 类名)/classattr eq 精确命中；miss 空集。
- 字符串匹配：initialSubString/anySubString/finalSubString/match("al*h?") 对 String 属性 MBean 精确命中；miss 空。
- 算术：eq(plus/minus/times/div(attr Count, const), const) 四种全部真过滤命中。
- 域限定：queryNames(new ObjectName("jck.q:*"), exp) 范围叠加过滤。
- queryMBeans 返回 ObjectInstance（name+className）。
- 静默负例（**两侧一致，不抛异常**）：数值属性 vs 字符串常量 eq → 空集（server 侧 CCE 被吞）；未知属性 eq → 空集。
### 版本差异
- 无（过滤语义两侧一致）。
