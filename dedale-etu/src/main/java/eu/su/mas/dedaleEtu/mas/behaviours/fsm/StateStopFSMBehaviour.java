package eu.su.mas.dedaleEtu.mas.behaviours.fsm;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.OneShotBehaviour;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class StateStopFSMBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = 7567689731496787661L;

    private List<String> list_agentNames;
    private HashMap<String, HashMap<String, Boolean>> dictVoisinsMessages;
    private MapRepresentation myMap;
    private int exitValue;

    public StateStopFSMBehaviour(final AbstractDedaleAgent myagent) {
        super(myagent);
    }

    public void action() {
        String myName = this.myAgent.getLocalName();

        System.out.println("\n-- START state F (StateStopFSMBehaviour): " + myName + " --");
        System.out.println("Nothing yet");
    }

    public int onEnd() {
        return exitValue;
    }
}
