# java.lang.Cloneable 测试设计案（JDK-8）

> 覆盖目标：**招牌契约闭环**。重点补：不覆写 clone() 时在类体内直调从 Object 继承的 protected
> clone()（同文件嵌套类可达，无需反射）拿浅拷贝：== 不同实例、同运行时类、基本字段按值、
> 引用/数组字段**共享同一对象**（改 clone.arr[0] 原 arr 可见——浅拷贝实证）、clone 不调用构造器；
> 未实现 Cloneable → CloneNotSupportedException 真触发 + 消息锁定（被克隆类的二进制名）；
> Cloneable 是纯标记接口（0 方法 0 字段 0 类型参数）；super.clone() 按运行时类分配（父类 clone
> 返回子类实例）；深拷贝对照；数组天生可克隆；库类型事实表。

## 1. API 提供了哪些功能

- **接口** `java.lang.Cloneable`：纯标记接口，0 个方法。作用：告知 `Object.clone()` 合法性——
  未实现它调用 `Object.clone()` → `CloneNotSupportedException`（消息为被克隆类的二进制名）。
- `Object.clone()`：protected native；逐字段浅拷贝；按 `getClass()` 分配（super.clone() 在子类
  上返回子类实例）；不调用构造器。

## 2. Java 用户如何使用

- 实现 Cloneable + 覆写 public clone()（covariant 返回类型）；
- protected clone() 仅在类体内/同包可达（外部包不可达——编译期事实，记录）；
- 需要深拷贝时在覆写中手动复制可变引用字段。

```java
class Box implements Cloneable {
    int[] data;
    public Box clone() { Box c = (Box) super.clone(); c.data = data.clone(); return c; }
}
```

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/Cloneable.html` + `Object.clone()`）：x.clone() != x、
  x.clone().getClass() == x.getClass()、不调用构造器三条约定。
- **JLS §6.6.2** protected 访问规则（类体内经 this/t 同类型引用可达）。
- **OpenJDK 8u-dev** `Object.clone()` native 语义（字段浅拷贝 + Cloneable 检查）。

## 4. 测试用例设计（映射到 CloneableTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：0 方法 0 字段非泛型；Object.clone protected/native/throws CNSE | testWhiteBox |
| P0 | 不覆写 clone：类体内 this.clone() 浅拷贝全闭环 | testShallowCloneWithoutOverride |
| P0 | 浅拷贝：数组字段共享引用、改 arr[0] 双方可见 | testShallowCloneWithoutOverride |
| P0 | clone 不调用构造器（ctorCount 计数） | testShallowCloneWithoutOverride |
| P0 | 未实现 Cloneable → CNSE 真触发 + 消息==类二进制名 | testNotCloneableDirect |
| P0 | super.clone() 返回子类运行时实例（Child 经 Parent.clone） | testSuperCloneReturnsRuntimeClass |
| P1 | 覆写 equals 的克隆：值相等、hashCode 相等、!= | testCloneEqualsContract |
| P1 | 深拷贝对照：新数组、互改不可见 | testDeepCopyContrast |
| P1 | 子类继承父类 public clone() | testInheritedPublicClone |
| P1 | 数组天生可克隆（int[]/String[]，不查 Cloneable） | testArraysAreAlwaysCloneable |
| P2 | getInterfaces 含 Cloneable；ArrayList 是、String 不是 | testMarkerFacts |
| P2 | 嵌套对象共享、两次克隆三实例两两互异 | testSharedNestedObjectAndMultiClone |
| P2 | 非 Cloneable 的子类仍不可克隆（消息随运行时类） | testSubOfNonCloneable |
| P2 | 库类型事实表：Date/ArrayList/HashMap 是、StringBuilder 不是 | testLibraryCloneableFacts |
| P0 | 非 Cloneable 覆写式（原有） | testNotCloneable |

## 易错规范细节

- **protected 直调姿势**：嵌套类体内 `this.clone()` 可达；从外部类 main 经 `obj.clone()` 不可达
  （编译错误 "clone() has protected access in java.lang.Object"）——必须经类体内的包装方法。
- **CNSE 消息**：恰为 `Object.clone()` 目标的 `getClass().getName()`（含 `$` 的嵌套二进制名）。
- **浅 vs 深**：浅拷贝共享可变内部状态（arr/嵌套对象 == 同一引用）；深拷贝需覆写中手动复制。
- **String 不可 instanceof Cloneable**（final 类编译错误）——用 `Class.isAssignableFrom` 断言。
- **构造器零调用**是 Object.clone 的硬约定（本批以静态计数器实证）。
