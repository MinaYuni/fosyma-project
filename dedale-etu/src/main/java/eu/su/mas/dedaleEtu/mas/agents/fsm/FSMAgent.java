package eu.su.mas.dedaleEtu.mas.agents.fsm;

import java.util.*;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.fsm.*;
//import eu.su.mas.dedaleEtu.mas.behaviours.SendPingBehaviour;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;

import eu.su.mas.dedaleEtu.mas.knowledge.FullMapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import org.omg.CORBA.PUBLIC_MEMBER;

//import javax.json.*;
//import org.json.*;

// Repartition des ressources de manière équitable : max du min ou indice de Gini

public class FSMAgent extends AbstractDedaleAgent {
    public int cpt = 0; //pour donner des identifiants aux agents pour la collecte
    private static final long serialVersionUID = -1969469610241668140L;

    /* PAS A JOUR
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
    private static final String E = "Collecte";
    private static final String G = "Random Walk";
    private static final String F = "Etat final";

    /*
    dict_voisins est un dictionnaire sur les états des messages envoyés à chaque agent :
        key (String) : nom des voisins
        value (dict) : permet de vérifier quels sont les messages qui ont été envoyé ou pas
            key (String) : type message envoyé ou reçu
                => exemple : "reception_carte", "envoie_carte", "reception_ACK", "envoie_ACK"
            value (Boolean) : si on a fait les actions correspondant à la clef
    */
    HashMap<String, HashMap<String, Boolean>> dictVoisinsMessages = new HashMap<>();

    /*
    dictionnaire pour garder les cartes qui ont été envoyé aux autres agents
        key (String) : nom des agents
        value (FullMapRepresantation) : dernière carte envoyée à l'agent key
    */
    HashMap<String, MapRepresentation> dictMapEnvoye = new HashMap<>();

    /*
    dictionnaire gardant en mémoire les capacités du sac à dos de chaque agent (soi-même compris)
        key (String) : nom des agents
        value (List) : capacités du sac
            [<Gold, 150>, <Diamond, 150>]
     */
    HashMap<String, List<Couple<Observation,Integer>>> dictBackpack = new HashMap<>();

    private MapRepresentation myMap;
    private FullMapRepresentation myFullMap;
    private List<String> listAgentNames = new ArrayList<String>();
    private Observation typeTreasure;
    private int id; //pour la collecte et interblocage (id prend valeur entre 1 à Nb Agents), pour savoir la priorité entre agents
    private int quantite; //pour savoir la quantité de trésor récupérer

    private String nodeBut ; //le noeud à atteindre
    private List<String> path;

    private String nextNode;
    private String predNode;
    private List<String> listNodeRessource ; //la liste des noeuds que l'agent doit récupérer
    private int nbPointRecolte ; //indique le nombre de recolte deja fait => va servie d'index dans listNodeRessource

    private boolean interblocage = false;
    private boolean culdesac = false;

