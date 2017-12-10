package com.liu.Map;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
public class TreeMap<K,V>
extends AbstractMap<K,V>
implements NavigableMap<K,V>, Cloneable, java.io.Serializable
{
	//比较器，因为TreeMap是有序的，通过comparator接口我们可以对TreeMap的内部排序进行精密的控制
	 private final Comparator<? super K> comparator;
	//TreeMap红-黑节点，为TreeMap的内部类
	 private transient Entry<K,V> root;
	 //容器大小
	 private transient int size = 0;
	//TreeMap修改次数
	 private transient int modCount = 0;
	 //红黑书颜色，红色
	 private static final boolean RED   = false;
	 //红黑色黑色
	 private static final boolean BLACK = true;
	 //无参构造，comparator=null,默认按照自然顺序排序
	 public TreeMap() {
	        comparator = null;
	    }
	 //设置自定义比较器的构造函数
	    public TreeMap(Comparator<? super K> comparator) {
	        this.comparator = comparator;
	    }
	    //指定Map的构造
	    public TreeMap(Map<? extends K, ? extends V> m) {
	        comparator = null;
	        putAll(m);
	    }
	  //  构造已知的SortedMap对象为TreeMap
	    public TreeMap(SortedMap<K, ? extends V> m) {
	        comparator = m.comparator();
	        try {
	            buildFromSorted(m.size(), m.entrySet().iterator(), null, null);
	        } catch (java.io.IOException cannotHappen) {
	        } catch (ClassNotFoundException cannotHappen) {
	        }
	    }
	    public int size() {//获取大小
	        return size;
	    }
	    public boolean containsKey(Object key) {//判断key是否存在
	        return getEntry(key) != null;
	    }
	    public boolean containsValue(Object value) {//判断值是否存在
	        for (Entry<K,V> e = getFirstEntry(); e != null; e = successor(e))//遍历红黑书，successor代表后继结点
	            if (valEquals(value, e.value))//通过对比值进行判断
	                return true;
	        return false;
	    }
	    public V get(Object key) {//key获值
	        Entry<K,V> p = getEntry(key);
	        return (p==null ? null : p.value);
	    }
	    public Comparator<? super K> comparator() {//获取比较器
	        return comparator;
	    }
	    public K firstKey() {//获取第一个key
	        return key(getFirstEntry());
	    }
	    public K lastKey() {//最后一个key
	        return key(getLastEntry());
	    }
	    public void putAll(Map<? extends K, ? extends V> map) {//将map所有存放到此Map中
	        int mapSize = map.size();
	        if (size==0 && mapSize!=0 && map instanceof SortedMap) {
	            Comparator<?> c = ((SortedMap<?,?>)map).comparator();
	            if (c == comparator || (c != null && c.equals(comparator))) {
	                ++modCount;
	                try {
	                    buildFromSorted(mapSize, map.entrySet().iterator(),
	                                    null, null);
	                } catch (java.io.IOException cannotHappen) {
	                } catch (ClassNotFoundException cannotHappen) {
	                }
	                return;
	            }
	        }
	        super.putAll(map);
	    }
	  //根据指定的key获取节点
	    final Entry<K,V> getEntry(Object key) {//
	        // Offload comparator-based version for sake of performance
	        if (comparator != null)
	        	//有比较器
	            return getEntryUsingComparator(key);
	        if (key == null)
	            throw new NullPointerException();
	        @SuppressWarnings("unchecked")
	            Comparable<? super K> k = (Comparable<? super K>) key;//没有比较器，key需要实现Comparable接口
	        Entry<K,V> p = root;//获取root节点
	      //循环找k
	        while (p != null) {
	            int cmp = k.compareTo(p.key);    //从p节点开始比较，
	            if (cmp < 0)
	                p = p.left; //如果当前节点的key，比p节点的key小，移动到左孩子
	            else if (cmp > 0)
	                p = p.right;//如果当前节点的key，比p节点的key大，移动到右孩子
	            else
	                return p;  //如果相等，返回p。
	        }
	        return null;
	    }
	    //通过参数自带的比较器进行获取实体
	    final Entry<K,V> getEntryUsingComparator(Object key) {
	        @SuppressWarnings("unchecked")
	            K k = (K) key;
	        //获取比较器
	        Comparator<? super K> cpr = comparator;
	        if (cpr != null) {
	        	 //获取root节点
	            Entry<K,V> p = root;
	            while (p != null) {
	            	 //从p节点开始比较，
	                int cmp = cpr.compare(k, p.key);
	                //如果当前节点的key，比p节点的key小，移动到左孩子
	                if (cmp < 0)
	                    p = p.left;
	              //如果当前节点的key，比p节点的key大，移动到右孩子
	                else if (cmp > 0)
	                    p = p.right;
	                //如果相等
	                else
	                	//返回p
	                    return p;
	            }
	        }
	        return null;
	    }
	  //获取TreeMap中大于或等于key的最小的节点；  
	  //若不存在(即TreeMap中所有节点的键都比key大)，就返回null  
	    final Entry<K,V> getCeilingEntry(K key) {
	        Entry<K,V> p = root;
	        while (p != null) {
	            int cmp = compare(key, p.key);
	            if (cmp < 0) {//情况1. 若p.key > key  
	                if (p.left != null) //若p有左子节点  
	                    p = p.left;//往左下走 
	                else
	                    return p;//否则返回p
	            } else if (cmp > 0) { //情况2：p.key < key  
	                if (p.right != null) {
	                    p = p.right;
	                } else {
	                	 // 若 p 不存在右孩子，则找出 p 的后继节点，并返回  
	                    // 注意：这里返回的 “p的后继节点”有2种可能性：第一，null；第二，TreeMap中大于key的最小的节点。  
	                    // 理解这一点的核心是，getCeilingEntry是从root开始遍历的。  
	                    // 若getCeilingEntry能走到这一步，那么，它之前“已经遍历过的节点的key”都 > key。  
	                    // 能理解上面所说的，那么就很容易明白，为什么“p的后继节点”又2种可能性了。  
	                    Entry<K,V> parent = p.parent;
	                    Entry<K,V> ch = p;
	                    while (parent != null && ch == parent.right) {
	                        ch = parent;
	                        parent = parent.parent;
	                    }
	                    return parent;
	                }
	            } else
	                return p;//情况3：p.key = key  
	        }
	        return null;
	    }
	 // 获取TreeMap中小于或等于key的最大的节点；  
	 // 若不存在(即TreeMap中所有节点的键都比key小)，就返回null  
	 // getFloorEntry的原理和getCeilingEntry类似，这里不再多说。  
	    final Entry<K,V> getFloorEntry(K key) {
	        Entry<K,V> p = root;
	        while (p != null) {
	            int cmp = compare(key, p.key);
	            if (cmp > 0) {
	                if (p.right != null)
	                    p = p.right;
	                else
	                    return p;
	            } else if (cmp < 0) {
	                if (p.left != null) {
	                    p = p.left;
	                } else {
	                    Entry<K,V> parent = p.parent;
	                    Entry<K,V> ch = p;
	                    while (parent != null && ch == parent.left) {
	                        ch = parent;
	                        parent = parent.parent;
	                    }
	                    return parent;
	                }
	            } else
	                return p;

	        }
	        return null;
	    }
	 // 获取TreeMap中大于key的最小的节点。  
	 // 若不存在，就返回null。  
	 // 请参照getCeilingEntry来对getHigherEntry进行理解。  
	    final Entry<K,V> getHigherEntry(K key) {
	        Entry<K,V> p = root;
	        while (p != null) {
	            int cmp = compare(key, p.key);
	            if (cmp < 0) {
	                if (p.left != null)//返回最小节点
	                    p = p.left;
	                else
	                    return p;
	            } else {
	                if (p.right != null) {
	                    p = p.right;
	                } else {//不存在右节点，像父亲节点查询右节点。
	                    Entry<K,V> parent = p.parent;
	                    Entry<K,V> ch = p;
	                    while (parent != null && ch == parent.right) {
	                        ch = parent;
	                        parent = parent.parent;
	                    }
	                    return parent;//返回父节点
	                } 
	            }
	        }
	        return null;
	    }
	 // 获取TreeMap中小于key的最大的节点。  
	 // 若不存在，就返回null。  
	 // 请参照getfloorEntry来对getlowerEntry进行理解。
	    final Entry<K,V> getLowerEntry(K key) {
	        Entry<K,V> p = root;
	        while (p != null) {
	            int cmp = compare(key, p.key);
	            if (cmp > 0) {
	                if (p.right != null)
	                    p = p.right;
	                else
	                    return p;
	            } else {
	                if (p.left != null) {
	                    p = p.left;
	                } else {
	                    Entry<K,V> parent = p.parent;
	                    Entry<K,V> ch = p;
	                    while (parent != null && ch == parent.left) {
	                        ch = parent;
	                        parent = parent.parent;
	                    }
	                    return parent;
	                }
	            }
	        }
	        return null;
	    }
	    //添加实体
	    public V put(K key, V value) {
	    	//或群根节点
	        Entry<K,V> t = root;
	      //如果根节点为空，该元素设置为root
	        if (t == null) {
	            compare(key, key); 
	            root = new Entry<>(key, value, null);//将添加实体设为根节点
	            size = 1;
	            modCount++;
	            return null;
	        }
	        int cmp;
	        Entry<K,V> parent;
	        // split comparator and comparable paths
	        Comparator<? super K> cpr = comparator;
	        //比较器不为空
	        if (cpr != null) {
	        	 //循环比较并确定元素插入的位置(找父亲节点)
	            do {
	            	//记录根节点
	                parent = t;
	                //将当前节点和根节点元素比较
	                cmp = cpr.compare(key, t.key);
	                //待插入key小于当前元素key，查找左边
	                if (cmp < 0)
	                    t = t.left;
	                //待插入key大于当前元素key，查找右边
	                else if (cmp > 0)
	                    t = t.right;
	                //相等，替换
	                else
	                    return t.setValue(value);
	            } while (t != null);
	        }
	        //比较器为null
	        else {
	        	//TreeMap元素，key不能为null
	            if (key == null)
	                throw new NullPointerException();
	            @SuppressWarnings("unchecked")
	            //key需要实现Comparable接口
	                Comparable<? super K> k = (Comparable<? super K>) key;
	          //循环比较并确定元素插入的位置
	            do {
	                parent = t;
	                cmp = k.compareTo(t.key);
	                if (cmp < 0)
	                    t = t.left;
	                else if (cmp > 0)
	                    t = t.right;
	                else
	                    return t.setValue(value);
	            } while (t != null);
	        }
	        //找到父亲节点，根据父亲节点创建一个新节点
	        Entry<K,V> e = new Entry<>(key, value, parent);
	        //如果待插入元素的key值小于父节点的key值，父节点左边插入
	        if (cmp < 0)
	            parent.left = e;
	        //如果待插入元素的key值大于父节点的key值，父节点右边插入
	        else
	            parent.right = e;
	      //对红黑树进行重新平衡
	        fixAfterInsertion(e);
	        size++;
	        modCount++;
	        return null;
	    }
	    //移除操作
	    public V remove(Object key) {
	        Entry<K,V> p = getEntry(key);//获取指定key实体
	        if (p == null)
	            return null;

	        V oldValue = p.value;
	        deleteEntry(p);//删除实体操作
	        return oldValue;//返回删除值
	    }
	    //清空操作
	    public void clear() {
	        modCount++;
	        size = 0;
	        root = null;
	    }
	    public Object clone() {
	        TreeMap<?,?> clone;
	        try {
	            clone = (TreeMap<?,?>) super.clone();
	        } catch (CloneNotSupportedException e) {
	            throw new InternalError(e);
	        }

	        // Put clone into "virgin" state (except for comparator)
	        clone.root = null;
	        clone.size = 0;
	        clone.modCount = 0;
	        clone.entrySet = null;
	        clone.navigableKeySet = null;
	        clone.descendingMap = null;

	        // Initialize clone with our mappings
	        try {
	            clone.buildFromSorted(size, entrySet().iterator(), null, null);
	        } catch (java.io.IOException cannotHappen) {
	        } catch (ClassNotFoundException cannotHappen) {
	        }

	        return clone;
	    }
	    //第一个实体
	    public Map.Entry<K,V> firstEntry() {
	        return exportEntry(getFirstEntry());
	    }
	    //最后一个实体
	    public Map.Entry<K,V> lastEntry() {
	        return exportEntry(getLastEntry());
	    }
	    //弹出第一个实体，即删除
	    public Map.Entry<K,V> pollFirstEntry() {
	        Entry<K,V> p = getFirstEntry();//获取第一个实体
	        Map.Entry<K,V> result = exportEntry(p);
	        if (p != null)
	            deleteEntry(p);//删除实体
	        return result;
	    }
	    //弹出最后一个实体，删除
	    public Map.Entry<K,V> pollLastEntry() {
	        Entry<K,V> p = getLastEntry();//获取最后一个实体
	        Map.Entry<K,V> result = exportEntry(p);
	        if (p != null)
	            deleteEntry(p);//删除
	        return result;
	    }
	    //小于key的最大实体
	    public Map.Entry<K,V> lowerEntry(K key) {
	        return exportEntry(getLowerEntry(key));
	    }
	    //小于key的最大最大key
	    public K lowerKey(K key) {
	        return keyOrNull(getLowerEntry(key));
	    }
	    //小于key的最大（包含自己）实体
	    public Map.Entry<K,V> floorEntry(K key) {
	        return exportEntry(getFloorEntry(key));
	    }
	    //于key的最大（包含自己）的key
	    public K floorKey(K key) {
	        return keyOrNull(getFloorEntry(key));
	    }
	    //大于指定key的最小实体（包含自己）
	    public Map.Entry<K,V> ceilingEntry(K key) {
	        return exportEntry(getCeilingEntry(key));
	    }
	    //大于指定key的最小key（包含自己）
	    public K ceilingKey(K key) {
	        return keyOrNull(getCeilingEntry(key));
	    }
	    //大于指定key的最小实体
	    public Map.Entry<K,V> higherEntry(K key) {
	        return exportEntry(getHigherEntry(key));
	    }
	  //大于指定key的最小key
	    public K higherKey(K key) {
	        return keyOrNull(getHigherEntry(key));
	    }
	    private transient EntrySet entrySet;
	    private transient KeySet<K> navigableKeySet;
	    private transient NavigableMap<K,V> descendingMap;//逆序Map
	    //key的Set集合
	    public Set<K> keySet() {
	        return navigableKeySet();
	    }
	    //可通行的key的Set集合，比keySet多些功能
	    public NavigableSet<K> navigableKeySet() {
	        KeySet<K> nks = navigableKeySet;
	        return (nks != null) ? nks : (navigableKeySet = new KeySet<>(this));
	    }
	    //逆序keySet
	    public NavigableSet<K> descendingKeySet() {
	        return descendingMap().navigableKeySet();
	    }
	    //所有值的集合
	    public Collection<V> values() {
	        Collection<V> vs = values;
	        if (vs == null) {
	            vs = new Values();
	            values = vs;
	        }
	        return vs;
	    }
	    //所有实体的Set集合
	    public Set<Map.Entry<K,V>> entrySet() {
	        EntrySet es = entrySet;
	        return (es != null) ? es : (entrySet = new EntrySet());
	    }
	    //逆序NavigableMap
	    public NavigableMap<K, V> descendingMap() {
	        NavigableMap<K, V> km = descendingMap;
	        return (km != null) ? km :
	            (descendingMap = new DescendingSubMap<>(this,
	                                                    true, null, true,
	                                                    true, null, true));
	    }
	    //返回[fromkey,tokey)的NavigableMap集合
	    public NavigableMap<K,V> subMap(K fromKey, boolean fromInclusive,
                K toKey,   boolean toInclusive) {
return new AscendingSubMap<>(this,
                 false, fromKey, fromInclusive,
                 false, toKey,   toInclusive);
}
	    //返回tokey前面的NavigableMap集合，不包含自己
	    public NavigableMap<K,V> headMap(K toKey, boolean inclusive) {
	        return new AscendingSubMap<>(this,
	                                     true,  null,  true,
	                                     false, toKey, inclusive);
	    }
	    //返回fromkey后面的NavigableMap集合，包含自己
	    public NavigableMap<K,V> tailMap(K fromKey, boolean inclusive) {
	        return new AscendingSubMap<>(this,
	                                     false, fromKey, inclusive,
	                                     true,  null,    true);
	    }
	    //返回[fromkey,tokey)的SortedMap集合
	    public SortedMap<K,V> subMap(K fromKey, K toKey) {
	        return subMap(fromKey, true, toKey, false);
	    }
	    //返回tokey前面的SortedMap集合，不包含自己
	    public SortedMap<K,V> headMap(K toKey) {
	        return headMap(toKey, false);
	    }
	  //返回fromkey后面的SortedMap集合，包含自己
	    public SortedMap<K,V> tailMap(K fromKey) {
	        return tailMap(fromKey, true);
	    }
	    //替换指定key和指定的值的值
	    @Override
	    public boolean replace(K key, V oldValue, V newValue) {
	        Entry<K,V> p = getEntry(key);//获取key
	        if (p!=null && Objects.equals(oldValue, p.value)) {//判断老的值与原来额值是否相等
	            p.value = newValue;//替换
	            return true;
	        }
	        return false;
	    }
	    //将指定的key的值替换为指定的值
	    @Override
	    public V replace(K key, V value) {
	        Entry<K,V> p = getEntry(key);
	        if (p!=null) {
	            V oldValue = p.value;
	            p.value = value;
	            return oldValue;
	        }
	        return null;
	    }
	    @Override
	    public void forEach(BiConsumer<? super K, ? super V> action) {
	        Objects.requireNonNull(action);
	        int expectedModCount = modCount;
	        for (Entry<K, V> e = getFirstEntry(); e != null; e = successor(e)) {
	            action.accept(e.key, e.value);

	            if (expectedModCount != modCount) {
	                throw new ConcurrentModificationException();
	            }
	        }
	    }
	    @Override
	    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
	        Objects.requireNonNull(function);
	        int expectedModCount = modCount;

	        for (Entry<K, V> e = getFirstEntry(); e != null; e = successor(e)) {
	            e.value = function.apply(e.key, e.value);

	            if (expectedModCount != modCount) {
	                throw new ConcurrentModificationException();
	            }
	        }
	    }
	    //获取所有值后的方法
	    class Values extends AbstractCollection<V> {
	        public Iterator<V> iterator() {//迭代器
	            return new ValueIterator(getFirstEntry());//返回一个值得迭代器
	        }

	        public int size() {//大小
	            return TreeMap.this.size();
	        }

	        public boolean contains(Object o) {//判断存在
	            return TreeMap.this.containsValue(o);
	        }

	        public boolean remove(Object o) {//移除
	            for (Entry<K,V> e = getFirstEntry(); e != null; e = successor(e)) {
	                if (valEquals(e.getValue(), o)) {
	                    deleteEntry(e);
	                    return true;
	                }
	            }
	            return false;
	        }

	        public void clear() {
	            TreeMap.this.clear();
	        }

	        public Spliterator<V> spliterator() {
	            return new ValueSpliterator<K,V>(TreeMap.this, null, null, 0, -1, 0);
	        }
	    }
	    //或有所有实体集的Set后的方法
	    class EntrySet extends AbstractSet<Map.Entry<K,V>> {
	        public Iterator<Map.Entry<K,V>> iterator() {
	            return new EntryIterator(getFirstEntry());
	        }
	        //重写contains方法，判断是否相等
	        public boolean contains(Object o) {
	            if (!(o instanceof Map.Entry))
	                return false;
	            Map.Entry<?,?> entry = (Map.Entry<?,?>) o;
	            Object value = entry.getValue();
	            Entry<K,V> p = getEntry(entry.getKey());
	            return p != null && valEquals(p.getValue(), value);
	        }
	        //移除指定的对象
	        public boolean remove(Object o) {
	            if (!(o instanceof Map.Entry))
	                return false;
	            Map.Entry<?,?> entry = (Map.Entry<?,?>) o;
	            Object value = entry.getValue();
	            Entry<K,V> p = getEntry(entry.getKey());
	            if (p != null && valEquals(p.getValue(), value)) {
	                deleteEntry(p);
	                return true;
	            }
	            return false;
	        }

	        public int size() {
	            return TreeMap.this.size();
	        }

	        public void clear() {
	            TreeMap.this.clear();
	        }

	        public Spliterator<Map.Entry<K,V>> spliterator() {
	            return new EntrySpliterator<K,V>(TreeMap.this, null, null, 0, -1, 0);
	        }
	    }
	    //key的迭代器
	    Iterator<K> keyIterator() {
	        return new KeyIterator(getFirstEntry());
	    }
	    //递减key迭代器
	    Iterator<K> descendingKeyIterator() {
	        return new DescendingKeyIterator(getLastEntry());
	    }
	    //KeySet实现类
	    static final class KeySet<E> extends AbstractSet<E> implements NavigableSet<E> {
	        private final NavigableMap<E, ?> m;
	        KeySet(NavigableMap<E,?> map) { m = map; }//传入Map到此Set中

	        public Iterator<E> iterator() {//key迭代器
	            if (m instanceof TreeMap)//如果传入的是TreeMap类型调用keyIterator函数，即返回一个key迭代器
	                return ((TreeMap<E,?>)m).keyIterator();
	            else
	                return ((TreeMap.NavigableSubMap<E,?>)m).keyIterator();
	        }

	        public Iterator<E> descendingIterator() {//递减迭代器，用于递减的方法进行迭代
	            if (m instanceof TreeMap)
	                return ((TreeMap<E,?>)m).descendingKeyIterator();
	            else
	                return ((TreeMap.NavigableSubMap<E,?>)m).descendingKeyIterator();
	        }

	        public int size() { return m.size(); }//大小
	        public boolean isEmpty() { return m.isEmpty(); }//判断是否为空
	        public boolean contains(Object o) { return m.containsKey(o); }//是否包含对象o
	        public void clear() { m.clear(); }//清楚所有
	        public E lower(E e) { return m.lowerKey(e); }//获取比e小的最大的key
	        public E floor(E e) { return m.floorKey(e); }//获取比e小或等于的key
	        public E ceiling(E e) { return m.ceilingKey(e); }//获取大于或等于的key
	        public E higher(E e) { return m.higherKey(e); }//获取大于的key
	        public E first() { return m.firstKey(); }//获取第一个key
	        public E last() { return m.lastKey(); }//获取最后一个key
	        public Comparator<? super E> comparator() { return m.comparator(); }//获取比较器
	        public E pollFirst() {//去除第一个实体并返回它的key
	            Map.Entry<E,?> e = m.pollFirstEntry();
	            return (e == null) ? null : e.getKey();
	        }
	        public E pollLast() {//去除最后一个实体并返回key
	            Map.Entry<E,?> e = m.pollLastEntry();
	            return (e == null) ? null : e.getKey();
	        }
	        public boolean remove(Object o) {//移除指定对象
	            int oldSize = size();
	            m.remove(o);
	            return size() != oldSize;
	        }
	        //截取keySet,截取为fromElement到toElement,大小为[fromElement,toElement)
	        public NavigableSet<E> subSet(E fromElement, boolean fromInclusive,
	                                      E toElement,   boolean toInclusive) {
	            return new KeySet<>(m.subMap(fromElement, fromInclusive,
	                                          toElement,   toInclusive));
	        }
	        public NavigableSet<E> headSet(E toElement, boolean inclusive) {
	            return new KeySet<>(m.headMap(toElement, inclusive));
	        }
	        public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
	            return new KeySet<>(m.tailMap(fromElement, inclusive));
	        }
	        //截取keySet,截取为fromElement到toElement,大小为[fromElement,toElement)
	        public SortedSet<E> subSet(E fromElement, E toElement) {
	            return subSet(fromElement, true, toElement, false);
	        }
	        public SortedSet<E> headSet(E toElement) {
	            return headSet(toElement, false);
	        }
	        public SortedSet<E> tailSet(E fromElement) {
	            return tailSet(fromElement, true);
	        }
	        public NavigableSet<E> descendingSet() {//返回一个递减Set
	            return new KeySet<>(m.descendingMap());
	        }

	        public Spliterator<E> spliterator() {
	            return keySpliteratorFor(m);
	        }
	    }
	    //私有实体迭代器类
	    abstract class PrivateEntryIterator<T> implements Iterator<T> {
	        Entry<K,V> next;//下一个节点
	        Entry<K,V> lastReturned;//最后操作的实体
	        int expectedModCount;

	        PrivateEntryIterator(Entry<K,V> first) {//构造函数，用来定义迭代器初始位置
	            expectedModCount = modCount;
	            lastReturned = null;
	            next = first;
	        }

	        public final boolean hasNext() {//判断是否还有下一个
	            return next != null;
	        }

	        final Entry<K,V> nextEntry() {//下一个实体
	            Entry<K,V> e = next;
	            if (e == null)
	                throw new NoSuchElementException();
	            if (modCount != expectedModCount)
	                throw new ConcurrentModificationException();
	            next = successor(e);//next变为后继节点
	            lastReturned = e;
	            return e;
	        }

	        final Entry<K,V> prevEntry() {//前一个实体
	            Entry<K,V> e = next;
	            if (e == null)
	                throw new NoSuchElementException();
	            if (modCount != expectedModCount)
	                throw new ConcurrentModificationException();
	            next = predecessor(e);//next变为前驱节点
	            lastReturned = e;
	            return e;
	        }

	        public void remove() {//删除最后操作的实体
	            if (lastReturned == null)
	                throw new IllegalStateException();
	            if (modCount != expectedModCount)
	                throw new ConcurrentModificationException();
	            // deleted entries are replaced by their successors
	            if (lastReturned.left != null && lastReturned.right != null)
	                next = lastReturned;
	            deleteEntry(lastReturned);
	            expectedModCount = modCount;
	            lastReturned = null;
	        }
	    }
	    //实体迭代器实现类
	    final class EntryIterator extends PrivateEntryIterator<Map.Entry<K,V>> {
	        EntryIterator(Entry<K,V> first) {
	            super(first);
	        }
	        public Map.Entry<K,V> next() {
	            return nextEntry();
	        }
	    }
	    //值得迭代器
	    final class ValueIterator extends PrivateEntryIterator<V> {
	        ValueIterator(Entry<K,V> first) {
	            super(first);
	        }
	        public V next() {
	            return nextEntry().value;
	        }
	    }
	    //key的迭代器类2
	    final class KeyIterator extends PrivateEntryIterator<K> {
	        KeyIterator(Entry<K,V> first) {
	            super(first);
	        }
	        public K next() {
	            return nextEntry().key;
	        }
	    }
	    //降序迭键迭代器
	    final class DescendingKeyIterator extends PrivateEntryIterator<K> {
	        DescendingKeyIterator(Entry<K,V> first) {
	            super(first);
	        }
	        public K next() {//像前进行
	            return prevEntry().key;
	        }
	        public void remove() {
	            if (lastReturned == null)
	                throw new IllegalStateException();
	            if (modCount != expectedModCount)
	                throw new ConcurrentModificationException();
	            deleteEntry(lastReturned);
	            lastReturned = null;
	            expectedModCount = modCount;
	        }
	    }
	    
	    //比较，用于对俩个对象进行比较，如果未制定比较器，则实现比较器接口比较，否则用指定的比较器进行比较
	    final int compare(Object k1, Object k2) {
	        return comparator==null ? ((Comparable<? super K>)k1).compareTo((K)k2)
	            : comparator.compare((K)k1, (K)k2);
	    }
	    //比较俩个对象是否相似（同一个对象，可以不同的引用）
	    static final boolean valEquals(Object o1, Object o2) {
	        return (o1==null ? o2==null : o1.equals(o2));
	    }
	    //防止用于修改返回的Entry,包装Entry
	    static <K,V> Map.Entry<K,V> exportEntry(TreeMap.Entry<K,V> e) {
	        return (e == null) ? null :
	            new AbstractMap.SimpleImmutableEntry<>(e);//新建一个类，里面的setValue的方法被静止，所以不会被修改
	    }
	    //实体不为空返回键
	    static <K,V> K keyOrNull(TreeMap.Entry<K,V> e) {
	        return (e == null) ? null : e.key;
	    }
	    //返回键
	    static <K> K key(Entry<K,?> e) {
	        if (e==null)
	            throw new NoSuchElementException();
	        return e.key;
	    }
	    //红黑树实体
	    static final class Entry<K,V> implements Map.Entry<K,V> {
	        K key;
	        V value;
	        Entry<K,V> left;//左子节点 
	        Entry<K,V> right;//右子节点 
	        Entry<K,V> parent;//父节点  
	        boolean color = BLACK;//树的颜色，默认为黑色 
	        Entry(K key, V value, Entry<K,V> parent) {//构造方法  
	            this.key = key;
	            this.value = value;
	            this.parent = parent;
	        }

	        public K getKey() {//获得key  
	            return key;
	        }

	        public V getValue() {//获得value  
	            return value;
	        }
	        public V setValue(V value) { //设置value 
	            V oldValue = this.value;
	            this.value = value;
	            return oldValue;
	        }

	        public boolean equals(Object o) {//key和value均相等才返回true  
	            if (!(o instanceof Map.Entry))
	                return false;
	            Map.Entry<?,?> e = (Map.Entry<?,?>)o;

	            return valEquals(key,e.getKey()) && valEquals(value,e.getValue());
	        }

	        public int hashCode() {//计算hashCode  
	            int keyHash = (key==null ? 0 : key.hashCode());
	            int valueHash = (value==null ? 0 : value.hashCode());
	            return keyHash ^ valueHash;
	        }

	        public String toString() {//重写toString方法  
	            return key + "=" + value;
	        }
	    }
	    final Entry<K,V> getFirstEntry() {//获得TreeMap里第一个节点(即根据key排序最小的节点)，如果TreeMap为空，返回null  
	        Entry<K,V> p = root;
	        if (p != null)
	            while (p.left != null)
	                p = p.left;
	        return p;
	    }
	    //获取最后一个实体
	    final Entry<K,V> getLastEntry() {
	        Entry<K,V> p = root;
	        if (p != null)
	            while (p.right != null)
	                p = p.right;
	        return p;
	    }
	    //获取后继节点
	    static <K,V> TreeMap.Entry<K,V> successor(Entry<K,V> t) {
	        if (t == null)
	            return null;
	        else if (t.right != null) {//右节点不为空时
	            Entry<K,V> p = t.right;//获取右节点
	            while (p.left != null)//遍历有节点的所有左子节点，即寻找比他大的节点的最小节点
	                p = p.left;
	            return p;
	        } else {//如果不存在右节点
	            Entry<K,V> p = t.parent;//获取父节点
	            Entry<K,V> ch = t;
	            while (p != null && ch == p.right) {//如果原节点不为父节左节点的话，继续寻找父亲节点
	                ch = p;
	                p = p.parent;
	            }
	            return p;//返回找到的节点
	        }
	    }
	    //获取前驱节点
	    static <K,V> Entry<K,V> predecessor(Entry<K,V> t) {//与前面的刚好相反
	        if (t == null)
	            return null;
	        else if (t.left != null) {
	            Entry<K,V> p = t.left;
	            while (p.right != null)
	                p = p.right;
	            return p;
	        } else {
	            Entry<K,V> p = t.parent;
	            Entry<K,V> ch = t;
	            while (p != null && ch == p.left) {
	                ch = p;
	                p = p.parent;
	            }
	            return p;
	        }
	    }
	    private static <K,V> boolean colorOf(Entry<K,V> p) {//获取实体的颜色，如果实体为空则默认为黑色
	        return (p == null ? BLACK : p.color);
	    }
	    private static <K,V> Entry<K,V> parentOf(Entry<K,V> p) {//获取父亲节点
	        return (p == null ? null: p.parent);
	    }

	    private static <K,V> void setColor(Entry<K,V> p, boolean c) {//设置实体颜色
	        if (p != null)
	            p.color = c;
	    }

	    private static <K,V> Entry<K,V> leftOf(Entry<K,V> p) {//获取左孩子节点
	        return (p == null) ? null: p.left;
	    }

	    private static <K,V> Entry<K,V> rightOf(Entry<K,V> p) {//获取右孩子系欸但
	        return (p == null) ? null: p.right;
	    }
	    /*************对红黑树节点x进行左旋操作 ******************/  
	    /* 
	     * 左旋示意图：对节点x进行左旋 
	     *     f                       f 
	     *    /                       / 
	     *   p                       r 
	     *  / \                     / \ 
	     * lx  r     ----->        p  ry 
	     *    / \                 / \ 
	     *   ly ry               lx ly 
	     * 左旋做了三件事： 
	     * 1. 将r的左子节点赋给p的右子节点,并将p赋给r左子节点的父节点(y左子节点非空时) 
	     * 2. 将p的父节点f(非空时)赋给r的父节点，同时更新p的子节点为r(左或右) 
	     * 3. 将r的左子节点设为p，将p的父节点设为r 
	     */  
	    private void rotateLeft(Entry<K,V> p) {
	        if (p != null) {
	        	  //1. 将r的左子节点赋给p的右子节点，并将p赋给r左子节点的父节点(r左子节点非空时)  
	            Entry<K,V> r = p.right;
	            p.right = r.left;//将r的左子节点赋给p的右子节点
	            if (r.left != null)
	                r.left.parent = p;//将p赋给r左子节点的父节点
	            //2. 将p的父节点f(非空时)赋给r的父节点，同时更新f的子节点为r(左或右)  
	            r.parent = p.parent; //将p的父节点f(非空时)赋给r的父节点
	            if (p.parent == null)
	                root = r; //如果p的父节点为空，则将r设为父节点  
	            else if (p.parent.left == p) //如果p是左子节点  
	                p.parent.left = r;//则也将r设为左子节点  
	            else
	                p.parent.right = r;//否则将r设为右子节点 
	            //3. 将r的左子节点设为p，将p的父节点设为r 
	            r.left = p;
	            p.parent = r;
	        }
	    }
	    /*************对红黑树节点y进行右旋操作 ******************/  
	    /* 
	     * 左旋示意图：对节点y进行右旋 
	     *        f                   f 
	     *       /                   / 
	     *      p                   l
	     *     / \                 / \ 
	     *    l  ry   ----->      lx  p 
	     *   / \                     / \ 
	     * lx  rx                   rx ry 
	     * 右旋做了三件事： 
	     * 1. 将l的右子节点赋给p的左子节点,并将p赋给l右子节点的父节点(l右子节点非空时) 
	     * 2. 将p的父节点f(非空时)赋给l的父节点，同时更新f的子节点为l(左或右) 
	     * 3. 将l的右子节点设为p，将p的父节点设为l
	     */  
	    private void rotateRight(Entry<K,V> p) {
	        if (p != null) {
	        	//1. 将l的右子节点赋给p的左子节点,并将p赋给l右子节点的父节点(l右子节点非空时) 
	            Entry<K,V> l = p.left;//获取p额左子节点为l
	            p.left = l.right;//将l的右子节点付给p的子节点
	            if (l.right != null) l.right.parent = p;//将p赋给l的右子节点的父节点
	            l.parent = p.parent;//将p的父节点赋值给l的父亲节点，
	            if (p.parent == null)
	                root = l;//如果没有父亲节点，则l为跟节点
	            //2. 将p的父节点f(非空时)赋给l的父节点，同时更新f的子节点为l(左或右) 
	            else if (p.parent.right == p)//判断原来的节点是父亲节点的左节点还是右节点
	                p.parent.right = l;//右节点赋值为右节点
	            else p.parent.left = l;//坐的就赋值为左的
	            //3. 将l的右子节点设为p，将p的父节点设为l
	            l.right = p;//p设为l的右节点
	            p.parent = l;//p的父亲节点设为l 即相互指向
	        }
	    }
	    /** 
	     * 新增节点后的修复操作 
	     * x 表示新增节点
	     * 1、叔父节点是黑色（若是空节点则默认为黑色）
			    这种情况下通过旋转和变色操作可以使红黑树恢复平衡。但是考虑当前节点n和父节点p的位置又分为四种情况：
			 A、n是p左子节点，p是g的左子节点。
			 B、n是p右子节点，p是g的右子节点。
			 C、n是p左子节点，p是g的右子节点。
			 D、n是p右子节点，p是g的左子节点。 
			 2、叔父节点是红色
	     */  
	    private void fixAfterInsertion(Entry<K,V> x) {
	        x.color = RED;//新增节点的颜色为红色  
	        //循环 直到 x不是根节点，且x的父节点不为红色
	        while (x != null && x != root && x.parent.color == RED) {
	        	  //如果X的父节点（P）是其父节点的父节点（G）的左节点  
	            if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
	            	//获取X的叔节点(U)
	                Entry<K,V> y = rightOf(parentOf(parentOf(x)));
	                //如果X的叔节点（U） 为红色（情况三）
	                if (colorOf(y) == RED) {
	                	 //将X的父节点（P）设置为黑色  
	                    setColor(parentOf(x), BLACK);
	                  //将X的叔节点（U）设置为黑色  
	                    setColor(y, BLACK);
	                    //将X的父节点的父节点（G）设置红色  
	                    setColor(parentOf(parentOf(x)), RED);
	                    x = parentOf(parentOf(x));
	                } 
	                //如果X的叔节点（U为黑色）；这里会存在两种情况（情况四、情况五）  
	                else {
	                    //如果X节点为其父节点（P）的右子树，则进行左旋转（情况四）  
	                    if (x == rightOf(parentOf(x))) {
	                    	//将X的父节点作为X  
	                        x = parentOf(x);
	                        //右旋转  
	                        rotateLeft(x);
	                    }
	                    //（情况五）  
                        //将X的父节点（P）设置为黑色  
	                    setColor(parentOf(x), BLACK);
	                    //将X的父节点的父节点（G）设置红色  
	                    setColor(parentOf(parentOf(x)), RED);
	                    //以X的父节点的父节点（G）为中心右旋转  
	                    rotateRight(parentOf(parentOf(x)));
	                }
	            } 
	            //如果X的父节点（P）是其父节点的父节点（G）的右节点  
	            else {
	            	 //获取X的叔节点（U）
	                Entry<K,V> y = leftOf(parentOf(parentOf(x)));
	                //如果X的叔节点（U） 为红色（情况三） 
	                if (colorOf(y) == RED) {
	                    //将X的父节点（P）设置为黑色  
	                    setColor(parentOf(x), BLACK);
	                    //将X的叔节点（U）设置为黑色  
	                    setColor(y, BLACK);
	                    //将X的父节点的父节点（G）设置红色  
	                    setColor(parentOf(parentOf(x)), RED);
	                    x = parentOf(parentOf(x));
	                }
	                //如果X的叔节点（U为黑色）；这里会存在两种情况（情况四、情况五）
	                else {
	                	  //如果X节点为其父节点（P）的左子树，则进行右旋转（情况四）  
	                    if (x == leftOf(parentOf(x))) {
	                    	//将X的父节点作为X 
	                        x = parentOf(x);
	                        //右旋转  
	                        rotateRight(x);
	                    }
	                    //将X的父节点（P）设置为黑色  
	                    setColor(parentOf(x), BLACK);
	                    //将X的父节点的父节点（G）设置红色 
	                    setColor(parentOf(parentOf(x)), RED);
	                    //以X的父节点的父节点（G）为中心左旋转 
	                    rotateLeft(parentOf(parentOf(x)));
	                }
	            }
	        }
	        //将根节点G强制设置为黑色  
	        root.color = BLACK;
	    }
	   /* 
	              这和"删除常规二叉查找树中删除节点的方法是一样的"。分3种情况：
	    1. 被删除节点没有儿子，即为叶节点。那么，直接将该节点删除就OK了。
	    2. 被删除节点只有一个儿子。那么，直接删除该节点，并用该节点的唯一子节点顶替它的位置。
	    3. 被删除节点有两个儿子。那么，先找出它的后继节点；然后把“它的后继节点的内容”复制给“该节点的内容”；之后，删除“它的后继节点”
	                        在这里，后继节点相当于替身，在将后继节点的内容复制给"被删除节点"之后，再将后继节点删除。这样就巧妙的将问题转换为"删除后继节点"的情况了，
	                       下面就考虑后继节点。 在"被删除节点"有两个非空子节点的情况下，它的后继节点不可能是双子非空。既然"的后继节点"不可能双子都非空，就意味着
	       "该节点的后继节点"要么没有儿子，要么只有一个儿子。若没有儿子，则按"情况① "进行处理；若只有一个儿子，则按"情况② "进行处理。
	       */
	 
	    private void deleteEntry(Entry<K,V> p) {
	        modCount++;
	        size--;
	        //如果p有两个孩子 
	        if (p.left != null && p.right != null) {
	        	 //获取p的继承节点
	            Entry<K,V> s = successor(p);
	            //将s的key设置为p的key
	            p.key = s.key;
	            //将s的value设置为p的value
	            p.value = s.value;
	            //将s设置为p
	            p = s;
	        } 

	      //开始修复被移除节点的树结构
	        //如果p有左孩子，获取左孩子，没有就获取右孩子
	        Entry<K,V> replacement = (p.left != null ? p.left : p.right);
	        //如果p是父节点的左孩子
	        if (replacement != null) {

	            replacement.parent = p.parent;
	          //如果p没有父亲，p就是root节点
	            if (p.parent == null)
	            	//将replacement设置为root节点
	                root = replacement;
	            //如果p是父节点的左孩子
	            else if (p == p.parent.left)
	                p.parent.left  = replacement;
	            //否则，将replacement设置为p的父亲的右孩子
	            else
	                p.parent.right = replacement;

	            //解除p节点的父亲和p节点的左右孩子的引用
	            p.left = p.right = p.parent = null;

	          //如果p为黑色
	            if (p.color == BLACK)
	            	//颜色修复
	                fixAfterDeletion(replacement);
	        } 
	        //p的父亲为null，说明p只有自己一个节点
	        else if (p.parent == null) { 
	            root = null;
	        } else {
	        	//如果p是黑色
	            if (p.color == BLACK)
	            	 //调整
	                fixAfterDeletion(p);
	            //上面判断过
	            if (p.parent != null) {
	            	 //p是父亲的左孩子
	                if (p == p.parent.left)
	                	//删除引用
	                    p.parent.left = null;
	                //p是父亲的右孩子
	                else if (p == p.parent.right)
	                	//删除引用
	                    p.parent.right = null;
	                //删除p对父亲的引用
	                p.parent = null;
	            }
	        }
	    }
	    //删除后的颜色修复
	    private void fixAfterDeletion(Entry<K,V> x) {
	        //循环，只要x不是root节点并且x的颜色是黑色的
	        while (x != root && colorOf(x) == BLACK) {
	            //如果x是它父亲的左孩子
	            if (x == leftOf(parentOf(x))) {
	                //获取到x节点父亲的右孩子
	                Entry<K,V> sib = rightOf(parentOf(x));
	                //如果sib(父亲右孩子)是红色
	                if (colorOf(sib) == RED) {
	                    //设置sib为黑色
	                    setColor(sib, BLACK);
	                    //设置x父节点为红色
	                    setColor(parentOf(x), RED);
	                    //x父节点左旋
	                    rotateLeft(parentOf(x));
	                    //将x父亲的右节点设置为sib，即sib移动到旋转后的x父亲的右孩子
	                    sib = rightOf(parentOf(x));
	                }
	                //如果sib的左右孩子都是黑色
	                if (colorOf(leftOf(sib))  == BLACK &&
	                        colorOf(rightOf(sib)) == BLACK) {
	                    //将sib设置为红色
	                    setColor(sib, RED);
	                    //将x的父亲设置为x，即x移动到父亲节点
	                    x = parentOf(x);
	                    //如果不是
	                } else {
	                    //如果sib的右孩子是黑色
	                    if (colorOf(rightOf(sib)) == BLACK) {
	                        //将sib的左孩子设置为黑色
	                        setColor(leftOf(sib), BLACK);
	                        //将sib设置为红色
	                        setColor(sib, RED);
	                        //右旋sib
	                        rotateRight(sib);
	                        //将x的父亲的右孩子设置为sib，即sib移动到旋转后的x父亲的右孩子
	                        sib = rightOf(parentOf(x));
	                    }
	                    //将sib设置成和x的父亲一样的颜色
	                    setColor(sib, colorOf(parentOf(x)));
	                    //将x的父亲设置为黑色
	                    setColor(parentOf(x), BLACK);
	                    //将sib的右孩子设置为黑色
	                    setColor(rightOf(sib), BLACK);
	                    //左旋x的父亲
	                    rotateLeft(parentOf(x));
	                    //将root设置为x，跳出循环
	                    x = root;
	                }
	                //x是一个右孩子
	            } else { // symmetric
	                //获取x父亲的左孩子
	                Entry<K,V> sib = leftOf(parentOf(x));
	                //如果sib为红色
	                if (colorOf(sib) == RED) {
	                    //将sib设置为黑色
	                    setColor(sib, BLACK);
	                    //将x的父亲设置为红色
	                    setColor(parentOf(x), RED);
	                    //右旋x的父亲
	                    rotateRight(parentOf(x));
	                    //将x的父亲的左孩子设置为sib，即sib移动到旋转后的x父亲的左孩子
	                    sib = leftOf(parentOf(x));
	                }
	                //如果sib的左右孩子都是黑色
	                if (colorOf(rightOf(sib)) == BLACK &&
	                        colorOf(leftOf(sib)) == BLACK) {
	                    //将sib设置为红色
	                    setColor(sib, RED);
	                    //将x的父亲设置为x，即x移动到父亲节点
	                    x = parentOf(x);
	                    //如果不是
	                } else {
	                    //如果sib的左孩子是黑色
	                    if (colorOf(leftOf(sib)) == BLACK) {
	                        //将sib的右孩子设置成黑色
	                        setColor(rightOf(sib), BLACK);
	                        //将sib设置成红色
	                        setColor(sib, RED);
	                        //左旋sib
	                        rotateLeft(sib);
	                        //将x的父亲的左孩子设置为sib，即sib移动到旋转后的x父亲的左孩子
	                        sib = leftOf(parentOf(x));
	                    }
	                    //将sib设置成和x的父亲一样的颜色
	                    setColor(sib, colorOf(parentOf(x)));
	                    //将x的父亲设置为黑色
	                    setColor(parentOf(x), BLACK);
	                    //将sib的左孩子设置为黑色
	                    setColor(leftOf(sib), BLACK);
	                    //右旋x的父亲
	                    rotateRight(parentOf(x));
	                    //将root设置为x，跳出循环
	                    x = root;
	                }
	            }
	        }
	        //将x设置为黑色
	        setColor(x, BLACK);
	    }
	    
	    
	    
