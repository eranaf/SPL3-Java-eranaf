package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.Future;
import bgu.spl.mics.SimplePublisher;
import bgu.spl.mics.Subscriber;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.passiveObjects.Diary;
import bgu.spl.mics.application.passiveObjects.MissionInfo;
import bgu.spl.mics.application.passiveObjects.Report;
import bgu.spl.mics.application.publishers.TimeService;
import javafx.util.Pair;

import java.util.List;

/**
 * M handles ReadyEvent - fills a report and sends agents to mission.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class M extends Subscriber {
	private int lastTick = 0;

	private int M;
	private static int index = 1;
	//make sure they are instances. the number of instances will be given in the start
	public M() {
		super("M"+index);
		M = index;
		index++;
	}

	@Override
	protected void initialize() {
		//Be sure to get a Tick broadcast before events
		subscribeBroadcast(TerminateBroadcast.class, (TerminateBroadcast b) -> {terminate();
			System.out.println(this.getName()+ " terminate!" );
		});
		subscribeBroadcast(TickBroadcast.class, (TickBroadcast b) -> {
			this.lastTick = b.getTimeTick();
		});
		subscribeEvent(MissionRecivedEvent.class, (MissionRecivedEvent e) -> {
			System.out.println(this.getName()+"; start missionEvent "+e.getMissionInfo().getMissionName()+"; this tick " + lastTick +"; this Time Issued - Expired " +e.getMissionInfo().getTimeIssued()+"-"+ e.getMissionInfo().getTimeExpired() + "; gaget " + e.getMissionInfo().getGadget() + "; agent " + e.getMissionInfo().getSerialAgentsNumbers() + "; duration  " + e.getMissionInfo().getDuration()) ;
			boolean executed = false;
			Report newReport = new Report();
			MissionInfo missionInfo = e.getMissionInfo();
			AgentsAvailableEvent AgentEvent = new AgentsAvailableEvent(missionInfo.getSerialAgentsNumbers());
			Future<Integer> AgentEventFuture = getSimplePublisher().sendEvent(AgentEvent);
			if(AgentEventFuture != null &&AgentEventFuture.get()!= null && AgentEventFuture.get()!=-1){ //TODO 25.12 AgentEventFuture!= null IF NULL ABANDOND AgentEventFuture.get()!= null
				//will enter if gets a number of the Moneypenny that got all agents
				System.out.println(this.getName() + " took agent " + missionInfo.getSerialAgentsNumbers());
				GadgetAvailableEvent GadgetEvent = new GadgetAvailableEvent(missionInfo.getGadget());
				Future<Pair<Boolean,Integer>> GadgetEventFuture = getSimplePublisher().sendEvent(GadgetEvent);
				if(GadgetEventFuture!= null && GadgetEventFuture.get()!=null && GadgetEventFuture.get().getKey()){//TODO 25.12 GadgetEventFuture!= null IF NULL ABANDOND
					System.out.println(this.getName() + " the gadget " + missionInfo.getGadget());
					//will check availability of gadget
					if(GadgetEventFuture.get().getValue()<missionInfo.getTimeExpired()){ //TODO check time of Q!!! not by M time
						//accomplished All
						SendAgentsEvent SendAgentsEvent = new SendAgentsEvent(missionInfo.getSerialAgentsNumbers(),missionInfo.getDuration());
						Future<List<String>> AgentNamesFuture = getSimplePublisher().sendEvent(SendAgentsEvent);
						if(AgentNamesFuture !=null && AgentNamesFuture.get() != null) {//TODO 25.12 AgentEventFuture!= null IF NULL ABANDOND
							newReport.setAgentsNames(AgentNamesFuture.get()); //TODO CHECK IF  - if null do realse //TODO 25.12 what is it???
							newReport.setQTime(GadgetEventFuture.get().getValue());
							executed = true;
							System.out.println(this.getName() + " - done this message;"+"  the agent " + missionInfo.getSerialAgentsNumbers() + " are free (maybe before here it 100%)");
						}
					}
					else{//aborted - time Expired
						//release all agents that were aquired
						System.out.println(this.getName() + " time expired");
						ReleaseAgentsEvent ReleaseEvent = new ReleaseAgentsEvent(missionInfo.getSerialAgentsNumbers());
						getSimplePublisher().sendEvent(ReleaseEvent);
					}
				}
				else{
					System.out.println(this.getName() + " fail taking the gadget " + missionInfo.getGadget());
					ReleaseAgentsEvent ReleaseEvent = new ReleaseAgentsEvent(missionInfo.getSerialAgentsNumbers());
					getSimplePublisher().sendEvent(ReleaseEvent);
				}
			}
			else{
				System.out.println(this.getName() + " fail taking agent "+missionInfo.getSerialAgentsNumbers());
			}
			complete(e, executed);
			Diary.getInstance().incrementTotal();
			//make the new report if the mission was successfully executed
			if(executed) {
				newReport.setMissionName(missionInfo.getMissionName());
				newReport.setM(M);
				newReport.setMoneypenny(AgentEventFuture.get());
				newReport.setAgentsSerialNumbers(missionInfo.getSerialAgentsNumbers());
				newReport.setGadgetName(missionInfo.getGadget());
				newReport.setTimeIssued(missionInfo.getTimeIssued());
				newReport.setTimeCreated(lastTick);
				Diary.getInstance().addReport(newReport);
			}
			//end report Making
		});
	}

}
