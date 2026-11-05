# javax.management.BadBinaryOpValueExpException 测试设计

## 1. API 功能
`BadBinaryOpValueExpException` 继承 `Exception`，表示二元表达式收到了非法 `ValueExp`。
构造器：`(ValueExp exp)`；可观察方法：getExp(), toString()。

## 2. Java 用户如何使用
```java
StringValueExp exp = new StringValueExp("x");
throw new BadBinaryOpValueExpException(exp);
// getExp() == exp; toString() -> "BadBinaryOpValueExpException: 'x'"
```

## 3. 开源代码实际用法调研
来源：JDK 源码 `~/git/jdk25u-dev/src/java.management/share/classes/javax/management/BadBinaryOpValueExpException.java` 及 Query 表达式求值路径；
JDK 自带 `test/jdk/javax/management` 的 Query 用例在构造非法查询表达式时触发这些异常。

## 4. 测试用例设计
- 白盒：public/非接口/非抽象、继承 `Exception`、可序列化、构造器与方法形态。
- 黑盒：构造后断言 `toString()` 精确内容、`getMessage()` 为 null（无 detail message）、`instanceof Exception`；getExp(), toString() 往返。
- 确定性：固定值字符串/对象。

## 深化（2026-11，批次 e）
新增维度：getExp 同一性、null 处理、toString 精确、序列化与 serialVersionUID、混合类型兄弟行为。
- getExp() 返回**构造传入的同一 ValueExp 实例**（== 断言）；null 允许且 getExp()==null。
- toString："BadBinaryOpValueExpException: 7" / "...: null"；getMessage() null、无 cause。
- 序列化 round-trip 后 getExp() 非 null 且值等价（"1"）；serialVersionUID 锁定 **5068475589449021227**（两侧一致）。
- 兄弟行为记录：算术/关系 apply 的混合类型操作数（数值 vs 字符串）抛**未受检 ClassCastException**（不是本异常）——两侧类型一致仅消息文案不同（JDK-27 带模块信息）。
### 版本差异
- 仅 ClassCastException 消息文案（JDK-8 短/JDK-27 带模块说明），类型与行为一致。
