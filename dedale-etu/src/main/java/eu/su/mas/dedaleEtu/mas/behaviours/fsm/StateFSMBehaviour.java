package eu.su.mas.dedaleEtu.mas.behaviours.fsm;
import jade.core.behaviours.OneShotBehaviour;
import java.lang.Math;

public class StateFSMBehaviour extends OneShotBehaviour {
	private int exitValueMax ;
	private int exitValue;
	
	public StateFSMBehaviour(int max) {
		super(); 
		exitValueMax = max;
	}
	
	public void action () {
		try {
			this.myAgent.doWait(500);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		exitValue = (int) (Math.random() * exitValueMax);
		System.out.println ("Val : " + exitValue);
	}
	
	public int onEnd() {return exitValue;}
}