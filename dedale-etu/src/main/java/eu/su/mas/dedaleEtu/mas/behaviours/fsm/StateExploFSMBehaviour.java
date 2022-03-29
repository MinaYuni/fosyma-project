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

// jai modifier list_voisin qui est devenu dict_voisin (voir FSMAgent.java)
// (car dans StateMailboxFSMBehaviours.java (c'est state C), dans message ACK-MAP (vers les ligne 65), jai besoin de connaitre les etats de l'agent par rapport au Receveur)
// jai fini d'adapter les changements de dico_voisin pour ce behviours


// Behaviour/comportement du state A (exploration)
public class StateExploFSMBehaviour extends OneShotBehaviour {
	private static final long serialVersionUID = 8567689731499787661L;
	
	private MapRepresentation myMap;
	private List<String> list_agentNames;
	private int exitValue;
	
	public StateExploFSMBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap, List<String> agentNames()) {
		super(myagent);
		this.myMap=myMap;
		this.list_agentNames=agentNames;
	}
	
	public void action() {

		if(this.myMap==null) {
			this.myMap= new MapRepresentation();
		}
		
		//0) Retrieve the current position
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		if (myPosition!=null){
			//List of observable from the agent's current position
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition

			/**
			 * Just added here to let you see what the agent is doing, otherwise he will be too quick
			 */
			try {
				this.myAgent.doWait(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}

			//1) remove the current node from openlist and add it to closedNodes.
			this.myMap.addNode(myPosition, MapAttribute.closed);

			//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
			String nextNode=null;
			Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
			while(iter.hasNext()){
				String nodeId=iter.next().getLeft();
				boolean isNewNode=this.myMap.addNewNode(nodeId);
				//the node may exist, but not necessarily the edge
				if (myPosition!=nodeId) {
					this.myMap.addEdge(myPosition, nodeId);
					if (nextNode==null && isNewNode) nextNode=nodeId;
				}
			}

			//3) while openNodes is not empty, continues.
			if (!this.myMap.hasOpenNode()){ 
				//Explo finished
				exitValue = 2; // aller en F : "Exploration finie"
				System.out.println(this.myAgent.getLocalName()+" - Exploration successufully done, behaviour removed.");
			}else{
				//4) select next move.
				//4.1 If there exist one open node directly reachable, go for it,
				//	 otherwise choose one from the openNode list, compute the shortestPath and go for it
				if (nextNode==null){
					//no directly accessible openNode
					//chose one, compute the path and take the first step.
					nextNode=this.myMap.getShortestPathToClosestOpenNode(myPosition).get(0);//getShortestPath(myPosition,this.openNodes.get(0)).get(0);
					System.out.println(this.myAgent.getLocalName()+"-currentPosition: "+myPosition+" -- list= "+this.myMap.getOpenNodes()+"| nextNode: "+nextNode);
				}else {
					System.out.println("nextNode notNUll - "+this.myAgent.getLocalName()+"-- list= "+this.myMap.getOpenNodes()+"\n -- nextNode: "+nextNode);
				}
				//4) At each time step, the agent blindly send all its graph to its surrounding to illustrate how to share its knowledge (the topology currently) with the the others agents. 	
				// If it was written properly, this sharing action should be in a dedicated behaviour set, the receivers be automatically computed, and only a subgraph would be shared.
							
				int n = this.list_agentNames.size();
				String myName = this.myAgent.getLocalName();
				
				// ACTION : Envoie un message PING avec le nomAgent
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				// les informations contenu dans le message (qu'on va envoyer) 
				msg.setProtocol("PING");
				msg.setContent(myName); // met son nom dans le ping envoyé 
				msg.setSender(this.myAgent.getAID()); //mettre une expediteur au message 
				
				// ajout des receveus du messsages (sauf moi meme)
				for (int i=0; i < n; i++) {
					String receiverAgent = this.list_agentNames.get(i);
					if (myName != receiverAgent) { // si c'est pas moi
						msg.addReceiver(new AID(receiverAgent,false));	//mettre une receveur du message 
					}
				}
				// envoyer un ping à tous les agents (sauf moi-même)
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg);

				//5) At each time step, the agent check if he received a ping from a teammate. 	
				// ACTION : Check reception PING
				MessageTemplate msgPing = MessageTemplate.and(
						MessageTemplate.MatchProtocol("PING"),
						MessageTemplate.MatchPerformative(ACLMessage.INFORM));
				
				ACLMessage msgPingReceived = this.myAgent.receive(msgPing);
				
				if (msgPingReceived != null) { //on a recu un PING donc on a agent a proximite donc MAJ list_voisin/dict_voisin de l'agent
					//ajouter le voisin a la liste (voir type de list_voisin dans FSMAgent.java)
					String nameRecever = msgPingReceived.getContent() //retourne String, ici on récupère le nameAgent de la personne qui a envoyé le ping 
					
					if not dict_voisin.containsKey(nameRecever) : //on a trouver un nouveau voisin => on l'ajoute dans dict_voisin 
			
						Dictionary<String, bool>> etat = new Hashtable<String, bool>() //etat de Agent par rapport a Recever, dico est vide car il a rien fait (on peut aussi tout initialiser a False) 
			
						//ajout Receveur (key de type String) et etat (value de type dico) dans le dico des voisins
						this.myAgent.dict_voisins.put(nameRecever, etat); 
					
					exitValue = 1; // aller en B : "Envoie carte"
				}
				else { //pas recu de message (PING) donc continuer a avancer dans la map
					((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
			
				}
			}

		}
	}
	
	public int onEnd() {return exitValue;}
}