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
	//�Ƚ�������ΪTreeMap������ģ�ͨ��comparator�ӿ����ǿ��Զ�TreeMap���ڲ�������о��ܵĿ���
	 private final Comparator<? super K> comparator;
	//TreeMap��-�ڽڵ㣬ΪTreeMap���ڲ���
	 private transient Entry<K,V> root;
	 //������С
	 private transient int size = 0;
	//TreeMap�޸Ĵ���
	 private transient int modCount = 0;
	 //�������ɫ����ɫ
	 private static final boolean RED   = false;
	 //���ɫ��ɫ
	 private static final boolean BLACK = true;
	 //�޲ι��죬comparator=null,Ĭ�ϰ�����Ȼ˳������
	 public TreeMap() {
	        comparator = null;
	    }
	 //�����Զ���Ƚ����Ĺ��캯��
	    public TreeMap(Comparator<? super K> comparator) {
	        this.comparator = comparator;
	    }
	    //ָ��Map�Ĺ���
	    public TreeMap(Map<? extends K, ? extends V> m) {
	        comparator = null;
	        putAll(m);
	    }
	  //  ������֪��SortedMap����ΪTreeMap
	    public TreeMap(SortedMap<K, ? extends V> m) {
	        comparator = m.comparator();
	        try {
	            buildFromSorted(m.size(), m.entrySet().iterator(), null, null);
	        } catch (java.io.IOException cannotHappen) {
	        } catch (ClassNotFoundException cannotHappen) {
	        }
	    }
	    public int size() {//��ȡ��С
	        return size;
	    }
	    public boolean containsKey(Object key) {//�ж�key�Ƿ����
	        return getEntry(key) != null;
	    }
	    public boolean containsValue(Object value) {//�ж�ֵ�Ƿ����
	        for (Entry<K,V> e = getFirstEntry(); e != null; e = successor(e))//��������飬successor�����̽��
	            if (valEquals(value, e.value))//ͨ���Ա�ֵ�����ж�
	                return true;
	        return false;
	    }
	    public V get(Object key) {//key��ֵ
	        Entry<K,V> p = getEntry(key);
	        return (p==null ? null : p.value);
	    }
	    public Comparator<? super K> comparator() {//��ȡ�Ƚ���
	        return comparator;
	    }
	    public K firstKey() {//��ȡ��һ��key
	        return key(getFirstEntry());
	    }
	    public K lastKey() {//���һ��key
	        return key(getLastEntry());
	    }
	    public void putAll(Map<? extends K, ? extends V> map) {//��map���д�ŵ���Map��
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
	  //����ָ����key��ȡ�ڵ�
	    final Entry<K,V> getEntry(Object key) {//
	        // Offload comparator-based version for sake of performance
	        if (comparator != null)
	        	//�бȽ���
	            return getEntryUsingComparator(key);
	        if (key == null)
	            throw new NullPointerException();
	        @SuppressWarnings("unchecked")
	            Comparable<? super K> k = (Comparable<? super K>) key;//û�бȽ�����key��Ҫʵ��Comparable�ӿ�
	        Entry<K,V> p = root;//��ȡroot�ڵ�
	      //ѭ����k
	        while (p != null) {
	            int cmp = k.compareTo(p.key);    //��p�ڵ㿪ʼ�Ƚϣ�
	            if (cmp < 0)
	                p = p.left; //�����ǰ�ڵ��key����p�ڵ��keyС���ƶ�������
	            else if (cmp > 0)
	                p = p.right;//�����ǰ�ڵ��key����p�ڵ��key���ƶ����Һ���
	            else
	                return p;  //�����ȣ�����p��
	        }
	        return null;
	    }
	    //ͨ�������Դ��ıȽ������л�ȡʵ��
	    final Entry<K,V> getEntryUsingComparator(Object key) {
	        @SuppressWarnings("unchecked")
	            K k = (K) key;
	        //��ȡ�Ƚ���
	        Comparator<? super K> cpr = comparator;
	        if (cpr != null) {
	        	 //��ȡroot�ڵ�
	            Entry<K,V> p = root;
	            while (p != null) {
	            	 //��p�ڵ㿪ʼ�Ƚϣ�
	                int cmp = cpr.compare(k, p.key);
	                //�����ǰ�ڵ��key����p�ڵ��keyС���ƶ�������
	                if (cmp < 0)
	                    p = p.left;
	              //�����ǰ�ڵ��key����p�ڵ��key���ƶ����Һ���
	                else if (cmp > 0)
	                    p = p.right;
	                //������
	                else
	                	//����p
	                    return p;
	            }
	        }
	        return null;
	    }
	  //��ȡTreeMap�д��ڻ����key����С�Ľڵ㣻  
	  //��������(��TreeMap�����нڵ�ļ�����key��)���ͷ���null  
	    final Entry<K,V> getCeilingEntry(K key) {
	        Entry<K,V> p = root;
	        while (p != null) {
	            int cmp = compare(key, p.key);
	            if (cmp < 0) {//���1. ��p.key > key  
	                if (p.left != null) //��p�����ӽڵ�  
	                    p = p.left;//�������� 
	                else
	                    return p;//���򷵻�p
	            } else if (cmp > 0) { //���2��p.key < key  
	                if (p.right != null) {
	                    p = p.right;
	                } else {
	                	 // �� p �������Һ��ӣ����ҳ� p �ĺ�̽ڵ㣬������  
	                    // ע�⣺���ﷵ�ص� ��p�ĺ�̽ڵ㡱��2�ֿ����ԣ���һ��null���ڶ���TreeMap�д���key����С�Ľڵ㡣  
	                    // �����һ��ĺ����ǣ�getCeilingEntry�Ǵ�root��ʼ�����ġ�  
	                    // ��getCeilingEntry���ߵ���һ������ô����֮ǰ���Ѿ��������Ľڵ��key���� > key��  
	                    // �����������˵�ģ���ô�ͺ��������ף�Ϊʲô��p�ĺ�̽ڵ㡱��2�ֿ������ˡ�  
	                    Entry<K,V> parent = p.parent;
	                    Entry<K,V> ch = p;
	                    while (parent != null && ch == parent.right) {
	                        ch = parent;
	                        parent = parent.parent;
	                    }
	                    return parent;
	                }
	            } else
	                return p;//���3��p.key = key  
	        }
	        return null;
	    }
	 // ��ȡTreeMap��С�ڻ����key�����Ľڵ㣻  
	 // ��������(��TreeMap�����нڵ�ļ�����keyС)���ͷ���null  
	 // getFloorEntry��ԭ���getCeilingEntry���ƣ����ﲻ�ٶ�˵��  
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
	 // ��ȡTreeMap�д���key����С�Ľڵ㡣  
	 // �������ڣ��ͷ���null��  
	 // �����getCeilingEntry����getHigherEntry������⡣  
	    final Entry<K,V> getHigherEntry(K key) {
	        Entry<K,V> p = root;
	        while (p != null) {
	            int cmp = compare(key, p.key);
	            if (cmp < 0) {
	                if (p.left != null)//������С�ڵ�
	                    p = p.left;
	                else
	                    return p;
	            } else {
	                if (p.right != null) {
	                    p = p.right;
	                } else {//�������ҽڵ㣬���׽ڵ��ѯ�ҽڵ㡣
	                    Entry<K,V> parent = p.parent;
	                    Entry<K,V> ch = p;
	                    while (parent != null && ch == parent.right) {
	                        ch = parent;
	                        parent = parent.parent;
	                    }
	                    return parent;//���ظ��ڵ�
	                } 
	            }
	        }
	        return null;
	    }
	 // ��ȡTreeMap��С��key�����Ľڵ㡣  
	 // �������ڣ��ͷ���null��  
	 // �����getfloorEntry����getlowerEntry������⡣
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
	    //���ʵ��
	    public V put(K key, V value) {
	    	//��Ⱥ���ڵ�
	        Entry<K,V> t = root;
	      //������ڵ�Ϊ�գ���Ԫ������Ϊroot
	        if (t == null) {
	            compare(key, key); 
	            root = new Entry<>(key, value, null);//�����ʵ����Ϊ���ڵ�
	            size = 1;
	            modCount++;
	            return null;
	        }
	        int cmp;
	        Entry<K,V> parent;
	        // split comparator and comparable paths
	        Comparator<? super K> cpr = comparator;
	        //�Ƚ�����Ϊ��
	        if (cpr != null) {
	        	 //ѭ���Ƚϲ�ȷ��Ԫ�ز����λ��(�Ҹ��׽ڵ�)
	            do {
	            	//��¼���ڵ�
	                parent = t;
	                //����ǰ�ڵ�͸��ڵ�Ԫ�رȽ�
	                cmp = cpr.compare(key, t.key);
	                //������keyС�ڵ�ǰԪ��key���������
	                if (cmp < 0)
	                    t = t.left;
	                //������key���ڵ�ǰԪ��key�������ұ�
	                else if (cmp > 0)
	                    t = t.right;
	                //��ȣ��滻
	                else
	                    return t.setValue(value);
	            } while (t != null);
	        }
	        //�Ƚ���Ϊnull
	        else {
	        	//TreeMapԪ�أ�key����Ϊnull
	            if (key == null)
	                throw new NullPointerException();
	            @SuppressWarnings("unchecked")
	            //key��Ҫʵ��Comparable�ӿ�
	                Comparable<? super K> k = (Comparable<? super K>) key;
	          //ѭ���Ƚϲ�ȷ��Ԫ�ز����λ��
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
	        //�ҵ����׽ڵ㣬���ݸ��׽ڵ㴴��һ���½ڵ�
	        Entry<K,V> e = new Entry<>(key, value, parent);
	        //���������Ԫ�ص�keyֵС�ڸ��ڵ��keyֵ�����ڵ���߲���
	        if (cmp < 0)
	            parent.left = e;
	        //���������Ԫ�ص�keyֵ���ڸ��ڵ��keyֵ�����ڵ��ұ߲���
	        else
	            parent.right = e;
	      //�Ժ������������ƽ��
	        fixAfterInsertion(e);
	        size++;
	        modCount++;
	        return null;
	    }
	    //�Ƴ�����
	    public V remove(Object key) {
	        Entry<K,V> p = getEntry(key);//��ȡָ��keyʵ��
	        if (p == null)
	            return null;

	        V oldValue = p.value;
	        deleteEntry(p);//ɾ��ʵ�����
	        return oldValue;//����ɾ��ֵ
	    }
	    //��ղ���
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
	    //��һ��ʵ��
	    public Map.Entry<K,V> firstEntry() {
	        return exportEntry(getFirstEntry());
	    }
	    //���һ��ʵ��
	    public Map.Entry<K,V> lastEntry() {
	        return exportEntry(getLastEntry());
	    }
	    //������һ��ʵ�壬��ɾ��
	    public Map.Entry<K,V> pollFirstEntry() {
	        Entry<K,V> p = getFirstEntry();//��ȡ��һ��ʵ��
	        Map.Entry<K,V> result = exportEntry(p);
	        if (p != null)
	            deleteEntry(p);//ɾ��ʵ��
	        return result;
	    }
	    //�������һ��ʵ�壬ɾ��
	    public Map.Entry<K,V> pollLastEntry() {
	        Entry<K,V> p = getLastEntry();//��ȡ���һ��ʵ��
	        Map.Entry<K,V> result = exportEntry(p);
	        if (p != null)
	            deleteEntry(p);//ɾ��
	        return result;
	    }
	    //С��key�����ʵ��
	    public Map.Entry<K,V> lowerEntry(K key) {
	        return exportEntry(getLowerEntry(key));
	    }
	    //С��key��������key
	    public K lowerKey(K key) {
	        return keyOrNull(getLowerEntry(key));
	    }
	    //С��key����󣨰����Լ���ʵ��
	    public Map.Entry<K,V> floorEntry(K key) {
	        return exportEntry(getFloorEntry(key));
	    }
	    //��key����󣨰����Լ�����key
	    public K floorKey(K key) {
	        return keyOrNull(getFloorEntry(key));
	    }
	    //����ָ��key����Сʵ�壨�����Լ���
	    public Map.Entry<K,V> ceilingEntry(K key) {
	        return exportEntry(getCeilingEntry(key));
	    }
	    //����ָ��key����Сkey�������Լ���
	    public K ceilingKey(K key) {
	        return keyOrNull(getCeilingEntry(key));
	    }
	    //����ָ��key����Сʵ��
	    public Map.Entry<K,V> higherEntry(K key) {
	        return exportEntry(getHigherEntry(key));
	    }
	  //����ָ��key����Сkey
	    public K higherKey(K key) {
	        return keyOrNull(getHigherEntry(key));
	    }
	    private transient EntrySet entrySet;
	    private transient KeySet<K> navigableKeySet;
	    private transient NavigableMap<K,V> descendingMap;//����Map
	    //key��Set����
	    public Set<K> keySet() {
	        return navigableKeySet();
	    }
	    //��ͨ�е�key��Set���ϣ���keySet��Щ����
	    public NavigableSet<K> navigableKeySet() {
	        KeySet<K> nks = navigableKeySet;
	        return (nks != null) ? nks : (navigableKeySet = new KeySet<>(this));
	    }
	    //����keySet
	    public NavigableSet<K> descendingKeySet() {
	        return descendingMap().navigableKeySet();
	    }
	    //����ֵ�ļ���
	    public Collection<V> values() {
	        Collection<V> vs = values;
	        if (vs == null) {
	            vs = new Values();
	            values = vs;
	        }
	        return vs;
	    }
	    //����ʵ���Set����
	    public Set<Map.Entry<K,V>> entrySet() {
	        EntrySet es = entrySet;
	        return (es != null) ? es : (entrySet = new EntrySet());
	    }
	    //����NavigableMap
	    public NavigableMap<K, V> descendingMap() {
	        NavigableMap<K, V> km = descendingMap;
	        return (km != null) ? km :
	            (descendingMap = new DescendingSubMap<>(this,
	                                                    true, null, true,
	                                                    true, null, true));
	    }
	    //����[fromkey,tokey)��NavigableMap����
	    public NavigableMap<K,V> subMap(K fromKey, boolean fromInclusive,
                K toKey,   boolean toInclusive) {
return new AscendingSubMap<>(this,
                 false, fromKey, fromInclusive,
                 false, toKey,   toInclusive);
}
	    //����tokeyǰ���NavigableMap���ϣ��������Լ�
	    public NavigableMap<K,V> headMap(K toKey, boolean inclusive) {
	        return new AscendingSubMap<>(this,
	                                     true,  null,  true,
	                                     false, toKey, inclusive);
	    }
	    //����fromkey�����NavigableMap���ϣ������Լ�
	    public NavigableMap<K,V> tailMap(K fromKey, boolean inclusive) {
	        return new AscendingSubMap<>(this,
	                                     false, fromKey, inclusive,
	                                     true,  null,    true);
	    }
	    //����[fromkey,tokey)��SortedMap����
	    public SortedMap<K,V> subMap(K fromKey, K toKey) {
	        return subMap(fromKey, true, toKey, false);
	    }
	    //����tokeyǰ���SortedMap���ϣ��������Լ�
	    public SortedMap<K,V> headMap(K toKey) {
	        return headMap(toKey, false);
	    }
	  //����fromkey�����SortedMap���ϣ������Լ�
	    public SortedMap<K,V> tailMap(K fromKey) {
	        return tailMap(fromKey, true);
	    }
	    //�滻ָ��key��ָ����ֵ��ֵ
	    @Override
	    public boolean replace(K key, V oldValue, V newValue) {
	        Entry<K,V> p = getEntry(key);//��ȡkey
	        if (p!=null && Objects.equals(oldValue, p.value)) {//�ж��ϵ�ֵ��ԭ����ֵ�Ƿ����
	            p.value = newValue;//�滻
	            return true;
	        }
	        return false;
	    }
	    //��ָ����key��ֵ�滻Ϊָ����ֵ
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
	    //��ȡ����ֵ��ķ���
	    class Values extends AbstractCollection<V> {
	        public Iterator<V> iterator() {//������
	            return new ValueIterator(getFirstEntry());//����һ��ֵ�õ�����
	        }

	        public int size() {//��С
	            return TreeMap.this.size();
	        }

	        public boolean contains(Object o) {//�жϴ���
	            return TreeMap.this.containsValue(o);
	        }

	        public boolean remove(Object o) {//�Ƴ�
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
	    //��������ʵ�弯��Set��ķ���
	    class EntrySet extends AbstractSet<Map.Entry<K,V>> {
	        public Iterator<Map.Entry<K,V>> iterator() {
	            return new EntryIterator(getFirstEntry());
	        }
	        //��дcontains�������ж��Ƿ����
	        public boolean contains(Object o) {
	            if (!(o instanceof Map.Entry))
	                return false;
	            Map.Entry<?,?> entry = (Map.Entry<?,?>) o;
	            Object value = entry.getValue();
	            Entry<K,V> p = getEntry(entry.getKey());
	            return p != null && valEquals(p.getValue(), value);
	        }
	        //�Ƴ�ָ���Ķ���
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
	    //key�ĵ�����
	    Iterator<K> keyIterator() {
	        return new KeyIterator(getFirstEntry());
	    }
	    //�ݼ�key������
	    Iterator<K> descendingKeyIterator() {
	        return new DescendingKeyIterator(getLastEntry());
	    }
	    //KeySetʵ����
	    static final class KeySet<E> extends AbstractSet<E> implements NavigableSet<E> {
	        private final NavigableMap<E, ?> m;
	        KeySet(NavigableMap<E,?> map) { m = map; }//����Map����Set��

	        public Iterator<E> iterator() {//key������
	            if (m instanceof TreeMap)//����������TreeMap���͵���keyIterator������������һ��key������
	                return ((TreeMap<E,?>)m).keyIterator();
	            else
	                return ((TreeMap.NavigableSubMap<E,?>)m).keyIterator();
	        }

	        public Iterator<E> descendingIterator() {//�ݼ������������ڵݼ��ķ������е���
	            if (m instanceof TreeMap)
	                return ((TreeMap<E,?>)m).descendingKeyIterator();
	            else
	                return ((TreeMap.NavigableSubMap<E,?>)m).descendingKeyIterator();
	        }

	        public int size() { return m.size(); }//��С
	        public boolean isEmpty() { return m.isEmpty(); }//�ж��Ƿ�Ϊ��
	        public boolean contains(Object o) { return m.containsKey(o); }//�Ƿ��������o
	        public void clear() { m.clear(); }//�������
	        public E lower(E e) { return m.lowerKey(e); }//��ȡ��eС������key
	        public E floor(E e) { return m.floorKey(e); }//��ȡ��eС����ڵ�key
	        public E ceiling(E e) { return m.ceilingKey(e); }//��ȡ���ڻ���ڵ�key
	        public E higher(E e) { return m.higherKey(e); }//��ȡ���ڵ�key
	        public E first() { return m.firstKey(); }//��ȡ��һ��key
	        public E last() { return m.lastKey(); }//��ȡ���һ��key
	        public Comparator<? super E> comparator() { return m.comparator(); }//��ȡ�Ƚ���
	        public E pollFirst() {//ȥ����һ��ʵ�岢��������key
	            Map.Entry<E,?> e = m.pollFirstEntry();
	            return (e == null) ? null : e.getKey();
	        }
	        public E pollLast() {//ȥ�����һ��ʵ�岢����key
	            Map.Entry<E,?> e = m.pollLastEntry();
	            return (e == null) ? null : e.getKey();
	        }
	        public boolean remove(Object o) {//�Ƴ�ָ������
	            int oldSize = size();
	            m.remove(o);
	            return size() != oldSize;
	        }
	        //��ȡkeySet,��ȡΪfromElement��toElement,��СΪ[fromElement,toElement)
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
	        //��ȡkeySet,��ȡΪfromElement��toElement,��СΪ[fromElement,toElement)
	        public SortedSet<E> subSet(E fromElement, E toElement) {
	            return subSet(fromElement, true, toElement, false);
	        }
	        public SortedSet<E> headSet(E toElement) {
	            return headSet(toElement, false);
	        }
	        public SortedSet<E> tailSet(E fromElement) {
	            return tailSet(fromElement, true);
	        }
	        public NavigableSet<E> descendingSet() {//����һ���ݼ�Set
	            return new KeySet<>(m.descendingMap());
	        }

	        public Spliterator<E> spliterator() {
	            return keySpliteratorFor(m);
	        }
	    }
	    //˽��ʵ���������
	    abstract class PrivateEntryIterator<T> implements Iterator<T> {
	        Entry<K,V> next;//��һ���ڵ�
	        Entry<K,V> lastReturned;//��������ʵ��
	        int expectedModCount;

	        PrivateEntryIterator(Entry<K,V> first) {//���캯�������������������ʼλ��
	            expectedModCount = modCount;
	            lastReturned = null;
	            next = first;
	        }

	        public final boolean hasNext() {//�ж��Ƿ�����һ��
	            return next != null;
	        }

	        final Entry<K,V> nextEntry() {//��һ��ʵ��
	            Entry<K,V> e = next;
	            if (e == null)
	                throw new NoSuchElementException();
	            if (modCount != expectedModCount)
	                throw new ConcurrentModificationException();
	            next = successor(e);//next��Ϊ��̽ڵ�
	            lastReturned = e;
	            return e;
	        }

	        final Entry<K,V> prevEntry() {//ǰһ��ʵ��
	            Entry<K,V> e = next;
	            if (e == null)
	                throw new NoSuchElementException();
	            if (modCount != expectedModCount)
	                throw new ConcurrentModificationException();
	            next = predecessor(e);//next��Ϊǰ���ڵ�
	            lastReturned = e;
	            return e;
	        }

	        public void remove() {//ɾ����������ʵ��
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
	    //ʵ�������ʵ����
	    final class EntryIterator extends PrivateEntryIterator<Map.Entry<K,V>> {
	        EntryIterator(Entry<K,V> first) {
	            super(first);
	        }
	        public Map.Entry<K,V> next() {
	            return nextEntry();
	        }
	    }
	    //ֵ�õ�����
	    final class ValueIterator extends PrivateEntryIterator<V> {
	        ValueIterator(Entry<K,V> first) {
	            super(first);
	        }
	        public V next() {
	            return nextEntry().value;
	        }
	    }
	    //key�ĵ�������2
	    final class KeyIterator extends PrivateEntryIterator<K> {
	        KeyIterator(Entry<K,V> first) {
	            super(first);
	        }
	        public K next() {
	            return nextEntry().key;
	        }
	    }
	    //�������������
	    final class DescendingKeyIterator extends PrivateEntryIterator<K> {
	        DescendingKeyIterator(Entry<K,V> first) {
	            super(first);
	        }
	        public K next() {//��ǰ����
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
	    
	    //�Ƚϣ����ڶ�����������бȽϣ����δ�ƶ��Ƚ�������ʵ�ֱȽ����ӿڱȽϣ�������ָ���ıȽ������бȽ�
	    final int compare(Object k1, Object k2) {
	        return comparator==null ? ((Comparable<? super K>)k1).compareTo((K)k2)
	            : comparator.compare((K)k1, (K)k2);
	    }
	    //�Ƚ����������Ƿ����ƣ�ͬһ�����󣬿��Բ�ͬ�����ã�
	    static final boolean valEquals(Object o1, Object o2) {
	        return (o1==null ? o2==null : o1.equals(o2));
	    }
	    //��ֹ�����޸ķ��ص�Entry,��װEntry
	    static <K,V> Map.Entry<K,V> exportEntry(TreeMap.Entry<K,V> e) {
	        return (e == null) ? null :
	            new AbstractMap.SimpleImmutableEntry<>(e);//�½�һ���࣬�����setValue�ķ�������ֹ�����Բ��ᱻ�޸�
	    }
	    //ʵ�岻Ϊ�շ��ؼ�
	    static <K,V> K keyOrNull(TreeMap.Entry<K,V> e) {
	        return (e == null) ? null : e.key;
	    }
	    //���ؼ�
	    static <K> K key(Entry<K,?> e) {
	        if (e==null)
	            throw new NoSuchElementException();
	        return e.key;
	    }
	    //�����ʵ��
	    static final class Entry<K,V> implements Map.Entry<K,V> {
	        K key;
	        V value;
	        Entry<K,V> left;//���ӽڵ� 
	        Entry<K,V> right;//���ӽڵ� 
	        Entry<K,V> parent;//���ڵ�  
	        boolean color = BLACK;//������ɫ��Ĭ��Ϊ��ɫ 
	        Entry(K key, V value, Entry<K,V> parent) {//���췽��  
	            this.key = key;
	            this.value = value;
	            this.parent = parent;
	        }

	        public K getKey() {//���key  
	            return key;
	        }

	        public V getValue() {//���value  
	            return value;
	        }
	        public V setValue(V value) { //����value 
	            V oldValue = this.value;
	            this.value = value;
	            return oldValue;
	        }

	        public boolean equals(Object o) {//key��value����Ȳŷ���true  
	            if (!(o instanceof Map.Entry))
	                return false;
	            Map.Entry<?,?> e = (Map.Entry<?,?>)o;

	            return valEquals(key,e.getKey()) && valEquals(value,e.getValue());
	        }

	        public int hashCode() {//����hashCode  
	            int keyHash = (key==null ? 0 : key.hashCode());
	            int valueHash = (value==null ? 0 : value.hashCode());
	            return keyHash ^ valueHash;
	        }

	        public String toString() {//��дtoString����  
	            return key + "=" + value;
	        }
	    }
	    final Entry<K,V> getFirstEntry() {//���TreeMap���һ���ڵ�(������key������С�Ľڵ�)�����TreeMapΪ�գ�����null  
	        Entry<K,V> p = root;
	        if (p != null)
	            while (p.left != null)
	                p = p.left;
	        return p;
	    }
	    //��ȡ���һ��ʵ��
	    final Entry<K,V> getLastEntry() {
	        Entry<K,V> p = root;
	        if (p != null)
	            while (p.right != null)
	                p = p.right;
	        return p;
	    }
	    //��ȡ��̽ڵ�
	    static <K,V> TreeMap.Entry<K,V> successor(Entry<K,V> t) {
	        if (t == null)
	            return null;
	        else if (t.right != null) {//�ҽڵ㲻Ϊ��ʱ
	            Entry<K,V> p = t.right;//��ȡ�ҽڵ�
	            while (p.left != null)//�����нڵ���������ӽڵ㣬��Ѱ�ұ�����Ľڵ����С�ڵ�
	                p = p.left;
	            return p;
	        } else {//����������ҽڵ�
	            Entry<K,V> p = t.parent;//��ȡ���ڵ�
	            Entry<K,V> ch = t;
	            while (p != null && ch == p.right) {//���ԭ�ڵ㲻Ϊ������ڵ�Ļ�������Ѱ�Ҹ��׽ڵ�
	                ch = p;
	                p = p.parent;
	            }
	            return p;//�����ҵ��Ľڵ�
	        }
	    }
	    //��ȡǰ���ڵ�
	    static <K,V> Entry<K,V> predecessor(Entry<K,V> t) {//��ǰ��ĸպ��෴
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
	    private static <K,V> boolean colorOf(Entry<K,V> p) {//��ȡʵ�����ɫ�����ʵ��Ϊ����Ĭ��Ϊ��ɫ
	        return (p == null ? BLACK : p.color);
	    }
	    private static <K,V> Entry<K,V> parentOf(Entry<K,V> p) {//��ȡ���׽ڵ�
	        return (p == null ? null: p.parent);
	    }

	    private static <K,V> void setColor(Entry<K,V> p, boolean c) {//����ʵ����ɫ
	        if (p != null)
	            p.color = c;
	    }

	    private static <K,V> Entry<K,V> leftOf(Entry<K,V> p) {//��ȡ���ӽڵ�
	        return (p == null) ? null: p.left;
	    }

	    private static <K,V> Entry<K,V> rightOf(Entry<K,V> p) {//��ȡ�Һ���ϵ�G��
	        return (p == null) ? null: p.right;
	    }
	    /*************�Ժ�����ڵ�x������������ ******************/  
	    /* 
	     * ����ʾ��ͼ���Խڵ�x�������� 
	     *     f                       f 
	     *    /                       / 
	     *   p                       r 
	     *  / \                     / \ 
	     * lx  r     ----->        p  ry 
	     *    / \                 / \ 
	     *   ly ry               lx ly 
	     * �������������£� 
	     * 1. ��r�����ӽڵ㸳��p�����ӽڵ�,����p����r���ӽڵ�ĸ��ڵ�(y���ӽڵ�ǿ�ʱ) 
	     * 2. ��p�ĸ��ڵ�f(�ǿ�ʱ)����r�ĸ��ڵ㣬ͬʱ����p���ӽڵ�Ϊr(�����) 
	     * 3. ��r�����ӽڵ���Ϊp����p�ĸ��ڵ���Ϊr 
	     */  
	    private void rotateLeft(Entry<K,V> p) {
	        if (p != null) {
	        	  //1. ��r�����ӽڵ㸳��p�����ӽڵ㣬����p����r���ӽڵ�ĸ��ڵ�(r���ӽڵ�ǿ�ʱ)  
	            Entry<K,V> r = p.right;
	            p.right = r.left;//��r�����ӽڵ㸳��p�����ӽڵ�
	            if (r.left != null)
	                r.left.parent = p;//��p����r���ӽڵ�ĸ��ڵ�
	            //2. ��p�ĸ��ڵ�f(�ǿ�ʱ)����r�ĸ��ڵ㣬ͬʱ����f���ӽڵ�Ϊr(�����)  
	            r.parent = p.parent; //��p�ĸ��ڵ�f(�ǿ�ʱ)����r�ĸ��ڵ�
	            if (p.parent == null)
	                root = r; //���p�ĸ��ڵ�Ϊ�գ���r��Ϊ���ڵ�  
	            else if (p.parent.left == p) //���p�����ӽڵ�  
	                p.parent.left = r;//��Ҳ��r��Ϊ���ӽڵ�  
	            else
	                p.parent.right = r;//����r��Ϊ���ӽڵ� 
	            //3. ��r�����ӽڵ���Ϊp����p�ĸ��ڵ���Ϊr 
	            r.left = p;
	            p.parent = r;
	        }
	    }
	    /*************�Ժ�����ڵ�y������������ ******************/  
	    /* 
	     * ����ʾ��ͼ���Խڵ�y�������� 
	     *        f                   f 
	     *       /                   / 
	     *      p                   l
	     *     / \                 / \ 
	     *    l  ry   ----->      lx  p 
	     *   / \                     / \ 
	     * lx  rx                   rx ry 
	     * �������������£� 
	     * 1. ��l�����ӽڵ㸳��p�����ӽڵ�,����p����l���ӽڵ�ĸ��ڵ�(l���ӽڵ�ǿ�ʱ) 
	     * 2. ��p�ĸ��ڵ�f(�ǿ�ʱ)����l�ĸ��ڵ㣬ͬʱ����f���ӽڵ�Ϊl(�����) 
	     * 3. ��l�����ӽڵ���Ϊp����p�ĸ��ڵ���Ϊl
	     */  
	    private void rotateRight(Entry<K,V> p) {
	        if (p != null) {
	        	//1. ��l�����ӽڵ㸳��p�����ӽڵ�,����p����l���ӽڵ�ĸ��ڵ�(l���ӽڵ�ǿ�ʱ) 
	            Entry<K,V> l = p.left;//��ȡp�����ӽڵ�Ϊl
	            p.left = l.right;//��l�����ӽڵ㸶��p���ӽڵ�
	            if (l.right != null) l.right.parent = p;//��p����l�����ӽڵ�ĸ��ڵ�
	            l.parent = p.parent;//��p�ĸ��ڵ㸳ֵ��l�ĸ��׽ڵ㣬
	            if (p.parent == null)
	                root = l;//���û�и��׽ڵ㣬��lΪ���ڵ�
	            //2. ��p�ĸ��ڵ�f(�ǿ�ʱ)����l�ĸ��ڵ㣬ͬʱ����f���ӽڵ�Ϊl(�����) 
	            else if (p.parent.right == p)//�ж�ԭ���Ľڵ��Ǹ��׽ڵ����ڵ㻹���ҽڵ�
	                p.parent.right = l;//�ҽڵ㸳ֵΪ�ҽڵ�
	            else p.parent.left = l;//���ľ͸�ֵΪ���
	            //3. ��l�����ӽڵ���Ϊp����p�ĸ��ڵ���Ϊl
	            l.right = p;//p��Ϊl���ҽڵ�
	            p.parent = l;//p�ĸ��׽ڵ���Ϊl ���໥ָ��
	        }
	    }
	    /** 
	     * �����ڵ����޸����� 
	     * x ��ʾ�����ڵ�
	     * 1���常�ڵ��Ǻ�ɫ�����ǿսڵ���Ĭ��Ϊ��ɫ��
			    ���������ͨ����ת�ͱ�ɫ��������ʹ������ָ�ƽ�⡣���ǿ��ǵ�ǰ�ڵ�n�͸��ڵ�p��λ���ַ�Ϊ���������
			 A��n��p���ӽڵ㣬p��g�����ӽڵ㡣
			 B��n��p���ӽڵ㣬p��g�����ӽڵ㡣
			 C��n��p���ӽڵ㣬p��g�����ӽڵ㡣
			 D��n��p���ӽڵ㣬p��g�����ӽڵ㡣 
			 2���常�ڵ��Ǻ�ɫ
	     */  
	    private void fixAfterInsertion(Entry<K,V> x) {
	        x.color = RED;//�����ڵ����ɫΪ��ɫ  
	        //ѭ�� ֱ�� x���Ǹ��ڵ㣬��x�ĸ��ڵ㲻Ϊ��ɫ
	        while (x != null && x != root && x.parent.color == RED) {
	        	  //���X�ĸ��ڵ㣨P�����丸�ڵ�ĸ��ڵ㣨G������ڵ�  
	            if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
	            	//��ȡX����ڵ�(U)
	                Entry<K,V> y = rightOf(parentOf(parentOf(x)));
	                //���X����ڵ㣨U�� Ϊ��ɫ���������
	                if (colorOf(y) == RED) {
	                	 //��X�ĸ��ڵ㣨P������Ϊ��ɫ  
	                    setColor(parentOf(x), BLACK);
	                  //��X����ڵ㣨U������Ϊ��ɫ  
	                    setColor(y, BLACK);
	                    //��X�ĸ��ڵ�ĸ��ڵ㣨G�����ú�ɫ  
	                    setColor(parentOf(parentOf(x)), RED);
	                    x = parentOf(parentOf(x));
	                } 
	                //���X����ڵ㣨UΪ��ɫ�������������������������ġ�����壩  
	                else {
	                    //���X�ڵ�Ϊ�丸�ڵ㣨P���������������������ת������ģ�  
	                    if (x == rightOf(parentOf(x))) {
	                    	//��X�ĸ��ڵ���ΪX  
	                        x = parentOf(x);
	                        //����ת  
	                        rotateLeft(x);
	                    }
	                    //������壩  
                        //��X�ĸ��ڵ㣨P������Ϊ��ɫ  
	                    setColor(parentOf(x), BLACK);
	                    //��X�ĸ��ڵ�ĸ��ڵ㣨G�����ú�ɫ  
	                    setColor(parentOf(parentOf(x)), RED);
	                    //��X�ĸ��ڵ�ĸ��ڵ㣨G��Ϊ��������ת  
	                    rotateRight(parentOf(parentOf(x)));
	                }
	            } 
	            //���X�ĸ��ڵ㣨P�����丸�ڵ�ĸ��ڵ㣨G�����ҽڵ�  
	            else {
	            	 //��ȡX����ڵ㣨U��
	                Entry<K,V> y = leftOf(parentOf(parentOf(x)));
	                //���X����ڵ㣨U�� Ϊ��ɫ��������� 
	                if (colorOf(y) == RED) {
	                    //��X�ĸ��ڵ㣨P������Ϊ��ɫ  
	                    setColor(parentOf(x), BLACK);
	                    //��X����ڵ㣨U������Ϊ��ɫ  
	                    setColor(y, BLACK);
	                    //��X�ĸ��ڵ�ĸ��ڵ㣨G�����ú�ɫ  
	                    setColor(parentOf(parentOf(x)), RED);
	                    x = parentOf(parentOf(x));
	                }
	                //���X����ڵ㣨UΪ��ɫ�������������������������ġ�����壩
	                else {
	                	  //���X�ڵ�Ϊ�丸�ڵ㣨P���������������������ת������ģ�  
	                    if (x == leftOf(parentOf(x))) {
	                    	//��X�ĸ��ڵ���ΪX 
	                        x = parentOf(x);
	                        //����ת  
	                        rotateRight(x);
	                    }
	                    //��X�ĸ��ڵ㣨P������Ϊ��ɫ  
	                    setColor(parentOf(x), BLACK);
	                    //��X�ĸ��ڵ�ĸ��ڵ㣨G�����ú�ɫ 
	                    setColor(parentOf(parentOf(x)), RED);
	                    //��X�ĸ��ڵ�ĸ��ڵ㣨G��Ϊ��������ת 
	                    rotateLeft(parentOf(parentOf(x)));
	                }
	            }
	        }
	        //�����ڵ�Gǿ������Ϊ��ɫ  
	        root.color = BLACK;
	    }
	   /* 
	              ���"ɾ����������������ɾ���ڵ�ķ�����һ����"����3�������
	    1. ��ɾ���ڵ�û�ж��ӣ���ΪҶ�ڵ㡣��ô��ֱ�ӽ��ýڵ�ɾ����OK�ˡ�
	    2. ��ɾ���ڵ�ֻ��һ�����ӡ���ô��ֱ��ɾ���ýڵ㣬���øýڵ��Ψһ�ӽڵ㶥������λ�á�
	    3. ��ɾ���ڵ����������ӡ���ô�����ҳ����ĺ�̽ڵ㣻Ȼ��ѡ����ĺ�̽ڵ�����ݡ����Ƹ����ýڵ�����ݡ���֮��ɾ�������ĺ�̽ڵ㡱
	                        �������̽ڵ��൱�������ڽ���̽ڵ�����ݸ��Ƹ�"��ɾ���ڵ�"֮���ٽ���̽ڵ�ɾ��������������Ľ�����ת��Ϊ"ɾ����̽ڵ�"������ˣ�
	                       ����Ϳ��Ǻ�̽ڵ㡣 ��"��ɾ���ڵ�"�������ǿ��ӽڵ������£����ĺ�̽ڵ㲻������˫�ӷǿա���Ȼ"�ĺ�̽ڵ�"������˫�Ӷ��ǿգ�����ζ��
	       "�ýڵ�ĺ�̽ڵ�"Ҫôû�ж��ӣ�Ҫôֻ��һ�����ӡ���û�ж��ӣ���"����� "���д�����ֻ��һ�����ӣ���"����� "���д���
	       */
	 
	    private void deleteEntry(Entry<K,V> p) {
	        modCount++;
	        size--;
	        //���p���������� 
	        if (p.left != null && p.right != null) {
	        	 //��ȡp�ļ̳нڵ�
	            Entry<K,V> s = successor(p);
	            //��s��key����Ϊp��key
	            p.key = s.key;
	            //��s��value����Ϊp��value
	            p.value = s.value;
	            //��s����Ϊp
	            p = s;
	        } 

	      //��ʼ�޸����Ƴ��ڵ�����ṹ
	        //���p�����ӣ���ȡ���ӣ�û�оͻ�ȡ�Һ���
	        Entry<K,V> replacement = (p.left != null ? p.left : p.right);
	        //���p�Ǹ��ڵ������
	        if (replacement != null) {

	            replacement.parent = p.parent;
	          //���pû�и��ף�p����root�ڵ�
	            if (p.parent == null)
	            	//��replacement����Ϊroot�ڵ�
	                root = replacement;
	            //���p�Ǹ��ڵ������
	            else if (p == p.parent.left)
	                p.parent.left  = replacement;
	            //���򣬽�replacement����Ϊp�ĸ��׵��Һ���
	            else
	                p.parent.right = replacement;

	            //���p�ڵ�ĸ��׺�p�ڵ�����Һ��ӵ�����
	            p.left = p.right = p.parent = null;

	          //���pΪ��ɫ
	            if (p.color == BLACK)
	            	//��ɫ�޸�
	                fixAfterDeletion(replacement);
	        } 
	        //p�ĸ���Ϊnull��˵��pֻ���Լ�һ���ڵ�
	        else if (p.parent == null) { 
	            root = null;
	        } else {
	        	//���p�Ǻ�ɫ
	            if (p.color == BLACK)
	            	 //����
	                fixAfterDeletion(p);
	            //�����жϹ�
	            if (p.parent != null) {
	            	 //p�Ǹ��׵�����
	                if (p == p.parent.left)
	                	//ɾ������
	                    p.parent.left = null;
	                //p�Ǹ��׵��Һ���
	                else if (p == p.parent.right)
	                	//ɾ������
	                    p.parent.right = null;
	                //ɾ��p�Ը��׵�����
	                p.parent = null;
	            }
	        }
	    }
	    //ɾ�������ɫ�޸�
	    private void fixAfterDeletion(Entry<K,V> x) {
	        //ѭ����ֻҪx����root�ڵ㲢��x����ɫ�Ǻ�ɫ��
	        while (x != root && colorOf(x) == BLACK) {
	            //���x�������׵�����
	            if (x == leftOf(parentOf(x))) {
	                //��ȡ��x�ڵ㸸�׵��Һ���
	                Entry<K,V> sib = rightOf(parentOf(x));
	                //���sib(�����Һ���)�Ǻ�ɫ
	                if (colorOf(sib) == RED) {
	                    //����sibΪ��ɫ
	                    setColor(sib, BLACK);
	                    //����x���ڵ�Ϊ��ɫ
	                    setColor(parentOf(x), RED);
	                    //x���ڵ�����
	                    rotateLeft(parentOf(x));
	                    //��x���׵��ҽڵ�����Ϊsib����sib�ƶ�����ת���x���׵��Һ���
	                    sib = rightOf(parentOf(x));
	                }
	                //���sib�����Һ��Ӷ��Ǻ�ɫ
	                if (colorOf(leftOf(sib))  == BLACK &&
	                        colorOf(rightOf(sib)) == BLACK) {
	                    //��sib����Ϊ��ɫ
	                    setColor(sib, RED);
	                    //��x�ĸ�������Ϊx����x�ƶ������׽ڵ�
	                    x = parentOf(x);
	                    //�������
	                } else {
	                    //���sib���Һ����Ǻ�ɫ
	                    if (colorOf(rightOf(sib)) == BLACK) {
	                        //��sib����������Ϊ��ɫ
	                        setColor(leftOf(sib), BLACK);
	                        //��sib����Ϊ��ɫ
	                        setColor(sib, RED);
	                        //����sib
	                        rotateRight(sib);
	                        //��x�ĸ��׵��Һ�������Ϊsib����sib�ƶ�����ת���x���׵��Һ���
	                        sib = rightOf(parentOf(x));
	                    }
	                    //��sib���óɺ�x�ĸ���һ������ɫ
	                    setColor(sib, colorOf(parentOf(x)));
	                    //��x�ĸ�������Ϊ��ɫ
	                    setColor(parentOf(x), BLACK);
	                    //��sib���Һ�������Ϊ��ɫ
	                    setColor(rightOf(sib), BLACK);
	                    //����x�ĸ���
	                    rotateLeft(parentOf(x));
	                    //��root����Ϊx������ѭ��
	                    x = root;
	                }
	                //x��һ���Һ���
	            } else { // symmetric
	                //��ȡx���׵�����
	                Entry<K,V> sib = leftOf(parentOf(x));
	                //���sibΪ��ɫ
	                if (colorOf(sib) == RED) {
	                    //��sib����Ϊ��ɫ
	                    setColor(sib, BLACK);
	                    //��x�ĸ�������Ϊ��ɫ
	                    setColor(parentOf(x), RED);
	                    //����x�ĸ���
	                    rotateRight(parentOf(x));
	                    //��x�ĸ��׵���������Ϊsib����sib�ƶ�����ת���x���׵�����
	                    sib = leftOf(parentOf(x));
	                }
	                //���sib�����Һ��Ӷ��Ǻ�ɫ
	                if (colorOf(rightOf(sib)) == BLACK &&
	                        colorOf(leftOf(sib)) == BLACK) {
	                    //��sib����Ϊ��ɫ
	                    setColor(sib, RED);
	                    //��x�ĸ�������Ϊx����x�ƶ������׽ڵ�
	                    x = parentOf(x);
	                    //�������
	                } else {
	                    //���sib�������Ǻ�ɫ
	                    if (colorOf(leftOf(sib)) == BLACK) {
	                        //��sib���Һ������óɺ�ɫ
	                        setColor(rightOf(sib), BLACK);
	                        //��sib���óɺ�ɫ
	                        setColor(sib, RED);
	                        //����sib
	                        rotateLeft(sib);
	                        //��x�ĸ��׵���������Ϊsib����sib�ƶ�����ת���x���׵�����
	                        sib = leftOf(parentOf(x));
	                    }
	                    //��sib���óɺ�x�ĸ���һ������ɫ
	                    setColor(sib, colorOf(parentOf(x)));
	                    //��x�ĸ�������Ϊ��ɫ
	                    setColor(parentOf(x), BLACK);
	                    //��sib����������Ϊ��ɫ
	                    setColor(leftOf(sib), BLACK);
	                    //����x�ĸ���
	                    rotateRight(parentOf(x));
	                    //��root����Ϊx������ѭ��
	                    x = root;
	                }
	            }
	        }
	        //��x����Ϊ��ɫ
	        setColor(x, BLACK);
	    }
	    
	    
	    
/////////////////////////////////////////////////////////////////////////////////////////////////////	    
	    private static final Object UNBOUNDED = new Object();

	 // TreeMap��SubMap����һ�������࣬ʵ���˹���������  
	 // ��������"(����)AscendingSubMap"��"(����)DescendingSubMap"�������ࡣ  
	    abstract static class NavigableSubMap<K,V> extends AbstractMap<K,V>
	        implements NavigableMap<K,V>, java.io.Serializable {
	        private static final long serialVersionUID = -2102997345730753016L;

	     // TreeMap�Ŀ��� 
	        final TreeMap<K,V> m;
	        // lo�ǡ���Map��Χ����Сֵ����hi�ǡ���Map��Χ�����ֵ����  
	        // loInclusive�ǡ��Ƿ����lo�ı�ǡ���hiInclusive�ǡ��Ƿ����hi�ı�ǡ�  
	        // fromStart�ǡ���ʾ�Ƿ�ӵ�һ���ڵ㿪ʼ���㡱��  
	        // toEnd�ǡ���ʾ�Ƿ���㵽���һ���ڵ�   
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

	     // �ж�key�Ƿ�̫С  
	        final boolean tooLow(Object key) {
	        	// ����SubMap����������ʼ�ڵ㡱��  
	            // ���ң���keyС����С��(lo)�����ߡ�key������С��(lo)������С��ȴû�����ڸ�SubMap�ڡ�  
	            // ���ж�key̫С���������������̫С��  
	            if (!fromStart) {
	                int c = m.compare(key, lo);
	                if (c < 0 || (c == 0 && !loInclusive))
	                    return true;
	            }
	            return false;
	        }
	     // �ж�key�Ƿ�̫��  
	        final boolean tooHigh(Object key) {
	        	// ����SubMap�������������ڵ㡱��  
	            // ���ң���key��������(hi)�����ߡ�key��������(hi)��������ȴû�����ڸ�SubMap�ڡ�  
	            // ���ж�key̫���������������̫��  
	            if (!toEnd) {
	                int c = m.compare(key, hi);
	                if (c > 0 || (c == 0 && !hiInclusive))
	                    return true;
	            }
	            return false;
	        }
	     // �ж�key�Ƿ��ڡ�lo��hi�������䷶Χ��  
	        final boolean inRange(Object key) {
	            return !tooLow(key) && !tooHigh(key);
	        }
	        // �ж�key�Ƿ��ڷ��������  
	        final boolean inClosedRange(Object key) {
	            return (fromStart || m.compare(key, lo) >= 0)
	                && (toEnd || m.compare(hi, key) >= 0);
	        }
	     // �ж�key�Ƿ���������, inclusive�����俪�ر�־  
	        final boolean inRange(Object key, boolean inclusive) {
	            return inclusive ? inRange(key) : inClosedRange(key);
	        }

	        // ������͵�Entry  
	        final TreeMap.Entry<K,V> absLowest() {
	            TreeMap.Entry<K,V> e =
	                (fromStart ?  m.getFirstEntry() :
	                 (loInclusive ? m.getCeilingEntry(lo) :
	                                m.getHigherEntry(lo)));
	            return (e == null || tooHigh(e.key)) ? null : e;
	        }
	        // ������ߵ�Entry  
	        final TreeMap.Entry<K,V> absHighest() {
	        	 // �������������ڵ㡱�������getLastEntry()�������һ���ڵ�  
	            // ����Ļ���������hi�������getFloorEntry(hi)��ȡС��/����hi������Entry;  
	            //           ���򣬵���getLowerEntry(hi)��ȡ����hi�����Entry  
	            TreeMap.Entry<K,V> e =
	                (toEnd ?  m.getLastEntry() :
	                 (hiInclusive ?  m.getFloorEntry(hi) :
	                                 m.getLowerEntry(hi)));
	            return (e == null || tooLow(e.key)) ? null : e;
	        }
	     // ����"����/����key����С��Entry"  
	        final TreeMap.Entry<K,V> absCeiling(K key) {
	        	// ֻ���ڡ�key̫С��������£�absLowest()���ص�Entry���ǡ�����/����key����СEntry��  
	            // ��������²��С����磬����������ʼ�ڵ㡱ʱ��absLowest()���ص�����СEntry�ˣ�  
	            if (tooLow(key))
	                return absLowest();
	            // ��ȡ������/����key����СEntry��  
	            TreeMap.Entry<K,V> e = m.getCeilingEntry(key);
	            return (e == null || tooHigh(e.key)) ? null : e;
	        }
	        // ����"����key����С��Entry"  
	        final TreeMap.Entry<K,V> absHigher(K key) {
	        	 // ֻ���ڡ�key̫С��������£�absLowest()���ص�Entry���ǡ�����key����СEntry��  
	            // ��������²��С����磬����������ʼ�ڵ㡱ʱ��absLowest()���ص�����СEntry��,����һ���ǡ�����key����СEntry����  
	            if (tooLow(key))
	                return absLowest();
	            // ��ȡ������key����СEntry��  
	            TreeMap.Entry<K,V> e = m.getHigherEntry(key);
	            return (e == null || tooHigh(e.key)) ? null : e;
	        }
	     // ����"С��/����key������Entry"  
	        final TreeMap.Entry<K,V> absFloor(K key) {
	        	 // ֻ���ڡ�key̫�󡱵�����£�(absHighest)���ص�Entry���ǡ�С��/����key�����Entry��  
	            // ��������²��С����磬�������������ڵ㡱ʱ��absHighest()���ص������Entry�ˣ� 
	            if (tooHigh(key))
	                return absHighest();
	            // ��ȡ"С��/����key������Entry"  
	            TreeMap.Entry<K,V> e = m.getFloorEntry(key);
	            return (e == null || tooLow(e.key)) ? null : e;
	        }
	        // ����"С��key������Entry"  
	        final TreeMap.Entry<K,V> absLower(K key) {
	        	 // ֻ���ڡ�key̫�󡱵�����£�(absHighest)���ص�Entry���ǡ�С��key�����Entry��  
	            // ��������²��С����磬�������������ڵ㡱ʱ��absHighest()���ص������Entry��,����һ���ǡ�С��key�����Entry����  
	            if (tooHigh(key))
	                return absHighest();
	            // ��ȡ"С��key������Entry" 
	            TreeMap.Entry<K,V> e = m.getLowerEntry(key);
	            return (e == null || tooLow(e.key)) ? null : e;
	        }

	        // ���ء��������ڵ��е���С�ڵ㡱�������ڵĻ�������null  
	        final TreeMap.Entry<K,V> absHighFence() {
	            return (toEnd ? null : (hiInclusive ?
	                                    m.getHigherEntry(hi) :
	                                    m.getCeilingEntry(hi)));
	        }

	        // ���ء�С����С�ڵ��е����ڵ㡱�������ڵĻ�������null  
	        final TreeMap.Entry<K,V> absLowFence() {
	            return (fromStart ? null : (loInclusive ?
	                                        m.getLowerEntry(lo) :
	                                        m.getFloorEntry(lo)));
	        }

	     // ���漸��abstract��������ҪNavigableSubMap��ʵ����ʵ�ֵķ���  
	        abstract TreeMap.Entry<K,V> subLowest();
	        abstract TreeMap.Entry<K,V> subHighest();
	        abstract TreeMap.Entry<K,V> subCeiling(K key);
	        abstract TreeMap.Entry<K,V> subHigher(K key);
	        abstract TreeMap.Entry<K,V> subFloor(K key);
	        abstract TreeMap.Entry<K,V> subLower(K key);

	     // ���ء�˳�򡱵ļ�������  
	        abstract Iterator<K> keyIterator();

	        abstract Spliterator<K> keySpliterator();

	     // ���ء����򡱵ļ�������  
	        abstract Iterator<K> descendingKeyIterator();

	        // public methods
	        // ����SubMap�Ƿ�Ϊ�ա��յĻ�������true�����򷵻�false 
	        public boolean isEmpty() {
	            return (fromStart && toEnd) ? m.isEmpty() : entrySet().isEmpty();
	        }
	     // ����SubMap�Ĵ�С 
	        public int size() {
	            return (fromStart && toEnd) ? m.size() : entrySet().size();
	        }
	        // ����SubMap�Ƿ������key  
	        public final boolean containsKey(Object key) {
	            return inRange(key) && m.containsKey(key);
	        }
	        // ��key-value ����SubMap��  
	        public final V put(K key, V value) {
	            if (!inRange(key))
	                throw new IllegalArgumentException("key out of range");
	            return m.put(key, value);
	        }
	        // ��ȡkey��Ӧֵ  
	        public final V get(Object key) {
	            return !inRange(key) ? null :  m.get(key);
	        }
	        // ɾ��key��Ӧ�ļ�ֵ��  
	        public final V remove(Object key) {
	            return !inRange(key) ? null : m.remove(key);
	        }
	     // ��ȡ������/����key����С��ֵ�ԡ�  
	        public final Map.Entry<K,V> ceilingEntry(K key) {
	            return exportEntry(subCeiling(key));
	        }
	     // ��ȡ������/����key����С����  
	        public final K ceilingKey(K key) {
	            return keyOrNull(subCeiling(key));
	        }
	       // ��ȡ������key����С��ֵ�ԡ�  
	        public final Map.Entry<K,V> higherEntry(K key) {
	            return exportEntry(subHigher(key));
	        }
	        // ��ȡ������key����С����  
	        public final K higherKey(K key) {
	            return keyOrNull(subHigher(key));
	        }
	        // ��ȡ��С��/����key������ֵ�ԡ� 
	        public final Map.Entry<K,V> floorEntry(K key) {
	            return exportEntry(subFloor(key));
	        }
	     // ��ȡ��С��/����key��������  
	        public final K floorKey(K key) {
	            return keyOrNull(subFloor(key));
	        }
	     // ��ȡ��С��key������ֵ�ԡ�  
	        public final Map.Entry<K,V> lowerEntry(K key) {
	            return exportEntry(subLower(key));
	        }
	        // ��ȡ��С��key��������  
	        public final K lowerKey(K key) {
	            return keyOrNull(subLower(key));
	        }
	        // ��ȡ"SubMap�ĵ�һ����"  
	        public final K firstKey() {
	            return key(subLowest());
	        }
	        // ��ȡ"SubMap�����һ����"  
	        public final K lastKey() {
	            return key(subHighest());
	        }
	        // ��ȡ"SubMap�ĵ�һ����"  
	        public final Map.Entry<K,V> firstEntry() {
	            return exportEntry(subLowest());
	        }
	        // ��ȡ"SubMap�����һ����"  
	        public final Map.Entry<K,V> lastEntry() {
	            return exportEntry(subHighest());
	        }
	     // ����"SubMap�ĵ�һ����ֵ��"������SubMap��ɾ���ļ�ֵ��  
	        public final Map.Entry<K,V> pollFirstEntry() {
	            TreeMap.Entry<K,V> e = subLowest();
	            Map.Entry<K,V> result = exportEntry(e);
	            if (e != null)
	                m.deleteEntry(e);
	            return result;
	        }
	        // ����"SubMap�����һ����ֵ��"������SubMap��ɾ���ļ�ֵ��  
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
	        // ����NavigableSet����ʵ���Ϸ��ص��ǵ�ǰ�����"Key����"��   
	        public final NavigableSet<K> navigableKeySet() {
	            KeySet<K> nksv = navigableKeySetView;
	            return (nksv != null) ? nksv :
	                (navigableKeySetView = new TreeMap.KeySet<>(this));
	        }
	        // ����"Key����"����  
	        public final Set<K> keySet() {
	            return navigableKeySet();
	        }
	     // ���ء����򡱵�Key���� 
	        public NavigableSet<K> descendingKeySet() {
	            return descendingMap().navigableKeySet();
	        }
	     // ����fromKey(����) �� toKey(������) ����map 
	        public final SortedMap<K,V> subMap(K fromKey, K toKey) {
	            return subMap(fromKey, true, toKey, false);
	        }
	        // ���ص�ǰMap��ͷ��(�ӵ�һ���ڵ� �� toKey, ������toKey)  
	        public final SortedMap<K,V> headMap(K toKey) {
	            return headMap(toKey, false);
	        }
	        // ���ص�ǰMap��β��[�� fromKey(����fromKeyKey) �� ���һ���ڵ�] 
	        public final SortedMap<K,V> tailMap(K fromKey) {
	            return tailMap(fromKey, true);
	        }

	        // View classes
	       // Map��Entry�ļ��� 
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

	     // SubMap�ĵ�����  
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
	         // ɾ����ǰ�ڵ�(���ڡ������SubMap��)��  
	            // ɾ��֮�󣬿��Լ���������������������û�䡣
	            final void removeAscending() {
	                if (lastReturned == null)
	                    throw new IllegalStateException();
	                if (m.modCount != expectedModCount)
	                    throw new ConcurrentModificationException();
	                // �����ص�ǿ��һ�¡�Ϊʲô��lastReturned�����Һ��Ӷ���Ϊ��ʱ��Ҫ���丳ֵ��next����  
	                // Ŀ����Ϊ�ˡ�ɾ��lastReturned�ڵ�֮��next�ڵ�ָ�����Ȼ����һ���ڵ㡱��  
	                //     ���ݡ�������������Կ�֪��  
	                //     ����ɾ���ڵ�����������ʱ����ô�����Ȱѡ����ĺ�̽ڵ�����ݡ����Ƹ����ýڵ�����ݡ���֮��ɾ�������ĺ�̽ڵ㡱��  
	                //     ����ζ�š�����ɾ���ڵ�����������ʱ��ɾ����ǰ�ڵ�֮��'�µĵ�ǰ�ڵ�'ʵ�����ǡ�ԭ�еĺ�̽ڵ�(����һ���ڵ�)������  
	                //     ����ʱnext��Ȼָ��"�µĵ�ǰ�ڵ�"��Ҳ����˵next����Ȼ��ָ����һ���ڵ㣻�ܼ��������������
	                if (lastReturned.left != null && lastReturned.right != null)
	                    next = lastReturned;
	                m.deleteEntry(lastReturned);
	                lastReturned = null;
	                expectedModCount = m.modCount;
	            }
	            // ɾ����ǰ�ڵ�(���ڡ������SubMap��)��  
	            // ɾ��֮�󣬿��Լ���������������������û�䡣  
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
	        // SubMap��Entry����������ֻ֧������������̳���SubMapIterator  
	        final class SubMapEntryIterator extends SubMapIterator<Map.Entry<K,V>> {
	            SubMapEntryIterator(TreeMap.Entry<K,V> first,
	                                TreeMap.Entry<K,V> fence) {
	                super(first, fence);
	            }
	         // ��ȡ��һ���ڵ�(����)  
	            public Map.Entry<K,V> next() {
	                return nextEntry();
	            }
	         // ɾ����ǰ�ڵ�(����) 
	            public void remove() {
	                removeAscending();
	            }
	        }
	        // ����SubMap��Entry����������ֻ֧�ֽ���������̳���SubMapIterator  
	        final class DescendingSubMapEntryIterator extends SubMapIterator<Map.Entry<K,V>> {
	            DescendingSubMapEntryIterator(TreeMap.Entry<K,V> last,
	                                          TreeMap.Entry<K,V> fence) {
	                super(last, fence);
	            }
	            // ��ȡ��һ���ڵ�(����)  
	            public Map.Entry<K,V> next() {
	                return prevEntry();
	            }
	         // ɾ����ǰ�ڵ�(����)  
	            public void remove() {
	                removeDescending();
	            }
	        }
	        // SubMap��Key����������ֻ֧������������̳���SubMapIterator  
	        final class SubMapKeyIterator extends SubMapIterator<K>
	            implements Spliterator<K> {
	            SubMapKeyIterator(TreeMap.Entry<K,V> first,
	                              TreeMap.Entry<K,V> fence) {
	                super(first, fence);
	            }
	         // ��ȡ��һ���ڵ�(����)  
	            public K next() {
	                return nextEntry().key;
	            }
	            // ɾ����ǰ�ڵ�(����) 
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
	     // ����SubMap��Key����������ֻ֧�ֽ���������̳���SubMapIterator  
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

	 // �����SubMap���̳���NavigableSubMap  
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
	        // ��ȡ����Map����  
	        // ��Χ�Ǵ�fromKey �� toKey��fromInclusive���Ƿ����fromKey�ı�ǣ�toInclusive���Ƿ����toKey�ı��
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
	        // ��ȡ��Map��ͷ������  
	        // ��Χ�ӵ�һ���ڵ� �� toKey, inclusive���Ƿ����toKey�ı��  
	        public NavigableMap<K,V> headMap(K toKey, boolean inclusive) {
	            if (!inRange(toKey, inclusive))
	                throw new IllegalArgumentException("toKey out of range");
	            return new AscendingSubMap<>(m,
	                                         fromStart, lo,    loInclusive,
	                                         false,     toKey, inclusive);
	        }
	        // ��ȡ��Map��β������  
	        // ��Χ�Ǵ� fromKey �� ���һ���ڵ㣬inclusive���Ƿ����fromKey�ı��  
	        public NavigableMap<K,V> tailMap(K fromKey, boolean inclusive) {
	            if (!inRange(fromKey, inclusive))
	                throw new IllegalArgumentException("fromKey out of range");
	            return new AscendingSubMap<>(m,
	                                         false, fromKey, inclusive,
	                                         toEnd, hi,      hiInclusive);
	        }
	     // ��ȡ��Ӧ�Ľ���Map  
	        public NavigableMap<K,V> descendingMap() {
	            NavigableMap<K,V> mv = descendingMapView;
	            return (mv != null) ? mv :
	                (descendingMapView =
	                 new DescendingSubMap<>(m,
	                                        fromStart, lo, loInclusive,
	                                        toEnd,     hi, hiInclusive));
	        }
	        // ���ء�����Key��������  
	        Iterator<K> keyIterator() {
	            return new SubMapKeyIterator(absLowest(), absHighFence());
	        }

	        Spliterator<K> keySpliterator() {
	            return new SubMapKeyIterator(absLowest(), absHighFence());
	        }
	        // ���ء�����Key��������  
	        Iterator<K> descendingKeyIterator() {
	            return new DescendingSubMapKeyIterator(absHighest(), absLowFence());
	        }
	        // ������EntrySet���ϡ���  
	        // ʵ����iterator()  
	        final class AscendingEntrySetView extends EntrySetView {
	            public Iterator<Map.Entry<K,V>> iterator() {
	                return new SubMapEntryIterator(absLowest(), absHighFence());
	            }
	        }
	        // ���ء�����EntrySet���ϡ�  
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

	 // �����SubMap���̳���NavigableSubMap  
	 // ���������SubMap������ʵ�ֻ����ǽ���SubMap�ıȽ�����ת����  
	    static final class DescendingSubMap<K,V>  extends NavigableSubMap<K,V> {
	        private static final long serialVersionUID = 912986545866120460L;
	        DescendingSubMap(TreeMap<K,V> m,
	                        boolean fromStart, K lo, boolean loInclusive,
	                        boolean toEnd,     K hi, boolean hiInclusive) {
	            super(m, fromStart, lo, loInclusive, toEnd, hi, hiInclusive);
	        }
	     // ��ת�ıȽ������ǽ�ԭʼ�Ƚ�����ת�õ��ġ�  
	        private final Comparator<? super K> reverseComparator =
	            Collections.reverseOrder(m.comparator);
	        // ��ȡ��ת�Ƚ���  
	        public Comparator<? super K> comparator() {
	            return reverseComparator;
	        }
	        // ��ȡ����Map����  
	        // ��Χ�Ǵ�fromKey �� toKey��fromInclusive���Ƿ����fromKey�ı�ǣ�toInclusive���Ƿ����toKey�ı�� 
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
	        // ��ȡ��Map��ͷ������  
	        // ��Χ�ӵ�һ���ڵ� �� toKey, inclusive���Ƿ����toKey�ı�� 
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
	        // ��ȡ��Map��β������  
	        // ��Χ�Ǵ� fromKey �� ���һ���ڵ㣬inclusive���Ƿ����fromKey�ı��  
	        public NavigableMap<K,V> descendingMap() {
	            NavigableMap<K,V> mv = descendingMapView;
	            return (mv != null) ? mv :
	                (descendingMapView =
	                 new AscendingSubMap<>(m,
	                                       fromStart, lo, loInclusive,
	                                       toEnd,     hi, hiInclusive));
	        }
	        // ��ȡ��Ӧ�Ľ���Map  
	        Iterator<K> keyIterator() {
	            return new DescendingSubMapKeyIterator(absHighest(), absLowFence());
	        }
	        // ���ء�����Key��������  
	        Spliterator<K> keySpliterator() {
	            return new DescendingSubMapKeyIterator(absHighest(), absLowFence());
	        }
	        // ���ء�����Key�������� 
	        Iterator<K> descendingKeyIterator() {
	            return new SubMapKeyIterator(absLowest(), absHighFence());
	        }
	    }  
	    // ������EntrySet���ϡ���  
	    // ʵ����iterator()  
	        final class DescendingEntrySetView extends EntrySetView {
	            public Iterator<Map.Entry<K,V>> iterator() {
	                return new DescendingSubMapEntryIterator(absHighest(), absLowFence());
	            }
	        }
	        // ���ء�����EntrySet���ϡ�  
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

//SubMap�Ǿɰ汾���࣬�µ�Java��û���õ���  
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
	 // �����Ѿ�һ���ź����map����һ��TreeMap 
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

