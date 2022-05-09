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

import java.util.HashMap;
import java.util.List;
//import javax.json.*;
//import org.json.*;


// Behaviour/comportement du state A (exploration)
public class StateBackFSMBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = 1567689731496787661L;

    private List<String> listAgentNames;
    private HashMap<String, List<Couple<Observation,Integer>>> dictBackpack;
    private HashMap<String, HashMap<String, Boolean>> dictVoisinsMessages;
    //private MapRepresentation myMap;
    private FullMapRepresentation myFullMap;
    private int exitValue;

    public StateBackFSMBehaviour(final AbstractDedaleAgent myagent) {
        super(myagent);
    }

    public void action() {
        String myName = this.myAgent.getLocalName();

        System.out.println("\n--- START state J (StateBackFSMBehaviour): " + myName + " --- ");

        // update information
        this.myFullMap = ((FSMAgent) this.myAgent).getMyFullMap();
        this.dictVoisinsMessages = ((FSMAgent) this.myAgent).getDictVoisinsMessages();
        this.listAgentNames = ((FSMAgent) this.myAgent).getListAgentNames();


        //update le dictBackpack de l'agent agent
        ((FSMAgent) this.myAgent).setDictBackpackAgent(this.myAgent.getLocalName(), ((FSMAgent) this.myAgent).getBackPackFreeSpace());
        this.dictBackpack = ((FSMAgent) this.myAgent).getDictBackpack();

        String backpackStr = this.dictBackpack.toString();
        System.out.println(myName + " [STATE J] -- backpackStr: " + backpackStr); // {AgentFSM_2=[], AgentFSM_1=[<Gold, 100>, <Diamond, 100>]}

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
        String nextPosition = ((FSMAgent) this.myAgent).getNextNode();

        if (myPosition != null) {

            Integer distanceCulDeSac = this.myFullMap.getDistanceLeaf(myPosition);

            // ACTION : Envoie CUL-DE-SAC pour la file indienne
            if (nextPosition != null) {
                ACLMessage msgSend = new ACLMessage(ACLMessage.INFORM);
                msgSend.setProtocol("RECULE");
                msgSend.setSender(this.myAgent.getAID()); // on met un expéditeur au message

                //on envoie Id (pour ordre de priorité entre agent), le nextNode et la distanceCulDeSac
                msgSend.setContent(((FSMAgent) this.myAgent).getId() + "/" + ((FSMAgent) this.myAgent).getNextNode() + "/" + distanceCulDeSac);

                for (String receiverAgent : this.listAgentNames) { // on récupère le nom d'un agent
                    if (!receiverAgent.equals(myName)) {
                        msgSend.addReceiver(new AID(receiverAgent, false));
                    }
                }
                ((AbstractDedaleAgent) this.myAgent).sendMessage(msgSend);
                System.out.println(myName + " [STATE J] -- finished sending message CUL-DE-SAC for interblock");
            }

            // ACTION : Check si reçu CUL-DE-SAC pour la file indienne
            MessageTemplate msg = MessageTemplate.and(
                    MessageTemplate.MatchProtocol("RECULE"),
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM));

            ACLMessage msgReceived = this.myAgent.receive(msg);
            String msgExpediteur = "";
            String nextNodeExpediteur = "";
            String nameExpediteur = "";
            int idExpediteur = -1;
            String[] l;
            int distanceCulDeSacExpediteur = -1;
            if (msgReceived != null) {
                System.out.println(myName + " [STATE J] received message CUL-DE-SAC for interblock");
                nameExpediteur = msgReceived.getSender().getLocalName();
                msgExpediteur = msgReceived.getContent();
                l = msgExpediteur.split("/");
                idExpediteur = Integer.valueOf(l[0]);
                nextNodeExpediteur = l[1];
                distanceCulDeSacExpediteur = Integer.valueOf(l[2]);
            }

            if(((FSMAgent) this.myAgent).getPathBack().size()==0){
                // agent doit reculer
                if(((FSMAgent) this.myAgent).getPredNode().equals(null)) {
                    // agent avait deja reculer
                    // agent doit encore reculer, car pas assez de place pour les autres pr reculer
                    if(distanceCulDeSac > distanceCulDeSacExpediteur && msgReceived != null ){
                        List<String> cheminBack = myFullMap.getPathBack(myPosition, 2, nextNodeExpediteur);
                        if (cheminBack.size() > 0) {
                            ((FSMAgent) this.myAgent).setPathBack(cheminBack);
                            List<String> pathBack = ((FSMAgent) this.myAgent).getPathBack();
                            nextPosition = pathBack.get(0);
                            ((FSMAgent) this.myAgent).setNextNode(nextPosition);
                            ((FSMAgent) this.myAgent).setPredNode(myPosition); //evite de repartir dans ce noeud (noeud qui bloque)
                            exitValue = 3 ; //retourne en A (exploration)
                            System.out.println(myName + " [STATE J] -- go to state A (explore) -- END --");
                        }
                    }
                }else {
                    // agent premiere fois qu'il recule
                    List<String> cheminBack = myFullMap.getPathBack(myPosition, 2);
                    if (cheminBack.size() > 0) {
                        ((FSMAgent) this.myAgent).setPathBack(cheminBack);
                        List<String> pathBack = ((FSMAgent) this.myAgent).getPathBack();
                        nextPosition = pathBack.get(0);
                        ((FSMAgent) this.myAgent).setNextNode(nextPosition);
                        ((FSMAgent) this.myAgent).setPredNode(null);
                    }
                }
            }else{
                // agent sait qu'il doit reculer et il continue de reculer
                List<String> pathBack = ((FSMAgent) this.myAgent).getPathBack();
                if(pathBack.size()>0){
                    nextPosition = pathBack.get(0);
                    ((FSMAgent) this.myAgent).setPathBack( pathBack.subList(1,pathBack.size()) );
                    ((FSMAgent) this.myAgent).setNextNode(nextPosition);
                }
            }

            ((AbstractDedaleAgent) this.myAgent).moveTo(nextPosition);
            System.out.println(myName + " [STATE J] move to "+ nextPosition);
        }
        exitValue = 1; //reste au state J (Back) pour reculer
        System.out.println(myName + " [STATE J] -- reminds in state J -- END --");
    }

    public int onEnd() {
        return exitValue;
    }
}
