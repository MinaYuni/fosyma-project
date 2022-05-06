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
import org.glassfish.pfl.basic.fsm.FSM;

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

        int nbAgents = this.listAgentNames.size()+1; //car listAgentName ne se compte pas lui-meme
        int nbPointGold = goldDict.size();
        int nbPointDiamond = diamondDict.size();
        int nbPointRessources = nbPointGold + nbPointDiamond;
        int nbAgentsGold = 0;
        int nbAgentsDiamond = 0;

        /*
        if (nbPointDiamond > nbPointGold){
            nbAgentsGold = (int) nbAgents * nbPointGold / 100*nbPointRessources;
            nbAgentsDiamond = nbAgents - nbAgentsGold;
            if(nbAgentsDiamond < 1 && nbPointDiamond > 0){ //on veut au moins 1 agent aille chercher diamond (et non 0 agent)
                nbAgentsDiamond = 1;
                nbAgentsGold = nbAgents - nbAgentsDiamond;
            }
        }else{
            nbAgentsDiamond = (int) nbAgents * nbPointDiamond / 100*nbPointRessources;
            nbAgentsGold = nbAgents - nbAgentsDiamond;
            if(nbAgentsGold < 1 && nbAgentsGold > 0){   //on veut au moins 1 agent aille chercher gold (et non 0 agent)
                nbAgentsGold = 1;
                nbAgentsDiamond = nbAgents - nbAgentsGold;
            }
        }

        System.out.println("[STATE E] -- NB AGENT : " + nbAgents + " -- NB AGENT GOLD : " + nbAgentsGold + " -- NB AGENT DIAMOND : " + nbAgentsDiamond);
        System.out.println("[STATE E] -- NB RESSOURCE : " + nbPointRessources + " -- NB RESSOURCE GOLD : " + nbPointGold + " -- NB RESSOURCE DIAMOND : " + nbPointDiamond);

        List<Couple<Observation, Integer>> listCapacity = ((FSMAgent) this.myAgent).getBackPackFreeSpace();

        //choix type trésor : on coupe en partition
        if(agentID<=nbAgentsGold){
            ((FSMAgent) this.myAgent).setTypeTreasure(Observation.GOLD);
        }else{
            ((FSMAgent) this.myAgent).setTypeTreasure(Observation.DIAMOND);
        }
        System.out.println(myName +"[state E] --- "+ myName + " Type Treasure " + ((FSMAgent) this.myAgent).getTypeTreasure());

        //Suppose exploration fini

        //Trier le dico Gold
        HashMap<String, Couple<Integer, String>> dictGold = this.myFullMap.getGoldDict();
        List<String> listG = new ArrayList<String>(dictGold.keySet());
        List<Integer> listGold = new ArrayList<Integer>();
        for (String s : listG) {
            // noeud s est du format "2_1"
            String mot = s.substring(0, 1) + s.substring(2);
            int value = (int) Integer.valueOf(mot);

            // noeud s est du format "21"
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

        // répartition des noeuds ressources pour chaque agent
        if (agentID <= nbPointRessources && ((FSMAgent)this.myAgent).getNodeBut() == null) {
            // SUPPOSE QUE nbAgent <= nbRessource !!! avec ou sans Wumpus
            int nodeBut=-1;
            if (((FSMAgent)this.myAgent).getTypeTreasure()==Observation.GOLD) {
                System.out.println(myName + "[State E] -- nodeBut : " + Observation.GOLD);
                nodeBut = listGold.get(agentID - 1); //les id vont de 1 à nbAgents
            }else if( ((FSMAgent)this.myAgent).getTypeTreasure()==Observation.DIAMOND){
                System.out.println(myName + "[State E] -- nodeBut : " + Observation.DIAMOND);
                nodeBut = listDiamond.get(agentID - 1 - nbPointGold); //les id vont de 1 à nbAgents
            }
            String nodeT = String.valueOf(nodeBut);

            // noeud nodeT est du format 2_1
            nodeT = nodeT.substring(0,1)+ "_" +nodeT.substring(1);

            ((FSMAgent) this.myAgent).setNodeBut(nodeT);
            System.out.println(myName + "[State E] -- nodeBut : " + nodeT);
            List<String> path = this.myFullMap.getShortestPath(myPosition, nodeT);
            ((FSMAgent) this.myAgent).setPath(path);
        }else{
            // SUPPOSE QUE nbAgent > nbRessource
            if (agentID <= nbPointRessources && ((FSMAgent)this.myAgent).getNodeBut() == null) {   //on va faire collecte avec certains agents (pour l'instant par défaut)
                int nodeBut = listGold.get(agentID); // a verifier
                ((FSMAgent) this.myAgent).setNodeBut(String.valueOf(nodeBut));
                String nodeT = String.valueOf(nodeBut);

                // noeud nodeT est du format 2_1
                nodeT = nodeT.substring(0,1)+ "_" +nodeT.substring(1);

                List<String> path = this.myFullMap.getShortestPath(myPosition, nodeT);
                ((FSMAgent) this.myAgent).setPath(path);
            }
        }
*/
        int maxNodeParAgent = nbPointRessources/nbAgents;
        this.listAgentNames.add(myName);

        Double min_diff = Double.POSITIVE_INFINITY;
        HashMap<String, List<Couple<Observation,Integer>>>  listCapacity = ((FSMAgent) this.myAgent).getDictBackpack();
        HashMap<String, Couple<Integer, String>> dictDiamond = this.myFullMap.getDiamondDict();
        HashMap<String, Couple<Integer, String>> dictGold = this.myFullMap.getGoldDict();

        HashMap<String, Integer> listAgent = new HashMap<>() ; //list nombre de node de chaque agent (cad agent 0 va aller voir "listAgent.get(0)" noeuds
        for (int i = 0; i < listAgentNames.size(); i++){
                listAgent.put(listAgentNames.get(i), 0);
        }

        if(nbPointRessources >= nbAgents) {
            //Répartition des golds
            for (String nodeGold : dictGold.keySet()) {
                Couple<Integer, String> quantiteGold = dictGold.get(nodeGold);
                String nameAgent = "";
                Double tmp = min_diff ;
                for (String s : listAgentNames) {
                    System.out.println("==== "+ listCapacity );
                    Integer capaciteGold = ((FSMAgent) this.myAgent).getDictBackPackObservationInteger(s , Observation.GOLD);
                    tmp = Double.valueOf(capaciteGold - quantiteGold.getLeft());
                    if ((tmp > 0) && tmp < min_diff && listAgent.get(s) <= maxNodeParAgent) {
                        min_diff = tmp;
                        nameAgent = s;
                    }
                }
                //MAJ
                listAgent.put(nameAgent, listAgent.get(nameAgent) + 1);

                //Ajout du noeud dans la listNodeRessource
                if (myName.equals(nameAgent)) {
                    ((FSMAgent) this.myAgent).addListNodeRessource(maxNodeParAgent, nodeGold);
                }
            }
            //Répartition des diamonds
            for (String nodeDiamond : dictDiamond.keySet()) {
                Couple<Integer, String> quantiteDiamond = dictDiamond .get(nodeDiamond);
                String nameAgent = "";
                Double tmp = min_diff ;
                for (String s : listAgentNames) {
                    Integer capaciteDiamond = ((FSMAgent) this.myAgent).getDictBackPackObservationInteger(s , Observation.GOLD);
                    tmp = Double.valueOf(capaciteDiamond - quantiteDiamond.getLeft());
                    if (tmp > 0 && tmp < min_diff && listAgent.get(s) <= maxNodeParAgent) {
                        min_diff = tmp;

                        nameAgent = s;
                    }
                }
                //MAJ
                listAgent.put(nameAgent, listAgent.get(nameAgent) + 1);

                //Ajout du noeud dans la listNodeRessource
                if (myName.equals(nameAgent)) {
                    ((FSMAgent) this.myAgent).addListNodeRessource(maxNodeParAgent, nodeDiamond);
                }
            }
        }else{
            //nbPointRessources < nbAgents
            Double max_diff = Double.NEGATIVE_INFINITY;
            for (String nodeGold : dictGold.keySet()) {
                Couple<Integer, String> quantiteGold = dictGold.get(nodeGold);
                int indexAgent = 0;
                Double tmp = max_diff ;
                Integer sumCapacityGold = 0;
                String nameAgent = "";
                for (String s : listAgentNames) {
                    Integer capaciteGold = ((FSMAgent) this.myAgent).getDictBackPackObservationInteger(s, Observation.GOLD);

                    if(sumCapacityGold + capaciteGold <= quantiteGold.getLeft()) {
                        sumCapacityGold = sumCapacityGold + capaciteGold;
                        nameAgent = s;
                    }
                }
                //MAJ
                listAgent.put(nameAgent, listAgent.get(nameAgent) + 1);

                //Ajout du noeud dans la listNodeRessource
                if (myName.equals(listAgentNames.get(indexAgent))) {
                    ((FSMAgent) this.myAgent).addListNodeRessource(maxNodeParAgent, nodeGold);
                }
            }
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
                        System.out.println(this.myAgent.getLocalName() + " - My treasure type is : " + ((AbstractDedaleAgent) this.myAgent).getMyTreasureType());
                        System.out.println(this.myAgent.getLocalName() + " - My current backpack capacity is:" + ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
                        System.out.println(this.myAgent.getLocalName() + " - Value of the treasure on the current position: " + o.getLeft() + ": " + o.getRight());

                        if (myPosition == ((FSMAgent) this.myAgent).getNodeBut()) {
                            int k = ((FSMAgent) this.myAgent).pick(); //vérifie le typeTreasure
                            System.out.println(this.myAgent.getLocalName() + " - The agent grabbed : " + k);
                            ((FSMAgent) this.myAgent).updateQuantite(k);
                        }
                        //System.out.println(this.myAgent.getLocalName() + " - The agent grabbed :" + k);
                        System.out.println(this.myAgent.getLocalName() + " - the remaining backpack capacity is: " + ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
                        b = true;
                        break;
                    case GOLD:
                        System.out.println(this.myAgent.getLocalName() + " - My treasure type is : " + ((AbstractDedaleAgent) this.myAgent).getMyTreasureType());
                        System.out.println(this.myAgent.getLocalName() + " - My current backpack capacity is:" + ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
                        System.out.println(this.myAgent.getLocalName() + " - Value of the treasure on the current position: " + o.getLeft() + ": " + o.getRight());
                        //Collecte Gold/Diamond
                        if (myPosition == ((FSMAgent) this.myAgent).getNodeBut()) {
                            int k = ((FSMAgent) this.myAgent).pick(); //vérifie le typeTreasure
                            System.out.println(this.myAgent.getLocalName() + " - The agent grabbed : " + k);
                            ((FSMAgent) this.myAgent).updateQuantite(k);
                        }
                        //System.out.println(this.myAgent.getLocalName() + " - The agent grabbed :" + k);
                        System.out.println(this.myAgent.getLocalName() + " - the remaining backpack capacity is: " + ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
                        b = true;
                        break;
                    default:
                        break;
                }
            }

            // ACTION : Choisir le prochain noeud
            String nextNode = null;
            String nodeBut;
            // If the agent picked (part of) the treasure
            if (b) {
                List<Couple<String, List<Couple<Observation, Integer>>>> lobs2 = ((AbstractDedaleAgent) this.myAgent).observe(); // myPosition
                System.out.println("STATE E : " + myName + " - State of the observations after trying to pick something " + lobs2);


                // ACTION : Choisir Prochain node de ressource
                //choix nodeBut
                nodeBut = (((FSMAgent)this.myAgent).getListNodeRessource()).get( (((FSMAgent)this.myAgent).getNbPointRecolte()) );
                ((FSMAgent)this.myAgent).setNodeBut(nodeBut);
                System.out.println(myName + "[State E] -- nodeBut : " + nodeBut);

                //retrouve le chemin pour aller au nodeBut
                List<String> path = this.myFullMap.getShortestPath(myPosition, nodeBut);
                ((FSMAgent) this.myAgent).setPath(path);
                nextNode = ((FSMAgent) this.myAgent).getNextNode();


            }else {
                // on n'a PAS trouvé un noeud point de ressource
                // Random move from the current position
                Random r = new Random();
                int moveId = 1 + r.nextInt(lobs.size() - 1); // removing the current position from the list of target, not necessary as to stay is an action but allow quicker random move
                nextNode = lobs.get(moveId).getLeft();
            }


            // ACTION : Envoie NEXT NODE pour interblocage
            if (((FSMAgent) this.myAgent).getNextNode() != null) {
                ACLMessage msgSend = new ACLMessage(ACLMessage.INFORM);
                msgSend.setProtocol("NEXT-NODE");
                msgSend.setSender(this.myAgent.getAID()); // on met un expéditeur au message

                msgSend.setContent(((FSMAgent) this.myAgent).getId()+"/"+((FSMAgent) this.myAgent).getNextNode());

                for (String receiverAgent : this.listAgentNames) { // on récupère le nom d'un agent
                    if(receiverAgent!=myName) {
                        msgSend.addReceiver(new AID(receiverAgent, false));
                    }
                }
                ((AbstractDedaleAgent) this.myAgent).sendMessage(msgSend);
                System.out.println(myName + " [STATE E] finished sending NEXT-NODE");
            }

            // ACTION : Check si reçu NEXT NODE pour interblocage
            MessageTemplate msg = MessageTemplate.and(
                    MessageTemplate.MatchProtocol("NEXT-NODE"),
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM));

            ACLMessage msgReceived = this.myAgent.receive(msg);
            String msgExpediteur = "";
            String nextNodeExpediteur = "";
            String nameExpediteur = "";
            int idExpediteur = -1;
            if (msgReceived != null && exitValue == -1) {
                System.out.println(myName + " [STATE E] received NEXT-NODE");
                nameExpediteur = msgReceived.getSender().getLocalName();
                msgExpediteur = (String) msgReceived.getContent();
                String[] l = msgExpediteur.split("/");
                idExpediteur = Integer.valueOf(l[0]);
                nextNodeExpediteur = l[1];
            }

            System.out.println( myName + "[State E] : " + myName + " will move to " + nextNode);
            if(nextNode.equals(nextNodeExpediteur) && ((FSMAgent) this.myAgent).getId() > idExpediteur ){ //un des deux vont changer de nextNode
                System.out.println( "[State E] : " + myName + " -- INTERBLOCAGE -- with : " + nameExpediteur );
                // agent est plus grand et meme noeud next qui va laisser
                if(((FSMAgent) this.myAgent).getNodeBut() != null) {
                    //agent attend quelque seconde avant de repartir
                    this.myAgent.doWait(10);
                    System.out.println( myName + "[State E] : " + myName + " wait 10 seconde");
                }else {
                    //continue à marcher de façon random
                    Random r = new Random();
                    int moveId = 1 + r.nextInt(lobs.size() - 1); // removing the current position from the list of target, not necessary as to stay is an action but allow quicker random move
                    nextNode = lobs.get(moveId).getLeft();
                    System.out.println( myName + "[State E] : " + myName + " is interblocking, change node : " + nextNode);
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
