package eu.su.mas.dedaleEtu.mas.behaviours.fsm;
import jade.core.behaviours.OneShotBehaviour;

import jade.core.AID;
import java.util.Iterator;
import java.util.List;
import java.io.IOException;
import java.lang.Math;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.lang.acl.UnreadableException;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;

import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMapBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;


// jai modifier list_voisins qui est devenu dict_voisins (voir FSMAgent.java)
// (car dans StateMailboxFSMBehaviours.java (c'est state C), dans message ACK-MAP (vers les ligne 65), jai besoin de connaitre les etats de l'agent par rapport au Receveur)
// jai fini d'adapter les changements de dico_voisin pour ce behviours


//Behaviours/comportement au state D : "Envoie ACK"
public class StateSendACKFSMBehaviour extends OneShotBehaviour {
	private static final long serialVersionUID = 8567689731499797661L;
	
	private MapRepresentation myMap;
	private List<String> list_agentNames;
	private int exitValue;
	
	public StateSendACKFSMBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap, List<String> agentNames) {
		super(myagent);
		this.myMap=myMap;
		this.list_agentNames=agentNames;
	}
	
	public void action() {	
		int n = this.list_agentNames.size();
        String myName = this.myAgent.getLocalName();
        
        // ACTION : Envoie un message PING avec le nomAgent
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        // les informations contenu dans le message (qu'on va envoyer) 
        msg.setProtocol("ACK-MAP");
        msg.setContent(myName); // met son nom dans le ping envoyé 
        msg.setSender(this.myAgent.getAID()); //mettre une expediteur au message 
        
        // ajout des receveus du messsages (sauf moi meme)
        for (int i=0; i < n; i++) {
            String receiverAgent = this.list_agentNames.get(i); //recupere le nom d'un agent
            
            if (myName != receiverAgent) { // si c'est pas moi
			    
                Dictionary<String, bool> etat = this.agent.dict_voisins.get(receiverAgent); //dico des actions de l'agent par rapport a Recever
        
                if (etat.get("recoit_carte")) { //agent a bien recu carte de ReceiverAgent

                    if (! etat.get("envoie_ACK")) { //agent a jamais envoyer de ACK pour de sa carte
                        msg.addReceiver(new AID(receiverAgent,false));	//mettre une receveur du message 

                        //MAJ dict_voisin
                        etat.put("envoie_ACK", true); //evite de renvoyer un ACK a chaque voisin dont agent a deja envoie un ACK
                    }
                }            
            }
        }

        // envoyer un ACK MAP à tous les agents dont jai recu la carte et pas encore envoyer de ACK auparavant
        ((AbstractDedaleAgent)this.myAgent).sendMessage(msg);

        exitValue = 1; // aller en C : "Attente ACK"
    }
}
