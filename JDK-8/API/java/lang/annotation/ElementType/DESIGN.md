# java.lang.annotation.ElementType — 设计方案（JDK 8）

## 1. API 功能
枚举：`@Target` 的取值域（注解可应用的程序元素种类）。
- **JDK 8 恰好 10 个常量**（声明顺序）：TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE, ANNOTATION_TYPE, PACKAGE, TYPE_PARAMETER, TYPE_USE
- 枚举机制：`values()`/`valueOf(String)`

## 2. Java 用户如何使用
```java
@Target({ElementType.METHOD, ElementType.TYPE_PARAMETER}) @interface Check {}
for (ElementType t : ElementType.values()) { ... }
ElementType.valueOf("FIELD");
```

## 3. 开源代码实际用法调研
- JDK `ElementType.java`：常量按引入版本排列；JDK 8 无 MODULE/RECORD_COMPONENT。
- Spring `@Target(ElementType.TYPE)`、JUnit 5 `@Target({TYPE, METHOD})` 等。

## 4. 测试用例设计 + 自动化实现
- 结构：枚举、父类 Enum、values/valueOf 存在、getEnumConstants 非空。
- `values()` 精确 10 常量及声明顺序；防御性拷贝。
- 全体常量 valueOf 往返；未知名抛 IllegalArgumentException。
- 每个常量 ordinal == 声明下标；代表性 ordinal（TYPE=0、PACKAGE=7、TYPE_USE=9）。
- compareTo/name/toString/equals/== 枚举机制。

## 易错规范细节（JDK 8）
- **版本差异**：JDK 8 = 10 常量（无 MODULE、RECORD_COMPONENT）；JDK 27 = 12 常量（多 MODULE ordinal=10、RECORD_COMPONENT ordinal=11）。双侧各自断言自己的 `values().length`。

## 契约深化（2026-11）
### API 功能
- 负例精确消息锁定（双侧逐字一致）：`valueOf("BOGUS")` → IllegalArgumentException "No enum constant java.lang.annotation.ElementType.BOGUS"；空串同构；`valueOf(null)` → NullPointerException。
- 枚举单例机制：values() 与 getEnumConstants() 共享同一批实例（== 逐一验证）；每个常量 getDeclaringClass==ElementType.class。
- JDK-8 值集再确认：**恰 10 值**（8 个先行值 + JSR 308 的 TYPE_PARAMETER/TYPE_USE，二者 Java 8 已有）；MODULE/RECORD_COMPONENT 明确缺席（按名扫描断言）。
- compareTo 精确差值（PACKAGE-TYPE==7、TYPE-TYPE_USE==-9）。

### 使用方式
valueOf 负例 try/catch 精确消息 + 数组身份扫描。

### 参考来源
javap 双侧（10 值 vs 12 值）；java.lang.Enum.valueOf 通用行为。

### 测试用例设计 + 易错规范细节（本轮新增）
- `testNegativeAndIdentity`：消息全等 3 例、单例共享、声明类、值集负断言、差值。
- 易错：任务简报称"8 侧 8 值"与实测不符——TYPE_PARAMETER/TYPE_USE 自 Java 8（JSR 308）即存在，按实测锁 10 值。
