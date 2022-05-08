package eu.su.mas.dedaleEtu.mas.behaviours.fsm;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.fsm.FSMAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.FullMapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

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
    //private MapRepresentation myMap;
    private FullMapRepresentation myFullMap;
    private int exitValue;

    public StateRandomWalkFSMBehaviour(final AbstractDedaleAgent myagent) {
        super(myagent);
    }

    public void action() {
        String myName = this.myAgent.getLocalName();

        System.out.println("\n--- START state G (StateRandomWalkFSMBehaviour): " + myName + " ---");

        try {
            this.myAgent.doWait(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // update information
        //this.myMap = ((FSMAgent) this.myAgent).getMyMap();
        this.myFullMap = ((FSMAgent) this.myAgent).getMyFullMap();
        //this.dictVoisinsMessages = ((FSMAgent) this.myAgent).getDictVoisinsMessages();
        this.listAgentNames = ((FSMAgent) this.myAgent).getListAgentNames();

        String myPosition = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition();
        System.out.println(myName + " [STATE G] -- myCurrentPosition is: " + myPosition);

        HashMap<String, Couple<Integer, String>> goldDict = this.myFullMap.getGoldDict();
        HashMap<String, Couple<Integer, String>> diamondDict = this.myFullMap.getDiamondDict();

        System.out.println(myName + " [STATE G] -- goldDict: " + goldDict + " | diamondDict: " + diamondDict);

        if (myPosition != null) {
            exitValue = -1;
            // List of observable from the agent's current position
            List<Couple<String, List<Couple<Observation, Integer>>>> lobs = ((AbstractDedaleAgent) this.myAgent).observe(); // myPosition
            System.out.println(myName + " [STATE G] -- list of observables: " + lobs
                    + " | size listAllNodes: " + this.myFullMap.getAllNodes().size()
                    + " | size listOpenNodes: " + this.myFullMap.getOpenNodes().size()
                    + " | size listClosedNodes: " + this.myFullMap.getClosedNodes().size()
                    + " | size listOtherNodes: " + this.myFullMap.getOtherNodes().size()
            );

            // list of observations associated to the currentPosition
            List<Couple<Observation, Integer>> lObservations = lobs.get(0).getRight();
            System.out.println(myName + " [STATE E] -- lObservations - " + lObservations);

            List<Couple<Observation, Integer>> listCapacity = ((FSMAgent) this.myAgent).getBackPackFreeSpace();
            for(Couple<Observation, Integer> capacity : listCapacity){
                if(((FSMAgent) this.myAgent).getTypeTreasure()==capacity.getLeft()||((FSMAgent) this.myAgent).getTypeTreasure()==null){
                    if (capacity.getRight() > 0){
                        exitValue = 2; //go to in stats E (state collect)
                        System.out.println(myName + " -- [STATE G] -- Go to state E (collect) ");
                    }
                }
            }
            // chose a random next node to go to
            Random r = new Random();
            int moveId = 1 + r.nextInt(lobs.size() - 1); // removing the current position from the list of target, not necessary as to stay is an action but allow quicker random move
            String nextNode = lobs.get(moveId).getLeft();
            System.out.println(myName + " [STATE G] will move to " + nextNode);
            ((AbstractDedaleAgent) this.myAgent).moveTo(nextNode);
        }

        // ACTION : Envoyer sa carte à tous ses voisins
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setProtocol("FULL-MAP");
        msg.setSender(this.myAgent.getAID()); // mettre un expéditeur

        //Envoit sa carte (qui est complete) à tous les agents
        for (String receiverAgent : this.listAgentNames) {
            msg.addReceiver(new AID(receiverAgent, false));
            System.out.println(myName + " [STATE G] will send MAP to " + receiverAgent);
        }

        // ajout de la carte de l'agent dans le message
        //SerializableSimpleGraph<String, MapRepresentation MapAttribute> mapSent = this.myMap.getSerializableGraph();
        SerializableSimpleGraph<String, HashMap<String, Object>> mapSent = this.myFullMap.getSerializableGraph();

        try {
            msg.setContentObject(mapSent);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // envoie en cours de la carte à tous les voisins
        ((AbstractDedaleAgent) this.myAgent).sendMessage(msg);

        // ACTION : Check si l'agent a reçu une carte
//        MessageTemplate msgMap = MessageTemplate.and(
//                MessageTemplate.MatchProtocol("FULL-MAP"),
//                MessageTemplate.MatchPerformative(ACLMessage.INFORM));
//
//        ACLMessage msgMapReceived = this.myAgent.receive(msgMap);
//
//        if (msgMapReceived != null) { // si l'agent a reçu une MAP
//            System.out.println(myName + " [STATE G] received FULL-MAP");
//
//            SerializableSimpleGraph<String, HashMap<String, Object>> mapReceived = null;
//            SerializableSimpleGraph<String, HashMap<String, Object>> allInformation = null;
//
//            try {
//                allInformation = (SerializableSimpleGraph<String, HashMap<String, Object>>) msgMapReceived.getContentObject();
//                mapReceived = allInformation; // pour l'instant, on n'a qu'une carte, mais après on pourra envoyer d'autres informations
//            } catch (UnreadableException e) {
//                e.printStackTrace();
//            }
//            assert mapReceived != null;
//            this.myFullMap.mergeMap(mapReceived);
//        }
        if(exitValue==-1){
            exitValue = 1; // reste au state G (Random Walk)
            System.out.println(myName + " STAYS in G");
        }
    }

    public int onEnd() {
        return exitValue;
    }
}
