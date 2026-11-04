# java.util.FormattableFlags 测试设计案（JDK-8）

> 覆盖目标：常量位值 + 与 formatTo 回调 flags 的对齐。

## 1. API 功能

- 常量：LEFT_JUSTIFY=1 / UPPERCASE=2 / ALTERNATE=4

## 2. 使用方式

String.format("%#-4S", formattable) 捕获 formatTo 的 flags == 7（三标志并集）。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/FormattableFlags`

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 三位常量精确值 1/2/4 | testFormattableFlags |
| P0 | %#-4S 回调 flags==7、%s flags==0 | testFlagsContracts |
| P1 | %-10.2S → LEFT|UPPER | testFlagsContracts |
| P0 | 契约深化：位值组合/Formattable 回调对齐 | testFlagsContracts |
