package eu.su.mas.dedaleEtu.mas.behaviours.fsm;
import jade.core.behaviours.OneShotBehaviour;
import java.lang.Math;
import java.util.List;

import eu.su.mas.dedaleEtu.mas.behaviours.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;

public class StateFSMBehaviourStartEnd extends OneShotBehaviour {
	public StateFSMBehaviourStartEnd() {
		super(); 
	}
	
	public void action () {
		try {
			this.myAgent.doWait(500);
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		System.out.println ("Start/end behaviour");
	}
}