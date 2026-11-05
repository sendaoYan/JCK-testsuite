# jdk.dynalink.linker.ConversionComparator 设计案

## 1. API 功能
类型转换比较器接口。

## 2. Java 用户如何使用
由链接服务实现。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/linker/ConversionComparator.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 interface；黑盒降级。
## 5. 契约深化（JDK-27）
- 单抽象 compareConversion。
- 注册约束（源码）：linker 必须同时实现 GuardingTypeConverterFactory+ConversionComparator 才被 TypeConverterFactory 收集为比较器。
- 真实闭环：自定义比较器经 LinkerServices.compareConversion 生效（TYPE_1_BETTER/TYPE_2_BETTER/无关 INDETERMINATE）。
- 默认规则闭环：方法调用转换可者胜、双可 INDETERMINATE、交换参数翻转裁决。
