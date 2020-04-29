import java.util.ArrayList;
import java.util.List;

public class NFAProcessor {
    List<NFAState> destinationStates;
    boolean accepting;
    List<String> destinationStateNames;

    public NFAProcessor() {
        this.destinationStates = new ArrayList<NFAState>();
        this.accepting = false;
        this.destinationStateNames = new ArrayList<String>();
    }

    public List<NFAState> getDestinationStates() {
        return destinationStates;
    }

    public void setDestinationStates(List<NFAState> destinationStates) {
        this.destinationStates = destinationStates;
    }

    public boolean isAccepting() {
        return accepting;
    }

    public void setAccepting(boolean accepting) {
        this.accepting = accepting;
    }

    public List<String> getDestinationStateNames() {
        return destinationStateNames;
    }

    public void addDestinationStateName(String destinationStateName) {
        this.destinationStateNames.add(destinationStateName);
    }

    public void setDestinationStateNames(List<String> destinationStateNames) {
        this.destinationStateNames = destinationStateNames;
    }
}