/////////////////////////////////////////////////////////////////////////////////////////////////////	    
	    private static final Object UNBOUNDED = new Object();

	 // TreeMap的SubMap，它一个抽象类，实现了公共操作。  
	 // 它包括了"(升序)AscendingSubMap"和"(降序)DescendingSubMap"两个子类。  
	    abstract static class NavigableSubMap<K,V> extends AbstractMap<K,V>
	        implements NavigableMap<K,V>, java.io.Serializable {
	        private static final long serialVersionUID = -2102997345730753016L;

	     // TreeMap的拷贝 
	        final TreeMap<K,V> m;
	        // lo是“子Map范围的最小值”，hi是“子Map范围的最大值”；  
	        // loInclusive是“是否包含lo的标记”，hiInclusive是“是否包含hi的标记”  
	        // fromStart是“表示是否从第一个节点开始计算”，  
	        // toEnd是“表示是否计算到最后一个节点   
	        final K lo, hi;
	        final boolean fromStart, toEnd;
	        final boolean loInclusive, hiInclusive;

	        NavigableSubMap(TreeMap<K,V> m,
	                        boolean fromStart, K lo, boolean loInclusive,
	                        boolean toEnd,     K hi, boolean hiInclusive) {
	            if (!fromStart && !toEnd) {
	                if (m.compare(lo, hi) > 0)
	                    throw new IllegalArgumentException("fromKey > toKey");
	            } else {
	                if (!fromStart) // type check
	                    m.compare(lo, lo);
	                if (!toEnd)
	                    m.compare(hi, hi);
	            }

	            this.m = m;
	            this.fromStart = fromStart;
	            this.lo = lo;
	            this.loInclusive = loInclusive;
	            this.toEnd = toEnd;
	            this.hi = hi;
	            this.hiInclusive = hiInclusive;
	        }

	     // 判断key是否太小  
	        final boolean tooLow(Object key) {
	        	// 若该SubMap不包括“起始节点”，  
	            // 并且，“key小于最小键(lo)”或者“key等于最小键(lo)，但最小键却没包括在该SubMap内”  
	            // 则判断key太小。其余情况都不是太小！  
	            if (!fromStart) {
	                int c = m.compare(key, lo);
	                if (c < 0 || (c == 0 && !loInclusive))
	                    return true;
	            }
	            return false;
	        }
	     // 判断key是否太大  
	        final boolean tooHigh(Object key) {
	        	// 若该SubMap不包括“结束节点”，  
	            // 并且，“key大于最大键(hi)”或者“key等于最大键(hi)，但最大键却没包括在该SubMap内”  
	            // 则判断key太大。其余情况都不是太大！  
	            if (!toEnd) {
	                int c = m.compare(key, hi);
	                if (c > 0 || (c == 0 && !hiInclusive))
	                    return true;
	            }
	            return false;
	        }
	     // 判断key是否在“lo和hi”开区间范围内  
	        final boolean inRange(Object key) {
	            return !tooLow(key) && !tooHigh(key);
	        }
	        // 判断key是否在封闭区间内  
	        final boolean inClosedRange(Object key) {
	            return (fromStart || m.compare(key, lo) >= 0)
	                && (toEnd || m.compare(hi, key) >= 0);
	        }
	     // 判断key是否在区间内, inclusive是区间开关标志  
	        final boolean inRange(Object key, boolean inclusive) {
	            return inclusive ? inRange(key) : inClosedRange(key);
	        }

	        // 返回最低的Entry  
	        final TreeMap.Entry<K,V> absLowest() {
	            TreeMap.Entry<K,V> e =
	                (fromStart ?  m.getFirstEntry() :
	                 (loInclusive ? m.getCeilingEntry(lo) :
	                                m.getHigherEntry(lo)));
	            return (e == null || tooHigh(e.key)) ? null : e;
	        }
	        // 返回最高的Entry  
	        final TreeMap.Entry<K,V> absHighest() {
	        	 // 若“包含结束节点”，则调用getLastEntry()返回最后一个节点  
	            // 否则的话，若包括hi，则调用getFloorEntry(hi)获取小于/等于hi的最大的Entry;  
	            //           否则，调用getLowerEntry(hi)获取大于hi的最大Entry  
	            TreeMap.Entry<K,V> e =
	                (toEnd ?  m.getLastEntry() :
	                 (hiInclusive ?  m.getFloorEntry(hi) :
	                                 m.getLowerEntry(hi)));
	            return (e == null || tooLow(e.key)) ? null : e;
	        }
	     // 返回"大于/等于key的最小的Entry"  
	        final TreeMap.Entry<K,V> absCeiling(K key) {
	        	// 只有在“key太小”的情况下，absLowest()返回的Entry才是“大于/等于key的最小Entry”  
	            // 其它情况下不行。例如，当包含“起始节点”时，absLowest()返回的是最小Entry了！  
	            if (tooLow(key))
	                return absLowest();
	            // 获取“大于/等于key的最小Entry”  
	            TreeMap.Entry<K,V> e = m.getCeilingEntry(key);
	            return (e == null || tooHigh(e.key)) ? null : e;
	        }
	        // 返回"大于key的最小的Entry"  
	        final TreeMap.Entry<K,V> absHigher(K key) {
	        	 // 只有在“key太小”的情况下，absLowest()返回的Entry才是“大于key的最小Entry”  
	            // 其它情况下不行。例如，当包含“起始节点”时，absLowest()返回的是最小Entry了,而不一定是“大于key的最小Entry”！  
	            if (tooLow(key))
	                return absLowest();
	            // 获取“大于key的最小Entry”  
	            TreeMap.Entry<K,V> e = m.getHigherEntry(key);
	            return (e == null || tooHigh(e.key)) ? null : e;
	        }
	     // 返回"小于/等于key的最大的Entry"  
	        final TreeMap.Entry<K,V> absFloor(K key) {
	        	 // 只有在“key太大”的情况下，(absHighest)返回的Entry才是“小于/等于key的最大Entry”  
	            // 其它情况下不行。例如，当包含“结束节点”时，absHighest()返回的是最大Entry了！ 
	            if (tooHigh(key))
	                return absHighest();
	            // 获取"小于/等于key的最大的Entry"  
	            TreeMap.Entry<K,V> e = m.getFloorEntry(key);
	            return (e == null || tooLow(e.key)) ? null : e;
	        }
	        // 返回"小于key的最大的Entry"  
	        final TreeMap.Entry<K,V> absLower(K key) {
	        	 // 只有在“key太大”的情况下，(absHighest)返回的Entry才是“小于key的最大Entry”  
	            // 其它情况下不行。例如，当包含“结束节点”时，absHighest()返回的是最大Entry了,而不一定是“小于key的最大Entry”！  
	            if (tooHigh(key))
	                return absHighest();
	            // 获取"小于key的最大的Entry" 
	            TreeMap.Entry<K,V> e = m.getLowerEntry(key);
	            return (e == null || tooLow(e.key)) ? null : e;
	        }

	        // 返回“大于最大节点中的最小节点”，不存在的话，返回null  
	        final TreeMap.Entry<K,V> absHighFence() {
	            return (toEnd ? null : (hiInclusive ?
	                                    m.getHigherEntry(hi) :
	                                    m.getCeilingEntry(hi)));
	        }

	        // 返回“小于最小节点中的最大节点”，不存在的话，返回null  
	        final TreeMap.Entry<K,V> absLowFence() {
	            return (fromStart ? null : (loInclusive ?
	                                        m.getLowerEntry(lo) :
	                                        m.getFloorEntry(lo)));
	        }

	     // 下面几个abstract方法是需要NavigableSubMap的实现类实现的方法  
	        abstract TreeMap.Entry<K,V> subLowest();
	        abstract TreeMap.Entry<K,V> subHighest();
	        abstract TreeMap.Entry<K,V> subCeiling(K key);
	        abstract TreeMap.Entry<K,V> subHigher(K key);
	        abstract TreeMap.Entry<K,V> subFloor(K key);
	        abstract TreeMap.Entry<K,V> subLower(K key);

	     // 返回“顺序”的键迭代器  
	        abstract Iterator<K> keyIterator();

	        abstract Spliterator<K> keySpliterator();

	     // 返回“逆序”的键迭代器  
	        abstract Iterator<K> descendingKeyIterator();

	        // public methods
	        // 返回SubMap是否为空。空的话，返回true，否则返回false 
	        public boolean isEmpty() {
	            return (fromStart && toEnd) ? m.isEmpty() : entrySet().isEmpty();
	        }
	     // 返回SubMap的大小 
	        public int size() {
	            return (fromStart && toEnd) ? m.size() : entrySet().size();
	        }
	        // 返回SubMap是否包含键key  
	        public final boolean containsKey(Object key) {
	            return inRange(key) && m.containsKey(key);
	        }
	        // 将key-value 插入SubMap中  
	        public final V put(K key, V value) {
	            if (!inRange(key))
	                throw new IllegalArgumentException("key out of range");
	            return m.put(key, value);
	        }
	        // 获取key对应值  
	        public final V get(Object key) {
	            return !inRange(key) ? null :  m.get(key);
	        }
	        // 删除key对应的键值对  
	        public final V remove(Object key) {
	            return !inRange(key) ? null : m.remove(key);
	        }
	     // 获取“大于/等于key的最小键值对”  
	        public final Map.Entry<K,V> ceilingEntry(K key) {
	            return exportEntry(subCeiling(key));
	        }
	     // 获取“大于/等于key的最小键”  
	        public final K ceilingKey(K key) {
	            return keyOrNull(subCeiling(key));
	        }
	       // 获取“大于key的最小键值对”  
	        public final Map.Entry<K,V> higherEntry(K key) {
	            return exportEntry(subHigher(key));
	        }
	        // 获取“大于key的最小键”  
	        public final K higherKey(K key) {
	            return keyOrNull(subHigher(key));
	        }
	        // 获取“小于/等于key的最大键值对” 
	        public final Map.Entry<K,V> floorEntry(K key) {
	            return exportEntry(subFloor(key));
	        }
	     // 获取“小于/等于key的最大键”  
	        public final K floorKey(K key) {
	            return keyOrNull(subFloor(key));
	        }
	     // 获取“小于key的最大键值对”  
	        public final Map.Entry<K,V> lowerEntry(K key) {
	            return exportEntry(subLower(key));
	        }
	        // 获取“小于key的最大键”  
	        public final K lowerKey(K key) {
	            return keyOrNull(subLower(key));
	        }
	        // 获取"SubMap的第一个键"  
	        public final K firstKey() {
	            return key(subLowest());
	        }
	        // 获取"SubMap的最后一个键"  
	        public final K lastKey() {
	            return key(subHighest());
	        }
	        // 获取"SubMap的第一个键"  
	        public final Map.Entry<K,V> firstEntry() {
	            return exportEntry(subLowest());
	        }
	        // 获取"SubMap的最后一个键"  
	        public final Map.Entry<K,V> lastEntry() {
	            return exportEntry(subHighest());
	        }
	     // 返回"SubMap的第一个键值对"，并从SubMap中删除改键值对  
	        public final Map.Entry<K,V> pollFirstEntry() {
	            TreeMap.Entry<K,V> e = subLowest();
	            Map.Entry<K,V> result = exportEntry(e);
	            if (e != null)
	                m.deleteEntry(e);
	            return result;
	        }
	        // 返回"SubMap的最后一个键值对"，并从SubMap中删除改键值对  
	        public final Map.Entry<K,V> pollLastEntry() {
	            TreeMap.Entry<K,V> e = subHighest();
	            Map.Entry<K,V> result = exportEntry(e);
	            if (e != null)
	                m.deleteEntry(e);
	            return result;
	        }

	        // Views
	        transient NavigableMap<K,V> descendingMapView;
	        transient EntrySetView entrySetView;
	        transient KeySet<K> navigableKeySetView;
	        // 返回NavigableSet对象，实际上返回的是当前对象的"Key集合"。   
	        public final NavigableSet<K> navigableKeySet() {
	            KeySet<K> nksv = navigableKeySetView;
	            return (nksv != null) ? nksv :
	                (navigableKeySetView = new TreeMap.KeySet<>(this));
	        }
	        // 返回"Key集合"对象  
	        public final Set<K> keySet() {
	            return navigableKeySet();
	        }
	     // 返回“逆序”的Key集合 
	        public NavigableSet<K> descendingKeySet() {
	            return descendingMap().navigableKeySet();
	        }
	     // 排列fromKey(包含) 到 toKey(不包含) 的子map 
	        public final SortedMap<K,V> subMap(K fromKey, K toKey) {
	            return subMap(fromKey, true, toKey, false);
	        }
	        // 返回当前Map的头部(从第一个节点 到 toKey, 不包括toKey)  
	        public final SortedMap<K,V> headMap(K toKey) {
	            return headMap(toKey, false);
	        }
	        // 返回当前Map的尾部[从 fromKey(包括fromKeyKey) 到 最后一个节点] 
	        public final SortedMap<K,V> tailMap(K fromKey) {
	            return tailMap(fromKey, true);
	        }

	        // View classes
	       // Map的Entry的集合 
	        abstract class EntrySetView extends AbstractSet<Map.Entry<K,V>> {
	            private transient int size = -1, sizeModCount;

	            public int size() {
	                if (fromStart && toEnd)
	                    return m.size();
	                if (size == -1 || sizeModCount != m.modCount) {
	                    sizeModCount = m.modCount;
	                    size = 0;
	                    Iterator<?> i = iterator();
	                    while (i.hasNext()) {
	                        size++;
	                        i.next();
	                    }
	                }
	                return size;
	            }

	            public boolean isEmpty() {
	                TreeMap.Entry<K,V> n = absLowest();
	                return n == null || tooHigh(n.key);
	            }

	            public boolean contains(Object o) {
	                if (!(o instanceof Map.Entry))
	                    return false;
	                Map.Entry<?,?> entry = (Map.Entry<?,?>) o;
	                Object key = entry.getKey();
	                if (!inRange(key))
	                    return false;
	                TreeMap.Entry<?,?> node = m.getEntry(key);
	                return node != null &&
	                    valEquals(node.getValue(), entry.getValue());
	            }

	            public boolean remove(Object o) {
	                if (!(o instanceof Map.Entry))
	                    return false;
	                Map.Entry<?,?> entry = (Map.Entry<?,?>) o;
	                Object key = entry.getKey();
	                if (!inRange(key))
	                    return false;
	                TreeMap.Entry<K,V> node = m.getEntry(key);
	                if (node!=null && valEquals(node.getValue(),
	                                            entry.getValue())) {
	                    m.deleteEntry(node);
	                    return true;
	                }
	                return false;
	            }
	        }

	     // SubMap的迭代器  
	        abstract class SubMapIterator<T> implements Iterator<T> {
	            TreeMap.Entry<K,V> lastReturned;
	            TreeMap.Entry<K,V> next;
	            final Object fenceKey;
	            int expectedModCount;

	            SubMapIterator(TreeMap.Entry<K,V> first,
	                           TreeMap.Entry<K,V> fence) {
	                expectedModCount = m.modCount;
	                lastReturned = null;
	                next = first;
	                fenceKey = fence == null ? UNBOUNDED : fence.key;
	            }

	            public final boolean hasNext() {
	                return next != null && next.key != fenceKey;
	            }

	            final TreeMap.Entry<K,V> nextEntry() {
	                TreeMap.Entry<K,V> e = next;
	                if (e == null || e.key == fenceKey)
	                    throw new NoSuchElementException();
	                if (m.modCount != expectedModCount)
	                    throw new ConcurrentModificationException();
	                next = successor(e);
	                lastReturned = e;
	                return e;
	            }

	            final TreeMap.Entry<K,V> prevEntry() {
	                TreeMap.Entry<K,V> e = next;
	                if (e == null || e.key == fenceKey)
	                    throw new NoSuchElementException();
	                if (m.modCount != expectedModCount)
	                    throw new ConcurrentModificationException();
	                next = predecessor(e);
	                lastReturned = e;
	                return e;
	            }
	         // 删除当前节点(用于“升序的SubMap”)。  
	            // 删除之后，可以继续升序遍历；红黑树特性没变。
	            final void removeAscending() {
	                if (lastReturned == null)
	                    throw new IllegalStateException();
	                if (m.modCount != expectedModCount)
	                    throw new ConcurrentModificationException();
	                // 这里重点强调一下“为什么当lastReturned的左右孩子都不为空时，要将其赋值给next”。  
	                // 目的是为了“删除lastReturned节点之后，next节点指向的仍然是下一个节点”。  
	                //     根据“红黑树”的特性可知：  
	                //     当被删除节点有两个儿子时。那么，首先把“它的后继节点的内容”复制给“该节点的内容”；之后，删除“它的后继节点”。  
	                //     这意味着“当被删除节点有两个儿子时，删除当前节点之后，'新的当前节点'实际上是‘原有的后继节点(即下一个节点)’”。  
	                //     而此时next仍然指向"新的当前节点"。也就是说next是仍然是指向下一个节点；能继续遍历红黑树。
	                if (lastReturned.left != null && lastReturned.right != null)
	                    next = lastReturned;
	                m.deleteEntry(lastReturned);
	                lastReturned = null;
	                expectedModCount = m.modCount;
	            }
	            // 删除当前节点(用于“降序的SubMap”)。  
	            // 删除之后，可以继续降序遍历；红黑树特性没变。  
	            final void removeDescending() {
	                if (lastReturned == null)
	                    throw new IllegalStateException();
	                if (m.modCount != expectedModCount)
	                    throw new ConcurrentModificationException();
	                m.deleteEntry(lastReturned);
	                lastReturned = null;
	                expectedModCount = m.modCount;
	            }

	        }
	        // SubMap的Entry迭代器，它只支持升序操作，继承于SubMapIterator  
	        final class SubMapEntryIterator extends SubMapIterator<Map.Entry<K,V>> {
	            SubMapEntryIterator(TreeMap.Entry<K,V> first,
	                                TreeMap.Entry<K,V> fence) {
	                super(first, fence);
	            }
	         // 获取下一个节点(升序)  
	            public Map.Entry<K,V> next() {
	                return nextEntry();
	            }
	         // 删除当前节点(升序) 
	            public void remove() {
	                removeAscending();
	            }
	        }
	        // 降序SubMap的Entry迭代器，它只支持降序操作，继承于SubMapIterator  
	        final class DescendingSubMapEntryIterator extends SubMapIterator<Map.Entry<K,V>> {
	            DescendingSubMapEntryIterator(TreeMap.Entry<K,V> last,
	                                          TreeMap.Entry<K,V> fence) {
	                super(last, fence);
	            }
	            // 获取下一个节点(降序)  
	            public Map.Entry<K,V> next() {
	                return prevEntry();
	            }
	         // 删除当前节点(降序)  
	            public void remove() {
	                removeDescending();
	            }
	        }
	        // SubMap的Key迭代器，它只支持升序操作，继承于SubMapIterator  
	        final class SubMapKeyIterator extends SubMapIterator<K>
	            implements Spliterator<K> {
	            SubMapKeyIterator(TreeMap.Entry<K,V> first,
	                              TreeMap.Entry<K,V> fence) {
	                super(first, fence);
	            }
	         // 获取下一个节点(升序)  
	            public K next() {
	                return nextEntry().key;
	            }
	            // 删除当前节点(升序) 
	            public void remove() {
	                removeAscending();
	            }
	            public Spliterator<K> trySplit() {
	                return null;
	            }
	            public void forEachRemaining(Consumer<? super K> action) {
	                while (hasNext())
	                    action.accept(next());
	            }
	            public boolean tryAdvance(Consumer<? super K> action) {
	                if (hasNext()) {
	                    action.accept(next());
	                    return true;
	                }
	                return false;
	            }
	            public long estimateSize() {
	                return Long.MAX_VALUE;
	            }
	            public int characteristics() {
	                return Spliterator.DISTINCT | Spliterator.ORDERED |
	                    Spliterator.SORTED;
	            }
	            public final Comparator<? super K>  getComparator() {
	                return NavigableSubMap.this.comparator();
	            }
	        }
	     // 降序SubMap的Key迭代器，它只支持降序操作，继承于SubMapIterator  
	        final class DescendingSubMapKeyIterator extends SubMapIterator<K>
	            implements Spliterator<K> {
	            DescendingSubMapKeyIterator(TreeMap.Entry<K,V> last,
	                                        TreeMap.Entry<K,V> fence) {
	                super(last, fence);
	            }
	            public K next() {
	                return prevEntry().key;
	            }
	            public void remove() {
	                removeDescending();
	            }
	            public Spliterator<K> trySplit() {
	                return null;
	            }
	            public void forEachRemaining(Consumer<? super K> action) {
	                while (hasNext())
	                    action.accept(next());
	            }
	            public boolean tryAdvance(Consumer<? super K> action) {
	                if (hasNext()) {
	                    action.accept(next());
	                    return true;
	                }
	                return false;
	            }
	            public long estimateSize() {
	                return Long.MAX_VALUE;
	            }
	            public int characteristics() {
	                return Spliterator.DISTINCT | Spliterator.ORDERED;
	            }
	        }
	    }

	 // 升序的SubMap，继承于NavigableSubMap  
	    static final class AscendingSubMap<K,V> extends NavigableSubMap<K,V> {
	        private static final long serialVersionUID = 912986545866124060L;

	        AscendingSubMap(TreeMap<K,V> m,
	                        boolean fromStart, K lo, boolean loInclusive,
	                        boolean toEnd,     K hi, boolean hiInclusive) {
	            super(m, fromStart, lo, loInclusive, toEnd, hi, hiInclusive);
	        }

	        public Comparator<? super K> comparator() {
	            return m.comparator();
	        }
	        // 获取“子Map”。  
	        // 范围是从fromKey 到 toKey；fromInclusive是是否包含fromKey的标记，toInclusive是是否包含toKey的标记
	        public NavigableMap<K,V> subMap(K fromKey, boolean fromInclusive,
	                                        K toKey,   boolean toInclusive) {
	            if (!inRange(fromKey, fromInclusive))
	                throw new IllegalArgumentException("fromKey out of range");
	            if (!inRange(toKey, toInclusive))
	                throw new IllegalArgumentException("toKey out of range");
	            return new AscendingSubMap<>(m,
	                                         false, fromKey, fromInclusive,
	                                         false, toKey,   toInclusive);
	        }
	        // 获取“Map的头部”。  
	        // 范围从第一个节点 到 toKey, inclusive是是否包含toKey的标记  
	        public NavigableMap<K,V> headMap(K toKey, boolean inclusive) {
	            if (!inRange(toKey, inclusive))
	                throw new IllegalArgumentException("toKey out of range");
	            return new AscendingSubMap<>(m,
	                                         fromStart, lo,    loInclusive,
	                                         false,     toKey, inclusive);
	        }
	        // 获取“Map的尾部”。  
	        // 范围是从 fromKey 到 最后一个节点，inclusive是是否包含fromKey的标记  
	        public NavigableMap<K,V> tailMap(K fromKey, boolean inclusive) {
	            if (!inRange(fromKey, inclusive))
	                throw new IllegalArgumentException("fromKey out of range");
	            return new AscendingSubMap<>(m,
	                                         false, fromKey, inclusive,
	                                         toEnd, hi,      hiInclusive);
	        }
	     // 获取对应的降序Map  
	        public NavigableMap<K,V> descendingMap() {
	            NavigableMap<K,V> mv = descendingMapView;
	            return (mv != null) ? mv :
	                (descendingMapView =
	                 new DescendingSubMap<>(m,
	                                        fromStart, lo, loInclusive,
	                                        toEnd,     hi, hiInclusive));
	        }
	        // 返回“升序Key迭代器”  
	        Iterator<K> keyIterator() {
	            return new SubMapKeyIterator(absLowest(), absHighFence());
	        }

	        Spliterator<K> keySpliterator() {
	            return new SubMapKeyIterator(absLowest(), absHighFence());
	        }
	        // 返回“降序Key迭代器”  
	        Iterator<K> descendingKeyIterator() {
	            return new DescendingSubMapKeyIterator(absHighest(), absLowFence());
	        }
	        // “升序EntrySet集合”类  
	        // 实现了iterator()  
	        final class AscendingEntrySetView extends EntrySetView {
	            public Iterator<Map.Entry<K,V>> iterator() {
	                return new SubMapEntryIterator(absLowest(), absHighFence());
	            }
	        }
	        // 返回“升序EntrySet集合”  
	        public Set<Map.Entry<K,V>> entrySet() {
	            EntrySetView es = entrySetView;
	            return (es != null) ? es : (entrySetView = new AscendingEntrySetView());
	        }

	        TreeMap.Entry<K,V> subLowest()       { return absLowest(); }
	        TreeMap.Entry<K,V> subHighest()      { return absHighest(); }
	        TreeMap.Entry<K,V> subCeiling(K key) { return absCeiling(key); }
	        TreeMap.Entry<K,V> subHigher(K key)  { return absHigher(key); }
	        TreeMap.Entry<K,V> subFloor(K key)   { return absFloor(key); }
	        TreeMap.Entry<K,V> subLower(K key)   { return absLower(key); }
	    }

	 // 降序的SubMap，继承于NavigableSubMap  
	 // 相比于升序SubMap，它的实现机制是将“SubMap的比较器反转”！  
	    static final class DescendingSubMap<K,V>  extends NavigableSubMap<K,V> {
	        private static final long serialVersionUID = 912986545866120460L;
	        DescendingSubMap(TreeMap<K,V> m,
	                        boolean fromStart, K lo, boolean loInclusive,
	                        boolean toEnd,     K hi, boolean hiInclusive) {
	            super(m, fromStart, lo, loInclusive, toEnd, hi, hiInclusive);
	        }
	     // 反转的比较器：是将原始比较器反转得到的。  
	        private final Comparator<? super K> reverseComparator =
	            Collections.reverseOrder(m.comparator);
	        // 获取反转比较器  
	        public Comparator<? super K> comparator() {
	            return reverseComparator;
	        }
	        // 获取“子Map”。  
	        // 范围是从fromKey 到 toKey；fromInclusive是是否包含fromKey的标记，toInclusive是是否包含toKey的标记 
	        public NavigableMap<K,V> subMap(K fromKey, boolean fromInclusive,
	                                        K toKey,   boolean toInclusive) {
	            if (!inRange(fromKey, fromInclusive))
	                throw new IllegalArgumentException("fromKey out of range");
	            if (!inRange(toKey, toInclusive))
	                throw new IllegalArgumentException("toKey out of range");
	            return new DescendingSubMap<>(m,
	                                          false, toKey,   toInclusive,
	                                          false, fromKey, fromInclusive);
	        }
	        // 获取“Map的头部”。  
	        // 范围从第一个节点 到 toKey, inclusive是是否包含toKey的标记 
	        public NavigableMap<K,V> headMap(K toKey, boolean inclusive) {
	            if (!inRange(toKey, inclusive))
	                throw new IllegalArgumentException("toKey out of range");
	            return new DescendingSubMap<>(m,
	                                          false, toKey, inclusive,
	                                          toEnd, hi,    hiInclusive);
	        }

	        public NavigableMap<K,V> tailMap(K fromKey, boolean inclusive) {
	            if (!inRange(fromKey, inclusive))
	                throw new IllegalArgumentException("fromKey out of range");
	            return new DescendingSubMap<>(m,
	                                          fromStart, lo, loInclusive,
	                                          false, fromKey, inclusive);
	        }
	        // 获取“Map的尾部”。  
	        // 范围是从 fromKey 到 最后一个节点，inclusive是是否包含fromKey的标记  
	        public NavigableMap<K,V> descendingMap() {
	            NavigableMap<K,V> mv = descendingMapView;
	            return (mv != null) ? mv :
	                (descendingMapView =
	                 new AscendingSubMap<>(m,
	                                       fromStart, lo, loInclusive,
	                                       toEnd,     hi, hiInclusive));
	        }
	        // 获取对应的降序Map  
	        Iterator<K> keyIterator() {
	            return new DescendingSubMapKeyIterator(absHighest(), absLowFence());
	        }
	        // 返回“升序Key迭代器”  
	        Spliterator<K> keySpliterator() {
	            return new DescendingSubMapKeyIterator(absHighest(), absLowFence());
	        }
	        // 返回“降序Key迭代器” 
	        Iterator<K> descendingKeyIterator() {
	            return new SubMapKeyIterator(absLowest(), absHighFence());
	        }
	    }  
	    // “降序EntrySet集合”类  
	    // 实现了iterator()  
	        final class DescendingEntrySetView extends EntrySetView {
	            public Iterator<Map.Entry<K,V>> iterator() {
	                return new DescendingSubMapEntryIterator(absHighest(), absLowFence());
	            }
	        }
	        // 返回“降序EntrySet集合”  
	        public Set<Map.Entry<K,V>> entrySet() {
	            EntrySetView es = entrySetView;
	            return (es != null) ? es : (entrySetView = new DescendingEntrySetView());
	        }

	        TreeMap.Entry<K,V> subLowest()       { return absHighest(); }
	        TreeMap.Entry<K,V> subHighest()      { return absLowest(); }
	        TreeMap.Entry<K,V> subCeiling(K key) { return absFloor(key); }
	        TreeMap.Entry<K,V> subHigher(K key)  { return absLower(key); }
	        TreeMap.Entry<K,V> subFloor(K key)   { return absCeiling(key); }
	        TreeMap.Entry<K,V> subLower(K key)   { return absHigher(key); }
	    }

