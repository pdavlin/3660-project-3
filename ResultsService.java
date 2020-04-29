import java.util.List;
import java.util.Map;

public class ResultsService {
    public static void printResults(Map<String, DFAState> dfaStateMachine, List<String> inputAlphabet) {
        System.out.println("% Q");
        dfaStateMachine.keySet().forEach(key -> {
            System.out.println(formatAsSet(key));
        });

        System.out.println("% Sigma");
        inputAlphabet.forEach(inputString -> {
            System.out.println(inputString);
        });

        System.out.println("% F");
        dfaStateMachine.forEach((key, val) -> {
            if (val.getAccepting())
                System.out.println(formatAsSet(key));
        });

        System.out.println("% Q0");
        dfaStateMachine.forEach((name, state) -> {
            if (state.getInitial())
            System.out.println(formatAsSet(name));
        });

        System.out.println("% Delta");
        dfaStateMachine.forEach((stateName, state) -> {

            final String stateNameFinal = stateName;

            inputAlphabet.forEach(inputString -> {
                try {
                String nextState = state.getNextStates(inputString).getName();

                System.out.println(formatAsSet(stateNameFinal) + " " + inputString + " " + formatAsSet(nextState));
                }
                catch (NullPointerException npe) {
                    
                }
            });
        });
    }

    private static String formatAsSet(String in) {
        return "{".concat(in).concat("}");
    }
}