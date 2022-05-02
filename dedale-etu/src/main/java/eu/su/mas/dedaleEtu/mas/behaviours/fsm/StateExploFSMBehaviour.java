package eu.su.mas.dedaleEtu.mas.behaviours.fsm;

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


// Behaviour/comportement du state A (exploration)
public class StateExploFSMBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = 8567689731499787661L;
    private final List<String> list_agentNames;
    private final HashMap<String, HashMap<String, Boolean>> dicoVoisinsMessages;
    private MapRepresentation myMap;
    private int exitValue;

    public StateExploFSMBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap, List<String> agentNames, HashMap<String, HashMap<String, Boolean>> dico) {
        super(myagent);
        this.myMap = myMap;
        this.list_agentNames = agentNames;
        this.dicoVoisinsMessages = dico;
    }

    public void action() {
        System.out.println("BEGIN : StateExploFSMBehaviour (state A), " + this.myAgent.getLocalName()+" - Begin exploration ");

        if (this.myMap == null) {
            this.myMap = new MapRepresentation();
        }

        //0) Retrieve the current position
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

            //1) remove the current node from openlist and add it to closedNodes
            this.myMap.addNode(myPosition, MapAttribute.closed);

            //2) get the surrounding nodes and, if not in closedNodes, add them to open nodes
            String nextNode = null;
            for (Couple<String, List<Couple<Observation, Integer>>> lob : lobs) {
                String nodeId = lob.getLeft();
                boolean isNewNode = this.myMap.addNewNode(nodeId);
                //the node may exist, but not necessarily the edge
                if (!myPosition.equals(nodeId)) {
                    this.myMap.addEdge(myPosition, nodeId);
                    if (nextNode == null && isNewNode) nextNode = nodeId;
                }
            }

            //3) while openNodes is not empty, continues
            if (!this.myMap.hasOpenNode()) { // si exploration fini
                exitValue = 2; // aller en F : "Exploration finie"
                System.out.println(this.myAgent.getLocalName() + " - Exploration successfully done.");
                System.out.println("END : StateExploFSMBehaviour (state A), " + this.myAgent.getLocalName()+" - finish exploration, go to state F");
            } else {

                //3.1) Select next move
                // there exist one open node directly reachable, go for it,
                // otherwise choose one from the openNode list, compute the shortestPath and go for it
                if (nextNode == null) { // if no directly accessible openNode
                    // chose one, compute the path and take the first step
                    nextNode = this.myMap.getShortestPathToClosestOpenNode(myPosition).get(0); //getShortestPath(myPosition,this.openNodes.get(0)).get(0);
                    System.out.println(this.myAgent.getLocalName() + "-currentPosition: " + myPosition + " -- list= " + this.myMap.getOpenNodes() + "| nextNode: " + nextNode);
                } else {
                    System.out.println("nextNode notNUll - " + this.myAgent.getLocalName() + "-- list= " + this.myMap.getOpenNodes() + "\n -- nextNode: " + nextNode);
                }

                //3.2) ACTION : envoie PING à chaque déplacement
                int n = this.list_agentNames.size();
                String myName = this.myAgent.getLocalName();

                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.setProtocol("PING");
                msg.setContent(myName); // mettre son nom dans le ping envoyé
                msg.setSender(this.myAgent.getAID()); // mettre une expéditeur au message

                // ajout des destinataires du ping (tous les autres agents, sauf moi_meme)
                for (String receiverAgent : this.list_agentNames) {
                    //System.out.println("myName: " + myName + "\treceiverAgent: " + receiverAgent);
                    if (!Objects.equals(myName, receiverAgent)) { // si c'est pas moi
                        System.out.println("STATE A : " + this.myAgent.getLocalName()+" -will send msg to " + receiverAgent);
                        msg.addReceiver(new AID(receiverAgent, false));    //mettre une receveur du message
                    }
                }
                // envoie du ping à tous les agents
                ((AbstractDedaleAgent) this.myAgent).sendMessage(msg);
                System.out.println("STATE A : " + this.myAgent.getLocalName()+" - FINISH SEND PING ");

                //3.3) At each time step, the agent check if he received a ping from a teammate
                // ACTION : Check reception PING
                MessageTemplate msgPing = MessageTemplate.and(
                        MessageTemplate.MatchProtocol("PING"),
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM));

                ACLMessage msgPingReceived = this.myAgent.receive(msgPing);
                // si reception PING, aller en B (envoyer sa carte),
                // sinon continuer déplacement
                if (msgPingReceived != null) { // réception PING, donc un autre agent est à proximité, donc MàJ dict_voisins de l'agent
                    // ajouter le voisin au dico (voir type de list_voisin dans FSMAgent.java)
                    String namePingReceived = msgPingReceived.getContent(); // récupérer le nom du voisin (nom donnée dans le message du ping reçu)

                    // si l'agent n'a pas encore rencontré l'envoyeur du ping, il est ajouté dans le dictionnaire (dict_voisins)
                    if (!this.dicoVoisinsMessages.containsKey(namePingReceived)) { //
                        HashMap<String, Boolean> etat = new HashMap<String, Boolean>();
                        // état de l'agent par rapport à l'envoyeur du ping : dico est vide car il n'a rien fait (on peut aussi tout initialiser a False)

                        // ajout de l'envoyeur et son état dans le dico des voisins
                        this.dicoVoisinsMessages.put(namePingReceived, etat);
                    }

                    //MAJ MAP
                    //myAgent.setMyMap(this.myMap);

                    exitValue = 1; // aller en B : "Envoie carte"
                    //this.myAgent.setMyMap(this.myMap);
                    System.out.println("Change state A to state B : StateSendMapFSMBehaviour (state A), " + this.myAgent.getLocalName()+" - go in state B (send MAP) ");

                } else { // pas reçu de message (PING) donc continuer a avancer dans la map
                    ((AbstractDedaleAgent) this.myAgent).moveTo(nextNode);

                }
            }
        }

    }

    public int onEnd() {
        return exitValue;
    }
}