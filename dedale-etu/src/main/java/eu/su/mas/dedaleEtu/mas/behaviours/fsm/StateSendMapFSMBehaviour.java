package eu.su.mas.dedaleEtu.mas.behaviours.fsm;

import eu.su.mas.dedaleEtu.mas.agents.fsm.FSMAgent;
import jade.core.behaviours.OneShotBehaviour;

import jade.core.AID;

import java.util.*;
import java.io.IOException;

import jade.lang.acl.ACLMessage;

import dataStructures.serializableGraph.SerializableSimpleGraph;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;

// comportement du state B (Envoie carte)
public class StateSendMapFSMBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = 2567689731496787661L;

    private HashMap<String, HashMap<String, Boolean>> dictVoisinsMessages;
    private MapRepresentation myMap;
    private int exitValue;

    public StateSendMapFSMBehaviour(final AbstractDedaleAgent myagent) {
        super(myagent);
    }

    public void action() {
        String myName = this.myAgent.getLocalName();

        System.out.println("\n-- START state B (StateSendMapFSMBehaviour): " + myName + " starts sending MAP --");

        // update information
        this.myMap = ((FSMAgent) this.myAgent).getMyMap();
        this.dictVoisinsMessages = ((FSMAgent) this.myAgent).getDictVoisinsMessages();

//        try {
//            this.myAgent.doWait(1000);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        // ACTION : Envoyer sa carte à tous ses voisins
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setProtocol("SHARE-MAP");
        msg.setSender(this.myAgent.getAID()); // mettre un expéditeur
        //msg.setContent(myName); // met son nom dans le message envoyé

        // ajouter les noms des destinataires (ici les noms des voisins) du message
        Set<String> voisins = this.dictVoisinsMessages.keySet(); // récupérer toutes les clés (tous les noms des voisins)

        for (String receiverAgent : voisins) {
            HashMap<String, Boolean> etat = this.dictVoisinsMessages.get(receiverAgent);

            if (!etat.get("envoie_MAP")) { // envoie sa carte aux nouveaux agents
                msg.addReceiver(new AID(receiverAgent, false));

                ((FSMAgent) this.myAgent).setDictVoisinsMessagesAgentAction(receiverAgent, "envoie_MAP", true);

                System.out.println("STATE B : " + myName + " sends MAP to " + receiverAgent);
            }
            else if (!etat.get("recoit_ACK")) { // renvoie sa carte si pas reçu d'ACK pour la carte qu'il a déjà envoyé
                msg.addReceiver(new AID(receiverAgent, false));

                ((FSMAgent) this.myAgent).setDictVoisinsMessagesAgentAction(receiverAgent, "envoie_MAP", true);

                System.out.println("STATE B : " + myName + " re-sends MAP to " + receiverAgent);
            }
        }

        // ajout de la carte de l'agent dans le message
        SerializableSimpleGraph<String, MapAttribute> mapSent = this.myMap.getSerializableGraph();

        try {
            msg.setContentObject(mapSent);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // envoie en cours de la carte à tous les voisins
        ((AbstractDedaleAgent) this.myAgent).sendMessage(msg);

        exitValue = 1; // aller en C = "Attente ACK et check Mailbox"
        System.out.println("-END state B (StateSendMapFSMBehaviour): " + myName + " finished sending MAP, goes to state C ");
    }

    public int onEnd() {
        return exitValue;
    }
}