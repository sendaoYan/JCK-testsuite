# java.lang.reflect.Member — 测试设计（JDK-27）

## 1. API 功能
`Member` 是反射成员的标记/查询接口（Field 直接实现；Method/Constructor 经 `Executable` 实现）：
- 常量：`PUBLIC == 0`、`DECLARED == 1`（作为 `Class.getMethods/getDeclaredMethods` 类查询的过滤器语义）。
- 抽象方法：`getDeclaringClass/getName/getModifiers/isSynthetic`。
- 默认方法：`accessFlags()`（JDK-20+，把 int 修饰符映射为 `Set<AccessFlag>`）。

## 2. Java 用户如何使用
以 `Member` 统一引用 Field/Method/Constructor，写与成员种类无关的公共逻辑（日志、过滤、访问检查）：`member.getName()/getDeclaringClass()/getModifiers()` + `Modifier.isXxx` 组合判定；`isSynthetic()` 过滤编译器生成成员（桥方法/合成字段）；`accessFlags()` 获取结构化标志。

## 3. 开源代码实际用法调研
- JDK 源码 `~/git/jdk25u-dev/src/java.base/share/classes/java/lang/reflect/Member.java` 及 `Executable.java`（实现 `accessFlags` 默认方法）。
- JDK 自测 `~/git/jdk25u-dev/test/jdk/java/lang/reflect/AccessFlag/MethodAccessFlagTest.java`：经 `Executable.accessFlags()` 校验真实方法的标志集合（Member 默认方法的同一实现）。
- Spring `ReflectionUtils.doWithMember` 体系 / Commons Lang `MemberUtils`（`getMember` 泛型约束到 `Member`）：统一成员遍历（对应场景 1/4）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`MemberTest.testBlackBox`，固定类 `Biz/Cmp`）：
1. 三种成员统一 Member 视图：name（构造器名为类名）/declaringClass/精确修饰符组合（`public static final`、`public synchronized`、包私有构造器 0）。
2. `isSynthetic`：源码成员 false；`Comparable<Cmp>` 桥方法 true。
3. `accessFlags()` 默认方法：字段精确三标志集合、方法含 PUBLIC+SYNCHRONIZED、包私有构造器空集合。
4. `Member` 视图与底层反射对象 `equals/hashCode` 一致；`instanceof` 分派；`toString` 含成员名。
白盒保留常量/结构断言，修正 Method/Constructor 经 Executable 间接实现关系。产出：`MemberTest.java`（24 断言通过）。
