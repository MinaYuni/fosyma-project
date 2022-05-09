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
import java.util.Random;
//import javax.json.*;
//import org.json.*;


// Behaviour/comportement du state A (exploration)
public class StateInterblocageFSMBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = 1567689731496787661L;

    private List<String> listAgentNames;
    private HashMap<String, List<Couple<Observation,Integer>>> dictBackpack;
    private HashMap<String, HashMap<String, Boolean>> dictVoisinsMessages;
    //private MapRepresentation myMap;
    private FullMapRepresentation myFullMap;
    private int exitValue;

    public StateInterblocageFSMBehaviour(final AbstractDedaleAgent myagent) {
        super(myagent);
    }

    public void action() {
        String myName = this.myAgent.getLocalName();

        System.out.println("\n--- START state I (StateInterblocageFSMBehaviour): " + myName + " --- ");

        // update information
        this.myFullMap = ((FSMAgent) this.myAgent).getMyFullMap();
        this.dictVoisinsMessages = ((FSMAgent) this.myAgent).getDictVoisinsMessages();
        this.listAgentNames = ((FSMAgent) this.myAgent).getListAgentNames();


        //update le dictBackpack de l'agent agent
        ((FSMAgent) this.myAgent).setDictBackpackAgent(this.myAgent.getLocalName(), ((FSMAgent) this.myAgent).getBackPackFreeSpace());
        this.dictBackpack = ((FSMAgent) this.myAgent).getDictBackpack();

        String backpackStr = this.dictBackpack.toString();
        System.out.println(myName + " [STATE I] -- backpackStr: " + backpackStr); // {AgentFSM_2=[], AgentFSM_1=[<Gold, 100>, <Diamond, 100>]}

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
        String nextNode = ((FSMAgent) this.myAgent).getNextNode();

        if (myPosition != null) {
            // Interblocage avec le golem


            // Interblocage file indienne/culdesac
            if(((FSMAgent) this.myAgent).getCuldesac()){
                Integer distanceCulDeSac = this.myFullMap.getDistanceLeaf(myPosition);
                if(distanceCulDeSac==-1){
                    // distanceCulDeSac ne peut pas etre de 0 par defaut (car sous-entend que agent est dans le cul de sac)
                    // donc par defaut c'est -1
                    System.out.println("==================== PAS NORMAL QUE DISTANCE N'A PAS ETE TROUVER =============");
                }

                // ACTION : Envoie CUL-DE-SAC pour la file indienne
                if (nextNode != null) {
                    ACLMessage msgSend = new ACLMessage(ACLMessage.INFORM);
                    msgSend.setProtocol("CUL-DE-SAC");
                    msgSend.setSender(this.myAgent.getAID()); // on met un expéditeur au message

                    //on envoie Id (pour ordre de priorité entre agent), le nextNode et la distanceCulDeSac
                    msgSend.setContent(((FSMAgent) this.myAgent).getId() + "/" + ((FSMAgent) this.myAgent).getNextNode() + "/" + distanceCulDeSac);

                    for (String receiverAgent : this.listAgentNames) { // on récupère le nom d'un agent
                        if (!receiverAgent.equals(myName)) {
                            msgSend.addReceiver(new AID(receiverAgent, false));
                        }
                    }
                    ((AbstractDedaleAgent) this.myAgent).sendMessage(msgSend);
                    System.out.println(myName + " [STATE I] -- finished sending message CUL-DE-SAC for interblock");
                }

                // ACTION : Check si reçu CUL-DE-SAC pour la file indienne
                MessageTemplate msg = MessageTemplate.and(
                        MessageTemplate.MatchProtocol("CUL-DE-SAC"),
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM));

                ACLMessage msgReceived = this.myAgent.receive(msg);
                String msgExpediteur = "";
                String nextNodeExpediteur = "";
                String nameExpediteur = "";
                int idExpediteur = -1;
                String[] l;
                int distanceCulDeSacExpediteur = -1;
                if (msgReceived != null) {
                    System.out.println(myName + " [STATE I] received message CUL-DE-SAC for interblock");
                    nameExpediteur = msgReceived.getSender().getLocalName();
                    msgExpediteur = msgReceived.getContent();
                    l = msgExpediteur.split("/");
                    idExpediteur = Integer.valueOf(l[0]);
                    nextNodeExpediteur = l[1];
                    distanceCulDeSacExpediteur = Integer.valueOf(l[2]);
                }

                if( distanceCulDeSac > distanceCulDeSacExpediteur){
                    //agent est loin du cul de sac, donc il est proche de la sortie, donc il doit se deplacer
                    exitValue = 2; //aller au state J (Back) pour reculer
                }else{
                    ((AbstractDedaleAgent) this.myAgent).moveTo(nextNode);
                    System.out.println(" [STATE A] - " + myName + " -- list= " + this.myFullMap.getOpenNodes() + " -- nextNode: " + nextNode+" -- END -- ");
                    exitValue = 1;
                }

            }

        }
    }

    public int onEnd() {
        return exitValue;
    }
}
