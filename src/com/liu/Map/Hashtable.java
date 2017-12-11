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
	 // ����key-value�����顣    
    // Hashtableͬ�����õ���������ͻ��ÿһ��Entry��������һ����������
	private transient Entry<?,?>[] table;
	 private transient int count; // Hashtable�м�ֵ�Ե�����    
	 private int threshold;   // ��ֵ�������ж��Ƿ���Ҫ����Hashtable��������threshold = ����*�������ӣ�    
	 private float loadFactor;  // ��������    
	 private transient int modCount = 0; // Hashtable���ı�Ĵ���������fail-fast���Ƶ�ʵ��  
	  private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
	 private static final long serialVersionUID = 1421746759512286392L;
	// ָ����������С���͡��������ӡ��Ĺ��캯��   
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
	// ָ����������С���Ĺ��캯�� 
	  public Hashtable(int initialCapacity) {
	        this(initialCapacity, 0.75f);
	    }
	// Ĭ�Ϲ��캯����
	  public Hashtable() {
		// Ĭ�Ϲ��캯����ָ����������С��11������������0.75    
	        this(11, 0.75f);
	    }
	// ��������Map���Ĺ��캯�� 
	  public Hashtable(Map<? extends K, ? extends V> t) {
	        this(Math.max(2*t.size(), 11), 0.75f);
	     // ������Map����ȫ��Ԫ�ض���ӵ�Hashtable��  
	        putAll(t);
	    }
	  //Hashtable�Ĵ�С
	  public synchronized int size() {
	        return count;
	    }
	  //�ж��Ƿ�Ϊ��
	  public synchronized boolean isEmpty() {
	        return count == 0;
	    }
	// ���ء�����key����ö�ٶ���
	  public synchronized Enumeration<K> keys() {
	        return this.<K>getEnumeration(KEYS);
	    }
	  // ���ء�����value����ö�ٶ���  
	  public synchronized Enumeration<V> elements() {
	        return this.<V>getEnumeration(VALUES);
	    }
	// �ж�Hashtable�Ƿ������ֵ(value)��   
	  public synchronized boolean contains(Object value) {
		   //ע�⣬Hashtable�е�value������null��    
	        // ����null�Ļ����׳��쳣!  
	        if (value == null) {
	            throw new NullPointerException();
	        }
	     // �Ӻ���ǰ����table�����е�Ԫ��(Entry)    
	        // ����ÿ��Entry(��������)������������жϽڵ��ֵ�Ƿ����value  
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
	  // �ж�Hashtable�Ƿ����key    
	  public boolean containsValue(Object value) {
	        return contains(value);
	    }
	  // �ж�Hashtable�Ƿ����key    
	  public synchronized boolean containsKey(Object key) {
	        Entry<?,?> tab[] = table;
	        //����hashֵ��ֱ����key��hashCode����  
	        int hash = key.hashCode();
	        // �����������е�����ֵ   
	        int index = (hash & 0x7FFFFFFF) % tab.length;
	        // �ҵ���key��Ӧ��Entry(����)����Ȼ�����������ҳ�����ϣֵ���͡���ֵ����key����ȵ�Ԫ��    
	        for (Entry<?,?> e = tab[index] ; e != null ; e = e.next) {
	            if ((e.hash == hash) && e.key.equals(key)) {
	                return true;
	            }
	        }
	        return false;
	    }
	  // ����key��Ӧ��value��û�еĻ�����null    
	   public synchronized V get(Object key) {
	        Entry<?,?> tab[] = table;
	        int hash = key.hashCode();
	     // ��������ֵ�� 
	        int index = (hash & 0x7FFFFFFF) % tab.length;
	        // �ҵ���key��Ӧ��Entry(����)����Ȼ�����������ҳ�����ϣֵ���͡���ֵ����key����ȵ�Ԫ��    
	        for (Entry<?,?> e = tab[index] ; e != null ; e = e.next) {
	            if ((e.hash == hash) && e.key.equals(key)) {
	                return (V)e.value;
	            }
	        }
	        return null;
	    }
	// ����Hashtable�ĳ��ȣ������ȱ��ԭ����2��+1   
	   @SuppressWarnings("unchecked")
	    protected void rehash() {
	        int oldCapacity = table.length;
	        Entry<?,?>[] oldMap = table;

	        //������������С��Entry����  
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
	      //�����ɵ�Hashtable���е�Ԫ�ظ��Ƶ����µ�Hashtable���� 
	        for (int i = oldCapacity ; i-- > 0 ;) {
	            for (Entry<K,V> old = (Entry<K,V>)oldMap[i] ; old != null ; ) {
	                Entry<K,V> e = old;
	                old = old.next;
	                //���¼���index  
	                int index = (e.hash & 0x7FFFFFFF) % newCapacity;
	                e.next = (Entry<K,V>)newMap[index];
	                newMap[index] = e;
	            }
	        }
	    }
	   // ����key-value����ӵ�Hashtable�� ����ʵ�� 
	   private void addEntry(int hash, K key, V value, int index) {
		   // ����Hashtable�в����ڼ�Ϊkey�ļ�ֵ�ԡ���  
	        // �����޸�ͳ������+1    
	        modCount++; 
	        Entry<?,?> tab[] = table;
	    //  ����Hashtableʵ�������� > ����ֵ��(��ֵ=�ܵ����� * ��������)    
	        //  �����Hashtable�Ĵ�С    
	        if (count >= threshold) {
	            // Rehash the table if the threshold is exceeded
	            rehash();

	            tab = table;
	            hash = key.hashCode();
	            index = (hash & 0x7FFFFFFF) % tab.length;
	        }

	        //���µ�key-value�Բ��뵽tab[index]�����������ͷ��㣩  
	        @SuppressWarnings("unchecked")
	        Entry<K,V> e = (Entry<K,V>) tab[index];
	        tab[index] = new Entry<>(hash, key, value, e);
	        count++;
	    }
	   // ����key-value����ӵ�Hashtable��    
	   public synchronized V put(K key, V value) {
		   // Hashtable�в��ܲ���valueΪnull��Ԫ�أ�����    
	        if (value == null) {
	            throw new NullPointerException();
	        }

	     // ����Hashtable���Ѵ��ڼ�Ϊkey�ļ�ֵ�ԡ���    
	        // ���á��µ�value���滻���ɵ�value��
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
	        //�������ʵ�巽��
	        addEntry(hash, key, value, index);
	        return null;
	    }
	   // ɾ��Hashtable�м�Ϊkey��Ԫ��   
	   public synchronized V remove(Object key) {
	        Entry<?,?> tab[] = table;
	        int hash = key.hashCode();
	        int index = (hash & 0x7FFFFFFF) % tab.length;
	        @SuppressWarnings("unchecked")
	        Entry<K,V> e = (Entry<K,V>)tab[index];
	        //��table[index]�������ҳ�Ҫɾ���Ľڵ㣬��ɾ���ýڵ㡣  
	        //��Ϊ�ǵ��������Ҫ������ɾ�ڵ��ǰһ���ڵ㣬������Ч��ɾ���ڵ�  
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
	   // ����Map(t)������ȫ��Ԫ����һ��ӵ�Hashtable��    
	   public synchronized void putAll(Map<? extends K, ? extends V> t) {
	        for (Map.Entry<? extends K, ? extends V> e : t.entrySet())
	            put(e.getKey(), e.getValue());
	    }
	   // ���Hashtable    
	    // ��Hashtable��table�����ֵȫ����Ϊnull    
	    public synchronized void clear() {
	        Entry<?,?> tab[] = table;
	        modCount++;
	        for (int index = tab.length; --index >= 0; )
	            tab[index] = null;
	        count = 0;
	    }
	 // ��¡һ��Hashtable������Object����ʽ���ء�
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
	   // ��Map��String�ķ�ʽ����
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
	    // ��ȡHashtable��ö�������    
	    // ��Hashtable��ʵ�ʴ�СΪ0,�򷵻ء���ö���ࡱ����    
	    // ���򣬷���������Enumerator�Ķ���   
	    private <T> Enumeration<T> getEnumeration(int type) {
	        if (count == 0) {
	            return Collections.emptyEnumeration();
	        } else {
	            return new Enumerator<>(type, false);
	        }
	    }
	 // ��ȡHashtable�ĵ�����    
	    // ��Hashtable��ʵ�ʴ�СΪ0,�򷵻ء��յ�����������    
	    // ���򣬷���������Enumerator�Ķ���(Enumeratorʵ���˵�������ö�������ӿ�) 
	    private <T> Iterator<T> getIterator(int type) {
	        if (count == 0) {
	            return Collections.emptyIterator();
	        } else {
	            return new Enumerator<>(type, true);
	        }
	    }
	    // Hashtable�ġ�key�ļ��ϡ�������һ��Set��û���ظ�Ԫ��  
	    private transient volatile Set<K> keySet;
	    // Hashtable�ġ�key-value�ļ��ϡ�������һ��Set��û���ظ�Ԫ��    
	    private transient volatile Set<Map.Entry<K,V>> entrySet;
	 // Hashtable�ġ�key-value�ļ��ϡ�������һ��Collection���������ظ�Ԫ��  
	    private transient volatile Collection<V> values;
	 // ����һ����synchronizedSet��װ���KeySet����    
	    // synchronizedSet��װ��Ŀ���Ƕ�KeySet�����з��������synchronized��ʵ�ֶ��߳�ͬ��
	    public Set<K> keySet() {
	        if (keySet == null)
	            keySet = Collections.synchronizedSet(new KeySet(), this);
	        return keySet;
	    }
	    // Hashtable��Key��Set���ϡ�    
	    // KeySet�̳���AbstractSet�����ԣ�KeySet�е�Ԫ��û���ظ��ġ�    
	    private class KeySet extends AbstractSet<K> {
	        public Iterator<K> iterator() {//��������
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
	 // ����һ����synchronizedSet��װ���EntrySet����    
	    // synchronizedSet��װ��Ŀ���Ƕ�EntrySet�����з��������synchronized��ʵ�ֶ��߳�ͬ��    
	    public Set<Map.Entry<K,V>> entrySet() {
	        if (entrySet==null)
	            entrySet = Collections.synchronizedSet(new EntrySet(), this);
	        return entrySet;
	    }
	    // Hashtable��Entry��Set���ϡ�    
	    // EntrySet�̳���AbstractSet�����ԣ�EntrySet�е�Ԫ��û���ظ��ġ�  
	    private class EntrySet extends AbstractSet<Map.Entry<K,V>> {
	        public Iterator<Map.Entry<K,V>> iterator() {
	            return getIterator(ENTRIES);
	        }

	        public boolean add(Map.Entry<K,V> o) {
	            return super.add(o);
	        }
	        // ����EntrySet���Ƿ����Object(0)    
	        // ���ȣ���table���ҵ�o��Ӧ��Entry����    
	        // Ȼ�󣬲���Entry�������Ƿ����Object    
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
	        // ɾ��Ԫ��Object(0)    
	        // ���ȣ���table���ҵ�o��Ӧ��Entry����  
	        // Ȼ��ɾ�������е�Ԫ��Object  
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
	 // ����һ����synchronizedCollection��װ���ValueCollection����    
	    // synchronizedCollection��װ��Ŀ���Ƕ�ValueCollection�����з��������synchronized��ʵ�ֶ��߳�ͬ�� 
	    public Collection<V> values() {
	        if (values==null)
	            values = Collections.synchronizedCollection(new ValueCollection(),
	                                                        this);
	        return values;
	    }
	    // Hashtable��value��Collection���ϡ�    
	    // ValueCollection�̳���AbstractCollection�����ԣ�ValueCollection�е�Ԫ�ؿ����ظ��ġ�
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
	    // ����equals()����    
	    // ������Hashtable������key-value��ֵ�Զ���ȣ����ж������������
	    public synchronized boolean equals(Object o) {
	        if (o == this)
	            return true;

	        if (!(o instanceof Map))
	            return false;
	        Map<?,?> t = (Map<?,?>) o;
	        if (t.size() != size())
	            return false;

	        try {
	        	// ͨ������������ȡ����ǰHashtable��key-value��ֵ��    
	            // ���жϸü�ֵ�ԣ�������Hashtable�С�    
	            // �������ڣ�����������false�����򣬱����ꡰ��ǰHashtable��������true��
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
	    // ����Entry��hashCode    
	    // �� Hashtable��ʵ�ʴ�СΪ0 ���� ��������<0���򷵻�0��    
	    // ���򣬷��ء�Hashtable�е�ÿ��Entry��key��value�����ֵ ���ܺ͡��� 
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
	    //�滻��ָ��key-valueλ�õ�ָ�ɹ�δtrue����false
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

	    //�滻ָ��keyλ�õ�ֵ�������ر��滻����ֵ
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
	    // java.io.Serializable��д�뺯��    
	    // ��Hashtable�ġ��ܵ�������ʵ�����������е�Entry����д�뵽�������    
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
	    // java.io.Serializable�Ķ�ȡ����������д�뷽ʽ����    
	    // ��Hashtable�ġ��ܵ�������ʵ�����������е�Entry�����ζ��� 
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

	     // Hashtable��Entry�ڵ㣬����������һ����������    
	    // Ҳ��ˣ����ǲ����ƶϳ�Hashtable����������ʵ�ֵ�ɢ�б�    
	    private static class Entry<K,V> implements Map.Entry<K,V> {
	    	 // ��ϣֵ   
	        final int hash;
	        final K key;
	        V value;
	        Entry<K,V> next;  // ָ�����һ��Entry�����������һ���ڵ�    

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
	        // ����value����value��null�����׳��쳣��    
	        public V setValue(V value) {
	            if (value == null)
	                throw new NullPointerException();

	            V oldValue = this.value;
	            this.value = value;
	            return oldValue;
	        }
	        // ����equals()�������ж�����Entry�Ƿ���ȡ�    
	        // ������Entry��key��value����ȣ�����Ϊ������ȡ�    
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

	    // Enumerator���������ṩ�ˡ�ͨ��elements()����Hashtable�Ľӿڡ� �� ��ͨ��entrySet()����Hashtable�Ľ�
	    private class Enumerator<T> implements Enumeration<T>, Iterator<T> {
	    	  // ָ��Hashtable��table   
	        Entry<?,?>[] table = Hashtable.this.table;
	        int index = table.length; // Hashtable���ܵĴ�С    
	        Entry<?,?> entry;
	        Entry<?,?> lastReturned;
	        int type;
	        // Enumerator�� ��������(Iterator)�� ���� ��ö����(Enumeration)���ı�־    
	        // iteratorΪtrue����ʾ���ǵ�������������ö���ࡣ    
	        boolean iterator;
	        // �ڽ�Enumerator����������ʹ��ʱ���õ�������ʵ��fail-fast���ơ�  
	        protected int expectedModCount = modCount;

	        Enumerator(int type, boolean iterator) {
	            this.type = type;
	            this.iterator = iterator;
	        }
	        // �ӱ���table�������ĩβ��ǰ���ң�ֱ���ҵ���Ϊnull��Entry��    
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
	        // ��ȡ��һ��Ԫ��    
	        // ע�⣺��hasMoreElements() ��nextElement() ���Կ�����Hashtable��elements()������ʽ��    
	        // ���ȣ��Ӻ���ǰ�ı���table���顣table�����ÿ���ڵ㶼��һ����������(Entry)��    
	        // Ȼ��������������������Entry��
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

	        // ������Iterator���ж��Ƿ������һ��Ԫ��    
	        // ʵ���ϣ����ǵ��õ�hasMoreElements()    
	        public boolean hasNext() {
	            return hasMoreElements();
	        }
	        // ��������ȡ��һ��Ԫ��    
	        // ʵ���ϣ����ǵ��õ�nextElement()  
	        public T next() {
	            if (modCount != expectedModCount)
	                throw new ConcurrentModificationException();
	            return nextElement();
	        }
	     // ��������remove()�ӿڡ�    
	        // ���ȣ�����table�������ҳ�Ҫɾ��Ԫ�����ڵ�Entry��    
	        // Ȼ��ɾ����������Entry�е�Ԫ�ء�   
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
