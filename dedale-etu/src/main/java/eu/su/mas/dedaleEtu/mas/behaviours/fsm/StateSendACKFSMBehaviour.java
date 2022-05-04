package eu.su.mas.dedaleEtu.mas.behaviours.fsm;

import jade.core.behaviours.OneShotBehaviour;
import eu.su.mas.dedaleEtu.mas.agents.fsm.FSMAgent;
import jade.core.AID;

import java.util.*;
import java.io.IOException;
import java.lang.Math;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.lang.acl.UnreadableException;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;

import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.fsm.FSMAgent;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMapBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;

//Behaviours/comportement au state D : "Envoie ACK"
public class StateSendACKFSMBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = 4567689731496787661L;

    private HashMap<String, HashMap<String, Boolean>> dictVoisinsMessages;
    private MapRepresentation MyMap;
    private int exitValue;

    public StateSendACKFSMBehaviour(final AbstractDedaleAgent myagent) {
        super(myagent);
    }

    public void action() {
        String myName = this.myAgent.getLocalName();

        System.out.println("\n--- START state D (StateSendACKFSMBehaviour): " + myName + " ---");

        // update information
        this.MyMap = ((FSMAgent) this.myAgent).getMyMap();
        this.dictVoisinsMessages = ((FSMAgent) this.myAgent).getDictVoisinsMessages();

//        try {
//            this.myAgent.doWait(1000);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        // ACTION : Envoie un ACK
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setProtocol("ACK-MAP");
        msg.setSender(this.myAgent.getAID()); // on met un expéditeur au message

        Set<String> voisins = this.dictVoisinsMessages.keySet();

        // ajout des receveurs du message (sauf moi-même)
        for (String receiverAgent : voisins) { // on récupère le nom d'un agent
            HashMap<String, Boolean> etat = this.dictVoisinsMessages.get(receiverAgent);

            if (etat.get("recoit_MAP")) {
                if (!etat.get("envoie_ACK")) {
                    msg.addReceiver(new AID(receiverAgent, false));
                    ((FSMAgent) this.myAgent).setDictVoisinsMessagesAgentAction(receiverAgent, "envoie_ACK", true);
                }
            }
        }

        // envoyer un ACK-MAP à tous les agents dont l'agent a reçu la carte et où il n'a pas encore envoyé de ACK auparavant
        ((AbstractDedaleAgent) this.myAgent).sendMessage(msg);
        System.out.println(myName + " [STATE D] finished sending ACK");

        exitValue = 1; // aller en C : "Attente ACK"
        System.out.println(myName + " CHANGES D to C: check Mailbox");
    }

    public int onEnd() {
        return exitValue;
    }
}
