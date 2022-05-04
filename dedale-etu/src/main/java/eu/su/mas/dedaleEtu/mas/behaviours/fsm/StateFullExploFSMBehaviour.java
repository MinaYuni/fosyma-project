package eu.su.mas.dedaleEtu.mas.behaviours.fsm;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.fsm.FSMAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.FullMapRepresentation;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


// Behaviour/comportement du state A (exploration)
public class StateFullExploFSMBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = 1567689731496787661L;

    private List<String> listAgentNames;
    private HashMap<String, HashMap<String, Boolean>> dictVoisinsMessages;
    //private MapRepresentation myMap;
    private FullMapRepresentation myFullMap;
    private int exitValue;

    public StateFullExploFSMBehaviour(final AbstractDedaleAgent myagent) {
        super(myagent);
    }

    public void action() {
        String myName = this.myAgent.getLocalName();

        System.out.println("\n--- START state A (StateFullExploFSMBehaviour): " + myName + " ---");

        // update information
        this.myFullMap = ((FSMAgent) this.myAgent).getMyFullMap();
        this.dictVoisinsMessages = ((FSMAgent) this.myAgent).getDictVoisinsMessages();
        this.listAgentNames = ((FSMAgent) this.myAgent).getListAgentNames();

//        try {
//            this.myAgent.doWait(1000);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        // 0) Retrieve the current position
        String myPosition = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition();

        if (myPosition != null) {
            System.out.println(myName + " [STATE A] -- currentPosition: " + myPosition);

            // list of observable from the agent's current position
            List<Couple<String, List<Couple<Observation, Integer>>>> lobs = ((AbstractDedaleAgent) this.myAgent).observe(); // myPosition
            //System.out.println(myName + " [STATE A] -- lobs: " + lobs);

            // list of observations associated to the currentPosition
            List<Couple<Observation, Integer>> lObservations = lobs.get(0).getRight();
            //System.out.println(myName + " [STATE A] -- lObservations: " + lObservations);

            for (Couple<Observation, Integer> o : lObservations) {
                System.out.println(myName + " [STATE A] -- obs: " + o);
                switch (o.getLeft()) {
                    case DIAMOND:
                        System.out.println(myName + " [STATE A] -- My treasure type: " + ((AbstractDedaleAgent) this.myAgent).getMyTreasureType());
                        System.out.println(myName + " [STATE A] -- My current backpack capacity:" + ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
                        System.out.println(myName + " [STATE A] -- Value of the treasure on the current position: " + o.getLeft() + " - " + o.getRight());
                        //System.out.println(myName + " [STATE A] -- The agent grabbed: " + ((AbstractDedaleAgent) this.myAgent).pick());
                        //System.out.println(myName + " [STATE A] -- The remaining backpack capacity: " + ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
                    case GOLD:
                        System.out.println(myName + " [STATE A] -- My treasure type: " + ((AbstractDedaleAgent) this.myAgent).getMyTreasureType());
                        System.out.println(myName + " [STATE A] -- My current backpack capacity:" + ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
                        System.out.println(myName + " [STATE A] -- Value of the treasure on the current position: " + o.getLeft() + " - " + o.getRight());
                        //System.out.println(myName + " [STATE A] -- The agent grabbed: " + ((AbstractDedaleAgent) this.myAgent).pick());
                        //System.out.println(myName + " [STATE A] -- The remaining backpack capacity: " + ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
                    case STENCH:
                        System.out.println(myName + " [STATE A] -- STENCH");
                    default:
                        break;
                }
            }

            // 1) remove the current node from openlist and add it to closedNodes
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            long time = timestamp.getTime();
            this.myFullMap.addNode(myPosition, FullMapRepresentation.MapAttribute.closed, lObservations, time);

            // 2) get the surrounding nodes and, if not in closedNodes, add them to open nodes
            String nextNode = null;
            for (Couple<String, List<Couple<Observation, Integer>>> lob : lobs) {
                String nodeId = lob.getLeft();
                boolean isNewNode = this.myFullMap.addNewNode(nodeId, lob.getRight(), time);
                // the node may exist, but not necessarily the edge
                if (!myPosition.equals(nodeId)) {
                    this.myFullMap.addEdge(myPosition, nodeId);
                    if (nextNode == null && isNewNode) nextNode = nodeId;
                }
            }

            //3) while openNodes is not empty, continues
            if (!this.myFullMap.hasOpenNode()) { // si exploration finie
                exitValue = 2; // aller en G : "Random Walk"
                System.out.println(myName + " [STATE A] - Exploration successfully done");
                System.out.println(myName + " CHANGES A to G : ranwom walk");
            } else {
                // 3.1) Select next move
                // there exist one open node directly reachable, go for it,
                // otherwise choose one from the openNode list, compute the shortestPath and go for it
                if (nextNode == null) { // if no directly accessible openNode
                    // chose one, compute the path and take the first step
                    nextNode = this.myFullMap.getShortestPathToClosestOpenNode(myPosition).get(0); //getShortestPath(myPosition,this.openNodes.get(0)).get(0);
                    System.out.println(myName + " - currentPosition: " + myPosition + " -- list= " + this.myFullMap.getOpenNodes() + " | nextNode: " + nextNode);
                } else {
                    System.out.println("nextNode notNUll - " + myName + " -- list= " + this.myFullMap.getOpenNodes() + " | nextNode: " + nextNode);
                }

                // MAJ MAP
                ((FSMAgent) this.myAgent).setMyFullMap(this.myFullMap);

                // 3.2) ACTION : envoie un PING à tout le monde à chaque déplacement
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.setProtocol("PING");
                //msg.setContent(myName); // mettre son nom dans le ping envoyé
                msg.setSender(this.myAgent.getAID()); // mettre un expéditeur au message

                // ajout des destinataires du ping (tous les autres agents, sauf moi-meme)
                for (String receiverAgent : this.listAgentNames) { // PROBLEME : quand un autre agent meurt => il y a une boucle infinie
                    if (!Objects.equals(myName, receiverAgent)) { // si ce n'est pas moi
                        System.out.println(myName + " [STATE A] will send msg to " + receiverAgent);
                        msg.addReceiver(new AID(receiverAgent, false)); // on met un receveur au message
                    }
                }
                // envoie du ping à tous les agents
                ((AbstractDedaleAgent) this.myAgent).sendMessage(msg);
                System.out.println(myName + " [STATE A] finished sending PING");

                // 3.3) At each time step, the agent check if he received a ping from a teammate
                // ACTION : Check reception PING
                MessageTemplate msgPing = MessageTemplate.and(
                        MessageTemplate.MatchProtocol("PING"),
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM));

                ACLMessage msgPingReceived = this.myAgent.receive(msgPing);

                // si reception PING, aller en B (envoyer sa carte),
                // sinon continuer déplacement
                if (msgPingReceived != null) { // réception PING, donc un autre agent est à proximité
                    System.out.println(myName + " [STATE A] received PING");

                    String namePingReceived = msgPingReceived.getSender().getLocalName();
                    ((FSMAgent) this.myAgent).setDictVoisinsMessagesAgentAction(namePingReceived, "recoit_PING", true);

                    // ancien emplacement de ((FSMAgent) this.myAgent).setMyMap(this.myMap);

                    exitValue = 1; // aller en B : "Envoie carte"
                    System.out.println(myName + " CHANGES A to B : send MAP");

                } else { // pas reçu de PING, donc continuer à avancer dans la map
                    ((FSMAgent) this.myAgent).resetDictVoisinsMessages();
                    ((AbstractDedaleAgent) this.myAgent).moveTo(nextNode);

                    //exitValue = 3 ; //aller state G (choix prochain destination/noeud)
                }
            }
        }
    }

    public int onEnd() {
        return exitValue;
    }
}
