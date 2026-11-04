# javax.print.attribute.standard.JobHoldUntil 黑盒行为测试设计

## 1. API 功能
DateTimeSyntax 子类，构造器 JobHoldUntil(Date)。getValue/getName/getCategory/equals/hashCode/toString。

## 2. Java 用户如何使用
new JobHoldUntil(fixedDate) 作为时间属性；null 抛 NPE。固定 Instant/Date 保证确定性。

## 3. 开源代码实际用法调研
实现权威 ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/print/attribute/standard/JobHoldUntil.java。

## 4. 测试用例设计 + 自动化实现
黑盒：固定 Date(0) 的 value/toString/equals/hashCode、不同 Date 不等、null NPE。白盒：public final、extends DateTimeSyntax、implements 各接口。

