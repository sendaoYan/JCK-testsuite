# java.beans.Expression — 设计案

## 1. API 功能
表达式（extends Statement），表示"目标对象调用方法"并缓存结果。构造器 `(target, methodName, arguments)` 与 `(value, target, methodName, arguments)`；getValue() 计算并缓存。

## 2. Java 用户如何使用
`new Expression("hi", "toUpperCase", args).getValue()` 求值；`XMLEncoder` 用其表示对象构造。

## 3. 开源代码实际用法调研
JDK 源码 `Expression.java`；PersistenceDelegate 生成之。

## 4. 测试用例设计 + 自动化实现
白盒：extends Statement。黑盒（`testBlackBox`）：目标/方法名、getValue 求值、预置 value 构造器。

## 5. 深化覆盖（批次 beansF，2026-11，87 断言）
参考来源补全：Oracle JDK 27 javadoc（java.desktop/java.beans.Expression）；JavaBeans spec 1.01-A；OpenJDK `test/jdk/java/beans/Statement/ClassForName`、`Test4530962` 回归思路；JDK-27 实现（对照 jdk28 源码）逐行核实。

新增测试方法：testWhiteBox（方法面）、testAccessorsAndCtor（null args→空数组、ctor/getArguments 双重防御克隆、构造器不校验、预置 value）、testDispatchByArityAndSpecificity（substring 元数选择、最特定重载、null 实参歧义→NoSuchMethodException、null 实参匹配唯一签名）、testCachingSemantics（缓存 stale、setValue/null 绑定、execute() 重算且只算一次）、testStaticAndClassTargets（Math.max/abs、parseInt、Class target 的 Class.class 实例方法、forName 特例）、testNewForms（StringBuilder/数组类 nary/int[] 装箱/Character 首字符/Integer(String)/无参 Class.newInstance 回退/接口 InstantiationException/错元数 NSME/包私有构造 IAE）、testArrayAccess（get + 装箱、越界 AIOOBE、"length" NSME）、testBoxingAndWidening（无 int→long 拓宽、null→primitive IAE）、testExceptionMatrix（异常同实例透传、NPE 在求值时、"class" 非保留名、同元数类型不符 NSME）、testToString（<unbound>=/Integer=/引号）。

**版本差异（实证，与 JDK-8 侧不同源码）**：`Expression(Class.class,"forName",{"java.lang.String",Boolean.TRUE})` —— JDK-9+ 在 Statement.invoke 加了 `arguments.length == 1` 守卫，2 参落入常规方法查找 → NoSuchMethodException（JDK-8 则仍按 args[0] 解析成功）。JDK-27 侧断言 NSME。

异常类型表与易错点见 JDK-8 侧 DESIGN.md 第四节（双侧实测一致，除上述 forName 差异与 NPE 消息措辞）。
