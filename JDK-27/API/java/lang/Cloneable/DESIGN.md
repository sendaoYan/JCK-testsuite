# Cloneable — 设计方案

## 1. API 功能
`java.lang.Cloneable`（接口）是标记接口，声明无任何方法，用于告诉 `Object.clone()` 允许字段级浅拷贝。未实现 `Cloneable` 的类调用 `clone()` 抛 `CloneNotSupportedException`；实现后返回同类的独立实例（浅拷贝）。

## 2. Java 用户如何使用
- 需要 `Object.clone()` 的自定义类声明 `implements Cloneable`，覆写 `public Object clone()`。
```java
class Box implements Cloneable {
  public Object clone() throws CloneNotSupportedException { return super.clone(); }
}
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/Cloneable`、`ArrayList.clone()`/数组克隆均依赖标记语义：不实现则 `clone()` 抛异常，实现则浅拷贝。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.Cloneable")` 验证为接口且无任何方法（标记接口）。
黑盒：未实现类 `clone()` 抛 `CloneNotSupportedException`；实现类克隆得到不同实例、同类、字段值相等（浅拷贝）。
对应 `<Simple>Test.java` 白盒 + 黑盒部分。

## 契约深化（2026-11）

### 1. API 功能（新增维度）
0 声明方法 / 0 公共方法 / 0 字段 / 0 类型参数（纯标记）；Object.clone 的
protected+native+throws CloneNotSupportedException 签名。

### 2. 使用方式（新增）
不覆写 clone 的类在**类体内直调继承的 this.clone()**（同文件嵌套类可达，无需反射）：
== 不同实例、同运行时类、基本字段按值、String/数组字段共享同一引用、改 clone.arr[0] 原 arr
可见（浅拷贝实证）、**构造器零调用**（ctorCount 计数）、null 字段原样保留、两次克隆三实例
两两互异；未实现 Cloneable → CNSE 消息==被克隆类二进制名（NoClone/SubNoClone 双档）；
super.clone() 按运行时类分配（Parent.clone() 在 Child 上返回 Child 实例、Child 继承
Cloneable-ness）；覆写 equals 的克隆值相等；深拷贝对照（新数组、互改隔离）；数组天生可克隆
（Cloneable.class.isAssignableFrom(int[].class)）；库类型事实（Date/ArrayList/HashMap 是、
StringBuilder/String 不是）；Object 本身不实现 Cloneable。

### 3. 参考来源
Oracle Java SE javadoc Cloneable/Object.clone（三条约定）；JLS §6.6.2 protected 类体内可达。

### 4. 测试用例设计 + 易错规范细节（新增方法）
testWhiteBoxShape；testShallowCloneWithoutOverride；testNotCloneableDirect（消息锁定）；
testSuperCloneReturnsRuntimeClass；testCloneEqualsContract；testDeepCopyContrast；
testInheritedPublicClone；testArraysAreAlwaysCloneable；testMarkerFacts；
testSharedNestedObjectAndMultiClone；testSubOfNonCloneable；testLibraryCloneableFacts。

易错：外部类直接 obj.clone() 编译报错（protected），必须经嵌套类体内包装方法；String 的
instanceof Cloneable 编译报错（final 类），用 Class 断言。
