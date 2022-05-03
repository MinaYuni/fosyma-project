package eu.su.mas.dedaleEtu.mas.behaviours.fsm;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.fsm.FSMAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

//Behaviour/comportement du state F (exploration fini)
public class StateStopFSMBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = 5567689731496787661L;

    private MapRepresentation myMap;
    private List<String> list_agentNames;
    private HashMap<String, HashMap<String, Boolean>> dictVoisinsMessages;
    private int exitValue;

    public StateStopFSMBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap, List<String> agentNames, HashMap<String, HashMap<String, Boolean>> dico) {
        super(myagent);
        this.myMap = myMap;
        this.list_agentNames = agentNames;
        this.dictVoisinsMessages = dico;
    }

    public void action() {
        int nb_agents = this.list_agentNames.size();
        String myName = this.myAgent.getLocalName();

        System.out.println("\n-- START state F (StateStopFSMBehaviour): " + myName + " --");

        // update information
        if (this.myMap == null) {
            this.myMap = new MapRepresentation();
        }
        this.dictVoisinsMessages = ((FSMAgent) this.myAgent).getDictVoisinsMessages();
        ((FSMAgent) this.myAgent).explorationFinish();

        String myPosition = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition();
        System.out.println("STATE F : " + myName + " -- myCurrentPosition is: " + myPosition);

        if (myPosition != null) {
            // List of observable from the agent's current position
            List<Couple<String, List<Couple<Observation, Integer>>>> lobs = ((AbstractDedaleAgent) this.myAgent).observe(); // myPosition
            System.out.println("STATE F : " + myName + " -- list of observables: " + lobs);

/*
            // ACTION : random deplacement
            // chose a random next node to go to
            Random r = new Random();
            int moveId = 1 + r.nextInt(lobs.size() - 1); // removing the current position from the list of target, not necessary as to stay is an action but allow quicker random move
            String nextNode = lobs.get(moveId).getLeft();
            System.out.println("STATE F : " + myName + " will move to " + nextNode);
            ((AbstractDedaleAgent) this.myAgent).moveTo(nextNode);
*/

            // ACTION : Check reception PING
            MessageTemplate msgPing = MessageTemplate.and(
                    MessageTemplate.MatchProtocol("PING"),
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM));

            ACLMessage msgPingReceived = this.myAgent.receive(msgPing);
            // si reception PING, envoie sa carte
            // sinon reste à sa place (tous les agents peuvent se coller ici, on pourrait créer des groupes de collecte)
            if (msgPingReceived != null) { // réception PING, donc un autre agent est à proximité, donc MàJ dict_voisins de l'agent
                System.out.println("STATE F : " + myName + " received PING");

                // ACTION : Envoyer sa carte à l'agent qui a envoyé le PING
                String namePingReceived = msgPingReceived.getSender().getLocalName(); // récupérer le nom du voisin (nom donnée dans le message du ping reçu)
                //creation du message
                ACLMessage msgSendingMap = new ACLMessage(ACLMessage.INFORM);
                msgSendingMap.setProtocol("FINISH-SHARE-MAP");
                msgSendingMap.setSender(this.myAgent.getAID()); //mettre un expéditeur

                // ajouter le noms du destinataire (ici l'agent qui a envoyé le PING) du message
                msgSendingMap.addReceiver(new AID(namePingReceived, false));
                System.out.println("STATE F : " + myName + " sends MAP to " + namePingReceived);


                // ajout de la carte de l'agent dans le message d'envoi
                SerializableSimpleGraph<String, MapRepresentation.MapAttribute> mapSent = (this.myMap).getSerializableGraph();

                //this.myMap.prepareMigration(); //generer SerializableSimpleGraph (et met this.myMap.g à null => optimiser place memoire ??? )
                //SerializableSimpleGraph<String, MapRepresentation.MapAttribute> mapSent = (this.myMap).getSg();

                try {
                    msgSendingMap.setContentObject(mapSent);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // envoie en cours de la carte à tous les voisins
                ((AbstractDedaleAgent) this.myAgent).sendMessage(msgSendingMap);
                System.out.println("STATE F : " + myName + " send MAP to " + namePingReceived );
            }

        }
    }

    public int onEnd() {
        return exitValue;
    }
}
