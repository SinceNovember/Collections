package com.liu.Map;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class HashMap<K,V> extends AbstractMap<K, V>implements Map<K, V>,Cloneable,Serializable{
	 private static final long serialVersionUID = 362498820763181265L;
	 /**
	     * 默认初始容量，默认为2的4次方 = 16，2的n次方是为了加快hash计算速度，；；减少hash冲突，，，h & (length-1)，，1111111
	     */
	 static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; 
	 /**
	     * 最大容量，默认为2的30次方，
	     */
	 static final int MAXIMUM_CAPACITY = 1 << 30;
	 /**
	     * 默认负载因子，默认为0.75
	     */
	 static final float DEFAULT_LOAD_FACTOR = 0.75f;
	/// 当桶(bucket)上的结点数大于这个值时会转成红黑树
	    static final int TREEIFY_THRESHOLD = 8; 
	 // 当桶(bucket)上的结点数小于这个值时树转链表
	 static final int UNTREEIFY_THRESHOLD = 6;
	/// 桶中结构转化为红黑树对应的table的最小大小
	 static final int MIN_TREEIFY_CAPACITY = 64;
	 final float loadFactor;//负载因子
	 transient Node<K,V>[] table;//Node数组，存放每个Entry
	 transient Set<Map.Entry<K,V>> entrySet;//每个Entry存放到Set中
	 transient int size;//当前Map中key-value映射的个数
	 transient int modCount;//Hash表结构性修改次数，用于实现迭代器快速失败行为
	 int threshold;//// 临界值 当实际大小(容量*填充因子)超过临界值时，会进行扩容
	  /** 
	 * 静态内部类，一个结点类，单向链表，内容为hash值，键和值
	 */
	transient Set<K>        keySet;
    transient Collection<V> values;
	static class Node<K,V> implements Map.Entry<K,V> {
	        final int hash;//hash值
	        final K key;//存储的键
	        V value;//存储的值
	        Node<K,V> next;//指向下一个Entry

	        Node(int hash, K key, V value, Node<K,V> next) {
	            this.hash = hash;
	            this.key = key;
	            this.value = value;
	            this.next = next;
	        }

			@Override
			public K getKey() {//获取当前结点的键
				return key;
			}

			@Override
			public V getValue() {//获取当前结点的键
				// TODO Auto-generated method stub
				return value;
			}
			   public final String toString() { return key + "=" + value; }
			   public final int hashCode() {//获取当前的hash值
		            return Objects.hashCode(key) ^ Objects.hashCode(value);
		        }
			@Override
			public V setValue(V newValue) {//设置当前结点的值为新值
				  V oldValue = value;
		            value = newValue;
		            return oldValue;
			}
			public final boolean equals(Object o) {//判断输入的对象与当前的Entry是否相等
	            if (o == this)
	                return true;
	            if (o instanceof Map.Entry) { //判断输入的死否是Entry的实例
	                Map.Entry<?,?> e = (Map.Entry<?,?>)o;//强制转换为Entry
	                if (Objects.equals(key, e.getKey()) &&
	                    Objects.equals(value, e.getValue()))//判断与当前的键值是否相等
	                    return true;
	            }
	            return false;
	        }
	 }
	/**  
	* @Title: hash  
	* @Description: 通过输入的键返回hash值
	*/  
	static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }
	  static Class<?> comparableClassFor(Object x) {
	        if (x instanceof Comparable) {
	            Class<?> c; Type[] ts, as; Type t; ParameterizedType p;
	            if ((c = x.getClass()) == String.class) // bypass checks
	                return c;
	            if ((ts = c.getGenericInterfaces()) != null) {
	                for (int i = 0; i < ts.length; ++i) {
	                    if (((t = ts[i]) instanceof ParameterizedType) &&
	                        ((p = (ParameterizedType)t).getRawType() ==
	                         Comparable.class) &&
	                        (as = p.getActualTypeArguments()) != null &&
	                        as.length == 1 && as[0] == c) // type arg is c
	                        return c;
	                }
	            }
	        }
	        return null;
	    }
	  @SuppressWarnings({"rawtypes","unchecked"}) // for cast to Comparable
	    static int compareComparables(Class<?> kc, Object k, Object x) {
	        return (x == null || x.getClass() != kc ? 0 :
	                ((Comparable)k).compareTo(x));
	    }
	  //tableSizeFor(initialCapacity)返回大于initialCapacity的最小的二次幂数值。
	  static final int tableSizeFor(int cap) {
	        int n = cap - 1;
	        n |= n >>> 1;
	        n |= n >>> 2;
	        n |= n >>> 4;
	        n |= n >>> 8;
	        n |= n >>> 16;
	        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
	    }

	  public HashMap(int initialCapacity, float loadFactor) {
		  // 初始容量不能小于0，否则报错
	        if (initialCapacity < 0)
	            throw new IllegalArgumentException("Illegal initial capacity: " +
	                                               initialCapacity);
	     // 初始容量不能大于最大值，否则为最大值
	        if (initialCapacity > MAXIMUM_CAPACITY)
	            initialCapacity = MAXIMUM_CAPACITY;
	        // 填充因子不能小于或等于0，不能为非数字
	        if (loadFactor <= 0 || Float.isNaN(loadFactor))
	            throw new IllegalArgumentException("Illegal load factor: " +
	                                               loadFactor);
	     // 初始化填充因子  
	        this.loadFactor = loadFactor;
	     // 初始化threshold大小
	        this.threshold = tableSizeFor(initialCapacity);
	    }
	  public HashMap(int initialCapacity) {
		// 调用HashMap(int, float)型构造函数
	        this(initialCapacity, DEFAULT_LOAD_FACTOR);
	    }
	   public HashMap() {
		   // 初始化填充因子     
	        this.loadFactor = DEFAULT_LOAD_FACTOR; // all other fields defaulted
	    }
	   public HashMap(Map<? extends K, ? extends V> m) {
		// 初始化填充因子 
	        this.loadFactor = DEFAULT_LOAD_FACTOR;
	     // 将m中的所有元素添加至HashMap中
	        putMapEntries(m, false);
	    }
	   //将参数里的Map添加到此Map中
	   final void putMapEntries(Map<? extends K, ? extends V> m, boolean evict) {
	        int s = m.size();
	        if (s > 0) {
	        	// 判断table是否已经初始化
	            if (table == null) { // pre-size
	            	// 未初始化，s为m的实际元素个数
	                float ft = ((float)s / loadFactor) + 1.0F;
	                int t = ((ft < (float)MAXIMUM_CAPACITY) ?
	                         (int)ft : MAXIMUM_CAPACITY);
	             // 计算得到的t大于阈值，则初始化阈值
	                if (t > threshold)
	                    threshold = tableSizeFor(t);
	            }
	         // 已初始化，并且m元素个数大于阈值，进行扩容处理
	            else if (s > threshold)
	                resize();
	         // 将m中的所有元素添加至HashMap中
	            for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
	                K key = e.getKey();
	                V value = e.getValue();
	                putVal(hash(key), key, value, false, evict);
	            }
	        }
	    }
	   //返回Map的长度
	   public int size() {
	        return size;
	    }
	   //判断Map是否为空
	   public boolean isEmpty() {
	        return size == 0;
	    }
	   /*通过key获取value的值
	    * 实际上判断key和key的hash值进行判断
	    */
	   public V get(Object key) {
	        Node<K,V> e;
	        return (e = getNode(hash(key), key)) == null ? null : e.value;
	    }
	   //通过hash值以及key获取Node(Entry)
	   final Node<K,V> getNode(int hash, Object key) {
	        Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
	        // table已经初始化，长度大于0，根据hash寻找table中的项也不为空
	        if ((tab = table) != null && (n = tab.length) > 0 &&
	            (first = tab[(n - 1) & hash]) != null) 
	        {
	        	 // 桶中第一项(数组元素)相等
	            if (first.hash == hash && // always check first node
	                ((k = first.key) == key || (key != null && key.equals(k))))//判断Map是否
	                return first;
	         // 桶中不止一个结点
	            if ((e = first.next) != null) {
	                if (first instanceof TreeeNode)
	                	// 在红黑树中查找
	                    return ((TreeNode<K,V>)first).getTreeNode(hash, key);
	             // 否则，在链表中查找
	                do {
	                    if (e.hash == hash &&
	                        ((k = e.key) == key || (key != null && key.equals(k))))
	                        return e;
	                } while ((e = e.next) != null);
	            }
	        }
	        return null;
	    }
	   //判断map中是否存在此key
	    public boolean containsKey(Object key) {
	        return getNode(hash(key), key) != null;
	    }
	    //将key-value存入map中
	    public V put(K key, V value) {
	        return putVal(hash(key), key, value, false, true);
	    }
	    //添加新的key-value实际函数
	 final V putVal(int hash, K key, V value, boolean onlyIfAbsent,  
                boolean evict) {  
     Node<K,V>[] tab; Node<K,V> p; int n, i;  
//先将table赋给tab，判断table是否为null或大小为0，若为真，就调用resize（）初始化  
     if ((tab = table) == null || (n = tab.length) == 0)  
         n = (tab = resize()).length;  
//通过i = (n - 1) & hash得到table中的index值，若为null，则直接添加一个newNode  
     if ((p = tab[i = (n - 1) & hash]) == null)  
         tab[i] = newNode(hash, key, value, null);  
     else {  
     //执行到这里，说明发生碰撞，即tab[i]不为空，需要组成单链表或红黑树  
         Node<K,V> e; K k;  
         if (p.hash == hash &&  
             ((k = p.key) == key || (key != null && key.equals(k))))  
//此时p指的是table[i]中存储的那个Node，如果待插入的节点中hash值和key值在p中已经存在，则将p赋给e  
             e = p;  
//如果table数组中node类的hash、key的值与将要插入的Node的hash、key不吻合，就需要在这个node节点链表或者树节点中查找。  
         else if (p instanceof TreeNode)  
         //当p属于红黑树结构时，则按照红黑树方式插入  
             e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);  
         else {  
 //到这里说明碰撞的节点以单链表形式存储，for循环用来使单链表依次向后查找  
             for (int binCount = 0; ; ++binCount) {  
     //将p的下一个节点赋给e，如果为null，创建一个新节点赋给p的下一个节点  
                 if ((e = p.next) == null) {  
                     p.next = newNode(hash, key, value, null);  
     //如果冲突节点达到8个，调用treeifyBin(tab, hash)，这个treeifyBin首先回去判断当前hash表的长度，如果不足64的话，实际上就只进行resize，扩容table，如果已经达到64，那么才会将冲突项存储结构改为红黑树。  

                     if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st  
                         treeifyBin(tab, hash);  
                     break;  
                 }  
//如果有相同的hash和key，则退出循环  
                 if (e.hash == hash &&  
                     ((k = e.key) == key || (key != null && key.equals(k))))  
                     break;  
                 p = e;//将p调整为下一个节点  
             }  
         }  
//若e不为null，表示已经存在与待插入节点hash、key相同的节点，hashmap后插入的key值对应的value会覆盖以前相同key值对应的value值，就是下面这块代码实现的  
         if (e != null) { // existing mapping for key  
             V oldValue = e.value;  
     //判断是否修改已插入节点的value  
             if (!onlyIfAbsent || oldValue == null)  
                 e.value = value;  
             afterNodeAccess(e);  
             return oldValue;  
         }  
     }  
     ++modCount;//插入新节点后，hashmap的结构调整次数+1  
     if (++size > threshold)  
         resize();//HashMap中节点数+1，如果大于threshold，那么要进行一次扩容  
     afterNodeInsertion(evict);  
     return null;  
 }  
	//进行扩容，会伴随着一次重新hash分配，并且会遍历hash表中所有的元素，是非常耗时的。在编写程序中，要尽量避免resize。
	final Node<K,V>[] resize() {
        Node<K,V>[] oldTab = table;//定义临时Node数组型变量，作为hash table  
        int oldCap = (oldTab == null) ? 0 : oldTab.length;//读取hash table的长度  
        int oldThr = threshold;//读取扩容门限
        int newCap, newThr = 0;//初始化新的table长度和门限值 
        if (oldCap > 0) {
        	//执行到这里，说明table已经初始化  
            if (oldCap >= MAXIMUM_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return oldTab;
            }
          //二倍扩容，容量和门限值都加倍  
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                     oldCap >= DEFAULT_INITIAL_CAPACITY)
                newThr = oldThr << 1; // double threshold
        }
        else if (oldThr > 0) 
            newCap = oldThr; //用构造器初始化了门限值，将门限值直接赋给新table容量
        else {               
        	//老的table容量和门限值都为0，初始化新容量，新门限值，在调用hashmap（）方式构造容器时，就采用这种方式初始化 
            newCap = DEFAULT_INITIAL_CAPACITY;
            newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
        if (newThr == 0) {
        	 //如果门限值为0，重新设置门限  ，为新的容量大小*加载因子
            float ft = (float)newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                      (int)ft : Integer.MAX_VALUE);
        }
        threshold = newThr;//更新新门限值为threshold  
        @SuppressWarnings({"rawtypes","unchecked"})
                    Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap]; //初始化新的table数组 
        table = newTab;
        //当原来的table不为null时，需要将table[i]中的节点迁移  
        if (oldTab != null) {
            for (int j = 0; j < oldCap; ++j) {
                Node<K,V> e;
              //取出链表中第一个节点保存，若不为null，继续下面操作
                if ((e = oldTab[j]) != null) {
                    oldTab[j] = null;//主动释放  
                    if (e.next == null)
                    	//链表中只有一个节点，没有后续节点，则直接重新计算在新table中的index，并将此节点存储到新table对应的index位置处  
                        newTab[e.hash & (newCap - 1)] = e;
                    else if (e instanceof TreeNode)
                    	 //若e是红黑树节点，则按红黑树移动 
                        ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                    else { // preserve order
                    	//迁移单链表中的每个节点  
                        Node<K,V> loHead = null, loTail = null;
                        Node<K,V> hiHead = null, hiTail = null;
                        Node<K,V> next;
                        do {
                        	/** 
                        	* 把链表上的键值对按hash值分成lo和hi两串，lo串的新索引位置与原先相同[原先位 
                        	* j]，hi串的新索引位置为[原先位置j+oldCap]； 
                        	* 链表的键值对加入lo还是hi串取决于 判断条件if ((e.hash & oldCap) == 0)，因为* capacity是2的幂，所以oldCap为10...0的二进制形式，若判断条件为真，意味着 
                        	* oldCap为1的那位对应的hash位为0，对新索引的计算没有影响（新索引 
                        	* =hash&(newCap-*1)，newCap=oldCap<<2）；若判断条件为假，则 oldCap为1的那位* 对应的hash位为1， 
                        	* 即新索引=hash&( newCap-1 )= hash&( (oldCap<<2) - 1)，相当于多了10...0， 
                        	* 即 oldCap 
                        	 
                        	* 例子： 
                        	* 旧容量=16，二进制10000；新容量=32，二进制100000 
                        	* 旧索引的计算： 
                        	* hash = xxxx xxxx xxxy xxxx 
                        	* 旧容量-1 1111 
                        	* &运算 xxxx 
                        	* 新索引的计算： 
                        	* hash = xxxx xxxx xxxy xxxx 
                        	* 新容量-1 1 1111 
                        	* &运算 y xxxx 
                        	* 新索引 = 旧索引 + y0000，若判断条件为真，则y=0(lo串索引不变)，否则y=1(hi串 
                        	* 索引=旧索引+旧容量10000) 
                        	   */  
                            next = e.next;
                            if ((e.hash & oldCap) == 0) {
                                if (loTail == null)
                                    loHead = e;
                                else
                                    loTail.next = e;
                                loTail = e;
                            }
                            else {
                                if (hiTail == null)
                                    hiHead = e;
                                else
                                    hiTail.next = e;
                                hiTail = e;
                            }
                        } while ((e = next) != null);
                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        if (hiTail != null) {
                            hiTail.next = null;
                            newTab[j + oldCap] = hiHead;
                        }
                    }
                }
            }
        }
        return newTab;
    }
	  final void treeifyBin(Node<K,V>[] tab, int hash) {
	        int n, index; Node<K,V> e;
	        if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
	            resize();
	        else if ((e = tab[index = (n - 1) & hash]) != null) {
	            TreeNode<K,V> hd = null, tl = null;
	            do {
	                TreeNode<K,V> p = replacementTreeNode(e, null);
	                if (tl == null)
	                    hd = p;
	                else {
	                    p.prev = tl;
	                    tl.next = p;
	                }
	                tl = p;
	            } while ((e = e.next) != null);
	            if ((tab[index] = hd) != null)
	                hd.treeify(tab);
	        }
	    }
	  //将Map m中的放入此HashMap中
	  public void putAll(Map<? extends K, ? extends V> m) {
	        putMapEntries(m, true);
	    }
	  //移除key为指定的Entry(Node),返回Value
	  public V remove(Object key) {
	        Node<K,V> e;
	        return (e = removeNode(hash(key), key, null, false, true)) == null ?
	            null : e.value;
	    }
	  //用来真实移除Entry的函数
	    final Node<K,V> removeNode(int hash, Object key, Object value,
                boolean matchValue, boolean movable)
	    {
	        Node<K,V>[] tab; Node<K,V> p; int n, index;
	        //把table数组赋tab数组，判断数据是否有长度找到hash对应的数组位置
	        if ((tab = table) != null && (n = tab.length) > 0 &&
	            (p = tab[index = (n - 1) & hash]) != null) {
	            Node<K,V> node = null, e; K k; V v;
	            if (p.hash == hash &&
	                ((k = p.key) == key || (key != null && key.equals(k))))//如果该桶的一个节点与所需的key相等
	                node = p;//赋值到node节点
	            else if ((e = p.next) != null) {//否则继续向下寻找
	                if (p instanceof TreeNode)//如果是红黑书的实例
	                    node = ((TreeNode<K,V>)p).getTreeNode(hash, key);//进入红黑树进行寻找
	                else {
	                    do {//进行遍历桶节点
	                        if (e.hash == hash &&
	                            ((k = e.key) == key ||
	                             (key != null && key.equals(k)))) {
	                            node = e;//找到赋值给node节点
	                            break;
	                        }
	                        p = e;//p记录当前位置
	                    } while ((e = e.next) != null);
	                }
	            }
	            if (node != null && (!matchValue || (v = node.value) == value ||
	                                 (value != null && value.equals(v)))) {//找到所需的节点
	                if (node instanceof TreeNode)//如果是节点在红黑树中
	                    ((TreeNode<K,V>)node).removeTreeNode(this, tab, movable);//利用红黑书删除函数进行删除
	                else if (node == p)//如果是第一节点
	                    tab[index] = node.next;//将下一个结点赋值到一位
	                else
	                    p.next = node.next;//删除指定节点(找到的结点的前一个节点指向找到结点的下一个节点）
	                ++modCount;
	                --size;
	                afterNodeRemoval(node);
	                return node;
	            }
	        }
	        return null;
	    }
	    //清除Map中元素
	    public void clear() {
	        Node<K,V>[] tab;
	        modCount++;
	        if ((tab = table) != null && size > 0) {
	            size = 0;
	            for (int i = 0; i < tab.length; ++i)
	                tab[i] = null;
	        }
	    }
	    //判断传入的value在Map中是否存在
	    public boolean containsValue(Object value) {
	        Node<K,V>[] tab; V v;
	        if ((tab = table) != null && size > 0) {
	            for (int i = 0; i < tab.length; ++i) {
	                for (Node<K,V> e = tab[i]; e != null; e = e.next) {//遍历所有Entry
	                    if ((v = e.value) == value ||
	                        (value != null && value.equals(v)))//找到返回true
	                        return true;
	                }
	            }
	        }
	        return false;
	    }
	    public Set<K> keySet() {//将所有的key存放到Set中返回
	        Set<K> ks = keySet;
	        if (ks == null) {
	            ks = new KeySet();
	            keySet = ks;
	        }
	        return ks;
	    }
	    //keyset实现
	    final class KeySet extends AbstractSet<K> {
	        public final int size()                 { return size; }//返回Map长度
	        public final void clear()               { HashMap.this.clear(); }//清楚Map
	        public final Iterator<K> iterator()     { return new KeyIterator(); }//返回key的迭代器
	        public final boolean contains(Object o) { return containsKey(o); }//判断指定的key是否存在Map中
	        public final boolean remove(Object key) {//移除指定key的节点                                                   
	            return removeNode(hash(key), key, null, false, true) != null;
	        }
	        public final Spliterator<K> spliterator() {
	            return new KeySpliterator<>(HashMap.this, 0, -1, 0, 0);
	        }
	        public final void forEach(Consumer<? super K> action) {
	            Node<K,V>[] tab;
	            if (action == null)
	                throw new NullPointerException();
	            if (size > 0 && (tab = table) != null) {
	                int mc = modCount;
	                for (int i = 0; i < tab.length; ++i) {
	                    for (Node<K,V> e = tab[i]; e != null; e = e.next)
	                        action.accept(e.key);
	                }
	                if (modCount != mc)
	                    throw new ConcurrentModificationException();
	            }
	        }
	    }
	    //用于将存放所有的值的Collection里面进行返回
	    public Collection<V> values() {
	        Collection<V> vs = values;
	        if (vs == null) {
	            vs = new Values();
	            values = vs;
	        }
	        return vs;
	    }
	    //实际进行获取值得函数
	    final class Values extends AbstractCollection<V> {
	        public final int size()                 { return size; }//返回Map长度
	        public final void clear()               { HashMap.this.clear(); }//清楚Map
	        public final Iterator<V> iterator()     { return new ValueIterator(); }//返回一个新的求值迭代器
	        public final boolean contains(Object o) { return containsValue(o); }//判断指定参数值是否存在Map中
	        public final Spliterator<V> spliterator() {
	            return new ValueSpliterator<>(HashMap.this, 0, -1, 0, 0);
	        }
	        public final void forEach(Consumer<? super V> action) {
	            Node<K,V>[] tab;
	            if (action == null)
	                throw new NullPointerException();
	            if (size > 0 && (tab = table) != null) {
	                int mc = modCount;
	                for (int i = 0; i < tab.length; ++i) {
	                    for (Node<K,V> e = tab[i]; e != null; e = e.next)
	                        action.accept(e.value);
	                }
	                if (modCount != mc)
	                    throw new ConcurrentModificationException();
	            }
	        }
	    }
	  	//返回所有的Entry实体		
	    public Set<Map.Entry<K,V>> entrySet() {
	        Set<Map.Entry<K,V>> es;
	        return (es = entrySet) == null ? (entrySet = new EntrySet()) : es;
	    }
	    final class EntrySet extends AbstractSet<Map.Entry<K,V>> {
	        public final int size()                 { return size; }
	        public final void clear()               { HashMap.this.clear(); }
	        public final Iterator<Map.Entry<K,V>> iterator() {//返回一个Map.Entry<K,V>的迭代器
	            return new EntryIterator();
	        }
	        public final boolean contains(Object o) {//判断指定的对象是否存在Map中
	            if (!(o instanceof Map.Entry))//判断o是不是Map.Entry的实例
	                return false;
	            Map.Entry<?,?> e = (Map.Entry<?,?>) o;
	            Object key = e.getKey();//将指定key的复制到新的key中
	            Node<K,V> candidate = getNode(hash(key), key);//获取key的节点
	            return candidate != null && candidate.equals(e);//判断指定节点与通过key找的结点是否相同
	        }
	        //移除指定的Entry
	        public final boolean remove(Object o) {
	            if (o instanceof Map.Entry) {
	                Map.Entry<?,?> e = (Map.Entry<?,?>) o;
	                Object key = e.getKey();
	                Object value = e.getValue();
	                return removeNode(hash(key), key, value, true, true) != null;
	            }
	            return false;
	        }
	        public final Spliterator<Map.Entry<K,V>> spliterator() {
	            return new EntrySpliterator<>(HashMap.this, 0, -1, 0, 0);
	        }
	        public final void forEach(Consumer<? super Map.Entry<K,V>> action) {
	            Node<K,V>[] tab;
	            if (action == null)
	                throw new NullPointerException();
	            if (size > 0 && (tab = table) != null) {
	                int mc = modCount;
	                for (int i = 0; i < tab.length; ++i) {
	                    for (Node<K,V> e = tab[i]; e != null; e = e.next)
	                        action.accept(e);
	                }
	                if (modCount != mc)
	                    throw new ConcurrentModificationException();
	            }
	        }
	    }
	    //判断指定的key是否存放结点，存在返回值，不存在反对defaultValue
	    @Override
	    public V getOrDefault(Object key, V defaultValue) {
	        Node<K,V> e;
	        return (e = getNode(hash(key), key)) == null ? defaultValue : e.value;
	    }
	    //添加一个指定key-value的Entry
	    @Override
	    public V putIfAbsent(K key, V value) {
	        return putVal(hash(key), key, value, true, true);
	    }
	    //移除指定的key-value的Node，成功返回true不成功返回false
	    @Override
	    public boolean remove(Object key, Object value) {
	        return removeNode(hash(key), key, value, true, true) != null;
	    }

	    //将指定的结点的值进行替换
	    @Override
	    public boolean replace(K key, V oldValue, V newValue) {
	        Node<K,V> e; V v;
	        if ((e = getNode(hash(key), key)) != null &&
	            ((v = e.value) == oldValue || (v != null && v.equals(oldValue)))) {
	            e.value = newValue;
	            afterNodeAccess(e);
	            return true;
	        }
	        return false;
	    }
	    @Override
	    public V computeIfAbsent(K key,
	                             Function<? super K, ? extends V> mappingFunction) {
	        if (mappingFunction == null)
	            throw new NullPointerException();
	        int hash = hash(key);
	        Node<K,V>[] tab; Node<K,V> first; int n, i;
	        int binCount = 0;
	        TreeNode<K,V> t = null;
	        Node<K,V> old = null;
	        if (size > threshold || (tab = table) == null ||
	            (n = tab.length) == 0)
	            n = (tab = resize()).length;
	        if ((first = tab[i = (n - 1) & hash]) != null) {
	            if (first instanceof TreeNode)
	                old = (t = (TreeNode<K,V>)first).getTreeNode(hash, key);
	            else {
	                Node<K,V> e = first; K k;
	                do {
	                    if (e.hash == hash &&
	                        ((k = e.key) == key || (key != null && key.equals(k)))) {
	                        old = e;
	                        break;
	                    }
	                    ++binCount;
	                } while ((e = e.next) != null);
	            }
	            V oldValue;
	            if (old != null && (oldValue = old.value) != null) {
	                afterNodeAccess(old);
	                return oldValue;
	            }
	        }
	        V v = mappingFunction.apply(key);
	        if (v == null) {
	            return null;
	        } else if (old != null) {
	            old.value = v;
	            afterNodeAccess(old);
	            return v;
	        }
	        else if (t != null)
	            t.putTreeVal(this, tab, hash, key, v);
	        else {
	            tab[i] = newNode(hash, key, v, first);
	            if (binCount >= TREEIFY_THRESHOLD - 1)
	                treeifyBin(tab, hash);
	        }
	        ++modCount;
	        ++size;
	        afterNodeInsertion(true);
	        return v;
	    }

	    public V computeIfPresent(K key,
	                              BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
	        if (remappingFunction == null)
	            throw new NullPointerException();
	        Node<K,V> e; V oldValue;
	        int hash = hash(key);
	        if ((e = getNode(hash, key)) != null &&
	            (oldValue = e.value) != null) {
	            V v = remappingFunction.apply(key, oldValue);
	            if (v != null) {
	                e.value = v;
	                afterNodeAccess(e);
	                return v;
	            }
	            else
	                removeNode(hash, key, null, false, true);
	        }
	        return null;
	    }

	    @Override
	    public V compute(K key,
	                     BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
	        if (remappingFunction == null)
	            throw new NullPointerException();
	        int hash = hash(key);
	        Node<K,V>[] tab; Node<K,V> first; int n, i;
	        int binCount = 0;
	        TreeNode<K,V> t = null;
	        Node<K,V> old = null;
	        if (size > threshold || (tab = table) == null ||
	            (n = tab.length) == 0)
	            n = (tab = resize()).length;
	        if ((first = tab[i = (n - 1) & hash]) != null) {
	            if (first instanceof TreeNode)
	                old = (t = (TreeNode<K,V>)first).getTreeNode(hash, key);
	            else {
	                Node<K,V> e = first; K k;
	                do {
	                    if (e.hash == hash &&
	                        ((k = e.key) == key || (key != null && key.equals(k)))) {
	                        old = e;
	                        break;
	                    }
	                    ++binCount;
	                } while ((e = e.next) != null);
	            }
	        }
	        V oldValue = (old == null) ? null : old.value;
	        V v = remappingFunction.apply(key, oldValue);
	        if (old != null) {
	            if (v != null) {
	                old.value = v;
	                afterNodeAccess(old);
	            }
	            else
	                removeNode(hash, key, null, false, true);
	        }
	        else if (v != null) {
	            if (t != null)
	                t.putTreeVal(this, tab, hash, key, v);
	            else {
	                tab[i] = newNode(hash, key, v, first);
	                if (binCount >= TREEIFY_THRESHOLD - 1)
	                    treeifyBin(tab, hash);
	            }
	            ++modCount;
	            ++size;
	            afterNodeInsertion(true);
	        }
	        return v;
	    }
	    @Override
	    public V merge(K key, V value,
	                   BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
	        if (value == null)
	            throw new NullPointerException();
	        if (remappingFunction == null)
	            throw new NullPointerException();
	        int hash = hash(key);
	        Node<K,V>[] tab; Node<K,V> first; int n, i;
	        int binCount = 0;
	        TreeNode<K,V> t = null;
	        Node<K,V> old = null;
	        if (size > threshold || (tab = table) == null ||
	            (n = tab.length) == 0)
	            n = (tab = resize()).length;
	        if ((first = tab[i = (n - 1) & hash]) != null) {
	            if (first instanceof TreeNode)
	                old = (t = (TreeNode<K,V>)first).getTreeNode(hash, key);
	            else {
	                Node<K,V> e = first; K k;
	                do {
	                    if (e.hash == hash &&
	                        ((k = e.key) == key || (key != null && key.equals(k)))) {
	                        old = e;
	                        break;
	                    }
	                    ++binCount;
	                } while ((e = e.next) != null);
	            }
	        }
	        if (old != null) {
	            V v;
	            if (old.value != null)
	                v = remappingFunction.apply(old.value, value);
	            else
	                v = value;
	            if (v != null) {
	                old.value = v;
	                afterNodeAccess(old);
	            }
	            else
	                removeNode(hash, key, null, false, true);
	            return v;
	        }
	        if (value != null) {
	            if (t != null)
	                t.putTreeVal(this, tab, hash, key, value);
	            else {
	                tab[i] = newNode(hash, key, value, first);
	                if (binCount >= TREEIFY_THRESHOLD - 1)
	                    treeifyBin(tab, hash);
	            }
	            ++modCount;
	            ++size;
	            afterNodeInsertion(true);
	        }
	        return value;
	    }

	    @Override
	    public void forEach(BiConsumer<? super K, ? super V> action) {
	        Node<K,V>[] tab;
	        if (action == null)
	            throw new NullPointerException();
	        if (size > 0 && (tab = table) != null) {
	            int mc = modCount;
	            for (int i = 0; i < tab.length; ++i) {
	                for (Node<K,V> e = tab[i]; e != null; e = e.next)
	                    action.accept(e.key, e.value);
	            }
	            if (modCount != mc)
	                throw new ConcurrentModificationException();
	        }
	    }
	    //替换所有function中的Entry
	    @Override
	    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
	        Node<K,V>[] tab;
	        if (function == null)
	            throw new NullPointerException();
	        if (size > 0 && (tab = table) != null) {
	            int mc = modCount;
	            for (int i = 0; i < tab.length; ++i) {
	                for (Node<K,V> e = tab[i]; e != null; e = e.next) {
	                    e.value = function.apply(e.key, e.value);
	                }
	            }
	            if (modCount != mc)
	                throw new ConcurrentModificationException();
	        }
	    }
	    //克隆
	    @SuppressWarnings("unchecked")
	    @Override
	    public Object clone() {
	        HashMap<K,V> result;
	        try {
	            result = (HashMap<K,V>)super.clone();
	        } catch (CloneNotSupportedException e) {
	            // this shouldn't happen, since we are Cloneable
	            throw new InternalError(e);
	        }
	        result.reinitialize();
	        result.putMapEntries(this, false);
	        return result;
	    }
	    final float loadFactor() { return loadFactor; }//返回加载因子
	    final int capacity() {//返回容量
	        return (table != null) ? table.length :
	            (threshold > 0) ? threshold :
	            DEFAULT_INITIAL_CAPACITY;
	    }
	    private void writeObject(java.io.ObjectOutputStream s)
	            throws IOException {
	            int buckets = capacity();
	            // Write out the threshold, loadfactor, and any hidden stuff
	            s.defaultWriteObject();
	            s.writeInt(buckets);
	            s.writeInt(size);
	            internalWriteEntries(s);
	        }

	        /**
	         * Reconstitute the {@code HashMap} instance from a stream (i.e.,
	         * deserialize it).
	         */
	        private void readObject(java.io.ObjectInputStream s)
	            throws IOException, ClassNotFoundException {
	            // Read in the threshold (ignored), loadfactor, and any hidden stuff
	            s.defaultReadObject();
	            reinitialize();
	            if (loadFactor <= 0 || Float.isNaN(loadFactor))
	                throw new InvalidObjectException("Illegal load factor: " +
	                                                 loadFactor);
	            s.readInt();                // Read and ignore number of buckets
	            int mappings = s.readInt(); // Read number of mappings (size)
	            if (mappings < 0)
	                throw new InvalidObjectException("Illegal mappings count: " +
	                                                 mappings);
	            else if (mappings > 0) { // (if zero, use defaults)
	                // Size the table using given load factor only if within
	                // range of 0.25...4.0
	                float lf = Math.min(Math.max(0.25f, loadFactor), 4.0f);
	                float fc = (float)mappings / lf + 1.0f;
	                int cap = ((fc < DEFAULT_INITIAL_CAPACITY) ?
	                           DEFAULT_INITIAL_CAPACITY :
	                           (fc >= MAXIMUM_CAPACITY) ?
	                           MAXIMUM_CAPACITY :
	                           tableSizeFor((int)fc));
	                float ft = (float)cap * lf;
	                threshold = ((cap < MAXIMUM_CAPACITY && ft < MAXIMUM_CAPACITY) ?
	                             (int)ft : Integer.MAX_VALUE);
	                @SuppressWarnings({"rawtypes","unchecked"})
	                    Node<K,V>[] tab = (Node<K,V>[])new Node[cap];
	                table = tab;

	                // Read the keys and values, and put the mappings in the HashMap
	                for (int i = 0; i < mappings; i++) {
	                    @SuppressWarnings("unchecked")
	                        K key = (K) s.readObject();
	                    @SuppressWarnings("unchecked")
	                        V value = (V) s.readObject();
	                    putVal(hash(key), key, value, false, false);
	                }
	            }
	        }
	        abstract class HashIterator {
	            Node<K,V> next;        // 下一个节点
	            Node<K,V> current;     // 当前结点
	            int expectedModCount;  // for fast-fail
	            int index;             // 索引

	            HashIterator() {
	                expectedModCount = modCount;
	                Node<K,V>[] t = table;//将table赋值给t
	                current = next = null;
	                index = 0;
	                if (t != null && size > 0) { // advance to first entry
	                    do {} while (index < t.length && (next = t[index++]) == null);//获取第一个Entry位置
	                }
	            }

	            public final boolean hasNext() {//判断是否有下一个，实指next当前的，应为执行nextNode结束前，会自动将索引往前移动。
	                return next != null;
	            }

	            final Node<K,V> nextNode() {//下一个节点
	                Node<K,V>[] t;
	                Node<K,V> e = next;
	                if (modCount != expectedModCount)
	                    throw new ConcurrentModificationException();
	                if (e == null)
	                    throw new NoSuchElementException();
	                if ((next = (current = e).next) == null && (t = table) != null) {
	                    do {} while (index < t.length && (next = t[index++]) == null);//将下一个table位置赋值给下一个节点，并且索引指向下一个位置
	                }
	                return e;
	            }

	            public final void remove() {//移除当前位置的Entry
	                Node<K,V> p = current;
	                if (p == null)
	                    throw new IllegalStateException();
	                if (modCount != expectedModCount)
	                    throw new ConcurrentModificationException();
	                current = null;
	                K key = p.key;
	                removeNode(hash(key), key, null, false, false);
	                expectedModCount = modCount;
	            }
	        }
	        //所有key的迭代器
	        final class KeyIterator extends HashIterator
	        implements Iterator<K> {
	        public final K next() { return nextNode().key; }
	    }
	      //所有value的迭代器
	    final class ValueIterator extends HashIterator
	        implements Iterator<V> {
	        public final V next() { return nextNode().value; }
	    }
	    //所有Map.Entry的迭代器
	    final class EntryIterator extends HashIterator
	        implements Iterator<Map.Entry<K,V>> {
	        public final Map.Entry<K,V> next() { return nextNode(); }
	    }
	    Node<K,V> newNode(int hash, K key, V value, Node<K,V> next) {//返回一个新的Node节点
	        return new Node<>(hash, key, value, next);
	    }
	    Node<K,V> replacementNode(Node<K,V> p, Node<K,V> next) {//创建一个指定内容并且指向指定的下一个节点的新节点
	        return new Node<>(p.hash, p.key, p.value, next);
	    }
	    TreeNode<K,V> newTreeNode(int hash, K key, V value, Node<K,V> next) {//返回一个新创建的红黑树节点
	        return new TreeNode<>(hash, key, value, next);
	    }
	    TreeNode<K,V> replacementTreeNode(Node<K,V> p, Node<K,V> next) {//根据传传入的节点，创建一个与新的红黑树节点
	        return new TreeNode<>(p.hash, p.key, p.value, next);
	    }
	    void reinitialize() {//初始化Map
	        table = null;
	        entrySet = null;
	        keySet = null;
	        values = null;
	        modCount = 0;
	        threshold = 0;
	        size = 0;
	    }
	    void afterNodeAccess(Node<K,V> p) { }
	    void afterNodeInsertion(boolean evict) { }
	    void afterNodeRemoval(Node<K,V> p) { }
	    //红黑树类
	    static final class TreeNode<K,V> extends LinkedHashMap.Entry<K,V> {
	        TreeNode<K,V> parent;  // 父亲结点
	        TreeNode<K,V> left;//左子节点
	        TreeNode<K,V> right;//右子节点
	        TreeNode<K,V> prev;  //前驱结点
	        boolean red;
	        TreeNode(int hash, K key, V val, Node<K,V> next) {
	            super(hash, key, val, next);
	        }
	        final TreeNode<K,V> root() {//返回根结点
	            for (TreeNode<K,V> r = this, p;;) {
	                if ((p = r.parent) == null)
	                    return r;
	                r = p;
	            }
	        }
	        /*红黑树暂未研究*/////
	        static <K,V> void moveRootToFront(Node<K,V>[] tab, TreeNode<K,V> root) {
	            int n;
	            if (root != null && tab != null && (n = tab.length) > 0) {
	                int index = (n - 1) & root.hash;
	                TreeNode<K,V> first = (TreeNode<K,V>)tab[index];
	                if (root != first) {
	                    Node<K,V> rn;
	                    tab[index] = root;
	                    TreeNode<K,V> rp = root.prev;
	                    if ((rn = root.next) != null)
	                        ((TreeNode<K,V>)rn).prev = rp;
	                    if (rp != null)
	                        rp.next = rn;
	                    if (first != null)
	                        first.prev = root;
	                    root.next = first;
	                    root.prev = null;
	                }
	                assert checkInvariants(root);
	            }
	        }
	        final TreeNode<K,V> find(int h, Object k, Class<?> kc) {
	            TreeNode<K,V> p = this;
	            do {
	                int ph, dir; K pk;
	                TreeNode<K,V> pl = p.left, pr = p.right, q;
	                if ((ph = p.hash) > h)
	                    p = pl;
	                else if (ph < h)
	                    p = pr;
	                else if ((pk = p.key) == k || (k != null && k.equals(pk)))
	                    return p;
	                else if (pl == null)
	                    p = pr;
	                else if (pr == null)
	                    p = pl;
	                else if ((kc != null ||
	                          (kc = comparableClassFor(k)) != null) &&
	                         (dir = compareComparables(kc, k, pk)) != 0)
	                    p = (dir < 0) ? pl : pr;
	                else if ((q = pr.find(h, k, kc)) != null)
	                    return q;
	                else
	                    p = pl;
	            } while (p != null);
	            return null;
	        }

	        /**
	         * Calls find for root node.
	         */
	        final TreeNode<K,V> getTreeNode(int h, Object k) {
	            return ((parent != null) ? root() : this).find(h, k, null);
	        }

	        /**
	         * Tie-breaking utility for ordering insertions when equal
	         * hashCodes and non-comparable. We don't require a total
	         * order, just a consistent insertion rule to maintain
	         * equivalence across rebalancings. Tie-breaking further than
	         * necessary simplifies testing a bit.
	         */
	        static int tieBreakOrder(Object a, Object b) {
	            int d;
	            if (a == null || b == null ||
	                (d = a.getClass().getName().
	                 compareTo(b.getClass().getName())) == 0)
	                d = (System.identityHashCode(a) <= System.identityHashCode(b) ?
	                     -1 : 1);
	            return d;
	        }

	        /**
	         * Forms tree of the nodes linked from this node.
	         * @return root of tree
	         */
	        final void treeify(Node<K,V>[] tab) {
	            TreeNode<K,V> root = null;
	            for (TreeNode<K,V> x = this, next; x != null; x = next) {
	                next = (TreeNode<K,V>)x.next;
	                x.left = x.right = null;
	                if (root == null) {
	                    x.parent = null;
	                    x.red = false;
	                    root = x;
	                }
	                else {
	                    K k = x.key;
	                    int h = x.hash;
	                    Class<?> kc = null;
	                    for (TreeNode<K,V> p = root;;) {
	                        int dir, ph;
	                        K pk = p.key;
	                        if ((ph = p.hash) > h)
	                            dir = -1;
	                        else if (ph < h)
	                            dir = 1;
	                        else if ((kc == null &&
	                                  (kc = comparableClassFor(k)) == null) ||
	                                 (dir = compareComparables(kc, k, pk)) == 0)
	                            dir = tieBreakOrder(k, pk);

	                        TreeNode<K,V> xp = p;
	                        if ((p = (dir <= 0) ? p.left : p.right) == null) {
	                            x.parent = xp;
	                            if (dir <= 0)
	                                xp.left = x;
	                            else
	                                xp.right = x;
	                            root = balanceInsertion(root, x);
	                            break;
	                        }
	                    }
	                }
	            }
	            moveRootToFront(tab, root);
	        }

	        /**
	         * Returns a list of non-TreeNodes replacing those linked from
	         * this node.
	         */
	        final Node<K,V> untreeify(HashMap<K,V> map) {
	            Node<K,V> hd = null, tl = null;
	            for (Node<K,V> q = this; q != null; q = q.next) {
	                Node<K,V> p = map.replacementNode(q, null);
	                if (tl == null)
	                    hd = p;
	                else
	                    tl.next = p;
	                tl = p;
	            }
	            return hd;
	        }

	        /**
	         * Tree version of putVal.
	         */
	        final TreeNode<K,V> putTreeVal(HashMap<K,V> map, Node<K,V>[] tab,
	                                       int h, K k, V v) {
	            Class<?> kc = null;
	            boolean searched = false;
	            TreeNode<K,V> root = (parent != null) ? root() : this;
	            for (TreeNode<K,V> p = root;;) {
	                int dir, ph; K pk;
	                if ((ph = p.hash) > h)
	                    dir = -1;
	                else if (ph < h)
	                    dir = 1;
	                else if ((pk = p.key) == k || (k != null && k.equals(pk)))
	                    return p;
	                else if ((kc == null &&
	                          (kc = comparableClassFor(k)) == null) ||
	                         (dir = compareComparables(kc, k, pk)) == 0) {
	                    if (!searched) {
	                        TreeNode<K,V> q, ch;
	                        searched = true;
	                        if (((ch = p.left) != null &&
	                             (q = ch.find(h, k, kc)) != null) ||
	                            ((ch = p.right) != null &&
	                             (q = ch.find(h, k, kc)) != null))
	                            return q;
	                    }
	                    dir = tieBreakOrder(k, pk);
	                }

	                TreeNode<K,V> xp = p;
	                if ((p = (dir <= 0) ? p.left : p.right) == null) {
	                    Node<K,V> xpn = xp.next;
	                    TreeNode<K,V> x = map.newTreeNode(h, k, v, xpn);
	                    if (dir <= 0)
	                        xp.left = x;
	                    else
	                        xp.right = x;
	                    xp.next = x;
	                    x.parent = x.prev = xp;
	                    if (xpn != null)
	                        ((TreeNode<K,V>)xpn).prev = x;
	                    moveRootToFront(tab, balanceInsertion(root, x));
	                    return null;
	                }
	            }
	        }

	        /**
	         * Removes the given node, that must be present before this call.
	         * This is messier than typical red-black deletion code because we
	         * cannot swap the contents of an interior node with a leaf
	         * successor that is pinned by "next" pointers that are accessible
	         * independently during traversal. So instead we swap the tree
	         * linkages. If the current tree appears to have too few nodes,
	         * the bin is converted back to a plain bin. (The test triggers
	         * somewhere between 2 and 6 nodes, depending on tree structure).
	         */
	        final void removeTreeNode(HashMap<K,V> map, Node<K,V>[] tab,
	                                  boolean movable) {
	            int n;
	            if (tab == null || (n = tab.length) == 0)
	                return;
	            int index = (n - 1) & hash;
	            TreeNode<K,V> first = (TreeNode<K,V>)tab[index], root = first, rl;
	            TreeNode<K,V> succ = (TreeNode<K,V>)next, pred = prev;
	            if (pred == null)
	                tab[index] = first = succ;
	            else
	                pred.next = succ;
	            if (succ != null)
	                succ.prev = pred;
	            if (first == null)
	                return;
	            if (root.parent != null)
	                root = root.root();
	            if (root == null || root.right == null ||
	                (rl = root.left) == null || rl.left == null) {
	                tab[index] = first.untreeify(map);  // too small
	                return;
	            }
	            TreeNode<K,V> p = this, pl = left, pr = right, replacement;
	            if (pl != null && pr != null) {
	                TreeNode<K,V> s = pr, sl;
	                while ((sl = s.left) != null) // find successor
	                    s = sl;
	                boolean c = s.red; s.red = p.red; p.red = c; // swap colors
	                TreeNode<K,V> sr = s.right;
	                TreeNode<K,V> pp = p.parent;
	                if (s == pr) { // p was s's direct parent
	                    p.parent = s;
	                    s.right = p;
	                }
	                else {
	                    TreeNode<K,V> sp = s.parent;
	                    if ((p.parent = sp) != null) {
	                        if (s == sp.left)
	                            sp.left = p;
	                        else
	                            sp.right = p;
	                    }
	                    if ((s.right = pr) != null)
	                        pr.parent = s;
	                }
	                p.left = null;
	                if ((p.right = sr) != null)
	                    sr.parent = p;
	                if ((s.left = pl) != null)
	                    pl.parent = s;
	                if ((s.parent = pp) == null)
	                    root = s;
	                else if (p == pp.left)
	                    pp.left = s;
	                else
	                    pp.right = s;
	                if (sr != null)
	                    replacement = sr;
	                else
	                    replacement = p;
	            }
	            else if (pl != null)
	                replacement = pl;
	            else if (pr != null)
	                replacement = pr;
	            else
	                replacement = p;
	            if (replacement != p) {
	                TreeNode<K,V> pp = replacement.parent = p.parent;
	                if (pp == null)
	                    root = replacement;
	                else if (p == pp.left)
	                    pp.left = replacement;
	                else
	                    pp.right = replacement;
	                p.left = p.right = p.parent = null;
	            }

	            TreeNode<K,V> r = p.red ? root : balanceDeletion(root, replacement);

	            if (replacement == p) {  // detach
	                TreeNode<K,V> pp = p.parent;
	                p.parent = null;
	                if (pp != null) {
	                    if (p == pp.left)
	                        pp.left = null;
	                    else if (p == pp.right)
	                        pp.right = null;
	                }
	            }
	            if (movable)
	                moveRootToFront(tab, r);
	        }

	        /**
	         * Splits nodes in a tree bin into lower and upper tree bins,
	         * or untreeifies if now too small. Called only from resize;
	         * see above discussion about split bits and indices.
	         *
	         * @param map the map
	         * @param tab the table for recording bin heads
	         * @param index the index of the table being split
	         * @param bit the bit of hash to split on
	         */
	        final void split(HashMap<K,V> map, Node<K,V>[] tab, int index, int bit) {
	            TreeNode<K,V> b = this;
	            // Relink into lo and hi lists, preserving order
	            TreeNode<K,V> loHead = null, loTail = null;
	            TreeNode<K,V> hiHead = null, hiTail = null;
	            int lc = 0, hc = 0;
	            for (TreeNode<K,V> e = b, next; e != null; e = next) {
	                next = (TreeNode<K,V>)e.next;
	                e.next = null;
	                if ((e.hash & bit) == 0) {
	                    if ((e.prev = loTail) == null)
	                        loHead = e;
	                    else
	                        loTail.next = e;
	                    loTail = e;
	                    ++lc;
	                }
	                else {
	                    if ((e.prev = hiTail) == null)
	                        hiHead = e;
	                    else
	                        hiTail.next = e;
	                    hiTail = e;
	                    ++hc;
	                }
	            }

	            if (loHead != null) {
	                if (lc <= UNTREEIFY_THRESHOLD)
	                    tab[index] = loHead.untreeify(map);
	                else {
	                    tab[index] = loHead;
	                    if (hiHead != null) // (else is already treeified)
	                        loHead.treeify(tab);
	                }
	            }
	            if (hiHead != null) {
	                if (hc <= UNTREEIFY_THRESHOLD)
	                    tab[index + bit] = hiHead.untreeify(map);
	                else {
	                    tab[index + bit] = hiHead;
	                    if (loHead != null)
	                        hiHead.treeify(tab);
	                }
	            }
	        }

	        /* ------------------------------------------------------------ */
	        // Red-black tree methods, all adapted from CLR

	        static <K,V> TreeNode<K,V> rotateLeft(TreeNode<K,V> root,
	                                              TreeNode<K,V> p) {
	            TreeNode<K,V> r, pp, rl;
	            if (p != null && (r = p.right) != null) {
	                if ((rl = p.right = r.left) != null)
	                    rl.parent = p;
	                if ((pp = r.parent = p.parent) == null)
	                    (root = r).red = false;
	                else if (pp.left == p)
	                    pp.left = r;
	                else
	                    pp.right = r;
	                r.left = p;
	                p.parent = r;
	            }
	            return root;
	        }

	        static <K,V> TreeNode<K,V> rotateRight(TreeNode<K,V> root,
	                                               TreeNode<K,V> p) {
	            TreeNode<K,V> l, pp, lr;
	            if (p != null && (l = p.left) != null) {
	                if ((lr = p.left = l.right) != null)
	                    lr.parent = p;
	                if ((pp = l.parent = p.parent) == null)
	                    (root = l).red = false;
	                else if (pp.right == p)
	                    pp.right = l;
	                else
	                    pp.left = l;
	                l.right = p;
	                p.parent = l;
	            }
	            return root;
	        }

	        static <K,V> TreeNode<K,V> balanceInsertion(TreeNode<K,V> root,
	                                                    TreeNode<K,V> x) {
	            x.red = true;
	            for (TreeNode<K,V> xp, xpp, xppl, xppr;;) {
	                if ((xp = x.parent) == null) {
	                    x.red = false;
	                    return x;
	                }
	                else if (!xp.red || (xpp = xp.parent) == null)
	                    return root;
	                if (xp == (xppl = xpp.left)) {
	                    if ((xppr = xpp.right) != null && xppr.red) {
	                        xppr.red = false;
	                        xp.red = false;
	                        xpp.red = true;
	                        x = xpp;
	                    }
	                    else {
	                        if (x == xp.right) {
	                            root = rotateLeft(root, x = xp);
	                            xpp = (xp = x.parent) == null ? null : xp.parent;
	                        }
	                        if (xp != null) {
	                            xp.red = false;
	                            if (xpp != null) {
	                                xpp.red = true;
	                                root = rotateRight(root, xpp);
	                            }
	                        }
	                    }
	                }
	                else {
	                    if (xppl != null && xppl.red) {
	                        xppl.red = false;
	                        xp.red = false;
	                        xpp.red = true;
	                        x = xpp;
	                    }
	                    else {
	                        if (x == xp.left) {
	                            root = rotateRight(root, x = xp);
	                            xpp = (xp = x.parent) == null ? null : xp.parent;
	                        }
	                        if (xp != null) {
	                            xp.red = false;
	                            if (xpp != null) {
	                                xpp.red = true;
	                                root = rotateLeft(root, xpp);
	                            }
	                        }
	                    }
	                }
	            }
	        }

	        static <K,V> TreeNode<K,V> balanceDeletion(TreeNode<K,V> root,
	                                                   TreeNode<K,V> x) {
	            for (TreeNode<K,V> xp, xpl, xpr;;)  {
	                if (x == null || x == root)
	                    return root;
	                else if ((xp = x.parent) == null) {
	                    x.red = false;
	                    return x;
	                }
	                else if (x.red) {
	                    x.red = false;
	                    return root;
	                }
	                else if ((xpl = xp.left) == x) {
	                    if ((xpr = xp.right) != null && xpr.red) {
	                        xpr.red = false;
	                        xp.red = true;
	                        root = rotateLeft(root, xp);
	                        xpr = (xp = x.parent) == null ? null : xp.right;
	                    }
	                    if (xpr == null)
	                        x = xp;
	                    else {
	                        TreeNode<K,V> sl = xpr.left, sr = xpr.right;
	                        if ((sr == null || !sr.red) &&
	                            (sl == null || !sl.red)) {
	                            xpr.red = true;
	                            x = xp;
	                        }
	                        else {
	                            if (sr == null || !sr.red) {
	                                if (sl != null)
	                                    sl.red = false;
	                                xpr.red = true;
	                                root = rotateRight(root, xpr);
	                                xpr = (xp = x.parent) == null ?
	                                    null : xp.right;
	                            }
	                            if (xpr != null) {
	                                xpr.red = (xp == null) ? false : xp.red;
	                                if ((sr = xpr.right) != null)
	                                    sr.red = false;
	                            }
	                            if (xp != null) {
	                                xp.red = false;
	                                root = rotateLeft(root, xp);
	                            }
	                            x = root;
	                        }
	                    }
	                }
	                else { // symmetric
	                    if (xpl != null && xpl.red) {
	                        xpl.red = false;
	                        xp.red = true;
	                        root = rotateRight(root, xp);
	                        xpl = (xp = x.parent) == null ? null : xp.left;
	                    }
	                    if (xpl == null)
	                        x = xp;
	                    else {
	                        TreeNode<K,V> sl = xpl.left, sr = xpl.right;
	                        if ((sl == null || !sl.red) &&
	                            (sr == null || !sr.red)) {
	                            xpl.red = true;
	                            x = xp;
	                        }
	                        else {
	                            if (sl == null || !sl.red) {
	                                if (sr != null)
	                                    sr.red = false;
	                                xpl.red = true;
	                                root = rotateLeft(root, xpl);
	                                xpl = (xp = x.parent) == null ?
	                                    null : xp.left;
	                            }
	                            if (xpl != null) {
	                                xpl.red = (xp == null) ? false : xp.red;
	                                if ((sl = xpl.left) != null)
	                                    sl.red = false;
	                            }
	                            if (xp != null) {
	                                xp.red = false;
	                                root = rotateRight(root, xp);
	                            }
	                            x = root;
	                        }
	                    }
	                }
	            }
	        }

	        /**
	         * Recursive invariant check
	         */
	        static <K,V> boolean checkInvariants(TreeNode<K,V> t) {
	            TreeNode<K,V> tp = t.parent, tl = t.left, tr = t.right,
	                tb = t.prev, tn = (TreeNode<K,V>)t.next;
	            if (tb != null && tb.next != t)
	                return false;
	            if (tn != null && tn.prev != t)
	                return false;
	            if (tp != null && t != tp.left && t != tp.right)
	                return false;
	            if (tl != null && (tl.parent != t || tl.hash > t.hash))
	                return false;
	            if (tr != null && (tr.parent != t || tr.hash < t.hash))
	                return false;
	            if (t.red && tl != null && tl.red && tr != null && tr.red)
	                return false;
	            if (tl != null && !checkInvariants(tl))
	                return false;
	            if (tr != null && !checkInvariants(tr))
	                return false;
	            return true;
	        }
	    }

}
