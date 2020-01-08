
package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.Subscriber;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.passiveObjects.Inventory;
import bgu.spl.mics.application.passiveObjects.Squad;

/**
 * Only this type of Subscriber can access the squad.
 * Three are several Moneypenny-instances - each of them holds a unique serial number that will later be printed on the report.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class Moneypenny extends Subscriber {
	//make sure they are instances. the number of instances will be given in the start
	private static int index = 1;
	private boolean IsSendMessage ;
	private int MoneyPenny;


	public Moneypenny() {
		super("MP"+Integer.toString(index));
		IsSendMessage = index%2==1;
		this.MoneyPenny = index;
		index++;
	}

	@Override
	protected void initialize() {
		subscribeBroadcast(TerminateBroadcast.class, (TerminateBroadcast b) -> {terminate();
			Squad.getInstance().setFlage_terminate(true);
			Squad.getInstance().releaseAgents(Squad.getInstance().getAllAgents());
		/**releasing all agent inorder to wake up one of Moneypenny that waiting to some agent
		 * it wouldn't cause a problem because after this we need to do send agent but in every other
		 * Moneypenny will have a terminate massage and woulden't to send agent so eventually
		 * and the mission will be aboard**/

			System.out.println(this.getName()+ " terminate!" );
		});
		if(IsSendMessage){
			subscribeEvent(SendAgentsEvent.class, (SendAgentsEvent e) -> {
				Squad.getInstance().sendAgents(e.getRequestedAgentsToSend(),e.getDuration());
				complete(e, Squad.getInstance().getAgentsNames(e.getRequestedAgentsToSend()));
			});
			subscribeEvent(ReleaseAgentsEvent.class, (ReleaseAgentsEvent e) -> {
				Squad.getInstance().releaseAgents(e.getAgentsToRelease());
			});
		}else {
			subscribeEvent(AgentsAvailableEvent.class, (AgentsAvailableEvent e) -> {
				if(Squad.getInstance().getAgents(e.getRequestedAgents()))
					complete(e, MoneyPenny);//e Has the gadget?
				else
					complete(e,-1);
			});
		}
	}

}
