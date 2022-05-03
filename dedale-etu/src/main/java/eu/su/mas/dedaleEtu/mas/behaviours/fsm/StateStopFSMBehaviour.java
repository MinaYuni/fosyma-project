package eu.su.mas.dedaleEtu.mas.behaviours.fsm;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.fsm.FSMAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class StateStopFSMBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = 5567689731496787661L;

    private MapRepresentation myMap;
    private List<String> list_agentNames;
    private HashMap<String, HashMap<String, Boolean>> dictVoisinsMessages;
    private int exitValue;

    public StateStopFSMBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap, List<String> agentNames, HashMap<String, HashMap<String, Boolean>> dico) {
        super(myagent);
        this.myMap = myMap;
        this.list_agentNames = agentNames;
        this.dictVoisinsMessages = dico;
    }

    public void action() {
        int nb_agents = this.list_agentNames.size();
        String myName = this.myAgent.getLocalName();

        System.out.println("\n-- START state F (StateStopFSMBehaviour): " + myName + " --");

        // update information
        if (this.myMap == null) {
            this.myMap = new MapRepresentation();
        }
        this.dictVoisinsMessages = ((FSMAgent) this.myAgent).getDictVoisinsMessages();

        String myPosition = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition();
        System.out.println("STATE F : " + myName + " -- myCurrentPosition is: " + myPosition);

        if (myPosition != null) {
            // List of observable from the agent's current position
            List<Couple<String, List<Couple<Observation, Integer>>>> lobs = ((AbstractDedaleAgent) this.myAgent).observe(); // myPosition
            System.out.println("STATE F : " + myName + " -- list of observables: " + lobs);

            // chose a random next node to go to
            Random r = new Random();
            int moveId = 1 + r.nextInt(lobs.size() - 1); // removing the current position from the list of target, not necessary as to stay is an action but allow quicker random move
            String nextNode = lobs.get(moveId).getLeft();
            System.out.println("STATE F : " + myName + " will move to " + nextNode);
            ((AbstractDedaleAgent) this.myAgent).moveTo(nextNode);
        }
    }

    public int onEnd() {
        return exitValue;
    }
}
