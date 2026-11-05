# java.lang.annotation.ElementType — 设计方案（JDK 27）

## 1. API 功能
枚举：`@Target` 取值域。
- 12 个常量（JDK 27 声明顺序）：TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE, ANNOTATION_TYPE, PACKAGE, TYPE_PARAMETER, TYPE_USE, MODULE, RECORD_COMPONENT
- 枚举机制：`values()`/`valueOf(String)`

## 2. Java 用户如何使用
```java
@Target({ElementType.METHOD, ElementType.TYPE_PARAMETER}) @interface Check {}
for (ElementType t : ElementType.values()) { ... }
ElementType.valueOf("FIELD");
```

## 3. 开源代码实际用法调研
- JDK `ElementType.java`：MODULE since 9、RECORD_COMPONENT since 16。
- Spring/JUnit/MyBatis 组合使用。

## 4. 测试用例设计 + 自动化实现
白盒：枚举、父类 Enum、getEnumConstants 非空、values/valueOf 存在。
黑盒：`values()` 精确 12 常量及声明顺序；防御性拷贝；全体 valueOf 往返；未知名抛 IAE；每个常量 ordinal == 下标（TYPE=0、PACKAGE=7、MODULE=10、RECORD_COMPONENT=11）；compareTo/name/toString/equals/==；MODULE/RECORD_COMPONENT 存在；switch 覆盖 12 常量。

## 易错规范细节（JDK 27）
- JDK 27 = 12 常量（含 MODULE、RECORD_COMPONENT）；JDK 8 = 10 常量（无二者）。双侧各自断言。

## 契约深化（2026-11）
### API 功能
- 负例精确消息锁定（与 JDK-8 逐字一致）：valueOf("BOGUS")/空串 → "No enum constant java.lang.annotation.ElementType...."；valueOf(null) → NPE。
- 枚举单例机制：values() 与 getEnumConstants() 共享实例；每常量 getDeclaringClass 正确。
- 27 侧值集差异精确：12 值 = JDK-8 的 10 值 + MODULE（JDK 9）+ RECORD_COMPONENT（JDK 16）；MODULE/RECORD_COMPONENT 名与 ordinal（10/11）锁定；compareTo 精确差值。

### 使用方式
valueOf 负例 + 数组身份扫描 + 差值断言。

### 参考来源
javap（jdk-27.0.1）；JEP 257 与 record 引入历史。

### 测试用例设计 + 易错规范细节（本轮新增）
- `testNegativeAndIdentity`：27 独有值按名/序数锁定，MODULE.compareTo(TYPE)==10。
- 易错：值集差异只体现在常量集合与 ordinal，valueOf 消息格式双侧一致可共文字锁。
