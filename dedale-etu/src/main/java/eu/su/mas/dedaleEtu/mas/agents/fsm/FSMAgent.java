package eu.su.mas.dedaleEtu.mas.agents.fsm;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.fsm.*;
//import eu.su.mas.dedaleEtu.mas.behaviours.SendPingBehaviour;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;

import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;

// Repartition des ressources de manière équitable : max du min ou indice de Gini

public class FSMAgent extends AbstractDedaleAgent {
    private static final long serialVersionUID = -1969469610241668140L;

    /*
    A (exploration): à chaque déplacement, envoie PING + check boite aux lettres
        if reception "ping" --> B (arc 1)
        if reception carte --> B (arc 1)
        if exploration finie --> G (arc 2)
        else A

     B: envoie de la partie de la carte manquante à son voisin (celui du "pong" reçu)
        --> C (arc 1)

     C: check boite aux lettres (ACK, carte, nouveau voisin)
        if reception nouveau "ping" :
            then --> B (arc 4)
        if reception carte d'un autre agent :
            then --> D (arc 2)
        if reception (nouveau) ACK de la carte envoyé :
            garde en mémoire du ACK reçu
        if reception de tous les ACK (mémoire) --> A (arc 3)
        else --> C (arc 1)

     D: envoie ACK de la carte qu'il a reçu
        --> C (arc 1)

     E: collecte

     G: quand l'exploration est finie (carte complète)
        --> random walk pour envoyer sa carte aux agents qu'il croise
    */
    private static final String A = "Exploration";
    private static final String B = "Envoie carte";
    private static final String C = "Check Mailbox";
    private static final String D = "Envoie ACK";
    private static final String E = "Collecte"; // pas encore d'arcs
    private static final String G = "Random Walk"; // pas encore d'arcs
    private static final String F = "Etat final";

    /*
    dict_voisins est un dictionnaire sur les états des messages envoyés à chaque agent :
        key (String) : noms des voisins
        value (dict) : permet de vérifier quels sont les messages qui ont été envoyé ou pas
            key (String) : type message envoyé ou reçu
                => exemple : "reception_carte", "envoie_carte", "reception_ACK", "envoie_ACK"
            value (bool) : si on a fait les actions correspondant à la clef
    */
    HashMap<String, HashMap<String, Boolean>> dictVoisinsMessages = new HashMap<>();

    /*
        dictionnaire pour garder les cartes qui ont été envoyé aux autres agents
    */
    HashMap<String, MapRepresentation> dictMapEnvoye = new HashMap<>();

    private MapRepresentation myMap;
    private List<String> listAgentNames = new ArrayList<String>();

    protected void setup() {
        super.setup();

        // get the parameters added to the agent at creation (if any)
        final Object[] args = getArguments();

        if (args.length == 0) {
            System.err.println("Error while creating the agent, names of agent to contact expected");
            System.exit(-1);
        } else {
            int i = 2; // WARNING YOU SHOULD ALWAYS START AT 2. This will be corrected in the next release.
            while (i < args.length) {
                this.listAgentNames.add((String) args[i]);
                i++;
            }
        }

        this.initDictVoisinsMessages();

        // liste des behaviours
        List<Behaviour> listBehaviours = new ArrayList<>();

        // FMS behaviour
        FSMBehaviour fsm = new FSMBehaviour(this);

        // Define the different states and behaviours
        fsm.registerFirstState(new StateExploFSMBehaviour(this), A);
        fsm.registerState(new StateSendMapFSMBehaviour(this), B);
        fsm.registerState(new StateMailboxFSMBehaviour(this), C);
        fsm.registerState(new StateSendACKFSMBehaviour(this), D);
        fsm.registerState(new StateCollectFSMBehaviour(this), E);
        fsm.registerState(new StateRandomWalkFSMBehaviour(this), G);
        fsm.registerLastState(new StateStopFSMBehaviour(this), F);

        // Register the transitions
        fsm.registerDefaultTransition(A, A); //Default
        fsm.registerTransition(A, B, 1);
        fsm.registerTransition(A, G, 2);
        fsm.registerTransition(B, C, 1);
        fsm.registerTransition(C, D, 2);
        fsm.registerTransition(C, C, 1);
        fsm.registerTransition(C, A, 3);
        fsm.registerTransition(C, B, 4);
        fsm.registerTransition(D, C, 1);
        fsm.registerTransition(G, G, 1);

        // Ajout de FSMBehaviour dans la liste des comportements
        listBehaviours.add(fsm);

        // Agent va exécuter la liste des comportements
        addBehaviour(new startMyBehaviours(this, listBehaviours));

        System.out.println("the agent " + this.getLocalName() + " is started");
    }


    // ------------------- Methode get et set --------------------- //

    public void resetDictVoisinsMessages() {
        for (String agent: this.listAgentNames) {
            this.setDictVoisinsMessagesAgentAction(agent, "recoit_PING", false);
            this.setDictVoisinsMessagesAgentAction(agent, "envoie_MAP", false);
            this.setDictVoisinsMessagesAgentAction(agent, "recoit_MAP", false);
            this.setDictVoisinsMessagesAgentAction(agent, "envoie_ACK", false);
            this.setDictVoisinsMessagesAgentAction(agent, "recoit_ACK", false);
        }
    }

    public HashMap<String, HashMap<String, Boolean>> getDictVoisinsMessages() {
        // Retourne le dictionnaire dictVoisinsMessages
        if (this.dictVoisinsMessages == null) {
            this.dictVoisinsMessages = new HashMap<>();
        }
        return this.dictVoisinsMessages;
    }

    public void setDictVoisinsMessages(HashMap<String, HashMap<String, Boolean>> dico) {
        // Met à jour le dictionnaire dictVoisinsMessages
        this.dictVoisinsMessages = dico;
    }

    public HashMap<String, Boolean> getDictVoisinsMessagesAgent(String agent) {
        // Retourne le dictionnaire des états des actions d'un agent
        return this.dictVoisinsMessages.get(agent);
    }

    public void setDictVoisinsMessagesAgent(String agent, HashMap<String, Boolean> dico) {
        // Met à jour le dictionnaire des états des actions d'un agent
        this.dictVoisinsMessages.put(agent, dico);
    }

    public Boolean getDictVoisinsMessagesAgentAction(String agent, String action) {
        // Retourne la valeur boolean d'action du dictionnaire des états des actions d'un agent
        HashMap<String, Boolean> dico = this.dictVoisinsMessages.get(agent);
        return dico.get(action);
    }

    public void setDictVoisinsMessagesAgentAction(String agent, String action, Boolean bool) {
        // Met à jour la valeur boolean d'action du dictionnaire des états des actions d'un agent
        HashMap<String, Boolean> dico = this.dictVoisinsMessages.get(agent);
        dico.put(action, bool);
        this.dictVoisinsMessages.put(agent, dico);
    }


    public MapRepresentation getMyMap() {
        return this.myMap;
    }

    public void setMyMap(MapRepresentation myMap) {
        this.myMap = myMap;
    }

    public HashMap<String, MapRepresentation> getDictMapEnvoye() {
        return this.dictMapEnvoye;
    }

    public void setDictMapEnvoye(String agent, MapRepresentation map) {
        /*
        FMSAgent fait la mise à jour de sa connaissance sur la carte d'un autre agent (appelé agent)
        /!\ map doit être la connaissance totale => une carte entière /!\
        */
        this.dictMapEnvoye.put(agent, map);
    }
}