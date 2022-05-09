package eu.su.mas.dedaleEtu.mas.behaviours.fsm;

//import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;

import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.fsm.FSMAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.FullMapRepresentation;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
//import jade.lang.acl.UnreadableException;
//import java.lang.Object;
//import java.lang.reflect.Array;

//import org.glassfish.pfl.basic.fsm.FSM;

import java.util.*;
//import java.util.stream.Collector;
//import java.util.stream.Collectors;


public class StateCollectFSMBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = 6567689731496787661L;

    private List<String> listAgentNames;
    //private HashMap<String, HashMap<String, Boolean>> dictVoisinsMessages;
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
        this.listAgentNames = ((FSMAgent) this.myAgent).getListAgentNames();


        if (((FSMAgent) this.myAgent).getDictBackpack() == null) {
            ((FSMAgent) this.myAgent).initDictBackpack();
            System.out.println("==== dictBackpack is NOT null :" + this.dictBackpack);
        }
        this.dictBackpack = ((FSMAgent) this.myAgent).getDictBackpack();
        System.out.println(myName + " [STATE E] -- this.dictBackpack : " + this.dictBackpack);

        String myPosition = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition();
        System.out.println(myName + " [STATE E] -- myCurrentPosition is: " + myPosition);

        HashMap<String, Couple<Integer, String>> goldDict = this.myFullMap.getGoldDict(); // HashMap<NomAgent, Couple<int, time>>
        HashMap<String, Couple<Integer, String>> diamondDict = this.myFullMap.getDiamondDict();
        System.out.println(myName + " [STATE E] -- goldDict: " + goldDict + " | diamondDict: " + diamondDict);

        List<String> pathDebut = ((FSMAgent) this.myAgent).getPath();
        System.out.println(myName + " [STATE E] -- pathDebut: " + pathDebut);

