package model.adt;

import exception.MyException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MyDictionary<K, V> implements MyIDictionary<K, V> {
    private Map<K, V> map;

    public MyDictionary() {
        this.map = new HashMap<>();
    }

    @Override
    public void put(K key, V value) throws MyException {
        // Punem valoarea direct. HashMap permite suprascrierea.
        map.put(key, value);
    }

    @Override
    public void update(K key, V value) throws MyException {
        // Update ar trebui să verifice dacă cheia există deja
        if (!map.containsKey(key)) {
            throw new MyException("Variable " + key + " is not defined. Cannot update.");
        }
        map.put(key, value);
    }

    @Override
    public V lookup(K key) throws MyException {
        V value = map.get(key);
        if (value == null) {
            throw new MyException("Variable " + key + " is not defined.");
        }
        return value;
    }

    @Override
    public boolean isDefined(K key) {
        return map.containsKey(key);
    }

    @Override
    public void remove(K key) throws MyException {
        if (!map.containsKey(key)) {
            throw new MyException("Variable " + key + " is not defined. Cannot remove.");
        }
        map.remove(key);
    }

    @Override
    public Map<K, V> getContent() {
        return map;
    }

    @Override
    public MyIDictionary<K, V> deepCopy() {
        MyDictionary<K, V> toReturn = new MyDictionary<>();
        // Facem o copie a structurii map-ului.
        // Valorile (Value) sunt imutabile (int, bool), deci e ok să copiem referințele lor.
        for (Map.Entry<K, V> entry : map.entrySet()) {
            try {
                toReturn.put(entry.getKey(), entry.getValue());
            } catch (MyException e) {
                // Această excepție nu va fi aruncată niciodată aici, deoarece punem într-un map gol
                System.out.println("Eroare la deepCopy: " + e.getMessage());
            }
        }
        return toReturn;
    }

    @Override
    public String toString() {
        // Optimizare: Formatare mai curată pentru GUI
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        for (Map.Entry<K, V> entry : map.entrySet()) {
            sb.append(entry.getKey().toString())
                    .append("->")
                    .append(entry.getValue().toString())
                    .append("; ");
        }
        sb.append("}");
        return sb.toString();
    }
}