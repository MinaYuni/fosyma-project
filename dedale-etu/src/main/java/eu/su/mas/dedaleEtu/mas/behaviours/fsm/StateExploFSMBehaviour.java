package eu.su.mas.dedaleEtu.mas.behaviours.fsm;


import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedaleEtu.mas.agents.fsm.FSMAgent;
import jade.core.behaviours.OneShotBehaviour;

import jade.core.AID;

import java.util.*;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import dataStructures.tuple.Couple;

import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.lang.acl.UnreadableException;


// Behaviour/comportement du state A (exploration)
public class StateExploFSMBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = 1567689731496787661L;
    private final List<String> list_agentNames;
    private HashMap<String, HashMap<String, Boolean>> dictVoisinsMessages;
    private MapRepresentation myMap;
    private int exitValue;

    public StateExploFSMBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap, List<String> agentNames, HashMap<String, HashMap<String, Boolean>> dico) {
        super(myagent);
        this.myMap = myMap;
        this.list_agentNames = agentNames;
        this.dictVoisinsMessages = dico;
    }

    public void action() {
        int nb_agents = this.list_agentNames.size();
        String myName = this.myAgent.getLocalName();

        System.out.println("\n-- START state A (StateExploFSMBehaviour): " + myName + " starts exploration --");

        // update information
        if (this.myMap == null) {
            this.myMap = new MapRepresentation();
        }
        this.dictVoisinsMessages = ((FSMAgent) this.myAgent).getDictVoisinsMessages();

        // 0) Retrieve the current position
        String myPosition = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition();
        //System.out.println("agent in position "+ myPosition);

        if (myPosition != null) {
            // list of observable from the agent's current position
            List<Couple<String, List<Couple<Observation, Integer>>>> lobs = ((AbstractDedaleAgent) this.myAgent).observe(); //myPosition

            try {
                this.myAgent.doWait(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 1) remove the current node from openlist and add it to closedNodes
            this.myMap.addNode(myPosition, MapAttribute.closed);

            // 2) get the surrounding nodes and, if not in closedNodes, add them to open nodes
            String nextNode = null;
            for (Couple<String, List<Couple<Observation, Integer>>> lob : lobs) {
                String nodeId = lob.getLeft();
                boolean isNewNode = this.myMap.addNewNode(nodeId);
                // the node may exist, but not necessarily the edge
                if (!myPosition.equals(nodeId)) {
                    this.myMap.addEdge(myPosition, nodeId);
                    if (nextNode == null && isNewNode) nextNode = nodeId;
                }
            }

            //ACTION : Check si l'agent a reçu une carte de ses voisins
            MessageTemplate msgMap = MessageTemplate.and(
                    MessageTemplate.MatchProtocol("FINISH-SHARE-MAP"),
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM));

            ACLMessage msgMapReceived = this.myAgent.receive(msgMap);

            if (msgMapReceived != null) {
                String nameExpediteur = msgMapReceived.getSender().getLocalName();
                System.out.println("STATE A : " + myName + " received MAP, from " + nameExpediteur);

                SerializableSimpleGraph<String, MapAttribute> mapReceived = null;
                SerializableSimpleGraph<String, MapAttribute> allInformation = null;
                try {
                    allInformation = (SerializableSimpleGraph<String, MapAttribute>) msgMapReceived.getContentObject();
                    mapReceived = allInformation; // pour l'instant, on n'a qu'une carte, mais après on pourra envoyer d'autres informations
                } catch (UnreadableException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                assert mapReceived != null;
                this.myMap.mergeMap(mapReceived);
                //this.myMap.loadSavedData() ; //car on a recu une carte complete

                /*
                //Normalement, il va dans la condition 'if' (voir ligne 111 à ligne 115 de ce fichier) pour aller au state F
                exitValue = 2; // aller en F : "Exploration fini"
                System.out.println("-CHANGE A to F (StateStopFSMBehaviour): " + myName + " goes to state F (Exploration fini) ");
                */
            }

            //3) while openNodes is not empty, continues
            if (!this.myMap.hasOpenNode()) { // si il n'y a plus de noeud ouvert => exploration finie
                exitValue = 2; // aller en F : "Exploration finie"
                System.out.println(myName + " - Exploration successfully done");
                System.out.println("- END state A (StateExploFSMBehaviour): " + myName + " finished exploring, goes to state F");
            } else {

                // 3.1) Select next move
                // there exist one open node directly reachable, go for it,
                // otherwise choose one from the openNode list, compute the shortestPath and go for it
                if (nextNode == null) { // if no directly accessible openNode
                    // chose one, compute the path and take the first step
                    nextNode = this.myMap.getShortestPathToClosestOpenNode(myPosition).get(0); //getShortestPath(myPosition,this.openNodes.get(0)).get(0);
                    System.out.println(myName + " - currentPosition: " + myPosition + " -- list= " + this.myMap.getOpenNodes() + " | nextNode: " + nextNode);
                } else {
                    System.out.println("nextNode notNUll - " + myName + " -- list= " + this.myMap.getOpenNodes() + " | nextNode: " + nextNode);
                }

                // 3.2) ACTION : envoie PING à chaque déplacement
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.setProtocol("PING");
                //msg.setContent(myName); // mettre son nom dans le ping envoyé
                msg.setSender(this.myAgent.getAID()); // mettre une expéditeur au message

                //msg.setContent( this.myMap.getNbNodes().toString() ); //mets le nombre de sommets visités

                // ajout des destinataires du ping (tous les autres agents, sauf moi_meme)
                for (String receiverAgent : this.list_agentNames) { // PROBLEME : quand un autre agent meurt => il y a une boucle infinie
                    if (!Objects.equals(myName, receiverAgent)) { // si ce n'est pas moi
                        System.out.println("STATE A : " + myName + " will send msg to " + receiverAgent);
                        msg.addReceiver(new AID(receiverAgent, false)); // on met un receveur au message
                    }
                }
                // envoie du ping à tous les agents
                ((AbstractDedaleAgent) this.myAgent).sendMessage(msg);
                System.out.println("STATE A : " + myName + " finished sending PING");


                // 3.3) At each time step, the agent check if he received a ping from a teammate
                // ACTION : Check reception PING
                MessageTemplate msgPing = MessageTemplate.and(
                        MessageTemplate.MatchProtocol("PING"),
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM));

                ACLMessage msgPingReceived = this.myAgent.receive(msgPing);
                // si reception PING, aller en B (envoyer sa carte),
                // sinon continuer déplacement
                if (msgPingReceived != null) { // réception PING, donc un autre agent est à proximité, donc MàJ dict_voisins de l'agent
                    System.out.println("STATE A : " + myName + " received PING");

                    // ajouter le voisin au dico (voir type de list_voisin dans FSMAgent.java)
                    String namePingReceived = msgPingReceived.getSender().getLocalName(); // récupérer le nom du voisin (nom donnée dans le message du ping reçu)

                    // si l'agent n'a pas encore rencontré l'envoyeur du ping, il est ajouté dans le dictionnaire (dict_voisins)
                    if (!this.dictVoisinsMessages.containsKey(namePingReceived)) { //
                        // état de l'agent par rapport à l'envoyeur du ping
                        HashMap<String, Boolean> etat = new HashMap<String, Boolean>();

                        //etat.put("nbNodes", this.myMap.getNbNodes().toString());

                        // ajout de l'envoyeur et son état dans le dico des voisins
                        this.dictVoisinsMessages.put(namePingReceived, etat);
                        // on a modifié le dico dictVoisinsMessages => utiliser la methode 'setDictVoisinsMessages' pour udapte !
                        ((FSMAgent) this.myAgent).setDictVoisinsMessages(this.dictVoisinsMessages);
                    }

                    // MAJ MAP
                    ((FSMAgent) this.myAgent).setMyMap(this.myMap);

                    exitValue = 1; // aller en B : "Envoie carte"
                    //this.myAgent.setMyMap(this.myMap);
                    System.out.println("-CHANGE A to B (StateSendMapFSMBehaviour): " + myName + " goes to state B (send MAP)");

                } else { // pas reçu de PING, donc continuer à avancer dans la map
                    ((AbstractDedaleAgent) this.myAgent).moveTo(nextNode);
                }
            }
        }
    }

    public int onEnd() {
        return exitValue;
    }
}