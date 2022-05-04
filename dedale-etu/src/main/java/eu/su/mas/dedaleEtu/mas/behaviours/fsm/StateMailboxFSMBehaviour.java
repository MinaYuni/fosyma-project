package eu.su.mas.dedaleEtu.mas.behaviours.fsm;

import jade.core.behaviours.OneShotBehaviour;
import eu.su.mas.dedaleEtu.mas.agents.fsm.FSMAgent;

import java.util.*;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import dataStructures.serializableGraph.SerializableSimpleGraph;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;


//Behaviours/comportement au state C
public class StateMailboxFSMBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = 3567689731496787661L;
    private final int timerMax = 10; // temps max d'attente
    private HashMap<String, HashMap<String, Boolean>> dictVoisinsMessages;
    private MapRepresentation MyMap;
    private int timerACK = 0;
    private int timerMAP = 0;
    private int exitValue;

    public StateMailboxFSMBehaviour(final AbstractDedaleAgent myagent) {
        super(myagent);
    }

    public void action() {
        exitValue = -1;
        String myName = this.myAgent.getLocalName();

        System.out.println("\n-- START state C (StateMailboxFSMBehaviour): " + myName + " starts state C --");

		// update information
		if (this.myMap==null){
			this.myMap = ((FSMAgent)this.myAgent).getMyMap();
		}
		this.dictVoisinsMessages = ((FSMAgent)this.myAgent).getDictVoisinsMessages();

        // 1) ACTION : Check si l'agent a reçu un ping d'un nouveau voisin
        MessageTemplate msgPing = MessageTemplate.and(
                MessageTemplate.MatchProtocol("PING"),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));

        ACLMessage msgPingReceived = this.myAgent.receive(msgPing);

        if (msgPingReceived != null) {
            System.out.println("STATE C : " + myName + " received PING");

            String namePingReceived = msgPingReceived.getSender().getLocalName();

            ((FSMAgent) this.myAgent).setDictVoisinsMessagesAgentAction(namePingReceived, "recoit_PING", true);

            exitValue = 4; // aller en B : "Envoie MAP"
            System.out.println("-CHANGE C to B (StateSendMapFSMBehaviour): " + myName + " goes to state B ");
        }

        // 2) ACTION : check si l'agent a reçu un ACK de ses voisins pour la carte qu'il a envoyé
        MessageTemplate msgACK = MessageTemplate.and(
                MessageTemplate.MatchProtocol("ACK-MAP"),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));

        ACLMessage msgACKMapReceived = this.myAgent.receive(msgACK);

        if (msgACKMapReceived != null) { // si l'agent a reçu un ACK-MAP
            System.out.println("STATE C : " + myName + " received ACK");

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
            System.out.println("STATE C : " + myName + " received MAP");

            String nameExpediteur = msgMapReceived.getSender().getLocalName();

            SerializableSimpleGraph<String, MapAttribute> mapReceived = null;
            SerializableSimpleGraph<String, MapAttribute> allInformation = null;

            try {
                allInformation = (SerializableSimpleGraph<String, MapAttribute>) msgMapReceived.getContentObject();
                mapReceived = allInformation; // pour l'instant, on n'a qu'une carte, mais après on pourra envoyer d'autres informations
            } catch (UnreadableException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            assert mapReceived != null;
            this.MyMap.mergeMap(mapReceived);

            // update de l'action "recoit_MAP"
            ((FSMAgent) this.myAgent).setDictVoisinsMessagesAgentAction(nameExpediteur, "recoit_MAP", true);

            exitValue = 2; // aller en D : "Envoie ACK"
            System.out.println("-CHANGE C to D (StateSendACKFSMBehaviour): " + myName + " goes to state D ");
        } else {
            timerMAP++;
        }

        // on récupère toutes les clés (les noms des voisins)
        Set<String> voisins = this.dictVoisinsMessages.keySet();

        // 4) Vérification si l'agent a fait toutes les actions
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
                System.out.println("-CHANGE C to A (StateExploFSMBehaviour): " + myName + " goes to state A");

            } else if (this.timerACK >= this.timerMax) {
                System.out.println("STATE C: " + myName + " TIMER ACK END");
                exitValue = 4; // aller en B : renvoyer sa carte
                this.timerACK = 0;
                System.out.println("-CHANGE C to B (StateExploFSMBehaviour): " + myName + " goes to state B");
            }
            else if (this.timerMAP >= this.timerMax) {
                System.out.println("STATE C: " + myName + " TIMER MAP END");
                exitValue = 3; // aller en A : l'agent continue l'exploration
                this.timerMAP = 0;
                ((FSMAgent) this.myAgent).resetDictVoisinsMessages();
                System.out.println("-CHANGE C to A (StateExploFSMBehaviour): " + myName + " goes to state A");
            }
            else { // sinon il existe un agent dont on n'a pas reçu de ACK, alors on reste au state C pour attendre son ACK
                exitValue = 1; // rester en C
                System.out.println("-STAY in state C (StateMailboxFSMBehaviour): " + myName + " reminds in state C | timerACK: " + this.timerACK + " -- timerMAP: " + this.timerMAP);
            }
        }

        System.out.println("STATE C : " + myName + ", EXIT VALUE: " + exitValue);
    }

    public int onEnd() {
        return exitValue;
    }
}