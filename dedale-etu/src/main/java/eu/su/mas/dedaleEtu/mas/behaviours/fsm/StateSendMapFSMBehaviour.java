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

public class StateSendMapFSMBehaviour extends OneShotBehaviour {
	private static final long serialVersionUID = 8567689731499797661L;
	
	private MapRepresentation myMap;
	private List<String> list_agentNames;
	private int exitValue;
	
	public StateSendMapFSMBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap, List<String> agentNames()) {
		super(myagent);
		this.myMap=myMap;
		this.list_agentNames=agentNames;
	}
	
	public void action() {	
		int nb_voisins = this.myAgent.list_voisins.size();
		String myName = this.myAgent.getLocalName();
		
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("SHARE-MAP");
		msg.setContent(myName); // met son nom dans le message envoyé 
		msg.setSender(this.myAgent.getAID());
		
		// envoyer sa carte à tous ses voisins
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
		exitValue = 1; //  aller en C = "Attente ACK et check Mailbox"
	}
	
	public int onEnd() {return exitValue;}
}