import java.util.HashMap;
import java.util.Map;

class DFAState {
    private String stateName;
    private final Map<String, DFAState> nextStates; // Map input string to list of next State Names
    private boolean accepting;
    private boolean initial;
    private boolean processed;

    public DFAState() {
        this.nextStates = new HashMap<String, DFAState>();
        this.accepting = false;
        this.initial = false;
    }

    public void setNextStates(String alpha, DFAState nextState) {
        this.nextStates.put(alpha, nextState);
    }

    public DFAState getNextStates(String alpha) {
        return nextStates.get(alpha);
    }

    public Map<String, DFAState> getAllNextStates() {
        return nextStates;
    }

    public void setAccepting(boolean accepting) {
        this.accepting = accepting;
    }

    public boolean getAccepting() {
        return this.accepting;
    }

	public void setName(String name) {
        this.stateName = name;
    }
    public String getName() {
        return this.stateName;
    }

	public void setInitial() {
        initial = true;
    }
    public boolean getInitial() {
        return this.initial;
	}

	public boolean getProcessed() {
		return this.processed;
    }

    public boolean isUnProcessed() {
        return !this.processed;
    }
    
    public void setProcessed() {
        this.processed = true;
    }
}