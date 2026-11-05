# FunctionalInterface — 设计方案

## 1. API 功能
`java.lang.FunctionalInterface`（注解，无成员）：标记接口为函数式接口（恰一个抽象方法），供编译器校验与 lambda/方法引用目标匹配。@Retention(RUNTIME)、@Documented、@Target({TYPE})。

## 2. Java 用户如何使用
```java
@FunctionalInterface
interface FI { int apply(int x); }
FI f = x -> x * 2;
```

## 3. 参考来源
JLS 9.8（函数式接口：default 方法与 Object 公有方法签名——如 equals(Object)——不计入抽象方法计数）；javap 双侧同构。

## 4. 测试用例设计 + 易错规范细节
- 四种实现形态（lambda/静态方法引用/实例方法引用/匿名类）行为断言。
- **default 方法不破坏 SAM**；**接口内重声明 equals(Object) 不破坏 SAM** 且实例仍用 Object.equals 身份语义。
- 泛型函数式接口经继承获得 SAM（StringFn extends Function）。
- 真实 API：Runnable/Callable/Function/Predicate 标注；Comparable 故意未标；Serializable 无抽象方法不可标。
- 无成员代理：hashCode==0、toString "@java.lang.FunctionalInterface()"。
- FunctionalInterfaceTest.java 现有 PASS 断言：42。
