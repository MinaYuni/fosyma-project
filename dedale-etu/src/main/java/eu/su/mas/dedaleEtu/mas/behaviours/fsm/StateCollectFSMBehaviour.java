package eu.su.mas.dedaleEtu.mas.behaviours.fsm;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.fsm.FSMAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.FullMapRepresentation;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.ArrayList;

import java.lang.Object;
//import javax.json.*;
//import javax.json.Json;
//import org.json.simple.*;
//import org.json.*;
//import org.json.simple.JSONValue;


public class StateCollectFSMBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = 6567689731496787661L;

    private List<String> listAgentNames;
    private HashMap<String, HashMap<String, Boolean>> dictVoisinsMessages;
    //private MapRepresentation myMap;
    private FullMapRepresentation myFullMap;
    private HashMap<String, List<Couple<Observation,Integer>>> dictBackpack;
    //private HashMap<Observation, List<String, Integer>> dictRepartition;

    private int exitValue;

    public StateCollectFSMBehaviour(final AbstractDedaleAgent myagent) {
        super(myagent);
    }

    public void action() {
        String myName = this.myAgent.getLocalName();
        System.out.println("\n--START state E (StateCollectFSMBehaviour): " + myName + " --");

        // update information
        this.myFullMap = ((FSMAgent) this.myAgent).getMyFullMap();
        this.dictVoisinsMessages = ((FSMAgent) this.myAgent).getDictVoisinsMessages();
        this.listAgentNames = ((FSMAgent) this.myAgent).getListAgentNames();

        if(this.dictBackpack==null){
            ((FSMAgent) this.myAgent).initDictBackpack();
        }
        this.dictBackpack = ((FSMAgent) this.myAgent).getDictBackpack();


        String myPosition = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition();
        System.out.println(myName + " [STATE E] -- myCurrentPosition is: " + myPosition);

        HashMap<String, Couple<Integer, String>> goldDict = this.myFullMap.getGoldDict(); // HashMap<NomAgent, Couple<int, time>>
        HashMap<String, Couple<Integer, String>> diamondDict = this.myFullMap.getDiamondDict();
        System.out.println(myName + " [STATE E] -- goldDict: " + goldDict + " | diamondDict: " + diamondDict);


        /*
            Soit N = Nd + Ng où Nd = nb agent qui va récolter de diamond et Ng = nb agent qui va récolter de gold
            Soit R = Rd + Rg où Rd = nb point récolte de diamond         et Rg = nb point récolte de gold

            on a donc le ratio Rd/100R
            donc on peut avoir Nd = N*Rd/100R et Ng = N-Nd
         */

        int nbAgents = this.listAgentNames.size();
        int nbPointGold = goldDict.size();
        int nbPointDiamond = diamondDict.size();
        int nbPointRessources = nbPointGold + nbPointDiamond;
        int nbAgentsGold = 0;
        int nbAgentsDiamond = 0;
        if (nbPointDiamond > nbPointGold){
            nbAgentsDiamond = nbAgents * nbPointDiamond / 100*nbPointRessources;
            nbAgentsGold = nbAgents - nbAgentsDiamond;
        }else{
            nbAgentsGold = nbAgents * nbPointGold / 100*nbPointRessources;
            nbAgentsDiamond = nbAgents - nbAgentsGold;
        }

        System.out.println("[STATE E] ------ NB AGENT : " + nbAgents + "--------------- NB AGENT GOLD : " + nbAgentsGold + "----- NB AGENT DIAMOND : " + nbAgentsDiamond);
        System.out.println("[STATE E] -- NB RESSOURCE : " + nbPointRessources + "-- NB RESSOURCE GOLD : " + nbPointGold + "-- NB RESSOURCE DIAMOND : " + nbPointDiamond);

        List<Couple<Observation, Integer>> listCapacity = ((FSMAgent) this.myAgent).getBackPackFreeSpace();


        // ACTION : Envoie dictBackpack
        ACLMessage msgSend = new ACLMessage(ACLMessage.INFORM);
        msgSend.setProtocol("SHARE-MIN");
        msgSend.setSender(this.myAgent.getAID()); // on met un expéditeur au message

        //String jsonText = JSONValue.toJSONString(this.dictBackpack);
        //System.out.print("[STATE E] ------ NB AGENT : " + jsonText);
        //msgSend.setContent(jsonText);

        for (String receiverAgent : this.listAgentNames) { // on récupère le nom d'un agent
            msgSend.addReceiver(new AID(receiverAgent, false));
        }
        ((AbstractDedaleAgent) this.myAgent).sendMessage(msgSend);
        System.out.println(myName + " [STATE E] finished sending ACK");


        // ACTION : Check si reçu dictBackpack
        MessageTemplate msg = MessageTemplate.and(
                MessageTemplate.MatchProtocol("SHARE-MIN"),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));

        ACLMessage msgReceived = this.myAgent.receive(msg);
        if (msgReceived != null && exitValue == -1) {
            System.out.println(myName + " [STATE E] received MAP");
            String nameExpediteur = msgReceived.getSender().getLocalName();
            String informationJSON = (String) msgReceived.getContent();

            //update DictBackpack
            //String[] info = informationJSON.split(",<>[]");
            //ArrayList<Couple<Observation, Integer>> list = new ArrayList();
            //list.add(new Couple( info[0], info[1]);
            //list.add(new Couple( info[2], info[3]);
            //((FSMAgent) this.myAgent).setDictBackpackAgent(nameExpediteur, list);

            //Object obj = JSONValue.parse(informationJSON);
            //JSONObject jsonObject = (JSONObject) obj;
            //((FSMAgent) this.myAgent).updateDictBackPath(jsonObject);
        }


        if (myPosition != null) {
            // List of observable from the agent's current position
            List<Couple<String, List<Couple<Observation, Integer>>>> lobs = ((AbstractDedaleAgent) this.myAgent).observe(); // myPosition
            System.out.println(myName + " [STATE E] -- list of observables: " + lobs);

            // list of observations associated to the currentPosition
            List<Couple<Observation, Integer>> lObservations = lobs.get(0).getRight();
            System.out.println(myName + " [STATE E] -- lObservations - " + lObservations);

            // example related to the use of the backpack for the treasure hunt
            boolean b = false;

            for (Couple<Observation, Integer> o : lObservations) {
                switch (o.getLeft()) {
                    case DIAMOND:
                    case GOLD:
                        System.out.println(this.myAgent.getLocalName() + " - My treasure type is : " + ((AbstractDedaleAgent) this.myAgent).getMyTreasureType());
                        System.out.println(this.myAgent.getLocalName() + " - My current backpack capacity is:" + ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
                        System.out.println(this.myAgent.getLocalName() + " - Value of the treasure on the current position: " + o.getLeft() + ": " + o.getRight());
                        System.out.println(this.myAgent.getLocalName() + " - The agent grabbed :" + ((AbstractDedaleAgent) this.myAgent).pick());
                        System.out.println(this.myAgent.getLocalName() + " - the remaining backpack capacity is: " + ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
                        b = true;
                        break;
                    default:
                        break;
                }
            }

            // If the agent picked (part of) the treasure
            if (b) {
                List<Couple<String, List<Couple<Observation, Integer>>>> lobs2 = ((AbstractDedaleAgent) this.myAgent).observe(); // myPosition
                System.out.println("STATE E : " + myName + " - State of the observations after trying to pick something " + lobs2);
            }

            // Random move from the current position
            Random r = new Random();
            int moveId = 1 + r.nextInt(lobs.size() - 1); // removing the current position from the list of target, not necessary as to stay is an action but allow quicker random move

            // The move action (if any) should be the last action of your behaviour
            String nextNode = lobs.get(moveId).getLeft();
            System.out.println("STATE E : " + myName + " will move to " + nextNode);
            ((AbstractDedaleAgent) this.myAgent).moveTo(nextNode);
        }
    }

    public int onEnd() {
        return exitValue;
    }
}
