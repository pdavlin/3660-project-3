import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NFAConvert {
    private static String CLASS_NAME = NFAConvert.class.getSimpleName();
    private static List<String> inputAlphabet = new ArrayList<String>();

    private static Map<String, NFAState> nfaStateMachine = new HashMap<String, NFAState>();
    private static Map<String, DFAState> dfaStateMachine = new HashMap<String, DFAState>();
    private static String initState;

    public static void main(final String args[]) {
        if (args.length == 0) {
            System.out.println(CLASS_NAME + ": no input files specified");
            System.exit(0);
        } else {
            buildNFA(args[0]);
        }
        initializeDfaStateMachine();
        buildDFA();
    }

    public static void buildNFA(String filename) {
        List<String> lines = new ArrayList<>();
        try {
            int collectionPhase = 0;
            final Stream<String> stream = Files.lines(Paths.get(filename));
            lines = stream.collect(Collectors.toList());
            for (final String line : lines) {
                if (line.startsWith("%")) {
                    collectionPhase++;
                } else {
                    switch (collectionPhase) {
                        case 1:
                            NFAState nfaState = new NFAState();
                            nfaState.setName(line);
                            nfaStateMachine.put(line, nfaState);
                            break;
                        case 2:
                            inputAlphabet.add(line);
                            break;
                        case 3:
                            nfaStateMachine.get(line).setAccepting();
                            break;
                        case 4:
                            initState = line;
                            nfaStateMachine.get(line).setInitial();
                            break;
                        case 5:
                            String[] deltaFields = line.split(" ");
                            if (deltaFields.length == 2) {
                                nfaStateMachine.get(deltaFields[0]).setNextStates("E",
                                        nfaStateMachine.get(deltaFields[1]));
                            } else
                                nfaStateMachine.get(deltaFields[0]).setNextStates(deltaFields[1],
                                        nfaStateMachine.get(deltaFields[2]));
                            break;
                    }
                }
            }
            stream.close();
        } catch (final IOException ioe) {
            System.out.println(CLASS_NAME + ": the file '" + filename + "' could not be opened");
            System.exit(0);
        }
    }

    public static void initializeDfaStateMachine() {
        DFAState initialDfaState = new DFAState();
        initialDfaState.setInitial();
        String initialDfaStateName = buildStateSetFromEmptyStringConnections(initState);
        for (String nfaStateName : initialDfaStateName.split(",")) {
            if (nfaStateMachine.get(nfaStateName).getAccepting()) {
                initialDfaState.setAccepting(true);
            }
        }
        initialDfaState.setName(initialDfaStateName);
        dfaStateMachine.put(initialDfaStateName, initialDfaState);
    }

    public static String buildStateSetFromEmptyStringConnections(String stateName) {
        List<String> possibleIncludedStates = new ArrayList<String>();
        List<NFAState> nfaList = new ArrayList<NFAState>();

        for (String nfaStateName : stateName.split(",")) {
            if (!possibleIncludedStates.contains(stateName)) {
                possibleIncludedStates.add(stateName);
            }
            if (nfaStateMachine.get(nfaStateName).getNextStates("E") != null) {
                nfaList.addAll(nfaStateMachine.get(nfaStateName).getNextStates("E"));
                while (nfaList.size() > 0) {
                    String nfaNextStateName = nfaList.get(0).getName();
                    if (!possibleIncludedStates.contains(nfaNextStateName)) {
                        possibleIncludedStates.add(nfaNextStateName);
                        if (nfaStateMachine.get(nfaNextStateName).getNextStates("E") != null)
                            nfaList.addAll(nfaStateMachine.get(nfaNextStateName).getNextStates("E"));
                    }
                    nfaList.remove(0);
                }
            }
        }
        return possibleIncludedStates.stream().collect(Collectors.joining(","));
    }

    public static void buildDFA() {
        DFAState dfaBaseState = getDfaStateToProcess();
        if (dfaBaseState == null) {
            ResultsService.printResults(dfaStateMachine, inputAlphabet);
            System.exit(0);
        } else {
            List<String> nfaStatesToCheck = Arrays.asList(dfaBaseState.getName().split(","));
            Map<String, NFAProcessor> checkedNfaStates = checkNfaStates(nfaStatesToCheck);
            addCheckedStatesToDfaStateMachine(checkedNfaStates, dfaBaseState.getName());
            dfaBaseState.setProcessed();
            dfaStateMachine.put(dfaBaseState.getName(), dfaBaseState);
            buildDFA();
        }
    }

    private static void addCheckedStatesToDfaStateMachine(Map<String, NFAProcessor> checkedNfaStates,
            String baseStateName) {
        checkedNfaStates.forEach((inputString, nfaProc) -> {
            String dfaStateName = "";
            for (String nfaState : nfaProc.getDestinationStateNames()) {
                dfaStateName = dfaStateName.concat(nfaState).concat(",");
            }
            dfaStateName = dfaStateName.substring(0, dfaStateName.length() - 1);
            String expandedStateSet = buildStateSetFromEmptyStringConnections(dfaStateName);
            if (!dfaStateMachine.containsKey(expandedStateSet)) {
                DFAState newDfaState = new DFAState();
                newDfaState.setName(expandedStateSet);
                newDfaState.setAccepting(nfaProc.isAccepting());
                dfaStateMachine.put(expandedStateSet, newDfaState);
                dfaStateMachine.get(baseStateName).setNextStates(inputString, newDfaState);
            } else {
                dfaStateMachine.get(baseStateName).setNextStates(inputString, dfaStateMachine.get(expandedStateSet));

            }
        });
    }

    public static DFAState getDfaStateToProcess() {
        for (String dfaStateName : dfaStateMachine.keySet()) {
            if (!dfaStateMachine.get(dfaStateName).getProcessed()) {
                return dfaStateMachine.get(dfaStateName);
            }
        }
        return null;
    }

    public static Map<String, NFAProcessor> checkNfaStates(List<String> nfaStatesToCheck) {
        Map<String, NFAProcessor> destinationNfaStates = new HashMap<>();
        nfaStatesToCheck.forEach(nfaStateName -> {
            inputAlphabet.forEach(inputCharacter -> {
                NFAProcessor nfaProcessor = new NFAProcessor();
                List<NFAState> nextNfaStates = nfaStateMachine.get(nfaStateName).getNextStates(inputCharacter);
                if (nextNfaStates != null) {
                    nextNfaStates.forEach(nfaState -> {
                        nfaProcessor.addDestinationStateName(nfaState.getName());
                        if (nfaState.getAccepting())
                            nfaProcessor.setAccepting(true);
                    });
                    if (!destinationNfaStates.containsKey(inputCharacter)) {
                        destinationNfaStates.put(inputCharacter, nfaProcessor);
                    } else {
                        NFAProcessor nfaProcessorToDelete = destinationNfaStates.get(inputCharacter);
                        destinationNfaStates.put(inputCharacter,
                                adjustNfaProcessor(nfaProcessor, nfaProcessorToDelete));
                    }
                }
            });
        });
        return destinationNfaStates;
    }

    private static NFAProcessor adjustNfaProcessor(NFAProcessor nfaProcessor, NFAProcessor nfaProcessorToDelete) {
        NFAProcessor outputNfaProcessor = new NFAProcessor();
        List<String> nfaStatesToDelete = nfaProcessorToDelete.getDestinationStateNames();
        List<String> incomingNfaStates = nfaProcessor.getDestinationStateNames();
        incomingNfaStates.forEach(incomingNfaState -> {
            if (!nfaStatesToDelete.contains(incomingNfaState)) {
                nfaStatesToDelete.add(incomingNfaState);
            }
        });
        outputNfaProcessor.setDestinationStateNames(nfaStatesToDelete);
        outputNfaProcessor.setAccepting(nfaProcessor.isAccepting() || nfaProcessorToDelete.isAccepting());
        return outputNfaProcessor;
    }
}