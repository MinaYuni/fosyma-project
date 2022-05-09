package eu.su.mas.dedaleEtu.mas.behaviours.fsm;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.fsm.FSMAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.FullMapRepresentation;
//import eu.su.mas.dedaleEtu.mas.knowledge.HashMapSerialize;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;
//import javax.json.*;
//import org.json.*;


// Behaviour/comportement du state A (exploration)
public class StateFullExploFSMBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = 1567689731496787661L;

    private List<String> listAgentNames;
    private HashMap<String, List<Couple<Observation,Integer>>> dictBackpack;
    private HashMap<String, HashMap<String, Boolean>> dictVoisinsMessages;
    //private MapRepresentation myMap;
    private FullMapRepresentation myFullMap;
    private final int timerWaitMax = 3;
    private final int maxOpenNode = 5;
    private int timerWait = 0;
    private int exitValue;

    public StateFullExploFSMBehaviour(final AbstractDedaleAgent myagent) {
        super(myagent);
    }

    public void action() {
        String myName = this.myAgent.getLocalName();

        System.out.println("\n--- START state A (StateFullExploFSMBehaviour): " + myName + " --- ");

        // update information
        this.myFullMap = ((FSMAgent) this.myAgent).getMyFullMap();
        this.dictVoisinsMessages = ((FSMAgent) this.myAgent).getDictVoisinsMessages();
        this.listAgentNames = ((FSMAgent) this.myAgent).getListAgentNames();


        //update le dictBackpack de l'agent agent
        ((FSMAgent) this.myAgent).setDictBackpackAgent(this.myAgent.getLocalName(), ((FSMAgent) this.myAgent).getBackPackFreeSpace());
        this.dictBackpack = ((FSMAgent) this.myAgent).getDictBackpack();

        String backpackStr = this.dictBackpack.toString();
        System.out.println(myName + " [STATE A] -- backpackStr: " + backpackStr); // {AgentFSM_2=[], AgentFSM_1=[<Gold, 100>, <Diamond, 100>]}

        //String str = "{AgentFSM_2=[], AgentFSM_1=[<Gold, 100>, <Diamond, 100>]}";
        //HashMap<String, List<Couple<Observation,Integer>>>
        //HashMap<String, List<Couple<String,Integer>>>
        //System.out.println("========= ON A : " + str);
        //HashMapSerialize stringToHashMap = new HashMapSerialize();
        //HashMap<String, List<Couple<Observation,Integer>>> listCapacity = stringToHashMap.HashMapFrom(str);
        //System.out.println("========= VOICI LE RESULTAT : " + listCapacity);
/*
        try {
            this.myAgent.doWait(500);
        } catch (Exception e) {
            e.printStackTrace();
        }
 */
        // 0) Retrieve the current position
        String myPosition = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition();
        String predPosition = ((FSMAgent) this.myAgent).getPredNode();

        if (myPosition != null) {
            if(timerWait<=timerWaitMax && myPosition.equals(predPosition)){
                this.timerWait = 0;
                ((FSMAgent) this.myAgent).setCuldesac(true);
                System.out.println(" [STATE A] --- " + myName + " -- bloquer avec golem " + ((FSMAgent) this.myAgent).getPositionGolem());
                // agent premiere fois qu'il recule
                List<String> cheminBack = this.myFullMap.getPathBack(myPosition, 4, ((FSMAgent) this.myAgent).getPositionGolem());
                if (cheminBack.size() > 0) {
                    ((FSMAgent) this.myAgent).setPathBack(cheminBack);
                    List<String> pathBack = ((FSMAgent) this.myAgent).getPathBack();
                    String nextPosition = pathBack.get(0);
                    ((FSMAgent) this.myAgent).setNextNode(nextPosition);
                    ((FSMAgent) this.myAgent).setPredNode(myPosition);
                }
            }
            if(((FSMAgent) this.myAgent).getCuldesac()){
                List<String> pathBack = ((FSMAgent) this.myAgent).getPathBack();
                String nextNode = ((FSMAgent) this.myAgent).getNextNode();
                // agent sait qu'il doit reculer et il continue de reculer
                if(pathBack.size()>0){
                    if(pathBack.size()==1){
                        ((FSMAgent) this.myAgent).setCuldesac(false);
                        //this.myAgent.doWait(10);
                    }
                    nextNode = pathBack.get(0);
                    ((FSMAgent) this.myAgent).setPathBack( pathBack.subList(1,pathBack.size()) );
                    ((FSMAgent) this.myAgent).setPredNode(myPosition);
                    ((FSMAgent) this.myAgent).setNextNode(nextNode);
                }
                ((AbstractDedaleAgent) this.myAgent).moveTo(nextNode);
                System.out.println(" [STATE A] - " + myName + " -- list= " + this.myFullMap.getOpenNodes() + " -- nextNode: " + nextNode+" -- END -- ");
            }
            else if (((FSMAgent) this.myAgent).getInterblocage()) {
                System.out.println(myName + " [STATE A] -- INTERBLOCAGE : "+((FSMAgent) this.myAgent).getInterblocage());

                String nextNode = ((FSMAgent) this.myAgent).getNextNode();

                boolean bouge = ((AbstractDedaleAgent) this.myAgent).moveTo(nextNode);
                if (bouge) {
                    ((FSMAgent) this.myAgent).setPredNode(myPosition);
                    ((FSMAgent) this.myAgent).resetDictVoisinsMessages();
                    ((FSMAgent) this.myAgent).setInterblocage(false);
                }else{
                    exitValue = 4; //go to state I (inteblocage)
                    System.out.println(myName + " [STATE A] -- go to state I (inteblocage) --- END -- ");
                }
            }
            else {
                //System.out.println(myName + " [STATE A] -- currentPosition: " + myPosition); //+ "-- list= " + this.myFullMap.getOpenNodes()

                // list of observable from the agent's current position
                List<Couple<String, List<Couple<Observation, Integer>>>> lobs = ((AbstractDedaleAgent) this.myAgent).observe(); // myPosition
                ((FSMAgent) this.myAgent).setCuldesac(lobs.size() == 2);
                //System.out.println(myName + " [STATE A] -- lobs: " + lobs);

                // list of observations associated to the currentPosition
                List<Couple<Observation, Integer>> lObservations = lobs.get(0).getRight();
                //System.out.println(myName + " [STATE A] -- lObservations: " + lObservations);

                for (Couple<Observation, Integer> o : lObservations) {
                    System.out.println(myName + " [STATE A] -- obs: " + o);
                    switch (o.getLeft()) {
                        case DIAMOND: case GOLD:
                            //System.out.println(myName + " [STATE A] -- My treasure type: " + ((AbstractDedaleAgent) this.myAgent).getMyTreasureType());
                            //System.out.println(myName + " [STATE A] -- My current backpack capacity:" + ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
                            System.out.println(myName + " [STATE A] -- Value of the treasure on the current position: " + o.getLeft() + " - " + o.getRight());
                            //System.out.println(myName + " [STATE A] -- The agent grabbed: " + ((AbstractDedaleAgent) this.myAgent).pick());
                            //System.out.println(myName + " [STATE A] -- The remaining backpack capacity: " + ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
                            break;
                        case STENCH:
                            System.out.println(myName + " [STATE A] -- STENCH: " + myPosition);
                            timerWait ++ ;
                            break;
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
                        if (nextNode == null && isNewNode) {
                            nextNode = nodeId;
                            ((FSMAgent) this.myAgent).setNextNode(nextNode);
                        }
                    }
                }

                //3) while openNodes is not empty, continues
                if (!this.myFullMap.hasOpenNode()) { // si exploration finie
                    exitValue = 2; // aller en G : "Random Walk"
                    System.out.println(myName + " [STATE A] - Exploration successfully done");
                    System.out.println(myName + " [STATE A] - CHANGES A to G : random walk");
                } else {
                    // 3.1) Select next move
                    // there exist one open node directly reachable, go for it,
                    // otherwise choose one from the openNode list, compute the shortestPath and go for it
                    if (nextNode == null) { // if no directly accessible openNode
                        // chose one, compute the path and take the first step
                        //nextNode = this.myFullMap.getShortestPathToClosestOpenNode(myPosition).get(0); //getShortestPath(myPosition,this.openNodes.get(0)).get(0);
                        List<String> path = this.myFullMap.getShortestPathToClosestOpenNode(myPosition); //getShortestPath(myPosition,this.openNodes.get(0)).get(0);
                        ((FSMAgent) this.myAgent).setPath(path);
                        nextNode = path.get(0);
                        ((FSMAgent) this.myAgent).setNextNode(nextNode);
                        System.out.println(myName + " [STATE A] - currentPosition: " + myPosition + " -- list= " + this.myFullMap.getOpenNodes() + " | nextNode: " + nextNode);
                    } else {
                        ((FSMAgent) this.myAgent).setNextNode(nextNode);
                        System.out.println(" [STATE A] - nextNode notNUll - " + myName + " -- list= " + this.myFullMap.getOpenNodes() + " | nextNode: " + nextNode);
                    }


                    while (nextNode.equals(((FSMAgent) this.myAgent).getPositionGolem())) {
                        List<String> path = this.myFullMap.getPathBack(myPosition, 2, nextNode);
                        ((FSMAgent) this.myAgent).setPath(path);
                        nextNode = path.get(0);
                        ((FSMAgent) this.myAgent).setNextNode(nextNode);
                        System.out.println(myName + " [STATE A] - currentPosition: " + myPosition + " -- list= " + this.myFullMap.getOpenNodes() + " | nextNode: " + nextNode);
                    }


                    // MAJ MAP
                    ((FSMAgent) this.myAgent).setMyFullMap(this.myFullMap);

                    // 3.2) ACTION : envoie un PING à tout le monde à chaque déplacement
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.setProtocol("PING");
                    msg.setSender(this.myAgent.getAID()); // mettre un expéditeur au message
                    // envoyer son ID, sa position actuelle, son prochain noeud, s'il est dans un cul-de-sac ou pas
                    boolean myCuldesac = ((FSMAgent) this.myAgent).getCuldesac();
                    msg.setContent(((FSMAgent) this.myAgent).getId() + "/" + myPosition + "/" + ((FSMAgent) this.myAgent).getNextNode() + "/" + String.valueOf(myCuldesac));

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

                    String nameExpediteur = "";
                    String msgExpediteur = "";
                    String nextNodeExpediteur = "";
                    String actualNodeExpediteur = "";
                    String culdesacExpediteur = "";

                    int idExpediteur = -1;

                    // si reception PING, aller en B (envoyer sa carte),
                    // sinon continuer déplacement
                    if (msgPingReceived != null) { // réception PING, donc un autre agent est à proximité
                        System.out.println(myName + " [STATE A] received PING");

                        nameExpediteur = msgPingReceived.getSender().getLocalName();
                        ((FSMAgent) this.myAgent).setDictVoisinsMessagesAgentAction(nameExpediteur, "recoit_PING", true);

                        //récupérer les éléments du message qui est l'id et nextNode de l'expéditeur
                        msgExpediteur = (String) msgPingReceived.getContent();
                        String[] msgSlipt = msgExpediteur.split("/");

                        idExpediteur = Integer.parseInt(msgSlipt[0]);
                        actualNodeExpediteur = msgSlipt[1];
                        nextNodeExpediteur = msgSlipt[2];
                        culdesacExpediteur = msgSlipt[3];

                        // interblocage face à face avec des directions opposées
                         if (myPosition.equals(nextNodeExpediteur) && nextNode.equals(actualNodeExpediteur)) {
                            ((FSMAgent) this.myAgent).setInterblocage(true);
                            System.out.println(myName + " [STATE A] -- INTERBLOCAGE : "+ ((FSMAgent) this.myAgent).getInterblocage() + " -- with : " + nameExpediteur);

                            // si l'expéditeur est dans un cul-de-sac, alors reculer
                            if (Objects.equals(culdesacExpediteur, "true")) {
                                if (predPosition != null) {
                                    nextNode = predPosition; // reculer
                                } // on devrait faire un else dans le cas où des agents spawn direct dans un cul-de-sac (donc interblocage sans avoir bougé donc predPosition==null)

                                ((FSMAgent) this.myAgent).setNextNode(nextNode);
                            }
                            // sinon celui qui bouge est l'agent avec l'ID le plus grand (donc si l'ID de l'expéditeur est plus petit que le mien, alors je bouge)
                            else if (idExpediteur < ((FSMAgent) this.myAgent).getId()){
                                String newNextNode = nextNode;

                                // choisir un autre prochain noeud différent de nextNode
                                while (newNextNode.equals(nextNode) && lobs.size() > 2) {
                                    Random r = new Random();
                                    int moveId = 1 + r.nextInt(lobs.size() - 1);
                                    newNextNode = lobs.get(moveId).getLeft();
                                }

                                nextNode = newNextNode;
                                ((FSMAgent) this.myAgent).setNextNode(nextNode);
                            }
                        }

                        exitValue = 1; // aller en B : "Envoie carte"
                        System.out.println(myName + " [STATE A]  CHANGES A to B : send MAP -- END -- ");

                    } else { // pas reçu de PING, donc continuer à avancer dans la map
                        boolean bouge = ((AbstractDedaleAgent) this.myAgent).moveTo(nextNode);
                        if (bouge) {
                            ((FSMAgent) this.myAgent).setPredNode(myPosition);
                            ((FSMAgent) this.myAgent).resetDictVoisinsMessages();
                            ((FSMAgent) this.myAgent).setInterblocage(false);
                        }else{
                            // agent n'a pas reussi a bougé
                            if(timerWait<=timerWaitMax && myPosition.equals(predPosition)){
                                this.timerWait = 0;
                                ((FSMAgent) this.myAgent).setCuldesac(true);
                                System.out.println(" [STATE A] - " + myName + " -- listOpenNode : " + this.myFullMap.getOpenNodes() + " -- nextNode: " + nextNode);

                                ((FSMAgent) this.myAgent).setPositionGolem(nextNode);
                                // agent premiere fois qu'il recule
                                List<String> cheminBack = this.myFullMap.getPathBack(myPosition, 4, nextNode);
                                if (cheminBack.size() > 0) {
                                    ((FSMAgent) this.myAgent).setPathBack(cheminBack);
                                    List<String> pathBack = ((FSMAgent) this.myAgent).getPathBack();
                                    String nextPosition = pathBack.get(0);
                                    ((FSMAgent) this.myAgent).setNextNode(nextPosition);
                                    ((FSMAgent) this.myAgent).setPredNode(myPosition);
                                }
                            }
                        }
                        System.out.println(" [STATE A] - " + myName + " -- list= " + this.myFullMap.getOpenNodes() + " -- nextNode: " + nextNode+" -- END -- ");
                    }
                }
            }
        }
    }

    public int onEnd() {
        return exitValue;
    }
}
