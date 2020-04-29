import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class NFAState {
    // Map input string to list of next State Names
    private final Map<String, List<NFAState>> nextStates;
    private boolean accepting;
    private boolean initial;
    private String stateName;

    public NFAState() {
        this.nextStates = new HashMap<String, List<NFAState>>();
        this.accepting = false;
        this.initial = false;
    }

    public void setNextStates(String alpha, NFAState nextState) {
        if (this.nextStates.get(alpha) == null) {
            this.nextStates.put(alpha, new ArrayList<NFAState>());
        }
        List<NFAState> nextStatesTemp = this.nextStates.get(alpha);
        nextStatesTemp.add(nextState);
        this.nextStates.put(alpha, nextStatesTemp);
    }

    public List<NFAState> getNextStates(String alpha) {
        return nextStates.get(alpha);
    }

    public Map<String, List<NFAState>> getAllNextStates() {
        return nextStates;
    }

    public void setAccepting() {
        this.accepting = true;
    }

    public boolean getAccepting() {
        return this.accepting;
    }

	public void setName(String name) {
        this.stateName = name;
    }
    public String getName(){
        return this.stateName;
    }

	public void setInitial() {
        initial = true;
	}
}