//SubMap是旧版本的类，新的Java中没有用到。  
	    private class SubMap extends AbstractMap<K,V>
	        implements SortedMap<K,V>, java.io.Serializable {
	        private static final long serialVersionUID = -6520786458950516097L;
	        private boolean fromStart = false, toEnd = false;
	        private K fromKey, toKey;
	        private Object readResolve() {
	            return new AscendingSubMap<>(TreeMap.this,
	                                         fromStart, fromKey, true,
	                                         toEnd, toKey, false);
	        }
	        public Set<Map.Entry<K,V>> entrySet() { throw new InternalError(); }
	        public K lastKey() { throw new InternalError(); }
	        public K firstKey() { throw new InternalError(); }
	        public SortedMap<K,V> subMap(K fromKey, K toKey) { throw new InternalError(); }
	        public SortedMap<K,V> headMap(K toKey) { throw new InternalError(); }
	        public SortedMap<K,V> tailMap(K fromKey) { throw new InternalError(); }
	        public Comparator<? super K> comparator() { throw new InternalError(); }
	    }

	    private static final long serialVersionUID = 919286545866124006L;
	    private void writeObject(java.io.ObjectOutputStream s)
	        throws java.io.IOException {
	        // Write out the Comparator and any hidden stuff
	        s.defaultWriteObject();

	        // Write out size (number of Mappings)
	        s.writeInt(size);

	        // Write out keys and values (alternating)
	        for (Iterator<Map.Entry<K,V>> i = entrySet().iterator(); i.hasNext(); ) {
	            Map.Entry<K,V> e = i.next();
	            s.writeObject(e.getKey());
	            s.writeObject(e.getValue());
	        }
	    }

	    /**
	     * Reconstitute the {@code TreeMap} instance from a stream (i.e.,
	     * deserialize it).
	     */
	    private void readObject(final java.io.ObjectInputStream s)
	        throws java.io.IOException, ClassNotFoundException {
	        // Read in the Comparator and any hidden stuff
	        s.defaultReadObject();

	        // Read in size
	        int size = s.readInt();

	        buildFromSorted(size, null, s, null);
	    }

	    /** Intended to be called only from TreeSet.readObject */
	    void readTreeSet(int size, java.io.ObjectInputStream s, V defaultVal)
	        throws java.io.IOException, ClassNotFoundException {
	        buildFromSorted(size, null, s, defaultVal);
	    }

	    /** Intended to be called only from TreeSet.addAll */
	    void addAllForTreeSet(SortedSet<? extends K> set, V defaultVal) {
	        try {
	            buildFromSorted(set.size(), set.iterator(), null, defaultVal);
	        } catch (java.io.IOException cannotHappen) {
	        } catch (ClassNotFoundException cannotHappen) {
	        }
	    }

	    private void buildFromSorted(int size, Iterator<?> it,
	                                 java.io.ObjectInputStream str,
	                                 V defaultVal)
	        throws  java.io.IOException, ClassNotFoundException {
	        this.size = size;
	        root = buildFromSorted(0, 0, size-1, computeRedLevel(size),
	                               it, str, defaultVal);
	    }
	    @SuppressWarnings("unchecked")
	 // 根据已经一个排好序的map创建一个TreeMap 
	    private final Entry<K,V> buildFromSorted(int level, int lo, int hi,
	                                             int redLevel,
	                                             Iterator<?> it,
	                                             java.io.ObjectInputStream str,
	                                             V defaultVal)
	        throws  java.io.IOException, ClassNotFoundException {

	        if (hi < lo) return null;

	        int mid = (lo + hi) >>> 1;

	        Entry<K,V> left  = null;
	        if (lo < mid)
	            left = buildFromSorted(level+1, lo, mid - 1, redLevel,
	                                   it, str, defaultVal);

	        // extract key and/or value from iterator or stream
	        K key;
	        V value;
	        if (it != null) {
	            if (defaultVal==null) {
	                Map.Entry<?,?> entry = (Map.Entry<?,?>)it.next();
	                key = (K)entry.getKey();
	                value = (V)entry.getValue();
	            } else {
	                key = (K)it.next();
	                value = defaultVal;
	            }
	        } else { // use stream
	            key = (K) str.readObject();
	            value = (defaultVal != null ? defaultVal : (V) str.readObject());
	        }

	        Entry<K,V> middle =  new Entry<>(key, value, null);

	        // color nodes in non-full bottommost level red
	        if (level == redLevel)
	            middle.color = RED;

	        if (left != null) {
	            middle.left = left;
	            left.parent = middle;
	        }

	        if (mid < hi) {
	            Entry<K,V> right = buildFromSorted(level+1, mid+1, hi, redLevel,
	                                               it, str, defaultVal);
	            middle.right = right;
	            right.parent = middle;
	        }

	        return middle;
	    }

	    private static int computeRedLevel(int sz) {
	        int level = 0;
	        for (int m = sz - 1; m >= 0; m = m / 2 - 1)
	            level++;
	        return level;
	    }

	    static <K> Spliterator<K> keySpliteratorFor(NavigableMap<K,?> m) {
	        if (m instanceof TreeMap) {
	            @SuppressWarnings("unchecked") TreeMap<K,Object> t =
	                (TreeMap<K,Object>) m;
	            return t.keySpliterator();
	        }
	        if (m instanceof DescendingSubMap) {
	            @SuppressWarnings("unchecked") DescendingSubMap<K,?> dm =
	                (DescendingSubMap<K,?>) m;
	            TreeMap<K,?> tm = dm.m;
	            if (dm == tm.descendingMap) {
	                @SuppressWarnings("unchecked") TreeMap<K,Object> t =
	                    (TreeMap<K,Object>) tm;
	                return t.descendingKeySpliterator();
	            }
	        }
	        @SuppressWarnings("unchecked") NavigableSubMap<K,?> sm =
	            (NavigableSubMap<K,?>) m;
	        return sm.keySpliterator();
	    }

	    final Spliterator<K> keySpliterator() {
	        return new KeySpliterator<K,V>(this, null, null, 0, -1, 0);
	    }

	    final Spliterator<K> descendingKeySpliterator() {
	        return new DescendingKeySpliterator<K,V>(this, null, null, 0, -2, 0);
	    }

	    static class TreeMapSpliterator<K,V> {
	        final TreeMap<K,V> tree;
	        TreeMap.Entry<K,V> current; // traverser; initially first node in range
	        TreeMap.Entry<K,V> fence;   // one past last, or null
	        int side;                   // 0: top, -1: is a left split, +1: right
	        int est;                    // size estimate (exact only for top-level)
	        int expectedModCount;       // for CME checks

	        TreeMapSpliterator(TreeMap<K,V> tree,
	                           TreeMap.Entry<K,V> origin, TreeMap.Entry<K,V> fence,
	                           int side, int est, int expectedModCount) {
	            this.tree = tree;
	            this.current = origin;
	            this.fence = fence;
	            this.side = side;
	            this.est = est;
	            this.expectedModCount = expectedModCount;
	        }

	        final int getEstimate() { // force initialization
	            int s; TreeMap<K,V> t;
	            if ((s = est) < 0) {
	                if ((t = tree) != null) {
	                    current = (s == -1) ? t.getFirstEntry() : t.getLastEntry();
	                    s = est = t.size;
	                    expectedModCount = t.modCount;
	                }
	                else
	                    s = est = 0;
	            }
	            return s;
	        }

	        public final long estimateSize() {
	            return (long)getEstimate();
	        }
	    }

	    static final class KeySpliterator<K,V>
	        extends TreeMapSpliterator<K,V>
	        implements Spliterator<K> {
	        KeySpliterator(TreeMap<K,V> tree,
	                       TreeMap.Entry<K,V> origin, TreeMap.Entry<K,V> fence,
	                       int side, int est, int expectedModCount) {
	            super(tree, origin, fence, side, est, expectedModCount);
	        }

	        public KeySpliterator<K,V> trySplit() {
	            if (est < 0)
	                getEstimate(); // force initialization
	            int d = side;
	            TreeMap.Entry<K,V> e = current, f = fence,
	                s = ((e == null || e == f) ? null :      // empty
	                     (d == 0)              ? tree.root : // was top
	                     (d >  0)              ? e.right :   // was right
	                     (d <  0 && f != null) ? f.left :    // was left
	                     null);
	            if (s != null && s != e && s != f &&
	                tree.compare(e.key, s.key) < 0) {        // e not already past s
	                side = 1;
	                return new KeySpliterator<>
	                    (tree, e, current = s, -1, est >>>= 1, expectedModCount);
	            }
	            return null;
	        }

	        public void forEachRemaining(Consumer<? super K> action) {
	            if (action == null)
	                throw new NullPointerException();
	            if (est < 0)
	                getEstimate(); // force initialization
	            TreeMap.Entry<K,V> f = fence, e, p, pl;
	            if ((e = current) != null && e != f) {
	                current = f; // exhaust
	                do {
	                    action.accept(e.key);
	                    if ((p = e.right) != null) {
	                        while ((pl = p.left) != null)
	                            p = pl;
	                    }
	                    else {
	                        while ((p = e.parent) != null && e == p.right)
	                            e = p;
	                    }
	                } while ((e = p) != null && e != f);
	                if (tree.modCount != expectedModCount)
	                    throw new ConcurrentModificationException();
	            }
	        }

	        public boolean tryAdvance(Consumer<? super K> action) {
	            TreeMap.Entry<K,V> e;
	            if (action == null)
	                throw new NullPointerException();
	            if (est < 0)
	                getEstimate(); // force initialization
	            if ((e = current) == null || e == fence)
	                return false;
	            current = successor(e);
	            action.accept(e.key);
	            if (tree.modCount != expectedModCount)
	                throw new ConcurrentModificationException();
	            return true;
	        }

	        public int characteristics() {
	            return (side == 0 ? Spliterator.SIZED : 0) |
	                Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.ORDERED;
	        }

	        public final Comparator<? super K>  getComparator() {
	            return tree.comparator;
	        }

	    }

	    static final class DescendingKeySpliterator<K,V>
	        extends TreeMapSpliterator<K,V>
	        implements Spliterator<K> {
	        DescendingKeySpliterator(TreeMap<K,V> tree,
	                                 TreeMap.Entry<K,V> origin, TreeMap.Entry<K,V> fence,
	                                 int side, int est, int expectedModCount) {
	            super(tree, origin, fence, side, est, expectedModCount);
	        }

	        public DescendingKeySpliterator<K,V> trySplit() {
	            if (est < 0)
	                getEstimate(); // force initialization
	            int d = side;
	            TreeMap.Entry<K,V> e = current, f = fence,
	                    s = ((e == null || e == f) ? null :      // empty
	                         (d == 0)              ? tree.root : // was top
	                         (d <  0)              ? e.left :    // was left
	                         (d >  0 && f != null) ? f.right :   // was right
	                         null);
	            if (s != null && s != e && s != f &&
	                tree.compare(e.key, s.key) > 0) {       // e not already past s
	                side = 1;
	                return new DescendingKeySpliterator<>
	                        (tree, e, current = s, -1, est >>>= 1, expectedModCount);
	            }
	            return null;
	        }

	        public void forEachRemaining(Consumer<? super K> action) {
	            if (action == null)
	                throw new NullPointerException();
	            if (est < 0)
	                getEstimate(); // force initialization
	            TreeMap.Entry<K,V> f = fence, e, p, pr;
	            if ((e = current) != null && e != f) {
	                current = f; // exhaust
	                do {
	                    action.accept(e.key);
	                    if ((p = e.left) != null) {
	                        while ((pr = p.right) != null)
	                            p = pr;
	                    }
	                    else {
	                        while ((p = e.parent) != null && e == p.left)
	                            e = p;
	                    }
	                } while ((e = p) != null && e != f);
	                if (tree.modCount != expectedModCount)
	                    throw new ConcurrentModificationException();
	            }
	        }

	        public boolean tryAdvance(Consumer<? super K> action) {
	            TreeMap.Entry<K,V> e;
	            if (action == null)
	                throw new NullPointerException();
	            if (est < 0)
	                getEstimate(); // force initialization
	            if ((e = current) == null || e == fence)
	                return false;
	            current = predecessor(e);
	            action.accept(e.key);
	            if (tree.modCount != expectedModCount)
	                throw new ConcurrentModificationException();
	            return true;
	        }

	        public int characteristics() {
	            return (side == 0 ? Spliterator.SIZED : 0) |
	                Spliterator.DISTINCT | Spliterator.ORDERED;
	        }
	    }

	    static final class ValueSpliterator<K,V>
	            extends TreeMapSpliterator<K,V>
	            implements Spliterator<V> {
	        ValueSpliterator(TreeMap<K,V> tree,
	                         TreeMap.Entry<K,V> origin, TreeMap.Entry<K,V> fence,
	                         int side, int est, int expectedModCount) {
	            super(tree, origin, fence, side, est, expectedModCount);
	        }

	        public ValueSpliterator<K,V> trySplit() {
	            if (est < 0)
	                getEstimate(); // force initialization
	            int d = side;
	            TreeMap.Entry<K,V> e = current, f = fence,
	                    s = ((e == null || e == f) ? null :      // empty
	                         (d == 0)              ? tree.root : // was top
	                         (d >  0)              ? e.right :   // was right
	                         (d <  0 && f != null) ? f.left :    // was left
	                         null);
	            if (s != null && s != e && s != f &&
	                tree.compare(e.key, s.key) < 0) {        // e not already past s
	                side = 1;
	                return new ValueSpliterator<>
	                        (tree, e, current = s, -1, est >>>= 1, expectedModCount);
	            }
	            return null;
	        }

	        public void forEachRemaining(Consumer<? super V> action) {
	            if (action == null)
	                throw new NullPointerException();
	            if (est < 0)
	                getEstimate(); // force initialization
	            TreeMap.Entry<K,V> f = fence, e, p, pl;
	            if ((e = current) != null && e != f) {
	                current = f; // exhaust
	                do {
	                    action.accept(e.value);
	                    if ((p = e.right) != null) {
	                        while ((pl = p.left) != null)
	                            p = pl;
	                    }
	                    else {
	                        while ((p = e.parent) != null && e == p.right)
	                            e = p;
	                    }
	                } while ((e = p) != null && e != f);
	                if (tree.modCount != expectedModCount)
	                    throw new ConcurrentModificationException();
	            }
	        }

	        public boolean tryAdvance(Consumer<? super V> action) {
	            TreeMap.Entry<K,V> e;
	            if (action == null)
	                throw new NullPointerException();
	            if (est < 0)
	                getEstimate(); // force initialization
	            if ((e = current) == null || e == fence)
	                return false;
	            current = successor(e);
	            action.accept(e.value);
	            if (tree.modCount != expectedModCount)
	                throw new ConcurrentModificationException();
	            return true;
	        }

	        public int characteristics() {
	            return (side == 0 ? Spliterator.SIZED : 0) | Spliterator.ORDERED;
	        }
	    }

	    static final class EntrySpliterator<K,V>
	        extends TreeMapSpliterator<K,V>
	        implements Spliterator<Map.Entry<K,V>> {
	        EntrySpliterator(TreeMap<K,V> tree,
	                         TreeMap.Entry<K,V> origin, TreeMap.Entry<K,V> fence,
	                         int side, int est, int expectedModCount) {
	            super(tree, origin, fence, side, est, expectedModCount);
	        }

	        public EntrySpliterator<K,V> trySplit() {
	            if (est < 0)
	                getEstimate(); // force initialization
	            int d = side;
	            TreeMap.Entry<K,V> e = current, f = fence,
	                    s = ((e == null || e == f) ? null :      // empty
	                         (d == 0)              ? tree.root : // was top
	                         (d >  0)              ? e.right :   // was right
	                         (d <  0 && f != null) ? f.left :    // was left
	                         null);
	            if (s != null && s != e && s != f &&
	                tree.compare(e.key, s.key) < 0) {        // e not already past s
	                side = 1;
	                return new EntrySpliterator<>
	                        (tree, e, current = s, -1, est >>>= 1, expectedModCount);
	            }
	            return null;
	        }

	        public void forEachRemaining(Consumer<? super Map.Entry<K, V>> action) {
	            if (action == null)
	                throw new NullPointerException();
	            if (est < 0)
	                getEstimate(); // force initialization
	            TreeMap.Entry<K,V> f = fence, e, p, pl;
	            if ((e = current) != null && e != f) {
	                current = f; // exhaust
	                do {
	                    action.accept(e);
	                    if ((p = e.right) != null) {
	                        while ((pl = p.left) != null)
	                            p = pl;
	                    }
	                    else {
	                        while ((p = e.parent) != null && e == p.right)
	                            e = p;
	                    }
	                } while ((e = p) != null && e != f);
	                if (tree.modCount != expectedModCount)
	                    throw new ConcurrentModificationException();
	            }
	        }

	        public boolean tryAdvance(Consumer<? super Map.Entry<K,V>> action) {
	            TreeMap.Entry<K,V> e;
	            if (action == null)
	                throw new NullPointerException();
	            if (est < 0)
	                getEstimate(); // force initialization
	            if ((e = current) == null || e == fence)
	                return false;
	            current = successor(e);
	            action.accept(e);
	            if (tree.modCount != expectedModCount)
	                throw new ConcurrentModificationException();
	            return true;
	        }

	        public int characteristics() {
	            return (side == 0 ? Spliterator.SIZED : 0) |
	                    Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.ORDERED;
	        }

	        @Override
	        public Comparator<Map.Entry<K, V>> getComparator() {
	            // Adapt or create a key-based comparator
	            if (tree.comparator != null) {
	                return Map.Entry.comparingByKey(tree.comparator);
	            }
	            else {
	                return (Comparator<Map.Entry<K, V>> & Serializable) (e1, e2) -> {
	                    @SuppressWarnings("unchecked")
	                    Comparable<? super K> k1 = (Comparable<? super K>) e1.getKey();
	                    return k1.compareTo(e2.getKey());
	                };
	            }
	        }
	    }
}

