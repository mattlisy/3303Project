import java.util.Objects;
class KeyPair implements Comparable<KeyPair> {

    private final boolean firstKey;
    private final int secondKey;

    public KeyPair(boolean firstKey, int secondKey) {
        this.firstKey = firstKey;
        this.secondKey = secondKey;
    }

    // Implement hashCode() and equals() methods
    // so that instances of KeyPair can be used as keys in HashMaps or ConcurrentHashmaps

    public boolean getfirstKey() {
	    return firstKey;
    }

    public int getsecondKey() {
		return secondKey;
    }
   

    @Override
    public int hashCode() {
        return Objects.hash(firstKey, secondKey);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        KeyPair other = (KeyPair) obj;
        return firstKey == other.firstKey && secondKey == other.secondKey;
    }

    @Override
    public String toString() {
	return "(" + (firstKey ? "Up " :"Down")  + ", " + secondKey + ")"; 
    }

    @Override
    public int compareTo(KeyPair other) {
        // Compare first keys
        int firstKeyComparison = Boolean.compare(this.firstKey, other.firstKey);
        if (firstKeyComparison != 0) {
            return firstKeyComparison;
        }
        
        // If first keys are equal, compare second keys
        return Integer.compare(this.secondKey, other.secondKey);
    }
		
    

}
