package org.povworld.collection.immutable;

import javax.annotation.concurrent.Immutable;

import org.povworld.collection.Map;

@Immutable
public interface ImmutableMap<K, V> extends Map<K, V> {
    
}
