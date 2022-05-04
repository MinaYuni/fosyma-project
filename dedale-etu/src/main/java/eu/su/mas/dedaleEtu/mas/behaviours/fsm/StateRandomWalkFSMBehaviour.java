package eu.su.mas.dedaleEtu.mas.behaviours.fsm;

import dataStructures.serializableGraph.SerializableSimpleGraph;
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
import java.util.Set;

//Behaviour/comportement du state G (random walk et envoie sa carte complete)
public class StateRandomWalkFSMBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = 5567689731496787661L;

    private List<String> listAgentNames;
    //private HashMap<String, HashMap<String, Boolean>> dictVoisinsMessages;
    private MapRepresentation myMap;
    private int exitValue;

    public StateRandomWalkFSMBehaviour(final AbstractDedaleAgent myagent) {
        super(myagent);
    }

    public void action() {
        String myName = this.myAgent.getLocalName();

        System.out.println("\n--- START state G (StateRandomWalkFSMBehaviour): " + myName + " ---");

        // update information
        this.myMap = ((FSMAgent) this.myAgent).getMyMap();
        //this.dictVoisinsMessages = ((FSMAgent) this.myAgent).getDictVoisinsMessages();
        this.listAgentNames = ((FSMAgent) this.myAgent).getListAgentNames();

        String myPosition = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition();
        System.out.println(myName + " [STATE G] -- myCurrentPosition is: " + myPosition);

        if (myPosition != null) {
            // List of observable from the agent's current position
            List<Couple<String, List<Couple<Observation, Integer>>>> lobs = ((AbstractDedaleAgent) this.myAgent).observe(); // myPosition
            System.out.println(myName + " [STATE G] -- list of observables: " + lobs);

            // chose a random next node to go to
            Random r = new Random();
            int moveId = 1 + r.nextInt(lobs.size() - 1); // removing the current position from the list of target, not necessary as to stay is an action but allow quicker random move
            String nextNode = lobs.get(moveId).getLeft();
            System.out.println(myName + " [STATE G] will move to " + nextNode);
            ((AbstractDedaleAgent) this.myAgent).moveTo(nextNode);
        }

        // ACTION : Envoyer sa carte à tous ses voisins
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setProtocol("SHARE-MAP");
        msg.setSender(this.myAgent.getAID()); // mettre un expéditeur

        //Envoit sa carte (qui est complete) à tous les agents
        for (String receiverAgent : this.listAgentNames) {
            msg.addReceiver(new AID(receiverAgent, false));

            System.out.println(myName + " [STATE G] sends MAP to " + receiverAgent);
        }

        // ajout de la carte de l'agent dans le message
        SerializableSimpleGraph<String, MapRepresentation.MapAttribute> mapSent = this.myMap.getSerializableGraph();

        try {
            msg.setContentObject(mapSent);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // envoie en cours de la carte à tous les voisins
        ((AbstractDedaleAgent) this.myAgent).sendMessage(msg);

        exitValue = 1; //reste au state G (RandomWalk)
    }

    public int onEnd() {
        return exitValue;
    }
}
