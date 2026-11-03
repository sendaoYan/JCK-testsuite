# javax.print.attribute.standard.JobImpressions 黑盒行为测试设计

## 1. API 功能
IntegerSyntax 子类，构造器 JobImpressions(int)（范围 0..Integer.MAX_VALUE）。方法 getValue/getCategory/getName/equals/hashCode/toString（toString 返回十进制字符串）。

## 2. Java 用户如何使用
new JobImpressions(N) 后放入 HashPrintRequestAttributeSet；越界值抛 IllegalArgumentException。

## 3. 开源代码实际用法调研
实现权威 ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/print/attribute/standard/JobImpressions.java。

## 4. 测试用例设计 + 自动化实现
黑盒：getValue/getName/getCategory/toString 精确值、equals/hashCode、上下边界、越界 IAE。白盒：public 类、extends IntegerSyntax、implements 各接口。

