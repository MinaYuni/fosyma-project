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
import java.util.*;

import java.lang.Object;
import java.util.stream.Collector;
import java.util.stream.Collectors;
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

        int agentID = ((FSMAgent) this.myAgent).getId();


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
        if (nbPointDiamond < nbPointGold){
            nbAgentsGold = (int) nbAgents * nbPointGold / 100*nbPointRessources;
            nbAgentsDiamond = nbAgents - nbAgentsGold;
        }else{
            nbAgentsDiamond = (int) nbAgents * nbPointDiamond / 100*nbPointRessources;
            nbAgentsGold = nbAgents - nbAgentsDiamond;
        }

        System.out.println("[STATE E] ------ NB AGENT : " + nbAgents + " ---------- NB AGENT GOLD : " + nbAgentsGold + " ----- NB AGENT DIAMOND : " + nbAgentsDiamond);
        System.out.println("[STATE E] -- NB RESSOURCE : " + nbPointRessources + " -- NB RESSOURCE GOLD : " + nbPointGold + " -- NB RESSOURCE DIAMOND : " + nbPointDiamond);

        List<Couple<Observation, Integer>> listCapacity = ((FSMAgent) this.myAgent).getBackPackFreeSpace();

/*
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
*/
        //choix type trésor : on coupe en partition
        if(agentID<=nbAgentsGold){
            ((FSMAgent) this.myAgent).setTypeTreasure(Observation.GOLD);
        }else{
            ((FSMAgent) this.myAgent).setTypeTreasure(Observation.DIAMOND);
        }
        System.out.println(myName +"[state E] --- Type Treasure " + ((FSMAgent) this.myAgent).getTypeTreasure());

        //Suppose exploration fini

        //Trier le dico Gold
        HashMap<String, Couple<Integer, String>> dictGold = this.myFullMap.getGoldDict();
        List<String> listG = new ArrayList<String>(dictGold.keySet());
        List<Integer> listGold = new ArrayList<Integer>();
        for(String s : listG){
            // noeud s est du format 2_1
            String mot = s.substring(0, 1) + s.substring(2);
            int value = (int) Integer.valueOf(mot);

            // noeud s est du format 21
            //int value = (int) Integer.valueOf(s);
            listGold.add(value);
        }
        Collections.sort(listGold); //trie la liste

        //Trier le dico diamond
        HashMap<String, Couple<Integer, String>> dictDiamond = this.myFullMap.getDiamondDict();
        List<String> listD = new ArrayList<String>(dictDiamond.keySet());
        List<Integer> listDiamond = new ArrayList<Integer>();
        for(String s : listD){
            // noeud s est du format 2_1
            String mot = s.substring(0, 1) + s.substring(2);
            int value = (int) Integer.valueOf(mot);

            // noeud s est du format 21
            //int value = (int) Integer.valueOf(s);
            listDiamond.add(value);
        }
        Collections.sort(listDiamond); //trie la liste


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
                        System.out.println(this.myAgent.getLocalName() + " - My treasure type is : " + ((AbstractDedaleAgent) this.myAgent).getMyTreasureType());
                        System.out.println(this.myAgent.getLocalName() + " - My current backpack capacity is:" + ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
                        System.out.println(this.myAgent.getLocalName() + " - Value of the treasure on the current position: " + o.getLeft() + ": " + o.getRight());
                        //System.out.println(this.myAgent.getLocalName() + " - The agent grabbed :" + k);
                        System.out.println(this.myAgent.getLocalName() + " - the remaining backpack capacity is: " + ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
                        b = true;
                        break;
                    case GOLD:
                        System.out.println(this.myAgent.getLocalName() + " - My treasure type is : " + ((AbstractDedaleAgent) this.myAgent).getMyTreasureType());
                        System.out.println(this.myAgent.getLocalName() + " - My current backpack capacity is:" + ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
                        System.out.println(this.myAgent.getLocalName() + " - Value of the treasure on the current position: " + o.getLeft() + ": " + o.getRight());
                        //System.out.println(this.myAgent.getLocalName() + " - The agent grabbed :" + k);
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

            //Collecte Gold/Diamond
            if (agentID > nbAgentsGold) {
                int k = ((FSMAgent) this.myAgent).pick();
                System.out.println(this.myAgent.getLocalName() + " - The agent grabbed : " + k);
                ((FSMAgent) this.myAgent).updateQuantite(k);

            }
            else if (agentID <= nbAgentsGold) {
                int k = ((FSMAgent) this.myAgent).pick();
                System.out.println(this.myAgent.getLocalName() + " - The agent grabbed : " + k);
                ((FSMAgent) this.myAgent).updateQuantite(k);
            }



            // Random move from the current position
            Random r = new Random();
            int moveId = 1 + r.nextInt(lobs.size() - 1); // removing the current position from the list of target, not necessary as to stay is an action but allow quicker random move

            // répartition des noeuds ressources pour chaque agent
            String nextNode = null;
            if (nbAgents <= nbPointRessources) {
                // SUPPOSE QUE nbAgent <= nbRessource !!! avec ou sans Wumpus
                int nodeBut = listGold.get(agentID-1); //les id vont de 1 à nbAgents
                String nodeT = String.valueOf(nodeBut);

                // noeud nodeT est du format 2_1
                nodeT = nodeT.substring(0,1)+ "_" +nodeT.substring(1);

                ((FSMAgent) this.myAgent).setNodeBut(nodeT);
                System.out.println(myName + "[State E] -- nodeBut : " + nodeT);
                List<String> path = this.myFullMap.getShortestPath(myPosition, nodeT);
                ((FSMAgent) this.myAgent).setPath(path);
                nextNode = path.get(0);
            }else{
                // SUPPOSE QUE nbAgent > nbRessource
                if (agentID <= nbPointRessources) {   //on va faire collecte avec certains agents (pour l'instant par défaut)
                    int nodeBut = listGold.get(agentID); // a verifier
                    ((FSMAgent) this.myAgent).setNodeBut(String.valueOf(nodeBut));
                    String nodeT = String.valueOf(nodeBut);

                    // noeud nodeT est du format 2_1
                    nodeT = nodeT.substring(0,1)+ "_" +nodeT.substring(1);

                    List<String> path = this.myFullMap.getShortestPath(myPosition, nodeT);
                    ((FSMAgent) this.myAgent).setPath(path);
                    nextNode = path.get(0);
                }else{
                    nextNode = lobs.get(moveId).getLeft();
                }
            }
            // The move action (if any) should be the last action of your behaviour
            System.out.println( myName + "[State E] : " + myName + " will move to " + nextNode);
            ((AbstractDedaleAgent) this.myAgent).moveTo(nextNode);
        }
        exitValue = 1 ; //continue en state E
        System.out.println(myName + "[State E] (StateCollectFSMBehaviour): " + myName + " --- END ---");

    }

    public int onEnd() {
        return exitValue;
    }
}
