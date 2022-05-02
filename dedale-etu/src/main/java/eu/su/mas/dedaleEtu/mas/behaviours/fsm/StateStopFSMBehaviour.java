package eu.su.mas.dedaleEtu.mas.behaviours.fsm;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.HashMap;
import java.util.List;

public class StateStopFSMBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = 8567689731499797561L;

    private MapRepresentation myMap;
    private List<String> list_agentNames;
    private HashMap<String, HashMap<String, Boolean>> dictVoisinsMessages;
    private int exitValue;

    public StateStopFSMBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap, List<String> agentNames, HashMap<String, HashMap<String, Boolean>> dico) {
        super(myagent);
        this.myMap=myMap;
        this.list_agentNames=agentNames;
        this.dictVoisinsMessages = dico;
    }

    public void action() {
        System.out.println("START state F (StateStopFSMBehaviour): " + this.myAgent.getLocalName());
        System.out.println("There is nothing yet in state F");
    }
}
