package eu.su.mas.dedaleEtu.mas.behaviours.fsm;

import eu.su.mas.dedaleEtu.mas.agents.fsm.FSMAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.FullMapRepresentation;
import jade.core.behaviours.OneShotBehaviour;

import jade.core.AID;

import java.util.*;
import java.io.IOException;

import jade.lang.acl.ACLMessage;

import dataStructures.serializableGraph.SerializableSimpleGraph;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;

// comportement du state B (Envoie carte)
public class StateSendMapFSMBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = 2567689731496787661L;

    private HashMap<String, HashMap<String, Boolean>> dictVoisinsMessages;
    //private MapRepresentation myMap;
    private FullMapRepresentation myFullMap;
    private int exitValue;

    public StateSendMapFSMBehaviour(final AbstractDedaleAgent myagent) {
        super(myagent);
    }

    public void action() {
        String myName = this.myAgent.getLocalName();

        System.out.println("\n--- START state B (StateSendMapFSMBehaviour): " + myName + " ---");

        // update information
        this.myFullMap = ((FSMAgent) this.myAgent).getMyFullMap();
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

                System.out.println(myName + " [STATE B] will send MAP to " + receiverAgent);
            }
            else if (!etat.get("recoit_ACK")) { // renvoie sa carte si pas reçu d'ACK pour la carte qu'il a déjà envoyé
                msg.addReceiver(new AID(receiverAgent, false));

                ((FSMAgent) this.myAgent).setDictVoisinsMessagesAgentAction(receiverAgent, "envoie_MAP", true);

                System.out.println(myName + " [STATE B] will re-send MAP to " + receiverAgent);
            }
        }

        // ajout de la carte de l'agent dans le message
        SerializableSimpleGraph<String, HashMap<String, Object>> mapSent = this.myFullMap.getSerializableGraph();

        //this.myMap.prepareMigration(); // generer SerializableSimpleGraph (et met this.myMap.g à null => optimiser place memoire ???? )
        //SerializableSimpleGraph<String, MapAttribute> mapSent = (this.myMap).getSg();

        try {
            msg.setContentObject(mapSent);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // envoie en cours de la carte à tous les voisins
        ((AbstractDedaleAgent) this.myAgent).sendMessage(msg);

        exitValue = 1; // aller en C = "Attente ACK et check Mailbox"
        System.out.println(myName + " CHANGES C to B: check Mailbox");
    }

    public int onEnd() {
        return exitValue;
    }
}