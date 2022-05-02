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
	private static final long serialVersionUID = 8567689731499797661L;

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
		System.out.println("START state C (StateMailboxFSMBehaviour): " + this.myAgent.getLocalName()+" starts state C ");

		if (this.myMap==null){
			this.myMap = ((FSMAgent)this.myAgent).getMyMap();
		}
		this.dictVoisinsMessages = ((FSMAgent)this.myAgent).getDictVoisinsMessages();


		// 1) ACTION : Check si l'agent a reçu un ping d'un nouveau voisin
		MessageTemplate msgPing = MessageTemplate.and(
				MessageTemplate.MatchProtocol("PING"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		
		ACLMessage msgReceived = this.myAgent.receive(msgPing);
		
		if (msgReceived != null) {
			System.out.println("STATE C : " + this.myAgent.getLocalName()+" received PING");
			exitValue = 4; // aller en B : "Envoie ACK"
			System.out.println("CHANGE C to B (StateSendMapFSMBehaviour): " + this.myAgent.getLocalName()+" goes to state B ");
		}
		
		// 2) ACTION : check si l'agent a reçu un ACK (de la carte qu'il a envoyé) de ses voisins 
		MessageTemplate msgACK = MessageTemplate.and(
				MessageTemplate.MatchProtocol("ACK-MAP"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		
		ACLMessage msgACKMapReceived = this.myAgent.receive(msgACK);
		
		if (msgACKMapReceived != null) { //on a recu un ACK-MAP
			System.out.println("STATE C : " + this.myAgent.getLocalName()+" received ACK");

			// Garde en memoire ACK recu
			// MAJ dict_voisins : on change etat "recoit_ACK1" de l'agent par rapport a Expediteur => recupere le dico etat puis mettre a TRUE avec key : "recoit_ACK1" 
			
			// Remarque : pour l'instant, Content contient que un String
			// car on a envoye QUE le nom de l'expediteur dans le message (voir state D, fichier pas encore creer)
			String nameExpediteur = msgACKMapReceived.getSender().getLocalName(); //retourne une chaine de caractere
			
			//recupere le dico etat
			HashMap<String, Boolean> etat = this.dictVoisinsMessages.get(nameExpediteur); // cest le dico des actions de agent par rapport a Expediteur
			 
			String key = "recoit_ACK";
			etat.put(key, true); //met VRAI pour action "recoit_carte2" (elle cree la cle avec value=TRUE ou update la value a TRUE)
			//on a modifier le dico etat => utilise la methode 'setDictVoisinsMessagesAgent' pour udapte !
			((FSMAgent)this.myAgent).setDictVoisinsMessagesAgent(nameExpediteur, etat);

			//FIN MAJ dict_voisins
		}

		// 3) ACTION : Check si l'agent a reçu une carte de ses voisins
		MessageTemplate msgMap = MessageTemplate.and(
				MessageTemplate.MatchProtocol("SHARE-MAP"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));

		ACLMessage msgMapReceived = this.myAgent.receive(msgMap);

		if (msgMapReceived != null) {
			System.out.println("STATE C : " + this.myAgent.getLocalName()+" received MAP");

			SerializableSimpleGraph<String, MapAttribute> mapReceived = null;
			SerializableSimpleGraph<String, MapAttribute> allInformation = null;
			try {
				allInformation = (SerializableSimpleGraph<String, MapAttribute>) msgMapReceived.getContentObject();
				mapReceived = allInformation; //Pour l'instant, on n'a qu'une carte, mais apres on pourra envoyer d'autre information
			} catch (UnreadableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			assert mapReceived != null;
			this.myMap.mergeMap(mapReceived);

			// MAJ dict_voisins : on change etat "recoit_carte" de l'agent par rapport a Expediteur
			String nameExpediteur = msgMapReceived.getSender().getLocalName(); //au state B, on a mis le nom dans message avec 'setContent'

			HashMap <String, Boolean> etat = this.dictVoisinsMessages.get(nameExpediteur); //dico des actions de l'agent par rapport a Expediteur
			String key = "recoit_carte";

			etat.put(key,true); //met VRAI pour action "recoit_carte" (elle cree la cle avec value=TRUE ou update la value a TRUE)

			//on a modifier le dico etat => utilise la methode 'setDictVoisinsMessagesAgent' pour udapte !
			((FSMAgent)this.myAgent).setDictVoisinsMessagesAgent(nameExpediteur, etat);

			exitValue = 2; // aller en D : "Envoie ACK"
			System.out.println("CHANGE C to D (StateSendACKFSMBehaviour): " + this.myAgent.getLocalName()+" goes to state D ");
		}

		// 4) Verifie si on a eu tous les ACK de la carte envoye par l'agent
		String key = "recoit_ACK";
		boolean haveAllACK = false; //on part du principe qu'il a recu 0 ACK (mais du coup il faudra ajouter un timer sinon il attendra à l'infini)
		Set<String> setOfKeys = this.dictVoisinsMessages.keySet(); // recupere toutes les cles donc tous les noms des voisins
        for(String nameNeighbor: setOfKeys){	
			HashMap<String, Boolean> etat = this.dictVoisinsMessages.get(nameNeighbor); //dico des actions de l'agent par rapport a son voisin nameNeighbor

			if (! etat.get(key)){ //pas recu de ACK venant de l'agent nameNeighbor
				// il existe un agent dont on n'a pas recu de ACK
				haveAllACK = false;
				break;
			} else {
				haveAllACK = true;
			}
		}

		if (haveAllACK) { //on a recu tous les ACK => on va en state A
			exitValue = 3; // aller en A : "Exploration" (agent a recu tous les ACK donc continue son exploration)
			System.out.println("CHANGE C to A (StateExploFSMBehaviour): " + this.myAgent.getLocalName()+" goes to state A");

		} else { //il existe un agent dont on n'a pas recu de ACK => on reste au state C
			exitValue = 1; // reste en C, car on a pas recu tous les ACK
			System.out.println("STAY in state C (StateMailboxFSMBehaviour): " + this.myAgent.getLocalName()+" reminds in state C");

		}
	}
}