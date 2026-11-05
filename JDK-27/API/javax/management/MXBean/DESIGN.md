# javax.management.MXBean 黑盒行为测试设计

## 1. API 功能
- 注解类型：`@MXBean`，`value()` 返回 true/false（默认 true），标记 MXBean 接口。

## 2. Java 用户如何使用
标注接口为 MXBean，使属性/操作类型映射为开放类型（如 List<String> 映射为 String[]）。
```java
@MXBean
public interface CacheMXBean { List<String> getNames(); }
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/mxbean/MXBeanAnnotationTest.java`、`MXBeanFlagTest.java`。
- 实现权威：`src/java.management/share/classes/javax/management/MXBean.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：`JMX.isMXBeanInterface` 对 @MXBean 接口为 true；@MXBean(false) 时 value() 返回 false；注解可在接口上反射读取。
- 白盒：isAnnotation、RUNTIME 保留、value() 返回 boolean 且默认 true。

## 深化（2026-11，批次 e）
新增维度：注解默认值、open 类型元数据、**open 转换真闭环**、非合规 MXBean、命名约定 vs @MXBean(false)。
- 注解：value() 返回 boolean、默认 TRUE、RUNTIME 保留；@MXBean / @MXBean(false) 取值。
- 元数据：MBeanInfo descriptor 字段 {immutableInfo="true", interfaceClassName=接口全名, mxbean="true"}（**字段值是 String 不是 Boolean，实测两侧一致**）；每个属性 descriptor 含 "openType"。
- open 转换实测（JDK-8 与 JDK-27 **完全一致**）：
  - `int[] getVec()` → getAttribute 返回 **int[]（[I）原样**（primitive 数组即其 open 表示，元素内容保持）；
  - `List<String> getNames()` → **String[]**（不是 List！List<E> 映射 E[]）；
  - `Map<String,Integer> getScores()` → **TabularData**（indexNames=["key"]、rowType 含 key(String)/value(Integer)、行内容 one→1/two→2 全遍历断言）；
  - `Color getColor()`（enum）→ **String "RED"**；
  - 属性 getType() 映射字符串：int / [Ljava.lang.String; / javax.management.openmbean.TabularData / [I / java.lang.String；
  - setAttribute round-trip（setX(7)→7）；错型 → InvalidAttributeValueException。
- 非合规：接口带不可 open 映射返回（无 getter POJO）→ 注册时 **NotCompliantMBeanException**（"cannot be translated into an open type"）；@MXBean(false)+接口无 MXBean 后缀 → 按标准内省失败 NotCompliant；@MXBean(false)+带后缀 → 同样失败。
- 命名约定：接口名以 MXBean 结尾且无注解 → 自动 MXBean 化（属性 openType 非 null、getAttribute 返回 String[]）。
### 版本差异
- 无行为差异（转换类型、异常类型、descriptor 字段值两侧一致）。
