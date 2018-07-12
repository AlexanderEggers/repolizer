package repolizer.repository.util;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QueryHashMap extends HashMap<String, Object> {

    @NotNull
    @Override
    public Set<Entry<String, Object>> entrySet() {
        Set<Entry<String, Object>> originSet = super.entrySet();
        Set<Entry<String, Object>> newSet = new HashSet<>();

        for (Entry<String, Object> entry : originSet) {
            String entryKey = entry.getKey();
            if (entryKey == null) {
                throw new IllegalArgumentException("Query map contained null key.");
            }
            Object entryValue = entry.getValue();
            if (entryValue == null) {
                throw new IllegalArgumentException(
                        "Query map contained null value for key '" + entryKey + "'.");
            } else if (entryValue instanceof List) {
                for (Object arrayValue : (List) entryValue) {
                    if (arrayValue != null) { // Skip null values
                        Entry<String, Object> newEntry = new AbstractMap.SimpleEntry<>(entryKey, arrayValue);
                        newSet.add(newEntry);
                    }
                }
            } else {
                Entry<String, Object> newEntry = new AbstractMap.SimpleEntry<>(entryKey, entryValue);
                newSet.add(newEntry);
            }
        }
        return newSet;
    }
}