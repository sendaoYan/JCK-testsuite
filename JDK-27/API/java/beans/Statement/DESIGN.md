# java.beans.Statement — 设计案

## 1. API 功能
语句，表示"目标对象调用方法"。构造器 `(target, methodName, arguments)`；getTarget/getMethodName/getArguments/execute()。

## 2. Java 用户如何使用
`new Statement(obj, "m", args).execute()` 反射调用；`Expression` 继承并缓存值。

## 3. 开源代码实际用法调研
JDK 源码 `Statement.java`；XMLEncoder 序列化基础。

## 4. 测试用例设计 + 自动化实现
白盒：类。黑盒（`testBlackBox`）：目标/方法名/参数、execute 返回值与 int 自动装箱。

## 5. 深化覆盖（批次 beansF，2026-11，61 断言）
参考来源补全：Oracle JDK 27 javadoc；JavaBeans spec 1.01-A；OpenJDK `test/jdk/java/beans/Statement/{Test4530962,Test6707226,Test6788531}` 回归思路；对照 jdk28 源码核实（JDK-9+ 去掉 doPrivileged 包装，派发逻辑与 8 相同）。

新增测试方法：testWhiteBox（方法面 + 不覆盖 equals）、testAccessorsAndCtor（null args→空数组、双重克隆、元素引用保留、构造器不校验）、testExecuteInstanceSideEffects（StringBuilder append 链、setText、两次 execute 计数、List.remove(int) 装箱派发）、testExecuteStaticAndClassTargets（静态 setter + Class.class 实例方法 + "new"）、testArrayOperations（String[]/int[]/boolean[] 的 "set"、AIOOBE/CCE/IAE 精确异常矩阵、"length" NSME）、testExceptionMatrix（属性名/字段名无回退均 NSME、异常同实例透传、null→primitive IAE、包私有构造 IAE）、testToString（引号、类型名渲染、null 渲染）。

双侧实测异常类型完全一致（消息措辞除外：JDK-14+ helpful messages），无版本差异断言。
