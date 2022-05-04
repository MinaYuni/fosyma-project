package eu.su.mas.dedaleEtu.mas.behaviours.fsm;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.fsm.FSMAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.FullMapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.OneShotBehaviour;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class StateStopFSMBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = 7567689731496787661L;

    private List<String> listAgentNames;
    private HashMap<String, HashMap<String, Boolean>> dictVoisinsMessages;
    private FullMapRepresentation myMap;
    private int exitValue;

    public StateStopFSMBehaviour(final AbstractDedaleAgent myagent) {
        super(myagent);
    }

    public void action() {
        String myName = this.myAgent.getLocalName();

        System.out.println("\n-- START state F (StateStopFSMBehaviour): " + myName + " --");

        // update information
        this.myMap = ((FSMAgent) this.myAgent).getMyFullMap();
        this.dictVoisinsMessages = ((FSMAgent) this.myAgent).getDictVoisinsMessages();
        this.listAgentNames = ((FSMAgent) this.myAgent).getListAgentNames();

        HashMap<String, Integer> goldDict = this.myMap.getGoldDict();
        HashMap<String, Integer> diamondDict = this.myMap.getDiamondDict();

        System.out.println(myName + " [STATE F] -- goldDict: " + goldDict);
        System.out.println(myName + " [STATE F] -- diamondDict: " + diamondDict);

    }

    public int onEnd() {
        return exitValue;
    }
}
