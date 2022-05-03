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

    private MapRepresentation myMap;
    private List<String> list_agentNames;
    private HashMap<String, HashMap<String, Boolean>> dictVoisinsMessages;
    private int exitValue;

    public StateSendMapFSMBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap, List<String> agentNames, HashMap<String, HashMap<String, Boolean>> dico) {
        super(myagent);
        this.myMap = myMap;
        this.list_agentNames = agentNames;
        this.dictVoisinsMessages = dico;
    }

    public void action() {
        int nb_agents = this.list_agentNames.size();
        String myName = this.myAgent.getLocalName();

        System.out.println("\n-- START state B (StateSendMapFSMBehaviour): " + myName + " starts sending MAP --");

        // update information
        if (this.myMap == null) {
            this.myMap = ((FSMAgent) this.myAgent).getMyMap();
        }
        this.dictVoisinsMessages = ((FSMAgent) this.myAgent).getDictVoisinsMessages();

        // ACTION : Envoyer sa carte à tous ses voisins
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setProtocol("SHARE-MAP");
        msg.setSender(this.myAgent.getAID()); //mettre un expéditeur
        //msg.setContent(myName); // met son nom dans le message envoyé

        // ajouter les noms des destinataires (ici les noms des voisins) du message
        Set<String> setOfKeys = this.dictVoisinsMessages.keySet(); // récupérer tous les clés (tous les noms des voisins)
        for (String receiverAgent : setOfKeys) {
            msg.addReceiver(new AID(receiverAgent, false));
            System.out.println("STATE B : " + myName + " sends MAP to " + receiverAgent);
        }
        // ajout de la carte de l'agent dans le message
        SerializableSimpleGraph<String, MapAttribute> mapSent = (this.myMap).getSerializableGraph();
        //SerializableSimpleGraph<String, MapAttribute> mapSent=(((FSMAgent)this.myAgent).getMyMap()).getSerializableGraph();


        //this.myMap.prepareMigration(); //generer SerializableSimpleGraph (et met this.myMap.g à null => optimiser place memoire ???? )
        //SerializableSimpleGraph<String, MapAttribute> mapSent = (this.myMap).getSg();

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