/*
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
        if (((FSMAgent) this.myAgent).getListNodeRessource() == null) {//on va repartir les points de ressources
            ((FSMAgent) this.myAgent).setListNodeRessource(new ArrayList<>());
            int nbAgents = this.listAgentNames.size();
            int nbPointGold = goldDict.size();
            int nbPointDiamond = diamondDict.size();
            int nbPointRessources = nbPointGold + nbPointDiamond;
            int nbAgentsGold ;
            int nbAgentsDiamond ;

            /*
                Soit N = Nd + Ng où Nd = nb agent qui va récolter de diamond et Ng = nb agent qui va récolter de gold
                Soit R = Rd + Rg où Rd = nb point récolte de diamond et Rg = nb point récolte de gold

                on a donc le ratio Rd/R
                donc on peut avoir Nd = N*Rd/R et Ng = N-Nd
             */

            if (nbPointDiamond < nbPointGold) {
                nbAgentsGold = nbAgents * nbPointGold / nbPointRessources;
                nbAgentsDiamond = nbAgents - nbAgentsGold;
            } else {
                nbAgentsDiamond = nbAgents * nbPointDiamond / nbPointRessources;
                nbAgentsGold = nbAgents - nbAgentsDiamond;
            }

            System.out.println("[STATE E] -- NB AGENT : " + nbAgents + " -- NB AGENT GOLD : " + nbAgentsGold + " -- NB AGENT DIAMOND : " + nbAgentsDiamond);
            System.out.println("[STATE E] -- NB RESSOURCE : " + nbPointRessources + " -- NB RESSOURCE GOLD : " + nbPointGold + " -- NB RESSOURCE DIAMOND : " + nbPointDiamond);

            //int maxNodeParAgent = (nbPointRessources / nbAgents); //nb de ressource par agent
            int maxNodeParAgent = (Integer.max(nbPointDiamond, nbPointGold)) / nbAgents; //nb de ressource par agent ==> permet équiter entre agent si tous les agents prennetn le meme type de ressources

            HashMap<String, Couple<Integer, String>> dictDiamond = this.myFullMap.getDiamondDict();
            HashMap<String, Couple<Integer, String>> dictGold = this.myFullMap.getGoldDict();

            HashMap<String, Integer> listRepartitionNodeCoop = new HashMap<>(); //list nombre de node de chaque agent (cad agent 0 va aller voir "listAgent.get(0)" noeuds
            for (String agent : listAgentNames) {
                listRepartitionNodeCoop.put(agent, 0);
            }
            if (nbPointRessources >= nbAgents) {
                // Par defaut, on commence par repartir les golds
                Double tmp;
                Double minDiff;
                //Répartition des golds
                for (String nodeGold : dictGold.keySet()) {
                    Couple<Integer, String> quantiteGold = dictGold.get(nodeGold);
                    minDiff = Double.POSITIVE_INFINITY;
                    String nameAgent_min = "";
                    for (String agent : listAgentNames) {
                        Integer capaciteGold = ((FSMAgent) this.myAgent).getDictBackPackObservationInteger(agent, Observation.GOLD);
                        tmp = Double.valueOf(capaciteGold - quantiteGold.getLeft());
                        if ((0 < tmp) && (tmp < minDiff) && (listRepartitionNodeCoop.get(agent) <= maxNodeParAgent)) {
                            // tmp est minimisé et en plus l'agent n'a pas atteint un nombre maxNodeParAgent de noeud à récuperer
                            if (((FSMAgent) this.myAgent).getTypeTreasure() == null || ((FSMAgent) this.myAgent).getTypeTreasure() == Observation.GOLD) {
                                minDiff = tmp;
                                nameAgent_min = agent;
                            }
                        }
                    }
                    // à la fin de la boucle : l'agent nameAgent_min qui va récupurer une quantité minDiff de ressource Gold du noeud nodeGold
                    if (!nameAgent_min.equals("")) {
                        // MAJ listRepartitionNodeCoop
                        // on a trouvé l'agent nameAgent_min qui va aller collecter le noeud nodeGold
                        listRepartitionNodeCoop.put(nameAgent_min, listRepartitionNodeCoop.get(nameAgent_min) + 1);
                    }

                    //Ajout du noeud dans la listNodeRessource
                    if (myName.equals(nameAgent_min) && listRepartitionNodeCoop.get(myName) <= maxNodeParAgent) {
                        ((FSMAgent) this.myAgent).addListNodeRessource(nodeGold);
                        //((FSMAgent) this.myAgent).setTypeTreasure(Observation.GOLD);
                    }
                }
                //Répartition des diamonds
                for (String nodeDiamond : dictDiamond.keySet()) {
                    Couple<Integer, String> quantiteDiamond = dictDiamond.get(nodeDiamond);
                    minDiff = Double.POSITIVE_INFINITY;
                    String nameAgent_min = "";
                    for (String agent : listAgentNames) {
                        Integer capaciteDiamond = ((FSMAgent) this.myAgent).getDictBackPackObservationInteger(agent, Observation.DIAMOND);
                        tmp = Double.valueOf(capaciteDiamond - quantiteDiamond.getLeft());
                        if ((0 < tmp) && (tmp < minDiff) && (listRepartitionNodeCoop.get(agent) <= maxNodeParAgent)) {
                            // tmp est minimisé et en plus l'agent n'a pas atteint un nombre maxNodeParAgent de noeud à récuperer
                            if (((FSMAgent) this.myAgent).getTypeTreasure() == null || ((FSMAgent) this.myAgent).getTypeTreasure() == Observation.DIAMOND) {
                                minDiff = tmp;
                                nameAgent_min = agent;
                            }
                        }
                    }
                    // à la fin de la boucle : l'agent nameAgent_min qui va récupurer une quantité minDiff de ressource Gold du noeud nodeGold
                    if (!nameAgent_min.equals("")) {
                        // MAJ listRepartitionNodeCoop
                        // on a trouvé l'agent nameAgent_min qui va aller collecter le noeud nodeGold
                        listRepartitionNodeCoop.put(nameAgent_min, listRepartitionNodeCoop.get(nameAgent_min) + 1);
                    }

                    //Ajout du noeud dans la listNodeRessource
                    if (myName.equals(nameAgent_min) && listRepartitionNodeCoop.get(myName) <= maxNodeParAgent) {
                        ((FSMAgent) this.myAgent).addListNodeRessource(nodeDiamond);
                        //((FSMAgent) this.myAgent).setTypeTreasure(Observation.DIAMOND);
                    }
                }
            } else {
                //nbPointRessources < nbAgents
                Double tmp;
                Double sum;
                // Par defaut, on commence par repartir les golds
                //Répartition des golds
                for (String nodeGold : dictGold.keySet()) {
                    Couple<Integer, String> quantiteGold = dictGold.get(nodeGold);
                    sum = 0.0;
                    for (String agent : listAgentNames) {
                        Integer capaciteGold = ((FSMAgent) this.myAgent).getDictBackPackObservationInteger(agent, Observation.GOLD);
                        tmp = Double.valueOf(capaciteGold - quantiteGold.getLeft());
                        //max par glouton
                        if ((0 < tmp) && (sum + tmp < quantiteGold.getLeft()) && (listRepartitionNodeCoop.get(agent) <= maxNodeParAgent)) {
                            if (((FSMAgent) this.myAgent).getTypeTreasure() == null || ((FSMAgent) this.myAgent).getTypeTreasure() == Observation.GOLD) {
                                sum = sum + tmp;
                                listRepartitionNodeCoop.put(agent, listRepartitionNodeCoop.get(agent) + 1);
                                //Ajout du noeud dans la listNodeRessource
                                if (myName.equals(agent)) {
                                    ((FSMAgent) this.myAgent).addListNodeRessource(nodeGold);
                                    ((FSMAgent) this.myAgent).setTypeTreasure(Observation.GOLD);
                                }
                            }
                        }
                    }
                }
                //Répartition des diamonds
                for (String nodeDiamond : dictDiamond.keySet()) {
                    Couple<Integer, String> quantiteDiamond = dictGold.get(nodeDiamond);
                    sum = 0.0;
                    for (String agent : listAgentNames) {
                        Integer capaciteDiamond = ((FSMAgent) this.myAgent).getDictBackPackObservationInteger(agent, Observation.DIAMOND);
                        tmp = Double.valueOf(capaciteDiamond - quantiteDiamond.getLeft());
                        //max par glouton
                        if ((0 < tmp) && (sum + tmp < quantiteDiamond.getLeft()) && (listRepartitionNodeCoop.get(agent) <= maxNodeParAgent)) {
                            if (((FSMAgent) this.myAgent).getTypeTreasure() == null || ((FSMAgent) this.myAgent).getTypeTreasure() == Observation.DIAMOND) {
                                sum = sum + tmp;
                                listRepartitionNodeCoop.put(agent, listRepartitionNodeCoop.get(agent) + 1);
                                //Ajout du noeud dans la listNodeRessource
                                if (myName.equals(agent)) {
                                    ((FSMAgent) this.myAgent).addListNodeRessource(nodeDiamond);
                                    ((FSMAgent) this.myAgent).setTypeTreasure(Observation.DIAMOND);
                                }
                            }
                        }
                    }
                }
            }
        }

        try {
            this.myAgent.doWait(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        exitValue = -1;
        if (myPosition != null) {
            if (((FSMAgent) this.myAgent).getInterblocage()) {
                System.out.println(myName + " [STATE E] -- INTERBLOCAGE : " + ((FSMAgent) this.myAgent).getInterblocage());

                List<Couple<String, List<Couple<Observation, Integer>>>> lobs = ((AbstractDedaleAgent) this.myAgent).observe(); // myPosition
                Random r = new Random();
                int moveId = 1 + r.nextInt(lobs.size() - 1);
                String nodeIntermediaire = lobs.get(moveId).getLeft();

                if (nodeIntermediaire != null) {
                    if (((AbstractDedaleAgent) this.myAgent).moveTo(nodeIntermediaire)==true) {
                        System.out.println(myName + " [State E] : -- myPosition : " + myPosition +" -- move to node " + nodeIntermediaire);

                        this.myAgent.doWait(10);
                        ((FSMAgent) this.myAgent).setInterblocage(false);
                    }
                }
            } else {
                // List of observable from the agent's current position
                List<Couple<String, List<Couple<Observation, Integer>>>> lobs = ((AbstractDedaleAgent) this.myAgent).observe(); // myPosition
                System.out.println(myName + " [STATE E] -- list of observables: " + lobs);

                // list of observations associated to the currentPosition
                List<Couple<Observation, Integer>> lObservations = lobs.get(0).getRight();
                System.out.println(myName + " [STATE E] -- lObservations - " + lObservations);

                List<Couple<Observation, Integer>> listCapacity = ((FSMAgent) this.myAgent).getBackPackFreeSpace();
                for(Couple<Observation, Integer> capacity : listCapacity){
                    if(((FSMAgent) this.myAgent).getTypeTreasure()==capacity.getLeft()){
                        if (capacity.getRight() <= 0){
                            exitValue = 2; //il n'y a plus de place donc go to state G (random walk)
                        }
                    }
                }
                // example related to the use of the backpack for the treasure hunt
                boolean reussiCollecte = false;
                // myPosition == lobs.get(0).getLeft()
                if (lObservations.isEmpty()){
                    // il y n'a pas de ressource dans ce noeud
                    if(((AbstractDedaleAgent) this.myAgent).getCurrentPosition().equals(((FSMAgent) this.myAgent).getNodeBut())) {
                        //un autre agent a récuperer la ressource, alors on passe au suivant
                        int index = ((FSMAgent) this.myAgent).getNbPointRecolte();
                        System.out.println(myName + " [State E] -- AVANT Changement Index -- lObservations : " + lObservations
                                + " -- NodeBut : " + ((FSMAgent) this.myAgent).getNodeBut()
                                + " -- Position : " + ((FSMAgent) this.myAgent).getCurrentPosition()
                                + " -- index : " + index );

                        index++;
                        ((FSMAgent) this.myAgent).setNbPointRecolte(index);
                    }
                }else{
                    // sinon on a qqch à récuperer

                    // agent fait la collecte sur le noeud actuel
                    for (Couple<Observation, Integer> o : lObservations) {
                        switch (o.getLeft()) {
                            case DIAMOND:
                                System.out.println(this.myAgent.getLocalName() + " - My position is : " + ((FSMAgent) this.myAgent).getCurrentPosition() );
                                System.out.println(this.myAgent.getLocalName() + " - MyPosition : " + myPosition );
                                System.out.println(this.myAgent.getLocalName() + " - My treasure type is : " + ((FSMAgent) this.myAgent).getMyTreasureType());
                                System.out.println(this.myAgent.getLocalName() + " - My current backpack capacity is:" + ((FSMAgent) this.myAgent).getBackPackFreeSpace());
                                System.out.println(this.myAgent.getLocalName() + " - Value of the treasure on the current position: " + o.getLeft() + ": " + o.getRight());

                                if (((FSMAgent) this.myAgent).getCurrentPosition().equals(((FSMAgent) this.myAgent).getNodeBut()) ){ //&& ((FSMAgent) this.myAgent).verifyTypeTreasure(Observation.DIAMOND)) {
                                    int k = ((FSMAgent) this.myAgent).pick(Observation.DIAMOND); //vérifie le typeTreasure
                                    System.out.println(this.myAgent.getLocalName() + " - The agent grabbed : " + k);
                                    //((FSMAgent) this.myAgent).setDictBackpackAgent(this.myAgent.getLocalName(), ((FSMAgent) this.myAgent).getBackPackFreeSpace());
                                    reussiCollecte = true;
                                    break;
                                }
                                //System.out.println(this.myAgent.getLocalName() + " - The agent grabbed :" + k);
                                //System.out.println(this.myAgent.getLocalName() + " - the remaining backpack capacity is: " + ((FSMAgent) this.myAgent).getBackPackFreeSpace());

                            case GOLD:
                                System.out.println(this.myAgent.getLocalName() + " - My position is : " + ((FSMAgent) this.myAgent).getCurrentPosition() );
                                System.out.println(this.myAgent.getLocalName() + " - MyPosition : " + myPosition );
                                System.out.println(this.myAgent.getLocalName() + " - My treasure type is : " + ((FSMAgent) this.myAgent).getMyTreasureType());
                                System.out.println(this.myAgent.getLocalName() + " - My current backpack capacity is:" + ((FSMAgent) this.myAgent).getBackPackFreeSpace());
                                System.out.println(this.myAgent.getLocalName() + " - Value of the treasure on the current position: " + o.getLeft() + ": " + o.getRight());
                                //Collecte Gold/Diamond
                                if (((FSMAgent) this.myAgent).getCurrentPosition().equals(((FSMAgent) this.myAgent).getNodeBut()) ){ //&& ((FSMAgent) this.myAgent).verifyTypeTreasure(Observation.GOLD)) {
                                    int k = ((FSMAgent) this.myAgent).pick(Observation.GOLD); //vérifie le typeTreasure
                                    System.out.println(this.myAgent.getLocalName() + " - The agent grabbed : " + k);
                                    //((FSMAgent) this.myAgent).setDictBackpackAgent(this.myAgent.getLocalName(), ((FSMAgent) this.myAgent).getBackPackFreeSpace());
                                    reussiCollecte = true;
                                    break;
                                }
                                //System.out.println(this.myAgent.getLocalName() + " - The agent grabbed :" + k);
                                //System.out.println(this.myAgent.getLocalName() + " - the remaining backpack capacity is: " + ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());

                            default:
                                break;
                        }
                    }
                }

                // ACTION : Choisir le prochain noeud
                String nextNode = null;
                String nodeBut = null;

                System.out.println(myName + " [STATE E] -- listNodeARecolter : " + ((FSMAgent) this.myAgent).getListNodeRessource() );

                int indexNode = ((FSMAgent) this.myAgent).getNbPointRecolte();  //cet index est incrémenté lorsqu'on a réussi à pick()
                int nbRessourceARecolter = ((FSMAgent) this.myAgent).getListNodeRessource().size(); //((FSMAgent) this.myAgent).getListNodeRessource().size();

                if (((FSMAgent) this.myAgent).getListNodeRessource().size() <= ((FSMAgent) this.myAgent).getNbPointRecolte()){
                    // il n'y a PLUS de point de recolte
                    // agent a récupéré tous ses ressources
                    ((FSMAgent) this.myAgent).setNodeBut(null);
                    ((FSMAgent) this.myAgent).setNextNode(null);
                    exitValue = 2; //go to in stats G (random walk
                    System.out.println(myName + " -- [STATE E] -- Go to state G (random walk) ");
                }else if (reussiCollecte && exitValue == -1 ){ //reussi collecte et il reste des noeuds à collecte, donc agent passe au nodeBut suivant
                    exitValue =1;
                    System.out.println(myName + " -- [STATE E] -- Reussi collect, prochaine : "+ nodeBut);
                    nodeBut = (((FSMAgent) this.myAgent).getListNodeRessource()).get(indexNode);
                    ((FSMAgent) this.myAgent).setNodeBut(nodeBut);

                    List<String> path = this.myFullMap.getShortestPath(((FSMAgent) this.myAgent).getCurrentPosition(), nodeBut);

                    while (path.size() == 0) {
                        //path est null donc agent va aller à un nodeIntermediaire pr aller au prochain point de ressource
                        System.out.println("====[PATH NULL]======= " + myName + " [State E] : " + myName + " path : " + path);
                        Random r = new Random();
                        int moveId = 1 + r.nextInt(lobs.size() - 1);
                        String nodeIntermediaire = lobs.get(moveId).getLeft();
                        //agent se deplace a coter
                        if( ((FSMAgent) this.myAgent).moveTo(nodeIntermediaire)) {
                            System.out.println("====[PATH NULL]======= " + myName + " [State E] :  MOVE TO nodeIntermediaire : " + nodeIntermediaire);
                            path = this.myFullMap.getShortestPath(nodeIntermediaire, nodeBut);
                            myPosition = nodeIntermediaire;
                        }
                        System.out.println("====[PATH NULL]======= " + myName + " [State E] : " + myName + " path : " + path);
                    }
                    ((FSMAgent) this.myAgent).setPath(path);
                    nextNode = path.get(0);
                    ((FSMAgent) this.myAgent).setNextNode(nextNode);
                    /*
                    if(path.size()>0) { // agent a trouver un chemin et va pouvoir aller au nextNode
                        ((FSMAgent) this.myAgent).setPath(path);
                        nextNode = path.get(0);
                        ((FSMAgent) this.myAgent).setNextNode(nextNode);
                     }
                     */

                    System.out.println("== " + myName + " [State E] -- Prochaine collect -- : "+ nodeBut
                            + " -- path : " + path
                            + " -- getCurrentPosition() : " + (((FSMAgent) this.myAgent).getCurrentPosition())
                            + " -- myPosition : " + myPosition
                            + " -- nextNode : " + nextNode
                            + " -- nodeBut : " + nodeBut );

                }else if (nbRessourceARecolter > indexNode && indexNode >= 0 ){
                    // agent n'a pas encore atteint le nodeBut
                    exitValue = 1;
                    nodeBut = (((FSMAgent) this.myAgent).getListNodeRessource()).get(indexNode);
                    ((FSMAgent) this.myAgent).setNodeBut(nodeBut);
                    List<String> path = this.myFullMap.getShortestPath(myPosition, nodeBut);

                    if(path.size()>0) {
                        ((FSMAgent) this.myAgent).setPath(path);
                        nextNode = path.get(0);
                        ((FSMAgent) this.myAgent).setNextNode(nextNode);
                    }
                    System.out.println("==" + myName + " [State E] -- PAS FINI "
                            + " -- path : " + path
                            + " -- getCurrentPosition() : " + (((FSMAgent) this.myAgent).getCurrentPosition())
                            + " -- myPosition : " + myPosition
                            + " -- nextNode : " + nextNode
                            + " -- nodeBut : " + nodeBut );

                }

                if(nextNode == null){
                    lobs = ((AbstractDedaleAgent) this.myAgent).observe();
                    Random r = new Random();
                    int moveId = 1 + r.nextInt(lobs.size() - 1); // removing the current position from the list of target, not necessary as to stay is an action but allow quicker random move
                    nextNode = lobs.get(moveId).getLeft();
                    ((FSMAgent) this.myAgent).setNextNode(nextNode);
                }

                // ACTION : Envoie PING pour interblocage
                if (nextNode != null) {
                    ACLMessage msgSend = new ACLMessage(ACLMessage.INFORM);
                    msgSend.setProtocol("PING");
                    msgSend.setSender(this.myAgent.getAID()); // on met un expéditeur au message

                    //on envoie Id (pour ordre de priorité entre agent) et le nextNode
                    msgSend.setContent(((FSMAgent) this.myAgent).getId() + "/" + ((FSMAgent) this.myAgent).getNextNode());

                    for (String receiverAgent : this.listAgentNames) { // on récupère le nom d'un agent
                        if (!receiverAgent.equals(myName)) {
                            msgSend.addReceiver(new AID(receiverAgent, false));
                        }
                    }
                    ((AbstractDedaleAgent) this.myAgent).sendMessage(msgSend);
                    System.out.println(myName + " [STATE E] -- finished sending message PING for interblock");
                }

                // ACTION : Check si reçu PING pour interblocage
                MessageTemplate msg = MessageTemplate.and(
                        MessageTemplate.MatchProtocol("PING"),
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM));

                ACLMessage msgReceived = this.myAgent.receive(msg);
                String msgExpediteur = "";
                String nextNodeExpediteur = "";
                String nameExpediteur = "";
                int idExpediteur = -1;
                String[] l;
                if (msgReceived != null) {
                    System.out.println(myName + " [STATE E] received message PING for interblock");
                    nameExpediteur = msgReceived.getSender().getLocalName();
                    msgExpediteur = msgReceived.getContent();
                    l = msgExpediteur.split("/");
                    idExpediteur = Integer.valueOf(l[0]);
                    nextNodeExpediteur = l[1];
                }
                // ACTION : Detection interblocage
                if (nextNode.equals(nextNodeExpediteur) ){//&& ((FSMAgent) this.myAgent).getId() > idExpediteur) { //un des deux vont changer de nextNode
                    System.out.println("[State E] : " + myName + " -- INTERBLOCAGE -- with : " + nameExpediteur);
                    // agent est plus grand et meme noeud next qui va changer de chemin pour laisser passer

                    ((FSMAgent) this.myAgent).setInterblocage(true);

                    lobs = ((AbstractDedaleAgent) this.myAgent).observe();
                    Random r = new Random();
                    int moveId = 1 + r.nextInt(lobs.size() - 1); // removing the current position from the list of target, not necessary as to stay is an action but allow quicker random move
                    String nodeIntermediaire = null;
                    while(nodeIntermediaire==null){
                        nodeIntermediaire = lobs.get(moveId).getLeft();
                    }
                    if (((AbstractDedaleAgent) this.myAgent).moveTo(nodeIntermediaire)==true) {
                        System.out.println(myName + " [State E] : -- myPosition : " + myPosition +" -- move to nodeIntermediaire " + nodeIntermediaire);

                        //agent s'est deplacer
                        ((FSMAgent) this.myAgent).setInterblocage(false);
                        if(((FSMAgent) this.myAgent).getId() > idExpediteur) {
                            //agent va attendre pour éviter encore le meme interblocage
                            this.myAgent.doWait(10);
                        }
                        if (((FSMAgent) this.myAgent).getNodeBut() != null) {
                            //Trouver le chemin pour aller au nodeBut
                            exitValue = 1; //reste au state E (collecte)
                            if(!((FSMAgent) this.myAgent).getCurrentPosition().equals(((FSMAgent) this.myAgent).getNodeBut())) {
                                List<String> path = this.myFullMap.getShortestPath(((FSMAgent) this.myAgent).getCurrentPosition(), nodeBut);
                                ((FSMAgent) this.myAgent).setPath(path);
                                nextNode = path.get(0);
                                ((FSMAgent) this.myAgent).setNextNode(nextNode);
                            }
                        }else{ //agent n'a pas de noeud de ressource
                            exitValue = 2; //va au state G (random walk)
                            System.out.println(myName + " [State E] : -- myPosition : " + myPosition + " -- move to node " + nextNode);
                        }
                    }

                }
                if(exitValue == 1) {
                    // agent reste au State E donc nextNode est non null
                    if(((AbstractDedaleAgent) this.myAgent).moveTo(nextNode)==true){
                        System.out.println("=====BLOC==================" + myName + " [State E] : -- myPosition : " + myPosition + " -- move to node " + nextNode);
                    }else{
                        System.out.println("=====BLOC==================" +  "[State E] : " + myName + " -- INTERBLOCAGE -- with : " + nameExpediteur);
                        // agent est plus grand et meme noeud next qui va changer de chemin pour laisser passer

                        ((FSMAgent) this.myAgent).setInterblocage(true);

                        lobs = ((AbstractDedaleAgent) this.myAgent).observe();
                        Random r = new Random();
                        int moveId = 1 + r.nextInt(lobs.size() - 1); // removing the current position from the list of target, not necessary as to stay is an action but allow quicker random move
                        String nodeIntermediaire = null;
                        while(nodeIntermediaire==null){
                            nodeIntermediaire = lobs.get(moveId).getLeft();
                        }
                        if (((AbstractDedaleAgent) this.myAgent).moveTo(nodeIntermediaire)==true) {
                            System.out.println("=====BLOC==================" +  myName + " [State E] : -- myPosition : " + myPosition +" -- move to nodeIntermediaire " + nodeIntermediaire);

                            //agent s'est deplacer
                            ((FSMAgent) this.myAgent).setInterblocage(false);
                            if(((FSMAgent) this.myAgent).getId() > idExpediteur) {
                                //agent va attendre pour éviter encore le meme interblocage
                                this.myAgent.doWait(10);
                            }
                            if (((FSMAgent) this.myAgent).getNodeBut() != null) {
                                //Trouver le chemin pour aller au nodeBut
                                exitValue = 1; //reste au state E (collecte)
                                if(!((FSMAgent) this.myAgent).getCurrentPosition().equals(((FSMAgent) this.myAgent).getNodeBut())) {
                                    List<String> path = this.myFullMap.getShortestPath(((FSMAgent) this.myAgent).getCurrentPosition(), nodeBut);
                                    ((FSMAgent) this.myAgent).setPath(path);
                                    nextNode = path.get(0);
                                    ((FSMAgent) this.myAgent).setNextNode(nextNode);
                                }
                            }else{ //agent n'a pas de noeud de ressource
                                exitValue = 2; //va au state G (random walk)
                                System.out.println("=====BLOC==================" + myName + " [State E] : -- myPosition : " + myPosition + " -- move to node " + nextNode);
                            }
                        }
                    }
                }
            }
        }
        if(exitValue==-1) {
            exitValue = 1; //continue en state E
            System.out.println(myName + " [State E] (StateCollectFSMBehaviour): " + myName + " --- END ---");
        }
    }


    public int onEnd() {
        return exitValue;
    }
}
