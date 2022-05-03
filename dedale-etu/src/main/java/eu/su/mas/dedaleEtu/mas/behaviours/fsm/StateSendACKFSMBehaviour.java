package eu.su.mas.dedaleEtu.mas.behaviours.fsm;

import jade.core.behaviours.OneShotBehaviour;
import eu.su.mas.dedaleEtu.mas.agents.fsm.FSMAgent;
import jade.core.AID;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.io.IOException;
import java.lang.Math;
import java.util.Objects;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.lang.acl.UnreadableException;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;

import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.fsm.FSMAgent;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMapBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;

//Behaviours/comportement au state D : "Envoie ACK"
public class StateSendACKFSMBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = 4567689731496787661L;
    private final List<String> list_agentNames;
    private MapRepresentation myMap;
    private HashMap<String, HashMap<String, Boolean>> dictVoisinsMessages;
    private int exitValue;

    public StateSendACKFSMBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap, List<String> agentNames, HashMap<String, HashMap<String, Boolean>> dico) {
        super(myagent);
        this.myMap = myMap;
        this.list_agentNames = agentNames;
        this.dictVoisinsMessages = dico;
    }

    public void action() {
        int nb_agents = this.list_agentNames.size();
        String myName = this.myAgent.getLocalName();

        System.out.println("\n-- START state D (StateSendACKFSMBehaviour): " + myName + " starts state D --");

        // update information
        if (this.myMap == null) {
            this.myMap = ((FSMAgent) this.myAgent).getMyMap();
        }
        this.dictVoisinsMessages = ((FSMAgent) this.myAgent).getDictVoisinsMessages();

        // ACTION : Envoie un ACK
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setProtocol("ACK-MAP");
        msg.setSender(this.myAgent.getAID()); // on met un expéditeur au message
        //msg.setContent(myName); // met son nom dans le ping envoyé

        // ajout des receveurs du message (sauf moi-même)
        for (String receiverAgent : this.list_agentNames) { // on récupère le nom d'un agent
            if (!Objects.equals(myName, receiverAgent)) { // si c'est pas moi
                //Hashtable<String, Boolean> etat = this.myAgent.dico_voisins_messages.get(receiverAgent); // dico des actions de l'agent par rapport à Recever
                HashMap<String, Boolean> etat = this.dictVoisinsMessages.get(receiverAgent); // dico des actions de l'agent par rapport à Recever

                if (etat.get("recoit_carte")) { // agent a bien reçu carte de receiverAgent
                    if (etat.get("envoie_ACK") == null) { // agent n'a jamais envoyé de ACK de sa carte
                        msg.addReceiver(new AID(receiverAgent, false));// mettre un receveur du message

                        //MAJ dict_voisin
                        etat.put("envoie_ACK", true); // on évite de renvoyer un ACK à chaque voisin dont agent a deja envoie un ACK
                        //this.dictVoisinsMessages.put(receiverAgent,etat);

                        //on a modifi" le dico dictVoisinsMessages => utiliser la methode 'setDictVoisinsMessages' pour udapte !
                        ((FSMAgent) this.myAgent).setDictVoisinsMessagesAgent(receiverAgent, etat);
                        //((FSMAgent)this.myAgent).setDictVoisinsMessages(this.dictVoisinsMessages);
                    }
                }
            }
        }

        // envoyer un ACK-MAP à tous les agents dont l'agent a reçu la carte et où il n'a pas encore envoyé de ACK auparavant
        ((AbstractDedaleAgent) this.myAgent).sendMessage(msg);
        System.out.println("STATE D : " + myName + " finished sending ACK");

        exitValue = 1; // aller en C : "Attente ACK"
        System.out.println("-END state D (StateSendACKFSMBehaviour): " + myName + " ends state D, goes to state C ");
    }

    public int onEnd() {
        return exitValue;
    }
}
