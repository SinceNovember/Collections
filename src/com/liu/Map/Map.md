## Map源码分析
### Map关系图
![](https://github.com/SinceNovember/Collections/blob/master/extendsimages/Map.jpg)
## `HashMap`
### 什么`HashMap`
  基于哈希表的一个Map接口实现，存储的对象是一个键值对对象(Node实现(Map.Entry(key,value)接口),它根据键的hashCode值存储数据，大多数情况下可以直接定位到它的值，因而具有很快的访问速度，但遍历顺序却是不确定的。 HashMap最多只允许一条记录的键为null，允许多条记录的值为null。HashMap非线程安全，即任一时刻可以有多个线程同时写HashMap，可能会导致数据的不一致。如果需要满足线程安全，可以用 Collections的synchronizedMap方法使HashMap具有线程安全的能力，或者使用ConcurrentHashMap。
### 内部实现
从结构实现来讲，HashMap是数组+链表+红黑树（JDK1.8增加了红黑树部分）实现的，如下如所示。
![](https://github.com/SinceNovember/Collections/blob/master/extendsimages/3.png)
