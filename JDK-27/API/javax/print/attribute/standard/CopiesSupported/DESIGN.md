# javax.print.attribute.standard.CopiesSupported 黑盒行为测试设计

## 1. API 功能
SetOfIntegerSyntax 子类，构造器 int/int,int。getMembers/contains/next/getName/getCategory/equals/hashCode/toString。

## 2. Java 用户如何使用
new CopiesSupported(1,3) 表示整数区间集合；contains/next 迭代；toString 输出规范形式（如 1-3,5）。

## 3. 开源代码实际用法调研
实现权威 ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/print/attribute/standard/CopiesSupported.java。

## 4. 测试用例设计 + 自动化实现
黑盒：区间构造、contains/next、toString 规范、equals/hashCode、字符串/数组/单值构造、非法字符串 IAE。白盒：public final、extends SetOfIntegerSyntax、implements 各接口。

