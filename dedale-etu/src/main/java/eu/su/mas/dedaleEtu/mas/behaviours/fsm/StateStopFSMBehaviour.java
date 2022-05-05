package eu.su.mas.dedaleEtu.mas.behaviours.fsm;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.fsm.FSMAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.FullMapRepresentation;
import fileManipulations.JSonUtil;
import jade.core.behaviours.OneShotBehaviour;
import dataStructures.tuple.Couple;
import java.util.HashMap;
import java.util.List;

public class StateStopFSMBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = 7567689731496787661L;

    private List<String> listAgentNames;
    private HashMap<String, HashMap<String, Boolean>> dictVoisinsMessages;
    private FullMapRepresentation myFullMap;
    private int exitValue;

    public StateStopFSMBehaviour(final AbstractDedaleAgent myagent) {
        super(myagent);
    }

    public void action() {
        String myName = this.myAgent.getLocalName();

        System.out.println("\n-- START state F (StateStopFSMBehaviour): " + myName + " --");

        // update information
        this.myFullMap = ((FSMAgent) this.myAgent).getMyFullMap();
        this.dictVoisinsMessages = ((FSMAgent) this.myAgent).getDictVoisinsMessages();
        this.listAgentNames = ((FSMAgent) this.myAgent).getListAgentNames();

        HashMap<String, Couple<Integer, String>> goldDict = this.myFullMap.getGoldDict();
        HashMap<String, Couple<Integer, String>> diamondDict = this.myFullMap.getDiamondDict();

        System.out.println(myName + " [STATE F] -- goldDict: " + goldDict);
        System.out.println(myName + " [STATE F] -- diamondDict: " + diamondDict);

    }

    public int onEnd() {
        return exitValue;
    }
}
