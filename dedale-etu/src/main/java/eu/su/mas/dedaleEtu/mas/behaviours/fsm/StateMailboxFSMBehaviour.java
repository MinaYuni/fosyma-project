package eu.su.mas.dedaleEtu.mas.behaviours.fsm;
import jade.core.behaviours.OneShotBehaviour;

import java.util.*;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import dataStructures.serializableGraph.SerializableSimpleGraph;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;


// jai modifier list_voisins qui est devenu dict_voisins (voir FSMAgent.java)
// (car dans StateMailboxFSMBehaviours.java (c'est state C), dans message ACK-MAP (vers les ligne 65), jai besoin de connaitre les etats de l'agent par rapport au Receveur)
// jai fini d'adapter les changements de dico_voisin pour ce behviours


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
		// 1) ACTION : Check si l'agent a reçu une carte de ses voisins 
		MessageTemplate msgMap = MessageTemplate.and(
				MessageTemplate.MatchProtocol("SHARE-MAP"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		
		ACLMessage msgMapReceived = this.myAgent.receive(msgMap);
		
		if (msgMapReceived != null) {
			SerializableSimpleGraph<String, MapAttribute> mapReceived = null;
			SerializableSimpleGraph<String, MapAttribute> allInformation = null;
			try {
				allInformation = (SerializableSimpleGraph<String, MapAttribute>) msgMapReceived.getContentObject();
			} catch (UnreadableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.myMap.mergeMap(mapReceived);

			// MAJ dict_voisins : on change etat "" de l'agent par rapport a Expediteur 
			String nameExpediteur = msgMapReceived.getContent(); //au state B, on a mis le nom dans message avec 'setContent'

			HashMap <String, Boolean> etat = this.dictVoisinsMessages.get(nameExpediteur); //dico des actions de l'agent par rapport a Expediteur
			String key = "recoit_carte";
			
			etat.put(key,true); //met VRAI pour action "recoit_carte2" (elle cree la cle avec value=TRUE ou update la value a TRUE)
			

			exitValue = 2; // aller en D : "Envoie ACK"
		}
		
		// 2) ACTION : check si l'agent a reçu un ACK (de la carte qu'il a envoyé) de ses voisins 
		MessageTemplate msgACK = MessageTemplate.and(
				MessageTemplate.MatchProtocol("ACK-MAP"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		
		ACLMessage msgACKMapReceived = this.myAgent.receive(msgACK);
		
		if (msgACKMapReceived != null) { //on a recu un ACK-MAP
			// Garde en memoire ACK recu
			// MAJ dict_voisins : on change etat "recoit_ACK1" de l'agent par rapport a Expediteur => recupere le dico etat puis mettre a TRUE avec key : "recoit_ACK1" 
			
			// Remarque : pour l'instant, Content contient que un String
			// car on a envoye QUE le nom de l'expediteur dans le message (voir state D, fichier pas encore creer)
			String nameExpediteur = msgACKMapReceived.getContent(); //retourne une chaine de caractere
			
			//recupere le dico etat
			HashMap<String, Boolean> etat = this.dictVoisinsMessages.get(nameExpediteur); // cest le dico des actions de agent par rapport a Expediteur
			 
			String key = "recoit_ACK";
			etat.put(key, true); //met VRAI pour action "recoit_carte2" (elle cree la cle avec value=TRUE ou update la value a TRUE)
			//FIN MAJ dict_voisins
		}


		// 3) ACTION : Check si l'agent  a reçu une carte de ses voisins
		// msgMap : MessageTemplate
		msgMap = MessageTemplate.and(
				MessageTemplate.MatchProtocol("SHARE-MAP"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		//msgMapReceived : ACLMessage
		msgMapReceived = this.myAgent.receive(msgMap);
		
		if (msgMapReceived != null) {
			SerializableSimpleGraph<String, MapAttribute> mapReceived=null;
			SerializableSimpleGraph<String, MapAttribute> allInformation = null;
			try {
				allInformation = (SerializableSimpleGraph<String, MapAttribute>) msgMapReceived.getContentObject();
			} catch (UnreadableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.myMap.mergeMap(mapReceived);

			// MAJ dict_voisins : on change etat "" de l'agent par rapport a Expediteur 
			String nameExpediteur = msgMapReceived.getContent(); //au state B, on a mis le nom dans message avec 'setContent'

			HashMap<String, Boolean> etat = this.dictVoisinsMessages.get(nameExpediteur); //dico des actions de l'agent par rapport a Expediteur
			String key = "recoit_carte";
			
			etat.put(key,true); //met VRAI pour action "recoit_carte2" (elle cree la cle avec value=TRUE ou update la value a TRUE)
			

			exitValue = 2; // aller en D : "Envoie ACK"
		}


		// 4) Verifie si on a eu tous les ACK de la carte envoye par l'agent
		String key = "recoit_ACK";
		Boolean haveAllACK = true;
		Set<String> setOfKeys = this.dictVoisinsMessages.keySet(); // recueere tous les cles donc tous les noms des voisins
        for(String nameNeighbor: setOfKeys){	
			HashMap<String, Boolean> etat = this.dictVoisinsMessages.get(nameNeighbor) ;//dico des actions de l'agent par rapport a son voisin nameNeighbor

			if (! etat.get(key)){ //pas recu de ACK venant de l'agent nameNeighbor
				haveAllACK = false	;
				break;
			}
		}

		if (haveAllACK) {
			exitValue = 3; // aller en A : "Exploration" (agent a recu tous les ACK donc continue son exploration)
		}else{
			exitValue = 1; // reste en C, car on a pas recu tous les ACK
		}
	}
}