package eu.su.mas.dedaleEtu.mas.behaviours.fsm;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedaleEtu.mas.knowledge.FullMapRepresentation;
import jade.core.behaviours.OneShotBehaviour;
import eu.su.mas.dedaleEtu.mas.agents.fsm.FSMAgent;

import java.util.*;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import dataStructures.serializableGraph.SerializableSimpleGraph;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;


//Behaviours/comportement au state C
public class StateMailboxFSMBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = 3567689731496787661L;
    private final int timerMax = 10; // temps max d'attente
    private HashMap<String, HashMap<String, Boolean>> dictVoisinsMessages;
    //private MapRepresentation MyMap;
    private FullMapRepresentation myFullMap;
    private int timerACK = 0;
    private int timerMAP = 0;
    private int exitValue;

    public StateMailboxFSMBehaviour(final AbstractDedaleAgent myagent) {
        super(myagent);
    }

    public void action() {
        exitValue = -1;
        String myName = this.myAgent.getLocalName();

        System.out.println("\n--- START state C (StateMailboxFSMBehaviour): " + myName + " ---");

        // update information
        this.myFullMap = ((FSMAgent) this.myAgent).getMyFullMap();
        this.dictVoisinsMessages = ((FSMAgent) this.myAgent).getDictVoisinsMessages();

//        try {
//            this.myAgent.doWait(500);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        // 1) ACTION : Check si l'agent a reçu un ping d'un nouveau voisin
        MessageTemplate msgPing = MessageTemplate.and(
                MessageTemplate.MatchProtocol("PING"),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));

        ACLMessage msgPingReceived = this.myAgent.receive(msgPing);

        if (msgPingReceived != null) {
            System.out.println(myName + " [STATE C] received PING");

            String namePingReceived = msgPingReceived.getSender().getLocalName();

            ((FSMAgent) this.myAgent).setDictVoisinsMessagesAgentAction(namePingReceived, "recoit_PING", true);

            exitValue = 4; // aller en B : "Envoie MAP"
            System.out.println(myName + " CHANGES C to B: send MAP");
        }

        // 2) ACTION : check si l'agent a reçu un ACK de ses voisins pour la carte qu'il a envoyé
        MessageTemplate msgACK = MessageTemplate.and(
                MessageTemplate.MatchProtocol("ACK-MAP"),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));

        ACLMessage msgACKMapReceived = this.myAgent.receive(msgACK);

        if (msgACKMapReceived != null) { // si l'agent a reçu un ACK-MAP
            System.out.println(myName + " [STATE C] received ACK");

            String nameExpediteur = msgACKMapReceived.getSender().getLocalName(); // on récupère l'envoyeur du message (chaine de caractères)

            // update de l'action "recoit_ACK"
            ((FSMAgent) this.myAgent).setDictVoisinsMessagesAgentAction(nameExpediteur, "recoit_ACK", true);
        } else {
            timerACK++;
        }

        // 3) ACTION : Check si l'agent a reçu une carte de ses voisins
        MessageTemplate msgMap = MessageTemplate.and(
                MessageTemplate.MatchProtocol("SHARE-MAP"),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));

        ACLMessage msgMapReceived = this.myAgent.receive(msgMap);

        if (msgMapReceived != null && exitValue == -1) { // si l'agent a reçu une MAP et il n'a rien à faire
            System.out.println(myName + " [STATE C] received MAP");

            String nameExpediteur = msgMapReceived.getSender().getLocalName();

            SerializableSimpleGraph<String, HashMap<String, Object>> mapReceived = null;
            SerializableSimpleGraph<String, HashMap<String, Object>> allInformation = null;

            try {
                allInformation = (SerializableSimpleGraph<String, HashMap<String, Object>>) msgMapReceived.getContentObject();
                mapReceived = allInformation; // pour l'instant, on n'a qu'une carte, mais après on pourra envoyer d'autres informations
            } catch (UnreadableException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            assert mapReceived != null;
            //this.myFullMap.mergeMap(mapReceived);
            HashMap<String, List<Couple<Observation,Integer>>> dictBackPack = this.myFullMap.mergeMapDict(mapReceived);
            //System.out.println(myName + " [STATE C] -- AVANT MERGE DICT : " + myName + " -- TYPE : " + ((AbstractDedaleAgent) this.myAgent).getMyTreasureType() );
            System.out.println(myName + " [STATE C] -- AVANT MERGE DICT : " + ((FSMAgent) this.myAgent).getDictBackpack() + " -- ||-- " + dictBackPack);
            ((FSMAgent)this.myAgent).updateDictBackPack(dictBackPack);
            //System.out.println(myName + " [STATE C] -- APRES MERGE DICT : " + myName + " -- TYPE : " + ((AbstractDedaleAgent) this.myAgent).getMyTreasureType() );
            System.out.println(myName + " [STATE C] -- APRES MERGE DICT : " + ((FSMAgent) this.myAgent).getDictBackpack() + " -- ||-- " + dictBackPack);

            // update de l'action "recoit_MAP"
            ((FSMAgent) this.myAgent).setDictVoisinsMessagesAgentAction(nameExpediteur, "recoit_MAP", true);

            exitValue = 2; // aller en D : "Envoie ACK"
            System.out.println(myName + " CHANGES C to D: send ACK");
        } else {
            timerMAP++;
        }

        // 4) Vérification si l'agent a fait toutes les actions

        // on récupère toutes les clés (les noms des voisins)
        Set<String> voisins = this.dictVoisinsMessages.keySet();
        int nb_voisins = voisins.size();

        int cptACKreceived = 0;
        int cptACKsend = 0;
        int cptMAPreceived = 0;
        int cptMAPsend = 0;

        for (String nameNeighbor : voisins) {
            HashMap<String, Boolean> etat = this.dictVoisinsMessages.get(nameNeighbor); // dico des actions de l'agent par rapport à son voisin nameNeighbor

            // vérification de la réception de l'ACK pour nameNeighbor
            if (etat.get("recoit_ACK")) {
                cptACKreceived++;
            }

            // vérification de l'envoie de l'ACK pour nameNeighbor
            if (etat.get("envoie_ACK")) {
                cptACKsend++;
            }

            // vérification de la réception de la MAP pour nameNeighbor
            if (etat.get("recoit_MAP")) {
                cptMAPreceived++;
            }

            // vérification de l'envoie de la MAP pour nameNeighbor
            if (etat.get("envoie_MAP")) {
                cptMAPsend++;
            }
        }

        if (exitValue == -1) { // si l'agent n'a rien à faire
            if (cptACKreceived == nb_voisins && cptACKsend == nb_voisins && cptMAPreceived == nb_voisins && cptMAPsend == nb_voisins) {
                exitValue = 3; // aller en A : l'agent continue l'exploration
                System.out.println(myName + " CHANGES C to A: continue exploration");

            } else if (this.timerACK == this.timerMax) {
                System.out.println(myName + " [STATE C] - TIMER ACK END");
                exitValue = 4; // aller en B : renvoyer sa carte
                System.out.println(myName + " CHANGES C to B: re-sending MAP");
            }
            else if (this.timerMAP >= this.timerMax) {
                System.out.println(myName + " [STATE C] - TIMER MAP END");
                exitValue = 3; // aller en A : l'agent continue l'exploration
                this.timerMAP = 0;
                this.timerACK = 0;
                ((FSMAgent) this.myAgent).resetDictVoisinsMessages();
                System.out.println(myName + " CHANGES C to A: continue exploration");
            }
            else { // sinon il existe un agent dont on n'a pas reçu de ACK, alors on reste au state C pour attendre son ACK
                exitValue = 1; // rester en C
                System.out.println(myName + " STAYS in C -- timerACK: " + this.timerACK + " | timerMAP: " + this.timerMAP);
            }
        }

        System.out.println("STATE C : " + myName + ", EXIT VALUE: " + exitValue);
    }

    public int onEnd() {
        return exitValue;
    }
}