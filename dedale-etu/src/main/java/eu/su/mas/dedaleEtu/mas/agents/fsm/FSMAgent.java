package eu.su.mas.dedaleEtu.mas.agents.fsm;

import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedaleEtu.mas.behaviours.fsm.StateFSMBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.fsm.StateFSMBehaviourStartEnd;
import eu.su.mas.dedaleEtu.mas.behaviours.fsm.StateExploFSMBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.fsm.StateSendMapFSMBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.fsm.StateMailboxFSMBehaviour;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;

import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;

public class FSMAgent extends AbstractDedaleAgent {
	
	private static final long serialVersionUID = -6431752865590433727L;
	private MapRepresentation myMap;
	List<String> list_agentNames = new ArrayList<String>(); 
	//List<String> list_voisins = new ArrayList<String>();
	
	// le dictionnaire etat_agent nous informe sur les etats de l'agent par rapport aux autres agents,
	// le dico possede :
	//		une cle de type String 
	// 			=> exemple de cle possible : "envoie_carte1", "recoit_ACK1", "recoit_carte2", "envoie_ACK2"
	//		une valeur booleenne, si oui ou non, on a fait les actions (cle correspond a des actions)
	Dictionary<String, bool> etat_agent = new Hashtable<String, bool>() 
	//cette elem va representer toutes les etats de l'agent par rapport a un autre agent 
	// ==> Cest pr garder en memoire : ACK recu, etc... (voir description state C, ou fichier StateMailboxFSMBehaviour.java)

	// list_voisin est un dico avec key nom_agent(type String) et value dico_etat
	Dictionary<String, Dictionary<String, bool>> > dict_voisins = new Hashtable<String, etat_agent>() 
			
	private static final String A = "Exploration en cours"; 
	private static final String B = "Envoie carte"; 
	private static final String C = "Attente ACK et check Mailbox"; 
	private static final String D = "Envoie ACK"; 
	private static final String F = "Exploration finie"; 
	// A (exploration) : bouger d'un noeud, puis envoyer un "ping", puis check boite aux lettres 
	//		si reception "pong" --> B (arc 1)
	//		si exploration finie --> F (arc 2)
	//		sinon A
	
	// B: récupérer la liste des voisins, puis envoie de la partie de la carte manquante à chaque voisin 
	//		--> C (arc 1)
	
	// C: attente ACK de la carte envoyé et check boite aux lettres pour reception carte
	//		if check reception carte d'un autre agent :
	//				then --> D (arc 2)
	// 		if check reception (nouveau) ACK de la carte envoyé :
	//				garde en mémoire du ACK reçu
	// 				if pas reception nouvelle carte --> C (arc 1)
	//				if reception carte --> D (arc 2)
	//		if reception de tous les ACK (mémoire) --> A (arc 3)
	//		else --> C (arc 1)
	
	// D: envoie ACK de la carte qu'il a reçu 
	//		--> C (arc 1)
	
	
	protected void setup() {
		FSMBehaviour fsm = new FSMBehaviour(this);
		// Define the different states and behaviours
		fsm. registerFirstState (new StateExploFSMBehaviour(this,this.myMap,list_agentNames), A);
		fsm. registerState (new StateSendMapFSMBehaviour(this,this.myMap,list_agentNames), B);
		fsm. registerState (new StateMailboxFSMBehaviour(this,this.myMap,list_agentNames), C);
		fsm. registerState (new StateFSMBehaviour(5), D);
		fsm. registerLastState (new StateExploFSMBehaviour(), F);
		
		// Register the transitions
		fsm. registerDefaultTransition (A,A);//Default
		fsm. registerTransition (A,B, 1) ; 
		fsm. registerTransition (A,F, 2) ; 
		fsm. registerTransition (B,C, 1) ; 
		fsm. registerTransition (C,D, 2) ; 
		fsm. registerTransition (C,C, 1) ; 
		fsm. registerTransition (C,A, 3) ; 
		fsm. registerTransition (D,C, 1) ; 
		
		addBehaviour(fsm);
		
		System.out.println("the  agent "+this.getLocalName()+ " is started");
	}
}