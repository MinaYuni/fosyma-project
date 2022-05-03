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

	private MapRepresentation myMap;
	private List<String> list_agentNames;
	private HashMap<String, HashMap<String, Boolean>> dictVoisinsMessages;
	private int exitValue;
	
	public StateMailboxFSMBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap, List<String> agentNames, HashMap<String, HashMap<String, Boolean>> dico ) {
		super(myagent);
		this.myMap=myMap;
		this.list_agentNames=agentNames;
		this.dictVoisinsMessages = dico;
	}
	
	public void action() {
		exitValue = -1;
		int nb_agents = this.list_agentNames.size();
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
			exitValue = 4; // aller en B : "Envoie MAP"
			System.out.println("-CHANGE C to B (StateSendMapFSMBehaviour): " + myName + " goes to state B ");
		}
		
		// 2) ACTION : check si l'agent a reçu un ACK (de la carte qu'il a envoyé) de ses voisins 
		MessageTemplate msgACK = MessageTemplate.and(
				MessageTemplate.MatchProtocol("ACK-MAP"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		
		ACLMessage msgACKMapReceived = this.myAgent.receive(msgACK);
		
		if (msgACKMapReceived != null && exitValue == -1) { // si l'agent a reçu un ACK-MAP et il n'a rien à faire
			System.out.println("STATE C : " + myName + " received ACK");

			// Garde en memoire du ACK reçu
			// MAJ dict_voisins : on change etat "recoit_ACK1" de l'agent par rapport a Expediteur
			// 		=> on recupère le dico etat, puis on le met à TRUE avec key : "recoit_ACK"

			String nameExpediteur = msgACKMapReceived.getSender().getLocalName(); // on récupère l'envoyeur du message (chaine de caractères)
			HashMap<String, Boolean> etat = this.dictVoisinsMessages.get(nameExpediteur); // dico des actions de agent par rapport à l'expéditeur
			String key = "recoit_ACK";
			etat.put(key, true); // on met VRAI pour l'action "recoit_carte" (elle crée la clé avec value=TRUE ou update la value à TRUE)

			// on a modifié le dico etat => utiliser la methode 'setDictVoisinsMessagesAgent' pour udapte !
			((FSMAgent)this.myAgent).setDictVoisinsMessagesAgent(nameExpediteur, etat);
		}

		// 3) ACTION : Check si l'agent a reçu une carte de ses voisins
		MessageTemplate msgMap = MessageTemplate.and(
				MessageTemplate.MatchProtocol("SHARE-MAP"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));

		ACLMessage msgMapReceived = this.myAgent.receive(msgMap);

		if (msgMapReceived != null && exitValue == -1) { // si l'agent a reçu une MAP et il n'a rien à faire
			System.out.println("STATE C : " + myName + " received MAP");

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
			this.myMap.mergeMap(mapReceived);

			// MAJ dict_voisins : on change etat "recoit_carte" de l'agent par rapport à l'expéditeur
			String nameExpediteur = msgMapReceived.getSender().getLocalName(); //au state B, on a mis le nom dans message avec 'setContent'
			HashMap <String, Boolean> etat = this.dictVoisinsMessages.get(nameExpediteur); //dico des actions de l'agent par rapport a Expediteur
			String key = "recoit_carte";
			etat.put(key,true); //met VRAI pour action "recoit_carte" (elle cree la cle avec value=TRUE ou update la value a TRUE)

			// on a modifié le dico etat => utilise la methode 'setDictVoisinsMessagesAgent' pour udapte !
			((FSMAgent)this.myAgent).setDictVoisinsMessagesAgent(nameExpediteur, etat);

			exitValue = 2; // aller en D : "Envoie ACK"
			System.out.println("-CHANGE C to D (StateSendACKFSMBehaviour): " + myName + " goes to state D ");
		}

		// 4) Vérification si l'agent a reçu tous les ACK de la carte qu'il a envoyé
		String key = "recoit_ACK";
		boolean haveAllACK = false; // on part du principe qu'il a reçu 0 ACK (mais du coup il faudra ajouter un timer sinon il risque d'attendre à l'infini)
		Set<String> setOfKeys = this.dictVoisinsMessages.keySet(); // on recupère toutes les clés (les noms des voisins)

        for(String nameNeighbor: setOfKeys){	
			HashMap<String, Boolean> etat = this.dictVoisinsMessages.get(nameNeighbor); //dico des actions de l'agent par rapport a son voisin nameNeighbor
			//System.out.println("STATE C (step 4): " + this.myAgent.getLocalName()+", etat: " + etat.get(key));

			if (etat.get(key) != null) { // peut être null si l'agent n'a jamais envoyé de ACK
				if (! etat.get(key)){ // il n'a pas reçu de ACK venant de l'agent nameNeighbor
					haveAllACK = false; // donc il existe un agent dont on n'a pas reçu de ACK
					break;
				} else {
					haveAllACK = true;
				}
			}
		}

		if (exitValue == -1) { // si l'agent n'a rien à faire
			if (haveAllACK) {  // si l'agent a reçu tous les ACK, alors il va en state A
				exitValue = 3; // aller en A : "Exploration" (l'agent a reçu tous les ACK donc il continue son exploration)
				System.out.println("-CHANGE C to A (StateExploFSMBehaviour): " + myName + " goes to state A");

			} else { // sinon il existe un agent dont on n'a pas reçu de ACK, alors on reste au state C pour attendre son ACK
				exitValue = 1; // rester en C
				System.out.println("-STAY in state C (StateMailboxFSMBehaviour): " + myName + " reminds in state C");
			}
		}

		System.out.println("STATE C : " + myName + ", EXIT VALUE: " + exitValue);
	}

	public int onEnd() {
		return exitValue;
	}
}