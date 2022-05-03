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
        if reception "finish" --> merge la carte et va en F (arc 2)
        if reception carte --> B (arc 1)
        if exploration finie --> F (arc 2)
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
            if pas reception nouvelle carte --> C (arc 1)
            if reception carte --> D (arc 2)
        if reception de tous les ACK (mémoire) --> A (arc 3)
        else --> C (arc 1)

     D: envoie ACK de la carte qu'il a reçu
        --> C (arc 1)

     E: collecte

     F: état final
        //quand l'exploration est finie --> faire un dernier mouvement aléatoire sur un des noeud voisins
        if reception "ping" -> envoie sa carte dans un message appelé finish

        (Remarque : agent ne bouge pas => pbl tous les agents vont etre coller
        on pourrait le faire deplacer ou creer des groupes de collecte par exemple)
    */
    private static final String A = "Exploration";
    private static final String B = "Envoie carte";
    private static final String C = "Check Mailbox";
    private static final String D = "Envoie ACK";
    private static final String E = "Collecte"; // pas encore d'arcs
    private static final String F = "Etat final";

    /*
    dict_voisins est un dictionnaire sur les états des messages envoyés à chaque agent :
        key (String) : noms des voisins
        value (dict) : dict_map_envoye (dico des états des messages)
            permet de vérifier quels sont les messages qui ont été envoyé ou pas
    */
    HashMap<String, HashMap<String, Boolean>> dictVoisinsMessages = new HashMap<>();

    /*
    dictionnaire pour garder les messages qui ont été envoyé aux autres agents
        key (String) : message envoyé ou reçu
            => exemple : "reception_carte", "envoie_carte", "reception_ACK", "envoie_ACK"
        value (bool) : si on a fait les actions correspondant à la clef
    */
    HashMap<String, MapRepresentation> dictMapEnvoye = new HashMap<String, MapRepresentation>();

    private MapRepresentation myMap;

    /*
        permet de savoir si l'agent a fini, si c'est le cas, alors il pourra communiquer aux autres sa carte !
     */
    private boolean explorationFinish = false;

    protected void setup() {
        super.setup();

        // get the parameters added to the agent at creation (if any)
        final Object[] args = getArguments();

        List<String> list_agentNames = new ArrayList<String>();

        if (args.length == 0) {
            System.err.println("Error while creating the agent, names of agent to contact expected");
            System.exit(-1);
        } else {
            int i = 2; // WARNING YOU SHOULD ALWAYS START AT 2. This will be corrected in the next release.
            while (i < args.length) {
                list_agentNames.add((String) args[i]);
                i++;
            }
        }

        // liste des behaviours
        List<Behaviour> listBehaviours = new ArrayList<Behaviour>();

        // FMS behaviour
        FSMBehaviour fsm = new FSMBehaviour(this);

        // Define the different states and behaviours
        fsm.registerFirstState(new StateExploFSMBehaviour(this, this.myMap, list_agentNames, this.dictVoisinsMessages), A);
        fsm.registerState(new StateSendMapFSMBehaviour(this, this.myMap, list_agentNames, this.dictVoisinsMessages), B);
        fsm.registerState(new StateMailboxFSMBehaviour(this, this.myMap, list_agentNames, this.dictVoisinsMessages), C);
        fsm.registerState(new StateSendACKFSMBehaviour(this, this.myMap, list_agentNames, this.dictVoisinsMessages), D);
        fsm.registerLastState(new StateStopFSMBehaviour(this, myMap, list_agentNames, this.dictVoisinsMessages), F);

        // Register the transitions
        fsm.registerDefaultTransition(A, A); //Default
        fsm.registerTransition(A, B, 1);
        fsm.registerTransition(A, F, 2);
        fsm.registerTransition(B, C, 1);
        fsm.registerTransition(C, D, 2);
        fsm.registerTransition(C, C, 1);
        fsm.registerTransition(C, A, 3);
        fsm.registerTransition(C, B, 4);
        fsm.registerTransition(D, C, 1);

        // Ajout de FSMBehaviour dans la liste des comportements
        listBehaviours.add(fsm);

        //Agent va executer la liste des comportements
        addBehaviour(new startMyBehaviours(this, listBehaviours));

        System.out.println("the  agent " + this.getLocalName() + " is started");
    }


    // ------------------- Methode get et set --------------------- //

    public HashMap<String, HashMap<String, Boolean>> getDictVoisinsMessages() {
        // Return the dictionary with agent (key) and dictionary_action (value)
        // Retourne le dictionnaire dont (clé = agent) et (value = dico_action)
        return this.dictVoisinsMessages;
    }

    public void setDictVoisinsMessages(HashMap<String, HashMap<String, Boolean>> dico) {
        // Replace the dictionary
        // Remplace le dictionnaire dont (clé = agent) et (value = dico_action) par dico
        this.dictVoisinsMessages = dico;
    }

    public HashMap<String, Boolean> getDictVoisinsMessagesAgent(String agent) {
        // Return the agent's dictionary_action with action (key) and bool (value)
        // Retourne le dictionnaire d'un agent agent dont (clé = action) et (value = bool)
        return this.dictVoisinsMessages.get(agent);
    }

    public void setDictVoisinsMessagesAgent(String agent, HashMap<String, Boolean> dico) {
        // Replace the dictionary
        // Remplace le dictionnaire d'un agent dont (clé = agent) et (value = dico_action) par dico
        this.dictVoisinsMessages.put(agent, dico);
    }

    public Boolean getDictVoisinsMessagesAgentAction(String agent, String action) {
        // Retourne la valeur boolean du dictionnaire d'un agent de l'action action
        HashMap<String, Boolean> dico = this.dictVoisinsMessages.get(agent);
        return dico.get(action);
    }

    public void setDictVoisinsMessagesAgentAction(String agent, String action, Boolean bool) {
        // Remplace la valeur boolean du dictionnaire d'un agent a l'action action par bool
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

    public void explorationFinish() {
        this.explorationFinish = true;
    }
}