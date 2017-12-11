package com.liu.Map;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Hashtable<K,V>
extends Dictionary<K,V>
implements Map<K,V>, Cloneable, java.io.Serializable {
	 // 保存key-value的数组。    
    // Hashtable同样采用单链表解决冲突，每一个Entry本质上是一个单向链表
	private transient Entry<?,?>[] table;
	 private transient int count; // Hashtable中键值对的数量    
	 private int threshold;   // 阈值，用于判断是否需要调整Hashtable的容量（threshold = 容量*加载因子）    
	 private float loadFactor;  // 加载因子    
	 private transient int modCount = 0; // Hashtable被改变的次数，用于fail-fast机制的实现  
	  private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
	 private static final long serialVersionUID = 1421746759512286392L;
	// 指定“容量大小”和“加载因子”的构造函数   
	 public Hashtable(int initialCapacity, float loadFactor) {
	        if (initialCapacity < 0)
	            throw new IllegalArgumentException("Illegal Capacity: "+
	                                               initialCapacity);
	        if (loadFactor <= 0 || Float.isNaN(loadFactor)) 
	            throw new IllegalArgumentException("Illegal Load: "+loadFactor);

	        if (initialCapacity==0)
	            initialCapacity = 1;
	        this.loadFactor = loadFactor;
	        table = new Entry<?,?>[initialCapacity];
	        threshold = (int)Math.min(initialCapacity * loadFactor, MAX_ARRAY_SIZE + 1);
	    }
	// 指定“容量大小”的构造函数 
	  public Hashtable(int initialCapacity) {
	        this(initialCapacity, 0.75f);
	    }
	// 默认构造函数。
	  public Hashtable() {
		// 默认构造函数，指定的容量大小是11；加载因子是0.75    
	        this(11, 0.75f);
	    }
	// 包含“子Map”的构造函数 
	  public Hashtable(Map<? extends K, ? extends V> t) {
	        this(Math.max(2*t.size(), 11), 0.75f);
	     // 将“子Map”的全部元素都添加到Hashtable中  
	        putAll(t);
	    }
	  //Hashtable的大小
	  public synchronized int size() {
	        return count;
	    }
	  //判断是否为空
	  public synchronized boolean isEmpty() {
	        return count == 0;
	    }
	// 返回“所有key”的枚举对象
	  public synchronized Enumeration<K> keys() {
	        return this.<K>getEnumeration(KEYS);
	    }
	  // 返回“所有value”的枚举对象  
	  public synchronized Enumeration<V> elements() {
	        return this.<V>getEnumeration(VALUES);
	    }
	// 判断Hashtable是否包含“值(value)”   
	  public synchronized boolean contains(Object value) {
		   //注意，Hashtable中的value不能是null，    
	        // 若是null的话，抛出异常!  
	        if (value == null) {
	            throw new NullPointerException();
	        }
	     // 从后向前遍历table数组中的元素(Entry)    
	        // 对于每个Entry(单向链表)，逐个遍历，判断节点的值是否等于value  
	        Entry<?,?> tab[] = table;
	        for (int i = tab.length ; i-- > 0 ;) {
	            for (Entry<?,?> e = tab[i] ; e != null ; e = e.next) {
	                if (e.value.equals(value)) {
	                    return true;
	                }
	            }
	        }
	        return false;
	    }
	  // 判断Hashtable是否包含key    
	  public boolean containsValue(Object value) {
	        return contains(value);
	    }
	  // 判断Hashtable是否包含key    
	  public synchronized boolean containsKey(Object key) {
	        Entry<?,?> tab[] = table;
	        //计算hash值，直接用key的hashCode代替  
	        int hash = key.hashCode();
	        // 计算在数组中的索引值   
	        int index = (hash & 0x7FFFFFFF) % tab.length;
	        // 找到“key对应的Entry(链表)”，然后在链表中找出“哈希值”和“键值”与key都相等的元素    
	        for (Entry<?,?> e = tab[index] ; e != null ; e = e.next) {
	            if ((e.hash == hash) && e.key.equals(key)) {
	                return true;
	            }
	        }
	        return false;
	    }
	  // 返回key对应的value，没有的话返回null    
	   public synchronized V get(Object key) {
	        Entry<?,?> tab[] = table;
	        int hash = key.hashCode();
	     // 计算索引值， 
	        int index = (hash & 0x7FFFFFFF) % tab.length;
	        // 找到“key对应的Entry(链表)”，然后在链表中找出“哈希值”和“键值”与key都相等的元素    
	        for (Entry<?,?> e = tab[index] ; e != null ; e = e.next) {
	            if ((e.hash == hash) && e.key.equals(key)) {
	                return (V)e.value;
	            }
	        }
	        return null;
	    }
	// 调整Hashtable的长度，将长度变成原来的2倍+1   
	   @SuppressWarnings("unchecked")
	    protected void rehash() {
	        int oldCapacity = table.length;
	        Entry<?,?>[] oldMap = table;

	        //创建新容量大小的Entry数组  
	        int newCapacity = (oldCapacity << 1) + 1;
	        if (newCapacity - MAX_ARRAY_SIZE > 0) {
	            if (oldCapacity == MAX_ARRAY_SIZE)
	                // Keep running with MAX_ARRAY_SIZE buckets
	                return;
	            newCapacity = MAX_ARRAY_SIZE;
	        }
	        Entry<?,?>[] newMap = new Entry<?,?>[newCapacity];

	        modCount++;
	        threshold = (int)Math.min(newCapacity * loadFactor, MAX_ARRAY_SIZE + 1);
	        table = newMap;
	      //将“旧的Hashtable”中的元素复制到“新的Hashtable”中 
	        for (int i = oldCapacity ; i-- > 0 ;) {
	            for (Entry<K,V> old = (Entry<K,V>)oldMap[i] ; old != null ; ) {
	                Entry<K,V> e = old;
	                old = old.next;
	                //重新计算index  
	                int index = (e.hash & 0x7FFFFFFF) % newCapacity;
	                e.next = (Entry<K,V>)newMap[index];
	                newMap[index] = e;
	            }
	        }
	    }
	   // 将“key-value”添加到Hashtable中 具体实现 
	   private void addEntry(int hash, K key, V value, int index) {
		   // 若“Hashtable中不存在键为key的键值对”，  
	        // 将“修改统计数”+1    
	        modCount++; 
	        Entry<?,?> tab[] = table;
	    //  若“Hashtable实际容量” > “阈值”(阈值=总的容量 * 加载因子)    
	        //  则调整Hashtable的大小    
	        if (count >= threshold) {
	            // Rehash the table if the threshold is exceeded
	            rehash();

	            tab = table;
	            hash = key.hashCode();
	            index = (hash & 0x7FFFFFFF) % tab.length;
	        }

	        //将新的key-value对插入到tab[index]处（即链表的头结点）  
	        @SuppressWarnings("unchecked")
	        Entry<K,V> e = (Entry<K,V>) tab[index];
	        tab[index] = new Entry<>(hash, key, value, e);
	        count++;
	    }
	   // 将“key-value”添加到Hashtable中    
	   public synchronized V put(K key, V value) {
		   // Hashtable中不能插入value为null的元素！！！    
	        if (value == null) {
	            throw new NullPointerException();
	        }

	     // 若“Hashtable中已存在键为key的键值对”，    
	        // 则用“新的value”替换“旧的value”
	        Entry<?,?> tab[] = table;
	        int hash = key.hashCode();
	        int index = (hash & 0x7FFFFFFF) % tab.length;
	        @SuppressWarnings("unchecked")
	        Entry<K,V> entry = (Entry<K,V>)tab[index];
	        for(; entry != null ; entry = entry.next) {
	            if ((entry.hash == hash) && entry.key.equals(key)) {
	                V old = entry.value;
	                entry.value = value;
	                return old;
	            }
	        }
	        //调用添加实体方法
	        addEntry(hash, key, value, index);
	        return null;
	    }
	   // 删除Hashtable中键为key的元素   
	   public synchronized V remove(Object key) {
	        Entry<?,?> tab[] = table;
	        int hash = key.hashCode();
	        int index = (hash & 0x7FFFFFFF) % tab.length;
	        @SuppressWarnings("unchecked")
	        Entry<K,V> e = (Entry<K,V>)tab[index];
	        //从table[index]链表中找出要删除的节点，并删除该节点。  
	        //因为是单链表，因此要保留带删节点的前一个节点，才能有效地删除节点  
	        for(Entry<K,V> prev = null ; e != null ; prev = e, e = e.next) {
	            if ((e.hash == hash) && e.key.equals(key)) {
	                modCount++;
	                if (prev != null) {
	                    prev.next = e.next;
	                } else {
	                    tab[index] = e.next;
	                }
	                count--;
	                V oldValue = e.value;
	                e.value = null;
	                return oldValue;
	            }
	        }
	        return null;
	    }
	   // 将“Map(t)”的中全部元素逐一添加到Hashtable中    
	   public synchronized void putAll(Map<? extends K, ? extends V> t) {
	        for (Map.Entry<? extends K, ? extends V> e : t.entrySet())
	            put(e.getKey(), e.getValue());
	    }
	   // 清空Hashtable    
	    // 将Hashtable的table数组的值全部设为null    
	    public synchronized void clear() {
	        Entry<?,?> tab[] = table;
	        modCount++;
	        for (int index = tab.length; --index >= 0; )
	            tab[index] = null;
	        count = 0;
	    }
	 // 克隆一个Hashtable，并以Object的形式返回。
	    public synchronized Object clone() {
	        try {
	            Hashtable<?,?> t = (Hashtable<?,?>)super.clone();
	            t.table = new Entry<?,?>[table.length];
	            for (int i = table.length ; i-- > 0 ; ) {
	                t.table[i] = (table[i] != null)
	                    ? (Entry<?,?>) table[i].clone() : null;
	            }
	            t.keySet = null;
	            t.entrySet = null;
	            t.values = null;
	            t.modCount = 0;
	            return t;
	        } catch (CloneNotSupportedException e) {
	            // this shouldn't happen, since we are Cloneable
	            throw new InternalError(e);
	        }
	    }
	   // 将Map以String的方式返回
	    public synchronized String toString() {
	        int max = size() - 1;
	        if (max == -1)
	            return "{}";

	        StringBuilder sb = new StringBuilder();
	        Iterator<Map.Entry<K,V>> it = entrySet().iterator();

	        sb.append('{');
	        for (int i = 0; ; i++) {
	            Map.Entry<K,V> e = it.next();
	            K key = e.getKey();
	            V value = e.getValue();
	            sb.append(key   == this ? "(this Map)" : key.toString());
	            sb.append('=');
	            sb.append(value == this ? "(this Map)" : value.toString());

	            if (i == max)
	                return sb.append('}').toString();
	            sb.append(", ");
	        }
	    }
	    // 获取Hashtable的枚举类对象    
	    // 若Hashtable的实际大小为0,则返回“空枚举类”对象；    
	    // 否则，返回正常的Enumerator的对象。   
	    private <T> Enumeration<T> getEnumeration(int type) {
	        if (count == 0) {
	            return Collections.emptyEnumeration();
	        } else {
	            return new Enumerator<>(type, false);
	        }
	    }
	 // 获取Hashtable的迭代器    
	    // 若Hashtable的实际大小为0,则返回“空迭代器”对象；    
	    // 否则，返回正常的Enumerator的对象。(Enumerator实现了迭代器和枚举两个接口) 
	    private <T> Iterator<T> getIterator(int type) {
	        if (count == 0) {
	            return Collections.emptyIterator();
	        } else {
	            return new Enumerator<>(type, true);
	        }
	    }
	    // Hashtable的“key的集合”。它是一个Set，没有重复元素  
	    private transient volatile Set<K> keySet;
	    // Hashtable的“key-value的集合”。它是一个Set，没有重复元素    
	    private transient volatile Set<Map.Entry<K,V>> entrySet;
	 // Hashtable的“key-value的集合”。它是一个Collection，可以有重复元素  
	    private transient volatile Collection<V> values;
	 // 返回一个被synchronizedSet封装后的KeySet对象    
	    // synchronizedSet封装的目的是对KeySet的所有方法都添加synchronized，实现多线程同步
	    public Set<K> keySet() {
	        if (keySet == null)
	            keySet = Collections.synchronizedSet(new KeySet(), this);
	        return keySet;
	    }
	    // Hashtable的Key的Set集合。    
	    // KeySet继承于AbstractSet，所以，KeySet中的元素没有重复的。    
	    private class KeySet extends AbstractSet<K> {
	        public Iterator<K> iterator() {//迭代器，
	            return getIterator(KEYS);
	        }
	        public int size() {
	            return count;
	        }
	        public boolean contains(Object o) {
	            return containsKey(o);
	        }
	        public boolean remove(Object o) {
	            return Hashtable.this.remove(o) != null;
	        }
	        public void clear() {
	            Hashtable.this.clear();
	        }
	    }
	 // 返回一个被synchronizedSet封装后的EntrySet对象    
	    // synchronizedSet封装的目的是对EntrySet的所有方法都添加synchronized，实现多线程同步    
	    public Set<Map.Entry<K,V>> entrySet() {
	        if (entrySet==null)
	            entrySet = Collections.synchronizedSet(new EntrySet(), this);
	        return entrySet;
	    }
	    // Hashtable的Entry的Set集合。    
	    // EntrySet继承于AbstractSet，所以，EntrySet中的元素没有重复的。  
	    private class EntrySet extends AbstractSet<Map.Entry<K,V>> {
	        public Iterator<Map.Entry<K,V>> iterator() {
	            return getIterator(ENTRIES);
	        }

	        public boolean add(Map.Entry<K,V> o) {
	            return super.add(o);
	        }
	        // 查找EntrySet中是否包含Object(0)    
	        // 首先，在table中找到o对应的Entry链表    
	        // 然后，查找Entry链表中是否存在Object    
	        public boolean contains(Object o) {
	            if (!(o instanceof Map.Entry))
	                return false;
	            Map.Entry<?,?> entry = (Map.Entry<?,?>)o;
	            Object key = entry.getKey();
	            Entry<?,?>[] tab = table;
	            int hash = key.hashCode();
	            int index = (hash & 0x7FFFFFFF) % tab.length;

	            for (Entry<?,?> e = tab[index]; e != null; e = e.next)
	                if (e.hash==hash && e.equals(entry))
	                    return true;
	            return false;
	        }
	        // 删除元素Object(0)    
	        // 首先，在table中找到o对应的Entry链表  
	        // 然后，删除链表中的元素Object  
	        public boolean remove(Object o) {
	            if (!(o instanceof Map.Entry))
	                return false;
	            Map.Entry<?,?> entry = (Map.Entry<?,?>) o;
	            Object key = entry.getKey();
	            Entry<?,?>[] tab = table;
	            int hash = key.hashCode();
	            int index = (hash & 0x7FFFFFFF) % tab.length;

	            @SuppressWarnings("unchecked")
	            Entry<K,V> e = (Entry<K,V>)tab[index];
	            for(Entry<K,V> prev = null; e != null; prev = e, e = e.next) {
	                if (e.hash==hash && e.equals(entry)) {
	                    modCount++;
	                    if (prev != null)
	                        prev.next = e.next;
	                    else
	                        tab[index] = e.next;

	                    count--;
	                    e.value = null;
	                    return true;
	                }
	            }
	            return false;
	        }

	        public int size() {
	            return count;
	        }

	        public void clear() {
	            Hashtable.this.clear();
	        }
	    }
	 // 返回一个被synchronizedCollection封装后的ValueCollection对象    
	    // synchronizedCollection封装的目的是对ValueCollection的所有方法都添加synchronized，实现多线程同步 
	    public Collection<V> values() {
	        if (values==null)
	            values = Collections.synchronizedCollection(new ValueCollection(),
	                                                        this);
	        return values;
	    }
	    // Hashtable的value的Collection集合。    
	    // ValueCollection继承于AbstractCollection，所以，ValueCollection中的元素可以重复的。
	    private class ValueCollection extends AbstractCollection<V> {
	        public Iterator<V> iterator() {
	            return getIterator(VALUES);
	        }
	        public int size() {
	            return count;
	        }
	        public boolean contains(Object o) {
	            return containsValue(o);
	        }
	        public void clear() {
	            Hashtable.this.clear();
	        }
	    }
	    // 重新equals()函数    
	    // 若两个Hashtable的所有key-value键值对都相等，则判断它们两个相等
	    public synchronized boolean equals(Object o) {
	        if (o == this)
	            return true;

	        if (!(o instanceof Map))
	            return false;
	        Map<?,?> t = (Map<?,?>) o;
	        if (t.size() != size())
	            return false;

	        try {
	        	// 通过迭代器依次取出当前Hashtable的key-value键值对    
	            // 并判断该键值对，存在于Hashtable中。    
	            // 若不存在，则立即返回false；否则，遍历完“当前Hashtable”并返回true。
	            Iterator<Map.Entry<K,V>> i = entrySet().iterator();
	            while (i.hasNext()) {
	                Map.Entry<K,V> e = i.next();
	                K key = e.getKey();
	                V value = e.getValue();
	                if (value == null) {
	                    if (!(t.get(key)==null && t.containsKey(key)))
	                        return false;
	                } else {
	                    if (!value.equals(t.get(key)))
	                        return false;
	                }
	            }
	        } catch (ClassCastException unused)   {
	            return false;
	        } catch (NullPointerException unused) {
	            return false;
	        }

	        return true;
	    }
	    // 计算Entry的hashCode    
	    // 若 Hashtable的实际大小为0 或者 加载因子<0，则返回0。    
	    // 否则，返回“Hashtable中的每个Entry的key和value的异或值 的总和”。 
	    public synchronized int hashCode() {
	    	 int h = 0;
	         if (count == 0 || loadFactor < 0)
	             return h;  // Returns zero

	         loadFactor = -loadFactor;  // Mark hashCode computation in progress
	         Entry<?,?>[] tab = table;
	         for (Entry<?,?> entry : tab) {
	             while (entry != null) {
	                 h += entry.hashCode();
	                 entry = entry.next;
	             }
	         }

	         loadFactor = -loadFactor;  // Mark hashCode computation complete

	         return h;
	     }
	    @Override
	    public synchronized V getOrDefault(Object key, V defaultValue) {
	        V result = get(key);
	        return (null == result) ? defaultValue : result;
	    }

	    @SuppressWarnings("unchecked")
	    @Override
	    public synchronized void forEach(BiConsumer<? super K, ? super V> action) {
	        Objects.requireNonNull(action);     // explicit check required in case
	                                            // table is empty.
	        final int expectedModCount = modCount;

	        Entry<?, ?>[] tab = table;
	        for (Entry<?, ?> entry : tab) {
	            while (entry != null) {
	                action.accept((K)entry.key, (V)entry.value);
	                entry = entry.next;

	                if (expectedModCount != modCount) {
	                    throw new ConcurrentModificationException();
	                }
	            }
	        }
	    }

	    @SuppressWarnings("unchecked")
	    @Override
	    public synchronized void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
	        Objects.requireNonNull(function);     // explicit check required in case
	                                              // table is empty.
	        final int expectedModCount = modCount;

	        Entry<K, V>[] tab = (Entry<K, V>[])table;
	        for (Entry<K, V> entry : tab) {
	            while (entry != null) {
	                entry.value = Objects.requireNonNull(
	                    function.apply(entry.key, entry.value));
	                entry = entry.next;

	                if (expectedModCount != modCount) {
	                    throw new ConcurrentModificationException();
	                }
	            }
	        }
	    }

	    @Override
	    public synchronized V putIfAbsent(K key, V value) {
	        Objects.requireNonNull(value);

	        // Makes sure the key is not already in the hashtable.
	        Entry<?,?> tab[] = table;
	        int hash = key.hashCode();
	        int index = (hash & 0x7FFFFFFF) % tab.length;
	        @SuppressWarnings("unchecked")
	        Entry<K,V> entry = (Entry<K,V>)tab[index];
	        for (; entry != null; entry = entry.next) {
	            if ((entry.hash == hash) && entry.key.equals(key)) {
	                V old = entry.value;
	                if (old == null) {
	                    entry.value = value;
	                }
	                return old;
	            }
	        }

	        addEntry(hash, key, value, index);
	        return null;
	    }

	    @Override
	    public synchronized boolean remove(Object key, Object value) {
	        Objects.requireNonNull(value);

	        Entry<?,?> tab[] = table;
	        int hash = key.hashCode();
	        int index = (hash & 0x7FFFFFFF) % tab.length;
	        @SuppressWarnings("unchecked")
	        Entry<K,V> e = (Entry<K,V>)tab[index];
	        for (Entry<K,V> prev = null; e != null; prev = e, e = e.next) {
	            if ((e.hash == hash) && e.key.equals(key) && e.value.equals(value)) {
	                modCount++;
	                if (prev != null) {
	                    prev.next = e.next;
	                } else {
	                    tab[index] = e.next;
	                }
	                count--;
	                e.value = null;
	                return true;
	            }
	        }
	        return false;
	    }
	    //替换掉指定key-value位置的指成功未true否则false
	    @Override
	    public synchronized boolean replace(K key, V oldValue, V newValue) {
	        Objects.requireNonNull(oldValue);
	        Objects.requireNonNull(newValue);
	        Entry<?,?> tab[] = table;
	        int hash = key.hashCode();
	        int index = (hash & 0x7FFFFFFF) % tab.length;
	        @SuppressWarnings("unchecked")
	        Entry<K,V> e = (Entry<K,V>)tab[index];
	        for (; e != null; e = e.next) {
	            if ((e.hash == hash) && e.key.equals(key)) {
	                if (e.value.equals(oldValue)) {
	                    e.value = newValue;
	                    return true;
	                } else {
	                    return false;
	                }
	            }
	        }
	        return false;
	    }

	    //替换指定key位置的值，并返回被替换掉的值
	    @Override
	    public synchronized V replace(K key, V value) {
	        Objects.requireNonNull(value);
	        Entry<?,?> tab[] = table;
	        int hash = key.hashCode();
	        int index = (hash & 0x7FFFFFFF) % tab.length;
	        @SuppressWarnings("unchecked")
	        Entry<K,V> e = (Entry<K,V>)tab[index];
	        for (; e != null; e = e.next) {
	            if ((e.hash == hash) && e.key.equals(key)) {
	                V oldValue = e.value;
	                e.value = value;
	                return oldValue;
	            }
	        }
	        return null;
	    }

	    @Override
	    public synchronized V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
	        Objects.requireNonNull(mappingFunction);

	        Entry<?,?> tab[] = table;
	        int hash = key.hashCode();
	        int index = (hash & 0x7FFFFFFF) % tab.length;
	        @SuppressWarnings("unchecked")
	        Entry<K,V> e = (Entry<K,V>)tab[index];
	        for (; e != null; e = e.next) {
	            if (e.hash == hash && e.key.equals(key)) {
	                // Hashtable not accept null value
	                return e.value;
	            }
	        }

	        V newValue = mappingFunction.apply(key);
	        if (newValue != null) {
	            addEntry(hash, key, newValue, index);
	        }

	        return newValue;
	    }

	    @Override
	    public synchronized V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
	        Objects.requireNonNull(remappingFunction);

	        Entry<?,?> tab[] = table;
	        int hash = key.hashCode();
	        int index = (hash & 0x7FFFFFFF) % tab.length;
	        @SuppressWarnings("unchecked")
	        Entry<K,V> e = (Entry<K,V>)tab[index];
	        for (Entry<K,V> prev = null; e != null; prev = e, e = e.next) {
	            if (e.hash == hash && e.key.equals(key)) {
	                V newValue = remappingFunction.apply(key, e.value);
	                if (newValue == null) {
	                    modCount++;
	                    if (prev != null) {
	                        prev.next = e.next;
	                    } else {
	                        tab[index] = e.next;
	                    }
	                    count--;
	                } else {
	                    e.value = newValue;
	                }
	                return newValue;
	            }
	        }
	        return null;
	    }

	    @Override
	    public synchronized V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
	        Objects.requireNonNull(remappingFunction);

	        Entry<?,?> tab[] = table;
	        int hash = key.hashCode();
	        int index = (hash & 0x7FFFFFFF) % tab.length;
	        @SuppressWarnings("unchecked")
	        Entry<K,V> e = (Entry<K,V>)tab[index];
	        for (Entry<K,V> prev = null; e != null; prev = e, e = e.next) {
	            if (e.hash == hash && Objects.equals(e.key, key)) {
	                V newValue = remappingFunction.apply(key, e.value);
	                if (newValue == null) {
	                    modCount++;
	                    if (prev != null) {
	                        prev.next = e.next;
	                    } else {
	                        tab[index] = e.next;
	                    }
	                    count--;
	                } else {
	                    e.value = newValue;
	                }
	                return newValue;
	            }
	        }

	        V newValue = remappingFunction.apply(key, null);
	        if (newValue != null) {
	            addEntry(hash, key, newValue, index);
	        }

	        return newValue;
	    }

	    @Override
	    public synchronized V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
	        Objects.requireNonNull(remappingFunction);

	        Entry<?,?> tab[] = table;
	        int hash = key.hashCode();
	        int index = (hash & 0x7FFFFFFF) % tab.length;
	        @SuppressWarnings("unchecked")
	        Entry<K,V> e = (Entry<K,V>)tab[index];
	        for (Entry<K,V> prev = null; e != null; prev = e, e = e.next) {
	            if (e.hash == hash && e.key.equals(key)) {
	                V newValue = remappingFunction.apply(e.value, value);
	                if (newValue == null) {
	                    modCount++;
	                    if (prev != null) {
	                        prev.next = e.next;
	                    } else {
	                        tab[index] = e.next;
	                    }
	                    count--;
	                } else {
	                    e.value = newValue;
	                }
	                return newValue;
	            }
	        }

	        if (value != null) {
	            addEntry(hash, key, value, index);
	        }

	        return value;
	    }
	    // java.io.Serializable的写入函数    
	    // 将Hashtable的“总的容量，实际容量，所有的Entry”都写入到输出流中    
	    private void writeObject(java.io.ObjectOutputStream s)
	            throws IOException {
	        Entry<Object, Object> entryStack = null;

	        synchronized (this) {
	            // Write out the threshold and loadFactor
	            s.defaultWriteObject();

	            // Write out the length and count of elements
	            s.writeInt(table.length);
	            s.writeInt(count);

	            // Stack copies of the entries in the table
	            for (int index = 0; index < table.length; index++) {
	                Entry<?,?> entry = table[index];

	                while (entry != null) {
	                    entryStack =
	                        new Entry<>(0, entry.key, entry.value, entryStack);
	                    entry = entry.next;
	                }
	            }
	        }

	        // Write out the key/value objects from the stacked entries
	        while (entryStack != null) {
	            s.writeObject(entryStack.key);
	            s.writeObject(entryStack.value);
	            entryStack = entryStack.next;
	        }
	    }
	    // java.io.Serializable的读取函数：根据写入方式读出    
	    // 将Hashtable的“总的容量，实际容量，所有的Entry”依次读出 
	    private void readObject(java.io.ObjectInputStream s)
	         throws IOException, ClassNotFoundException
	    {
	        // Read in the threshold and loadFactor
	        s.defaultReadObject();

	        // Validate loadFactor (ignore threshold - it will be re-computed)
	        if (loadFactor <= 0 || Float.isNaN(loadFactor))
	            throw new StreamCorruptedException("Illegal Load: " + loadFactor);

	        // Read the original length of the array and number of elements
	        int origlength = s.readInt();
	        int elements = s.readInt();

	        // Validate # of elements
	        if (elements < 0)
	            throw new StreamCorruptedException("Illegal # of Elements: " + elements);


	        origlength = Math.max(origlength, (int)(elements / loadFactor) + 1);


	        int length = (int)((elements + elements / 20) / loadFactor) + 3;
	        if (length > elements && (length & 1) == 0)
	            length--;
	        length = Math.min(length, origlength);
	        table = new Entry<?,?>[length];
	        threshold = (int)Math.min(length * loadFactor, MAX_ARRAY_SIZE + 1);
	        count = 0;

	        // Read the number of elements and then all the key/value objects
	        for (; elements > 0; elements--) {
	            @SuppressWarnings("unchecked")
	                K key = (K)s.readObject();
	            @SuppressWarnings("unchecked")
	                V value = (V)s.readObject();
	            // sync is eliminated for performance
	            reconstitutionPut(table, key, value);
	        }
	    }


	    private void reconstitutionPut(Entry<?,?>[] tab, K key, V value)
	        throws StreamCorruptedException
	    {
	        if (value == null) {
	            throw new java.io.StreamCorruptedException();
	        }
	        // Makes sure the key is not already in the hashtable.
	        // This should not happen in deserialized version.
	        int hash = key.hashCode();
	        int index = (hash & 0x7FFFFFFF) % tab.length;
	        for (Entry<?,?> e = tab[index] ; e != null ; e = e.next) {
	            if ((e.hash == hash) && e.key.equals(key)) {
	                throw new java.io.StreamCorruptedException();
	            }
	        }
	        // Creates the new entry.
	        @SuppressWarnings("unchecked")
	            Entry<K,V> e = (Entry<K,V>)tab[index];
	        tab[index] = new Entry<>(hash, key, value, e);
	        count++;
	    }

	     // Hashtable的Entry节点，它本质上是一个单向链表。    
	    // 也因此，我们才能推断出Hashtable是由拉链法实现的散列表    
	    private static class Entry<K,V> implements Map.Entry<K,V> {
	    	 // 哈希值   
	        final int hash;
	        final K key;
	        V value;
	        Entry<K,V> next;  // 指向的下一个Entry，即链表的下一个节点    

	        protected Entry(int hash, K key, V value, Entry<K,V> next) {
	            this.hash = hash;
	            this.key =  key;
	            this.value = value;
	            this.next = next;
	        }

	        @SuppressWarnings("unchecked")
	        protected Object clone() {
	            return new Entry<>(hash, key, value,
	                                  (next==null ? null : (Entry<K,V>) next.clone()));
	        }

	        // Map.Entry Ops

	        public K getKey() {
	            return key;
	        }

	        public V getValue() {
	            return value;
	        }
	        // 设置value。若value是null，则抛出异常。    
	        public V setValue(V value) {
	            if (value == null)
	                throw new NullPointerException();

	            V oldValue = this.value;
	            this.value = value;
	            return oldValue;
	        }
	        // 覆盖equals()方法，判断两个Entry是否相等。    
	        // 若两个Entry的key和value都相等，则认为它们相等。    
	        public boolean equals(Object o) {
	            if (!(o instanceof Map.Entry))
	                return false;
	            Map.Entry<?,?> e = (Map.Entry<?,?>)o;

	            return (key==null ? e.getKey()==null : key.equals(e.getKey())) &&
	               (value==null ? e.getValue()==null : value.equals(e.getValue()));
	        }

	        public int hashCode() {
	            return hash ^ Objects.hashCode(value);
	        }

	        public String toString() {
	            return key.toString()+"="+value.toString();
	        }
	    }

	    // Types of Enumerations/Iterations
	    private static final int KEYS = 0;
	    private static final int VALUES = 1;
	    private static final int ENTRIES = 2;

	    // Enumerator的作用是提供了“通过elements()遍历Hashtable的接口” 和 “通过entrySet()遍历Hashtable的接
	    private class Enumerator<T> implements Enumeration<T>, Iterator<T> {
	    	  // 指向Hashtable的table   
	        Entry<?,?>[] table = Hashtable.this.table;
	        int index = table.length; // Hashtable的总的大小    
	        Entry<?,?> entry;
	        Entry<?,?> lastReturned;
	        int type;
	        // Enumerator是 “迭代器(Iterator)” 还是 “枚举类(Enumeration)”的标志    
	        // iterator为true，表示它是迭代器；否则，是枚举类。    
	        boolean iterator;
	        // 在将Enumerator当作迭代器使用时会用到，用来实现fail-fast机制。  
	        protected int expectedModCount = modCount;

	        Enumerator(int type, boolean iterator) {
	            this.type = type;
	            this.iterator = iterator;
	        }
	        // 从遍历table的数组的末尾向前查找，直到找到不为null的Entry。    
	        public boolean hasMoreElements() {
	            Entry<?,?> e = entry;
	            int i = index;
	            Entry<?,?>[] t = table;
	            /* Use locals for faster loop iteration */
	            while (e == null && i > 0) {
	                e = t[--i];
	            }
	            entry = e;
	            index = i;
	            return e != null;
	        }
	        // 获取下一个元素    
	        // 注意：从hasMoreElements() 和nextElement() 可以看出“Hashtable的elements()遍历方式”    
	        // 首先，从后向前的遍历table数组。table数组的每个节点都是一个单向链表(Entry)。    
	        // 然后，依次向后遍历单向链表Entry。
	        @SuppressWarnings("unchecked")
	        public T nextElement() {
	            Entry<?,?> et = entry;
	            int i = index;
	            Entry<?,?>[] t = table;
	            /* Use locals for faster loop iteration */
	            while (et == null && i > 0) {
	                et = t[--i];
	            }
	            entry = et;
	            index = i;
	            if (et != null) {
	                Entry<?,?> e = lastReturned = entry;
	                entry = e.next;
	                return type == KEYS ? (T)e.key : (type == VALUES ? (T)e.value : (T)e);
	            }
	            throw new NoSuchElementException("Hashtable Enumerator");
	        }

	        // 迭代器Iterator的判断是否存在下一个元素    
	        // 实际上，它是调用的hasMoreElements()    
	        public boolean hasNext() {
	            return hasMoreElements();
	        }
	        // 迭代器获取下一个元素    
	        // 实际上，它是调用的nextElement()  
	        public T next() {
	            if (modCount != expectedModCount)
	                throw new ConcurrentModificationException();
	            return nextElement();
	        }
	     // 迭代器的remove()接口。    
	        // 首先，它在table数组中找出要删除元素所在的Entry，    
	        // 然后，删除单向链表Entry中的元素。   
	        public void remove() {
	            if (!iterator)
	                throw new UnsupportedOperationException();
	            if (lastReturned == null)
	                throw new IllegalStateException("Hashtable Enumerator");
	            if (modCount != expectedModCount)
	                throw new ConcurrentModificationException();

	            synchronized(Hashtable.this) {
	                Entry<?,?>[] tab = Hashtable.this.table;
	                int index = (lastReturned.hash & 0x7FFFFFFF) % tab.length;

	                @SuppressWarnings("unchecked")
	                Entry<K,V> e = (Entry<K,V>)tab[index];
	                for(Entry<K,V> prev = null; e != null; prev = e, e = e.next) {
	                    if (e == lastReturned) {
	                        modCount++;
	                        expectedModCount++;
	                        if (prev == null)
	                            tab[index] = e.next;
	                        else
	                            prev.next = e.next;
	                        count--;
	                        lastReturned = null;
	                        return;
	                    }
	                }
	                throw new ConcurrentModificationException();
	            }
	        }
	    }
}