    protected void setup() {
        super.setup();

        this.cpt ++;
        this.id = cpt; //compris entre 1 à NbAgents
        this.quantite = 0;
        this.typeTreasure = null;
        this.nodeBut = "";
        this.nextNode = "";
        this.nbPointRecolte= 0;
        this.listNodeRessource = null;

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
        this.initDictBackpack();
        this.initPath();

        // liste des behaviours
        List<Behaviour> listBehaviours = new ArrayList<>();

        // FMS behaviour
        FSMBehaviour fsm = new FSMBehaviour(this);

        // Define the different states and behaviours
        fsm.registerFirstState(new StateFullExploFSMBehaviour(this), A);
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
        fsm.registerTransition(A, F, 3);
        fsm.registerTransition(B, C, 1);
        fsm.registerTransition(C, D, 2);
        fsm.registerTransition(C, C, 1);
        fsm.registerTransition(C, A, 3);
        fsm.registerTransition(C, B, 4);
        fsm.registerTransition(D, C, 1);
        fsm.registerTransition(G, G, 1);
        fsm.registerTransition(G, E, 2);
        fsm.registerTransition(E, E, 1);
        fsm.registerTransition(E, G, 2);

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

    public void initDictVoisinsMessages() {
        for (String agent: this.listAgentNames) {
            // création du dictionnaire de l'état des actions de l'agent par rapport à l'envoyeur du ping
            HashMap<String, Boolean> etat = new HashMap<>();
            // initialisation des états des actions à faire
            etat.put("recoit_PING", false); // reception d'un ping
            etat.put("envoie_MAP", false);  // carte à envoyer au ping reçu
            etat.put("recoit_MAP", false);  // attente de la carte de l'agent qui a envoyé le ping
            etat.put("envoie_ACK", false);  // ACK à envoyer pour la carte reçue par l'agent du ping
            etat.put("recoit_ACK", false);  // attente ACK de sa carte par l'agent du ping
            this.dictVoisinsMessages.put(agent, etat);
        }
    }

    public void initPath(){
        if (this.path==null){
            this.path = new ArrayList<>();
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
        if (this.myMap == null) {
            this.myMap = new MapRepresentation();
        }
        return this.myMap;
    }

    public void setMyMap(MapRepresentation myMap) {
        this.myMap = myMap;
    }

    public FullMapRepresentation getMyFullMap() {
        if (this.myFullMap == null) {
            this.myFullMap = new FullMapRepresentation();
        }
        return this.myFullMap;
    }

    public void setMyFullMap(FullMapRepresentation myMap) {
        this.myFullMap = myMap;
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

    public List<String> getListAgentNames() {
        return listAgentNames;
    }

    public void initDictBackpack() {
        List<Couple<Observation,Integer>> tmp = new ArrayList<>();

        for (String agent : this.listAgentNames) {
            this.dictBackpack.put(agent, tmp);
        }

        // ajouter soi-même
        this.dictBackpack.put(this.getLocalName(), tmp);
    }

    public HashMap<String, List<Couple<Observation,Integer>>> getDictBackpack() {
        return this.dictBackpack;
    }
    public void setDictBackpack(HashMap<String, List<Couple<Observation,Integer>>> dico) {
        this.dictBackpack=dico;
    }

    public List<Couple<Observation,Integer>> getDictBackpackAgent(String agent) {
        return (this.dictBackpack).get(agent);
    }

    public void setDictBackpackAgent(String agent, List<Couple<Observation,Integer>> listBackpackSpace) {
        this.dictBackpack.put(agent, listBackpackSpace);
    }


    public HashMap<String, List<Couple<String,Integer>>> stringToHashMap(String listString) {
        //{AgentFSM_2=[], AgentFSM_1=[<Gold, 100>, <Diamond, 100>]}
        HashMap<String, List<Couple<String,Integer>>> finalDict = new HashMap<>();
        //String tmpStr = listString.replaceAll("}\{", "");
        return finalDict;
    }


    public Integer getDictBackPackObservationInteger(String agent, Observation o){
        List<Couple<Observation,Integer>> l = this.dictBackpack.get(agent);
        int i = 0;
        for(Couple<Observation,Integer> c : l){
            i++;
            if(c.getLeft()==o){
                return c.getRight();
            }
        }
        return 0;
    }

    public void setDictBackPackObservationInteger(String agent, Observation o , Integer quantite ){
        List<Couple<Observation,Integer>> l = this.dictBackpack.get(agent);
        int i = 0;
        for(Couple<Observation,Integer> c : l){
            i++;
            if(c.getLeft()==o){
               break;
            }
        }
        l.set(i, new Couple<>(o, quantite));
        this.dictBackpack.put(agent, l);
    }

    public void updateDictBackPack(HashMap<String, List<Couple<Observation,Integer>>> dico) {
        System.out.println(this.getName() + "[FSMAGENT] -- UPDATE -- DEBUT : " + this.dictBackpack);
        if (dico!=null) {
            for (String agent : dico.keySet()) {
                List<Couple<Observation, Integer>> listCapacityNew = dico.get(agent);

                if (listCapacityNew.isEmpty()) {// si listCapacityNew est vide
                    // si agent existe dans this.dictBackpack, alors on ne change rien
                    // sinon on ajoute agent avec liste vide dans this.dictBackpack
                    if(!this.dictBackpack.containsKey(agent)) {
                        this.setDictBackpackAgent(agent, new ArrayList<>() );
                    }
                }else{
                    //sinon listCapacityNew n'est pas vide
                    List<Couple<Observation, Integer>> listCapacity = this.dictBackpack.get(agent);
                    if(!this.dictBackpack.containsKey(agent) || listCapacity.isEmpty()) {
                        // si agent n'existe pas dans this.dictBackpack
                        this.setDictBackpackAgent(agent, listCapacityNew );
                    }else {
                        Couple<Observation, Integer> cGoldNew = listCapacityNew.get(0);
                        Couple<Observation, Integer> cGold = listCapacity.get(0);

                        Couple<Observation, Integer> cDiamondNew = listCapacityNew.get(1);
                        Couple<Observation, Integer> cDiamond = listCapacity.get(1);

                        if (cGoldNew.getLeft().equals(cGold.getLeft())) { //le cas où le premier element dans les 2 listes est la meme observation
                            // compare capacité gold de l'agent agent
                            if (cGoldNew.getRight() > cGold.getRight()) {  // cGold et cGoldNew sont de meme observation
                                System.out.println(this.getName() + "[FSMAgent] -- updateDictBackPack -- AVANT : "+this.dictBackpack+" -- listCapacity : " + listCapacity+" -- listCapacityNew : " + listCapacityNew);
                                this.setDictBackPackObservationInteger(agent, cGold.getLeft(), cGoldNew.getRight());
                                System.out.println(this.getName() + "[FSMAgent] -- updateDictBackPack -- AVANT : "+this.dictBackpack+" -- listCapacity : " + listCapacity+" -- listCapacityNew : " + listCapacityNew);
                            }
                            // compare capacité diamond de l'agent agent
                            if (cDiamondNew.getRight() > cDiamond.getRight()) {
                                System.out.println(this.getName() + "[FSMAgent] -- updateDictBackPack -- AVANT : "+this.dictBackpack+" -- listCapacity : " + listCapacity+" -- listCapacityNew : " + listCapacityNew);
                                this.setDictBackPackObservationInteger(agent, cDiamond.getLeft(), cDiamondNew.getRight());
                                System.out.println(this.getName() + "[FSMAgent] -- updateDictBackPack -- APRES : "+this.dictBackpack+" -- listCapacity : " + listCapacity+" -- listCapacityNew : " + listCapacityNew);
                            }
                        } else { //le cas où le premier element dans les 2 listes n'est PAS la meme observation
                            // compare capacité gold de l'agent agent
                            if (cGoldNew.getRight() > cDiamond.getRight()) {  //cDiamond et cGoldNew sont de meme observation
                                System.out.println(this.getName() + "[FSMAgent] -- updateDictBackPack -- AVANT : "+this.dictBackpack+" -- listCapacity : " + listCapacity+" -- listCapacityNew : " + listCapacityNew);
                                this.setDictBackPackObservationInteger(agent, cDiamond.getLeft(), cGoldNew.getRight());
                                System.out.println(this.getName() + "[FSMAgent] -- updateDictBackPack -- APRES : "+this.dictBackpack+" -- listCapacity : " + listCapacity+" -- listCapacityNew : " + listCapacityNew);
                            }
                            // compare capacité diamond de l'agent agent
                            if (cDiamondNew.getRight() > cGold.getRight()) {
                                System.out.println(this.getName() + "[FSMAgent] -- updateDictBackPack -- AVANT : "+this.dictBackpack+" -- listCapacity : " + listCapacity+" -- listCapacityNew : " + listCapacityNew);
                                this.setDictBackPackObservationInteger(agent, cGold.getLeft(), cDiamondNew.getRight());
                                System.out.println(this.getName() + "[FSMAgent] -- updateDictBackPack -- APRES : "+this.dictBackpack+" -- listCapacity : " + listCapacity+" -- listCapacityNew : " + listCapacityNew);
                            }
                        }
                    }
                }
            }
        }
        System.out.println(this.getName() + "[FSMAGENT] -- UPDATE -- FIN : " + this.dictBackpack);

    }
    public int getId(){
        return this.id;
    }

    public void updateQuantite(int k){
        this.quantite = k + this.quantite;
    }

    public Observation getTypeTreasure() {
        return typeTreasure;
    }

    public void setTypeTreasure(Observation obs){
        this.typeTreasure = obs;
    }

    public int pick(Observation obs){
        if (this.verifyTypeTreasure(obs)){
            int q = this.pick();
            this.updateQuantite(q);
            this.nbPointRecolte = this.nbPointRecolte + 1;
            return q;
        }
        return 0;
    }

    public String getNodeBut(){return this.nodeBut;}
    public void setNodeBut(String s){
        this.nodeBut = s;
    }

    public List<String> getPath(){
        return this.path;
    }

    public void setPath(List<String> path){
        this.path = path;
    }

    public int getNbPointRecolte() {
        return nbPointRecolte;
    }

    public void setNbPointRecolte(int nbPointRecolte) {
        this.nbPointRecolte = nbPointRecolte;
    }

    public String getNextNode() {
        return this.nextNode;
    }

    public void setNextNode(String nextNode) {
        this.nextNode = nextNode;
    }

    public String getPredNode() {
        return this.predNode;
    }

    public void setPredNode(String predNode) {
        this.nextNode = predNode;
    }

    public List<String> getListNodeRessource() {
        return listNodeRessource;
    }

    public void setListNodeRessource(List<String> listNodeRessource) {
        this.listNodeRessource = listNodeRessource;
    }

    public boolean addListNodeRessource(String s) {
        if( (!this.listNodeRessource.contains(s))) {
            this.listNodeRessource.add(s);
            return true;
        }
        return false;
    }

    public void choixNext(){
        this.nextNode = listNodeRessource.get(this.nbPointRecolte);
    }

    public boolean getInterblocage(){
        return this.interblocage;
    }

    public void setInterblocage(boolean flag) {
        this.interblocage = flag;
    }

    public boolean getCuldesac(){
        return this.culdesac;
    }

    public void setCuldesac(boolean flag) {
        this.interblocage = flag;
    }

    public boolean verifyTypeTreasure(Observation o) {
        if(this.typeTreasure==o || this.typeTreasure == null){
            return true;
        }
        return false;
    }

    /*
    public static Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
        Map<String, Object> retMap = new HashMap<String, Object>();

        if(json != JSONObject.NULL) {
            retMap = toMap(json);
        }
        return retMap;
    }

    public static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();

        Iterator<String> keysItr = object.keys();
        while(keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }
    */


}