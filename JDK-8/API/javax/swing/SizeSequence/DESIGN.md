# javax.swing.SizeSequence 测试设计

## 1. API 功能
有序条目尺寸表 + 前缀和索引：getSize/setSize/getSizes/setSizes 读写，
getPosition(index)/getIndex(position) 双向映射，insertEntries/removeEntries 增删条目并自动
维护映射；BasicScrollBarUI/文本布局用它把偏移换算成行号。

## 2. Java 用户如何使用
`SizeSequence rows = new SizeSequence(new int[]{10,20,30}); rows.getIndex(35); // 2`
滚动/分页场景 insertEntries(start, count, size) 处理插入。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk/src/java.desktop/share/classes/javax/swing/SizeSequence.java`（内部数组带 0 号哨兵）；
BasicScrollBarUI/JTable rowHeight 的使用。

## 4. 测试用例设计 + 易错细节
- 构造器矩阵：() 空、(n) 全 0、(n,v) 全 v、(int[]) 拷贝（后续改源数组不影响）；
  **(-1,5) 抛 NegativeArraySizeException**（直接炸在数组分配，不是 IAE）。
- [10,20,30] 边界矩阵：条目区间 [0,10)/[10,30)/[30,60)，getIndex(9)=0、getIndex(10)=1、
  getIndex(59)=2；**越界位置 60/61 → 3（条目数）**；getPosition(3)=60（末尾返回总和）。
- **getPosition(-1)=0 不抛**（负索引走内部哨兵位）；getSize(-1)/setSize(-1,5) 也不抛，
  且 setSize(-1) 会写进内部前缀和哨兵——setSizes 可修复任何状态。
- insertEntries(1,2,5) 于 [10,50,30] → [10,5,5,50,30]（整型数组精确锁定）；
  removeEntries(0,1) → 首项变为 5。
- setSizes/getSizes 往返 [1,2,3]。

自动化实现位于同目录 `*Test.java`。
