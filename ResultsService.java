import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ResultsService {
    public static void printResults(Map<String, DFAState> dfaStateMachine, List<String> inputAlphabet) {
        System.out.println("% Q");

        dfaStateMachine.keySet().stream().map(entry -> formatAsSet(entry)).sorted().forEach(System.out::println);

        System.out.println("% Sigma");
        inputAlphabet.stream().sorted().forEach(System.out::println);

        System.out.println("% F");
        dfaStateMachine.values().stream().filter(DFAState::getAccepting).map(DFAState::getName)
                .map(ResultsService::formatAsSet).sorted().forEach(System.out::println);

        System.out.println("% Q0");
        dfaStateMachine.values().stream().filter(DFAState::getInitial).map(DFAState::getName)
                .map(ResultsService::formatAsSet).sorted().forEach(System.out::println);

        System.out.println("% Delta");
        dfaStateMachine.values().stream().flatMap(state -> {
            return inputAlphabet.stream().map(functionName -> {
                String nextStateName = state.getNextStates(functionName).getName();
                return formatAsSet(state.getName()) + " " + functionName + " " + formatAsSet(nextStateName);
            }).sorted();
        }).sorted().forEach(System.out::println);
    }
    private static String formatAsSet(String in) {
        return "{".concat(in).concat("}");
    }
}