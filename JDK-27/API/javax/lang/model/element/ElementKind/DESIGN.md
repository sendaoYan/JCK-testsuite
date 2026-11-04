# javax.lang.model.element.ElementKind — 设计方案

## 1. API 功能
枚举：元素种类（CLASS/INTERFACE/ENUM/RECORD/METHOD/FIELD/…）及 isClass/isInterface/isDeclaredType/isField/isExecutable/isInitializer/isVariable 判定。

## 2. Java 用户如何使用
遍历元素时按 `getKind()` 分支处理；`ElementKind.values()`/`valueOf` 用于反序列化/白名单校验。

## 3. 开源代码实际用法调研
JDK `~/git/jdk25u-dev/src/java.compiler/share/classes/javax/lang/model/element/ElementKind.java`；javac 元素遍历广泛使用 `getKind()` 分支。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 反射断言类型形态（接口/枚举/抽象类/继承链/关键成员/枚举常量）。
黑盒：白盒断言 21 个常量子集与枚举形态；黑盒断言各 is* 判定（CLASS/ENUM/RECORD isClass、INTERFACE/ANNOTATION_TYPE isInterface、FIELD/ENUM_CONSTANT isField 等）。 对应 `ElementKindTest.java` 的 `testBlackBox()`。
