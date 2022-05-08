package eu.su.mas.dedaleEtu.mas.behaviours.fsm;
import jade.core.behaviours.OneShotBehaviour;

public class TEST_StateFSMBehaviourStartEnd extends OneShotBehaviour {
	public TEST_StateFSMBehaviourStartEnd() {
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