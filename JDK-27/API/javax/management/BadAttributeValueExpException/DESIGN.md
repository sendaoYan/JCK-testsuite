# javax.management.BadAttributeValueExpException 测试设计

## 1. API 功能
`BadAttributeValueExpException` 继承 `Exception`，表示查询构造方法收到了非法 MBean 属性值。
构造器：`(Object val)`；可观察方法：toString()。

## 2. Java 用户如何使用
```java
throw new BadAttributeValueExpException(someValue);
// toString() -> "BadAttributeValueException: " + val
```

## 3. 开源代码实际用法调研
来源：JDK 源码 `~/git/jdk25u-dev/src/java.management/share/classes/javax/management/BadAttributeValueExpException.java` 及 Query 表达式求值路径；
JDK 自带 `test/jdk/javax/management` 的 Query 用例在构造非法查询表达式时触发这些异常。

## 4. 测试用例设计
- 白盒：public/非接口/非抽象、继承 `Exception`、可序列化、构造器与方法形态。
- 黑盒：构造后断言 `toString()` 精确内容、`getMessage()` 为 null（无 detail message）、`instanceof Exception`；。
- 确定性：固定值字符串/对象。

## 深化（2026-11，批次 e）
新增维度：精确 toString 表、真实触发路径、序列化与 serialVersionUID。
- toString 实测形态：**"BadAttributeValueException: <value>"（类名短形式，无 "Exp"）**；null → "...: null"；数值对象 → 其字符串形式；getMessage() 恒 null（value 不进 detail message）；无公开 getter（javap 双侧确认，val 字段 JDK-8 为 Object/JDK-27 为 String，但构造即 toString 化，行为一致）。
- 真实触发：无 QueryEval 上下文的 AttributeValueExp.apply（属性不可解析）即抛本异常。
- 序列化 round-trip 保 toString；serialVersionUID 锁定 **-3105272988410493376**（两侧实测一致）。
### 版本差异
- 字段类型：JDK-8 val 为 Object，JDK-27 为 String（序列化安全修复的演进）；对外行为（构造/toString/round-trip）两侧一致。
