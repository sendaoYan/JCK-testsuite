# jdk.dynalink.linker.ConversionComparator.Comparison（枚举） 设计案

## 1. API 功能
转换比较裁决：INDETERMINATE / TYPE_1_BETTER / TYPE_2_BETTER。

## 2. Java 用户如何使用
ConversionComparator.compareConversion 的返回值；LinkerServices.compareConversion 聚合各比较器（非 INDETERMINATE 即采纳）。

## 3. 开源代码实际用法调研
来源：JDK-27 src.zip ConversionComparator.java。

## 4. 测试用例设计 + 易错点
声明顺序枚举值表、valueOf/toString/name 一致、嵌套类归属、isEnum。

