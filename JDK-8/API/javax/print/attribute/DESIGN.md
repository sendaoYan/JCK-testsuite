# javax.print.attribute 包级 黑盒行为测试设计

## 1. API 功能
属性值类型系统（7 个语法基类 + Attribute/4 分型标记接口 + AttributeSet/4 分型集合接口 + Hash* 5 实现 + AttributeSetUtilities + UnmodifiableSetException）与 standard/ ~70 具体属性。

## 2. Java 用户如何使用
构造 PrintRequest/Doc/Job/Service 属性集合并传递给打印 API；按类别读取默认值/支持值。

## 3. 开源代码实际用法调研
JDK 源码 javax.print.attribute（双 JDK 实测探针为断言基准）。

## 4. 语法基类契约（均已深化，双侧绿）
- EnumSyntax：无 equals 覆写（身份等）；hashCode=value；clone 返回 this；序列化 readResolve 单例（无 VALUES 表子类反序列化 → InvalidObjectException）。
- IntegerSyntax：基类 equals 宽松（instanceof 按值），子类严格按类；越界由子类 IAE。
- TextSyntax/DateTimeSyntax/URISyntax：locale/Date 引用语义、跨构造等值。
- SetOfIntegerSyntax："1:3,5,9:11" 解析+归并+排序；contains/next 语义；基类对 null/""/倒序静默空集（子类加 IAE）；标准子类精确 IAE 消息。
- Size2DSyntax：INCH=25400/MM=1000 微米；跨单位等值；toString(units,suffix) 空格；负值 IAE "x < 0"。
- ResolutionSyntax：构造 units<1 IAE "units is < 1"；getter IAE ": units is < 1"；物理 dRU 归一。
- HashAttributeSet 家族：add 同类别异值替换返回 true / 同值 false；跨分型 add → CCE；null NPE（消息 null）；toArray/get/containsKey/equals/hashCode/addAll/remove 矩阵。
- AttributeSetUtilities：5 分型 unmodifiableView 写 → UnmodifiableSetException；synchronizedView 写透传；verifyAttributeValue/verifyAttributeCategory（CCE/NPE）；unmodifiableView(null)/synchronizedView(null) NPE。
- 4 个分型标记接口：成员矩阵（Copies 非 DocAttribute、Chromaticity 是 DocAttribute、PrinterName 仅 ServiceAttribute 等）+ HashDocAttributeSet 跨分型 add CCE。
- SupportedValuesAttribute：-supported 名表 + round-trip。
- UnmodifiableSetException：()/（String）、RuntimeException、序列化。

## 5. standard/ 家族
~70 枚举子类常量值/toString/getName/getCategory 全表 + 序列化单例 + IAE 边界矩阵；Media 联合语义、MediaSize 精确毫米表、PrinterResolution 参数序、JobStateReasons/PrinterStateReasons 集合语义。详见各子目录 DESIGN。

## 6. 运行纪律
纯 headless；jtreg 双侧全绿（JDK-8 75/75 attribute+standard 相关，JDK-27 77/77）。
