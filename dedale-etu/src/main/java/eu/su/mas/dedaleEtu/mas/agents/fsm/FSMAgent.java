package eu.su.mas.dedaleEtu.mas.agents.fsm;

import java.util.ArrayList;
import java.util.List;
import java.util.Dictionary;
import java.util.Hashtable;

import eu.su.mas.dedaleEtu.mas.behaviours.fsm.StateFSMBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.fsm.StateFSMBehaviourStartEnd;
import eu.su.mas.dedaleEtu.mas.behaviours.fsm.StateExploFSMBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.fsm.StateSendMapFSMBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.fsm.StateMailboxFSMBehaviour;
//import eu.su.mas.dedaleEtu.mas.behaviours.SendPingBehaviour;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;

import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;

public class FSMAgent extends AbstractDedaleAgent {
	
	private static final long serialVersionUID = -6431752865590433727L;
	private MapRepresentation myMap;
	List<String> list_agentNames = new ArrayList<String>(); 
	//List<String> list_voisins = new ArrayList<String>();
	
	// Repartition des ressources de manière équitable : 
	// max du min 
	// indice de Gini 

	// dict_voisins est un dictionnaire sur les états des messages envoyés à chaque agent :
	// key (String) : noms des voisins  
	// value (dict) : dico des états des messages (type du message envoyé/reçu, son état fait ou non)  
	// 		dictionnaire etat_msg permet de vérfier quels sont les messages qui a été envoyé ou pas 
	// 		key (String) : messagé envoyé ou reçu  
	// 			=> exemple : "recepition_carte", "envoie_carte", "reception_ACK", "envoie_ACK"
	// 		value (bool) : si on a fait les actions correspondant à la clef

	Hashtable<String, Hashtable<String, Boolean>> dict_voisins_messages = new Hashtable<String,  Hashtable<String, Boolean> >();
	
	// dictionnaire pour garder en mémoire les cartes qui a été envoyé aux autres agents
	Dictionary<String, MapRepresentation> dict_map_envoye = new Hashtable<String, MapRepresentation >();
			
	private static final String A = "Exploration en cours"; 
	private static final String B = "Envoie carte"; 
	private static final String C = "Check Mailbox"; 
	private static final String D = "Envoie ACK"; 
	private static final String F = "Exploration finie"; 
	
	// A (exploration): à chaque déplacement, envoie PING + check boite aux lettres 
	//		si reception "ping" --> B (arc 1) 
	//		si reception carte --> B (arc 1)  
	//		si exploration finie --> F (arc 2)
	//		sinon A
	
	// B: envoie de la partie de la carte manquante à son voisin (celui du "pong" reçu)
	//		--> C (arc 1)
	
	// C: check boite aux lettres (ACK, carte, nouveau voisin) 
	//		if reception nouveau "pong" --> B (arc 4)
	// 		if reception carte d'un autre agent :
	//				then --> D (arc 2)
	// 		if reception (nouveau) ACK de la carte envoyé :
	//				garde en mémoire du ACK reçu
	// 				if pas reception nouvelle carte --> C (arc 1)
	//				if reception carte --> D (arc 2)
	//		if reception de tous les ACK (mémoire) --> A (arc 3)
	//		else --> C (arc 1)
	
	// D: envoie ACK de la carte qu'il a reçu 
	//		--> C (arc 1)
	protected void setup() {
		// FMS behaviour
		FSMBehaviour fsm = new FSMBehaviour(this);
	
		// Define the different states and behaviours
		fsm. registerFirstState (new StateExploFSMBehaviour(this,this.myMap,list_agentNames), A);
		fsm. registerState (new StateSendMapFSMBehaviour(this,this.myMap,list_agentNames), B);
		fsm. registerState (new StateMailboxFSMBehaviour(this,this.myMap,list_agentNames), C);
		fsm. registerState (new StateFSMBehaviour(5), D);
		fsm. registerLastState (new StateExploFSMBehaviour(this, myMap, list_agentNames), F);
		
		// Register the transitions
		fsm. registerDefaultTransition (A,A); //Default
		fsm. registerTransition (A,B, 1) ; 
		fsm. registerTransition (A,F, 2) ; 
		fsm. registerTransition (B,C, 1) ; 
		fsm. registerTransition (C,D, 2) ; 
		fsm. registerTransition (C,C, 1) ; 
		fsm. registerTransition (C,A, 3) ; 
		fsm. registerTransition (C,B, 4) ;
		fsm. registerTransition (D,C, 1) ; 
		
		this.addBehaviour(fsm);
		// this.addBehaviour(tbf.wrap(envoiePing));
		
		System.out.println("the  agent "+this.getLocalName()+ " is started");
	}


	// ------------------- Methode get et set --------------------- //
	public Hashtable<String, Hashtable<String, Boolean>> get_dict_voisins_messages(){
		// Return the dictionary with agent (key) and dictionary_aciton (value)
		// Retourne le dictionnaire dont (clé = agent) et (value = dico_action)
		return this.dict_voisins_messages;
	}

	public void set_dict_voisins_messages(Hashtable<String, Hashtable<String, Boolean>> dico){
		// Replace the dictionary
		// Remplace le dictionnaire dont (clé = agent) et (value = dico_action) par dico
		this.dict_voisins_messages = dico;
	}

	public Hashtable<String, Boolean> get_dict_voisins_messages_agent(String agent){
		// Return the agent's dictionary_action with action (key) and bool (value)
		// Retourne le dictionnaire d'un agent agent dont (clé = action) et (value = bool)
		return this.dict_voisins_messages.get(agent);
	}

	public void set_dict_voisins_messages_agent(String agent, Hashtable<String, Boolean> dico){
		// Replace the dictionary
		// Remplace le dictionnaire d'un agent dont (clé = agent) et (value = dico_action) par dico
		this.dict_voisins_messages.get(agent) = dico;
	}

	public Hashtable<String, Boolean> get_dict_voisins_messages_agent(String agent, String action){
		// Retourne la valeur booleen du dictionnaire d'un agent de l'action action
		return this.dict_voisins_messages.get(agent).get(action);
	}

	public void set_dict_voisins_messages_agent(String agent, String action, Boolean bool){
		// Remplace la valeur booleen du dictionnaire d'un agent a l'action action par bool
		this.dict_voisins_messages.get(agent).get(action) = bool;
	}


}