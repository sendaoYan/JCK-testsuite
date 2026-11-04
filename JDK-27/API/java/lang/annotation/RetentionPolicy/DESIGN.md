# java.lang.annotation.RetentionPolicy — 设计方案

## 1. API 功能
枚举：`@Retention` 的取值域：
- 3 个常量：SOURCE（编译丢弃）、CLASS（保留到 classfile，反射不可见，默认）、RUNTIME（反射可见）
- 枚举机制：`values()`/`valueOf(String)`

## 2. Java 用户如何使用
```java
@Retention(RetentionPolicy.RUNTIME) @interface A {}
switch (policy) {
    case SOURCE:  ...  // 工具注解
    case CLASS:   ...  // 字节码工具（如 ASM 消费）
    case RUNTIME: ...  // 反射框架
}
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/annotation/RetentionPolicy.java`：javadoc 说明 CLASS 是无 @Retention 时的默认。
- Lombok 注解（`@Getter` 等）保留在 SOURCE/CLASS 供编译器处理；Spring/Jakarta 注解一律 RUNTIME。
- JDK `test/jdk/java/lang/annotation`（Retention 相关用例）验证三种策略下反射可见性。

## 4. 测试用例设计 + 自动化实现
白盒：枚举、父类 Enum、3 常量、values/valueOf 存在。
黑盒：
- `values()` 恰 3 个且顺序 SOURCE→CLASS→RUNTIME；
- 防御性拷贝；三个常量 `valueOf` 往返；大小写敏感抛 IAE；
- ordinal 0/1/2；compareTo/name/toString；
- switch 消费范式遍历全体；身份相等。
对应 `RetentionPolicyTest.java` 的 `testBlackBox()`。
