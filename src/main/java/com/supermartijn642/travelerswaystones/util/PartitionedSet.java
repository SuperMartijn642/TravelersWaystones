package com.supermartijn642.travelerswaystones.util;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class PartitionedSet<K, V> {

    private final Map<K,Set<V>> map = new HashMap<>();
    private final Map<K,Set<V>> mapViews = new HashMap<>();
    private final Map<K,Set<V>> view = Collections.unmodifiableMap(this.mapViews);
    private final Function<V,K> keyExtractor;
    private final Supplier<Set<V>> setConstructor;

    public PartitionedSet(Function<V,K> keyExtractor, Supplier<Set<V>> setConstructor){
        this.keyExtractor = keyExtractor;
        this.setConstructor = setConstructor;
    }

    public PartitionedSet(Function<V,K> keyExtractor){
        this(keyExtractor, HashSet::new);
    }

    public Set<V> get(K key){
        return this.mapViews.getOrDefault(key, Set.of());
    }

    public Set<V> getPartition(V value){
        return this.get(this.keyExtractor.apply(value));
    }

    public boolean add(V value){
        K key = this.keyExtractor.apply(value);
        return this.map.computeIfAbsent(key, k -> {
            Set<V> set = this.setConstructor.get();
            this.mapViews.put(k, Collections.unmodifiableSet(set));
            return set;
        }).add(value);
    }

    public void addAll(PartitionedSet<? extends K,? extends V> partitioning){
        for(Map.Entry<? extends K,? extends Set<? extends V>> entry : partitioning.map.entrySet()){
            K key = entry.getKey();
            Set<? extends V> values = entry.getValue();
            this.map.computeIfAbsent(key, k -> {
                Set<V> set = this.setConstructor.get();
                this.mapViews.put(k, Collections.unmodifiableSet(set));
                return set;
            }).addAll(values);
        }
    }

    public boolean remove(V value){
        K key = this.keyExtractor.apply(value);
        Set<V> values = this.map.get(key);
        if(values.remove(value)){
            if(values.isEmpty()){
                this.map.remove(key);
                this.mapViews.remove(key);
            }
            return true;
        }
        return false;
    }

    public void clear(){
        this.map.clear();
        this.mapViews.clear();
    }

    public Map<K,Set<V>> getMapView(){
        return this.view;
    }
}
