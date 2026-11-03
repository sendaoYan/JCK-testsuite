# javax.print.attribute.standard.Sides 黑盒行为测试设计

## 1. API 功能
EnumSyntax 枚举类（EnumSyntax 子类），实现 DocAttribute、PrintRequestAttribute、PrintJobAttribute。常量 ONE_SIDED, TWO_SIDED_LONG_EDGE, TWO_SIDED_SHORT_EDGE, DUPLEX, TUMBLE。getName 为 "sides"，getCategory 为 Sides.class。toString 返回小写连字符名，getValue 返回枚举整数值，clone 返回单例自身，hashCode 等于 value。

## 2. Java 用户如何使用
直接引用静态常量（如 Sides.ONE_SIDED），配合 HashAttributeSet 作为打印请求/作业属性。getValue() 得到 IPP 枚举值，toString() 得到标准字符串名。

## 3. 开源代码实际用法调研
实现权威 ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/print/attribute/standard/Sides.java；test/jdk 打印属性测试引用常量（如 javax/print 属性类）。

## 4. 测试用例设计 + 自动化实现
黑盒：每个常量 getValue/toString 精确值、getCategory/getName 精确字符串、clone 单例、hashCode==value、equals 身份语义、getEnumValueTable/getStringTable/getOffset 反射校验。白盒：public 类、extends EnumSyntax、implements 各接口。

