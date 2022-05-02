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
	private static final long serialVersionUID = 8567689731499797661L;

	private MapRepresentation myMap;
	private List<String> list_agentNames;
	private HashMap<String, HashMap<String, Boolean>> dictVoisinsMessages;
	private int exitValue;
	
	public StateSendMapFSMBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap, List<String> agentNames, HashMap<String, HashMap<String, Boolean>> dico) {
		super(myagent);
		this.myMap=myMap;
		this.list_agentNames=agentNames;
		this.dictVoisinsMessages = dico;
	}
	
	public void action() {
		System.out.println("BEGIN : StateSendMapFSMBehaviour (state B), " + this.myAgent.getLocalName()+" - Begin send MAP ");

		//int nb_voisins = this.myAgent.list_voisins.size();
		String myName = this.myAgent.getLocalName();

		if (this.myMap==null){
			this.myMap = ((FSMAgent)this.myAgent).getMyMap();
		}

		// ACTION : Envoyer sa carte à tous ses voisins
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("SHARE-MAP");
		msg.setContent(myName); // met son nom dans le message envoyé 
		msg.setSender(this.myAgent.getAID());	//mettre un expediteur
		
		// ajouter les noms des destinataires (ici les noms des voisins) du message
		Set<String> setOfKeys = this.dictVoisinsMessages.keySet(); // recuperer tous les cles donc tous les noms des voisins
        for(String receiverAgent: setOfKeys){
			msg.addReceiver(new AID(receiverAgent,false));
			System.out.println("STATE B " + this.myAgent.getLocalName()+" send MAP to "+ receiverAgent );

		}
		//ajout de la carte de Agent dans le message
		//SerializableSimpleGraph<String, MapAttribute> mapSent=(((FSMAgent)this.myAgent).getMyMap()).getSerializableGraph();
		SerializableSimpleGraph<String, MapAttribute> mapSent=(this.myMap).getSerializableGraph();

		try {
			msg.setContentObject(mapSent);
		} catch (IOException e) {
			e.printStackTrace();
		}

		//envoie en cours de la carte a tous les voisins
		((AbstractDedaleAgent)this.myAgent).sendMessage(msg);

		exitValue = 1; //  aller en C = "Attente ACK et check Mailbox"
		System.out.println("END : StateSendMapFSMBehaviour (state B), " + this.myAgent.getLocalName()+" - Finish to send MAP, go state C ");
	}
	
	public int onEnd() {return exitValue;}
}