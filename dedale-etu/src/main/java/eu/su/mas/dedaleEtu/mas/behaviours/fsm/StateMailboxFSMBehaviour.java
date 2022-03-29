package eu.su.mas.dedaleEtu.mas.behaviours.fsm;
import jade.core.behaviours.OneShotBehaviour;

import java.io.IOException;
import java.lang.Math;
import java.util.Iterator;
import java.util.List;

import eu.su.mas.dedaleEtu.mas.behaviours.ACLMessage;
import eu.su.mas.dedaleEtu.mas.behaviours.AID;
import eu.su.mas.dedaleEtu.mas.behaviours.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.behaviours.Couple;
import eu.su.mas.dedaleEtu.mas.behaviours.MessageTemplate;
import eu.su.mas.dedaleEtu.mas.behaviours.Observation;
import eu.su.mas.dedaleEtu.mas.behaviours.SerializableSimpleGraph;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMapBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.UnreadableException;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;

public class StateMailboxFSMBehaviour extends OneShotBehaviour {
	private static final long serialVersionUID = 8567689731499797661L;
	
	private MapRepresentation myMap;
	private List<String> list_agentNames;
	private int exitValue;
	
	public StateMailboxFSMBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap, List<String> agentNames()) {
		super(myagent);
		this.myMap=myMap;
		this.list_agentNames=agentNames;
	}
	
	public void action() {	
		//check si l'agent a reçu une carte de ses voisins 
		MessageTemplate msgMap = MessageTemplate.and(
				MessageTemplate.MatchProtocol("SHARE-MAP"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		
		ACLMessage msgMapReceived = this.myAgent.receive(msgMap);
		
		if (msgMapReceived != null) {
			SerializableSimpleGraph<String, MapAttribute> mapReceived=null;
			try {
				sgreceived = (SerializableSimpleGraph<String, MapAttribute>)mapReceived.getContentObject();
			} catch (UnreadableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.myMap.mergeMap(mapReceived);
			exitValue = 2; // aller en D : "Envoie ACK"
		}
		
		//check si l'agent a reçu un ACK (de la caret qu'il a envoyé) de ses voisins 
		MessageTemplate msgACK = MessageTemplate.and(
				MessageTemplate.MatchProtocol("ACK-MAP"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		
		ACLMessage msgACKMapReceived = this.myAgent.receive(msgACK);
		
		if (msgACKMapReceived != null) {
			// TODO
		}
		
		// TODO (les lignes suivantes ne sont pas correctes)
		int nb_voisins = this.myAgent.list_voisins.size();
		String myName = this.myAgent.getLocalName();
		
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("SHARE-MAP");
		msg.setContent(myName); // met son nom dans le message envoyé 
		msg.setSender(this.myAgent.getAID());
		
		// envoyer un à tous les agents (sauf moi-même)
		for (int i=0; i < nb_voisins; i++) {
			String receiverAgent = this.myAgent.list_voisins.get(i);
			msg.addReceiver(new AID(receiverAgent,false));	
		}
		
		SerializableSimpleGraph<String, MapAttribute> mapSent=this.myMap.getSerializableGraph();
		try {					
			msg.setContentObject(mapSent);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
		exitValue = 1;
		
		//At each time step, the agent check if he received a message from a teammate. 	
		MessageTemplate msgPing = MessageTemplate.and(
				MessageTemplate.MatchProtocol("PING"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		
		ACLMessage msgPingReceived = this.myAgent.receive(msgPing);
		
		if (msgPingReceived != null) {
			this.myAgent.list_voisins.add(msgPingReceived.getContent()); // récupère le nom de la personne qui a envoyé le ping 
			exitValue = 1; // aller en B : "Envoie carte"
		}
	}
	
	public int onEnd() {return exitValue;}
}