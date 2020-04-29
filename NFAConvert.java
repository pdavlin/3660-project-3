import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.naming.spi.DirStateFactory.Result;

public class NFAConvert {
    private static String CLASS_NAME = NFAConvert.class.getSimpleName();
    private static List<String> inputAlphabet = new ArrayList<String>();

    private static Map<String, NFAState> nfaStateMachine = new HashMap<String, NFAState>();
    private static Map<String, DFAState> dfaStateMachine = new HashMap<String, DFAState>();

    private static Boolean debugMode = false;

    private static String initState;

    public static void main(final String args[]) {
        debugMode = (args.length == 2 && args[1].equals("debug"));
        if (args.length == 0) {
            // System.out.println(CLASS_NAME + ": no input files specified");
            buildNFA("input.txt");
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
        // initialDfaState.setName(buildStateSetFromEmptyStringConnections(initState));
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

        for (String nfaStateName : stateName.split(",")) {
            if (!possibleIncludedStates.contains(stateName))
                possibleIncludedStates.add(stateName);
            List<NFAState> nfaList = nfaStateMachine.get(nfaStateName).getNextStates("E");
            for (NFAState nfaNextState : nfaList) {
                String nfaNextStateName = nfaNextState.getName();
                if (!possibleIncludedStates.contains(nfaNextStateName))
                    possibleIncludedStates.add(nfaNextStateName);
            }
        }

        String dfaStateString = "";
        List<NFAState> nfaList = nfaStateMachine.get(stateName).getNextStates("E");
        for (String state : possibleIncludedStates) {
            dfaStateString = dfaStateString.concat(state).concat(",");
        }

        return dfaStateString.substring(0, dfaStateString.length() - 1);
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
            String debug = "debug";
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

            if (!dfaStateMachine.containsKey(dfaStateName)) {
                DFAState newDfaState = new DFAState();
                newDfaState.setName(dfaStateName);
                newDfaState.setAccepting(nfaProc.isAccepting());
                dfaStateMachine.put(dfaStateName, newDfaState);
                String debug = "debug";
                dfaStateMachine.get(baseStateName).setNextStates(inputString, newDfaState);
            } else {
                dfaStateMachine.get(baseStateName).setNextStates(inputString, dfaStateMachine.get(dfaStateName));
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
                    // destinationNfaStates.put(inputCharacter, concatenatedDestinationStates);
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