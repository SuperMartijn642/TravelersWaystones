package com.supermartijn642.travelerswaystones.util;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class IndexedSet<K, V> {

    private final Map<K,V> map = new HashMap<>();
    private final Map<K,V> view = Collections.unmodifiableMap(this.map);
    private final Function<V,K> keyExtractor;

    public IndexedSet(Function<V,K> keyExtractor){
        this.keyExtractor = keyExtractor;
    }

    @Nullable
    public V get(K key){
        return this.map.get(key);
    }

    public boolean containsKey(K key){
        return this.map.containsKey(key);
    }

    public boolean contains(V value){
        return this.containsKey(this.keyExtractor.apply(value));
    }

    public boolean add(V value){
        K key = this.keyExtractor.apply(value);
        return this.map.put(key, value) == null;
    }

    public void addAll(IndexedSet<? extends K,? extends V> index){
        this.map.putAll(index.map);
    }

    public boolean removeKey(K key){
        return this.map.remove(key) != null;
    }

    public boolean remove(V value){
        return this.removeKey(this.keyExtractor.apply(value));
    }

    public void clear(){
        this.map.clear();
    }

    public Map<K,V> getMapView(){
        return this.view;
    }
}
