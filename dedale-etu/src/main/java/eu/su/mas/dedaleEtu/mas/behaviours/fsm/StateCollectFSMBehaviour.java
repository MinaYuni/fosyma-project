package eu.su.mas.dedaleEtu.mas.behaviours.fsm;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.fsm.FSMAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.FullMapRepresentation;
import jade.core.behaviours.OneShotBehaviour;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class StateCollectFSMBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = 6567689731496787661L;

    private HashMap<String, HashMap<String, Boolean>> dictVoisinsMessages;
    //private MapRepresentation myMap;
    private FullMapRepresentation myFullMap;
    private int exitValue;

    public StateCollectFSMBehaviour(final AbstractDedaleAgent myagent) {
        super(myagent);
    }

    public void action() {
        String myName = this.myAgent.getLocalName();
        System.out.println("\n--START state E (StateCollectFSMBehaviour): " + myName + " --");

        // update information
        this.myFullMap = ((FSMAgent) this.myAgent).getMyFullMap();
        this.dictVoisinsMessages = ((FSMAgent) this.myAgent).getDictVoisinsMessages();

        String myPosition = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition();
        System.out.println("STATE E : " + myName + " -- myCurrentPosition is: " + myPosition);

        if (myPosition != null) {
            // List of observable from the agent's current position
            List<Couple<String, List<Couple<Observation, Integer>>>> lobs = ((AbstractDedaleAgent) this.myAgent).observe(); // myPosition
            System.out.println("STATE E : " + myName + " -- list of observables: " + lobs);

            // Little pause to allow you to follow what is going on
//            try {
//                System.out.println("Press enter in the console to allow the agent " + this.myAgent.getLocalName() + " to execute its next move");
//                System.in.read();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            // list of observations associated to the currentPosition
            List<Couple<Observation, Integer>> lObservations = lobs.get(0).getRight();
            System.out.println("STATE E : " + myName + "lObservations - " + lObservations);

            // example related to the use of the backpack for the treasure hunt
            boolean b = false;

            for (Couple<Observation, Integer> o : lObservations) {
                System.out.println("STATE E : " + myName + "-o: " + o + "| o.getLeft(): "+ o.getLeft());
                switch (o.getLeft()) {
                    case DIAMOND:
                        System.out.println("STATE E : " + myName + "case DIAMOND");
                    case GOLD:
                        System.out.println("STATE E : " + myName + "case GOLD");
                        System.out.println(this.myAgent.getLocalName() + " - My treasure type is : " + ((AbstractDedaleAgent) this.myAgent).getMyTreasureType());
                        System.out.println(this.myAgent.getLocalName() + " - My current backpack capacity is:" + ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
                        System.out.println(this.myAgent.getLocalName() + " - Value of the treasure on the current position: " + o.getLeft() + ": " + o.getRight());
                        System.out.println(this.myAgent.getLocalName() + " - The agent grabbed :" + ((AbstractDedaleAgent) this.myAgent).pick());
                        System.out.println(this.myAgent.getLocalName() + " - the remaining backpack capacity is: " + ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
                        b = true;
                        break;
                    default:
                        break;
                }
            }

            // If the agent picked (part of) the treasure
            if (b) {
                List<Couple<String, List<Couple<Observation, Integer>>>> lobs2 = ((AbstractDedaleAgent) this.myAgent).observe(); // myPosition
                System.out.println("STATE E : " + myName + " - State of the observations after trying to pick something " + lobs2);
            }

            // Random move from the current position
            Random r = new Random();
            int moveId = 1 + r.nextInt(lobs.size() - 1); // removing the current position from the list of target, not necessary as to stay is an action but allow quicker random move

            // The move action (if any) should be the last action of your behaviour
            String nextNode = lobs.get(moveId).getLeft();
            System.out.println("STATE E : " + myName + " will move to " + nextNode);
            ((AbstractDedaleAgent) this.myAgent).moveTo(nextNode);
        }
    }

    public int onEnd() {
        return exitValue;
    }
}
