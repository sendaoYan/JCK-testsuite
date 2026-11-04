# java.time.chrono.JapaneseEra 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
五纪元类（非枚举）：`MEIJI/TAISHO/SHOWA/HEISEI` 常量、`of(int)`、`valueOf(String)`、`values()`、`getValue/getDisplayName/range/query`。

## 2. Java 用户如何使用
```java
JapaneseEra e = JapaneseEra.valueOf("Reiwa");
e.getValue();  // 3
```

## 3. 参考来源
- JDK-8 `javap -p` 方法签名清单（本机 `/data/yansendao/JDK/binary/temurin/jdk8u372-b07`）。
- OpenJDK `jdk8u` 源码 `src/share/classes/java/time/` 对应类与内嵌 javadoc。
- OpenJDK `test/jdk/java/time/tck/` TCK 用例矩阵（行为权威来源）。
- JDK-27 侧同名 DESIGN.md 的交叉核对（跨版本行为差异按侧锁定）。

## 4. 测试用例设计 + 易错细节
- values 顺序 `[-1 Meiji, 0 Taisho, 1 Showa, 2 Heisei, 3 Reiwa]`；valueOf **大小写敏感**（"Reiwa" ✓、"REIWA"/"heisei" IAE `japaneseEra is invalid`、null NPE `japaneseEra`）；displayName 三档；range(YEAR_OF_ERA) UTE；query(precision)=Eras。

### 易错细节（JDK-8 实测锁定）
- `valueOf` 要求 `Reiwa` 首字母大写的驼峰，不是枚举常量名 `REIWA`。
- **JDK-8 无 `JapaneseEra.REIWA` 公共常量**（private 字段）；JDK-27 有——按侧锁定。
- Showa 的 SHORT 显示名 JDK-8 `Showa` vs JDK-27 `Shōwa`（长音 macron）——按侧锁定。
