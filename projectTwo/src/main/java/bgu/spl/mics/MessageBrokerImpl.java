package bgu.spl.mics;

import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.subscribers.M;
import com.sun.corba.se.impl.presentation.rmi.DynamicMethodMarshallerImpl;
import javafx.util.Pair;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The {@link MessageBrokerImpl class is the implementation of the MessageBroker interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */

public class MessageBrokerImpl implements MessageBroker {
	private static class MessageBrokerHolder {
		private static MessageBrokerImpl instance = new MessageBrokerImpl();
	}
	//where to initialize??

	//Data structures
	private ConcurrentHashMap<Subscriber, Pair<ConcurrentLinkedQueue<Message>, ConcurrentLinkedQueue<Class<? extends Message>>>> mapSubscriber = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Class<? extends Message>, ConcurrentLinkedQueue<Subscriber>> mapEventClass = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Class<? extends Message>, ConcurrentLinkedQueue<Subscriber>> mapBroadcastClass = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Message, Future> mapEventFuture = new ConcurrentHashMap<>(); //only has Event - but need to ask also about broudcast
	private boolean terminate = false;


	//Data Semaphore
	//private ConcurrentHashMap<Subscriber, Semaphore> mapSubscriberSemaphore = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Class<? extends Event>, Semaphore> mapEventClassSemaphore = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Class<? extends Broadcast>, Semaphore> mapBroadcastClassSemaphore = new ConcurrentHashMap<>();

	//private Semaphore semaphoreMaster = new Semaphore(1, true);
	//Lock Master in order to lock any other Objects.
	//Lock any Object you Access???


	/**
	 * Retrieves the single instance of this class.
	 */

	public static MessageBroker getInstance() {
		return MessageBrokerHolder.instance;
	}

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, Subscriber m) {
		//check: if work in paleness

		synchronized (m) {
			if (mapSubscriber.containsKey(m)) {
				//Lock subscriber
				mapEventClass.putIfAbsent(type, new ConcurrentLinkedQueue<>());
				//there is a key type so we only need only to add the Subscriber to the Queue of the Subscriber who need to get event
				ConcurrentLinkedQueue<Subscriber> subscriberConcurrentLinkedQueue = mapEventClass.get(type);
				subscriberConcurrentLinkedQueue.add(m);
				//add to subscriber int the mapSubscriber this type in order to delete this subscriber from the mapEventClass.key(type) when unregister
				mapSubscriber.get(m).getValue().add(type);
				mapEventClassSemaphore.putIfAbsent(type, new Semaphore(1, true));
			}
		}
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, Subscriber m) {
		//check: if work in paleness
		synchronized (m) {
			if (mapSubscriber.containsKey(m)) {
				//Lock subscriber
				mapBroadcastClass.putIfAbsent(type, new ConcurrentLinkedQueue<>());
				//there is a key type so we only need only to add the Subscriber to the Queue of the Subscriber who need to get event
				ConcurrentLinkedQueue<Subscriber> subscriberConcurrentLinkedQueue = mapBroadcastClass.get(type);
				subscriberConcurrentLinkedQueue.add(m);
				//add to subscriber int the mapSubscriber this type in order to delete this subscriber from the mapEventClass.key(type) when unregister
				mapSubscriber.get(m).getValue().add(type);
				mapBroadcastClassSemaphore.putIfAbsent(type, new Semaphore(1, true));
			}
		}
	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		mapEventFuture.get(e).resolve(result);
		mapEventFuture.remove(e);
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		if (b.getClass() == TerminateBroadcast.class) {
			System.out.println("start terminate terminate terminate terminate");
			terminate = true;
		}

		//check: if work in paleness
		if (mapBroadcastClass.containsKey(b.getClass())) {
			ConcurrentLinkedQueue<Subscriber> subscribers = mapBroadcastClass.get(b.getClass());
			Iterator iterator = subscribers.iterator();
			while (iterator.hasNext()) {
				Subscriber s = (Subscriber) iterator.next();
				synchronized (s) {
					if (mapSubscriber.containsKey(s)) { //check if subscriber is in unregister process
						mapSubscriber.get(s).getKey().add(b);
						s.notifyAll();
					}
				}
			}
		}
	}

	@Override
	public <T> Future<T> sendEvent(Event<T> e) {//check all (19.12 adi)

		/*if( mapEventClass.get(e.getClass())==null||mapEventClass.get(e.getClass()).peek()==null)
			return null;
		Subscriber s = mapEventClass.get(e.getClass()).peek();
		synchronized (s) {
			if()
			Subscriber messageSubscribers = mapEventClass.get(e.getClass()).poll();
			mapEventClass.get(e.getClass()).add(messageSubscribers);
			Future<T> futureEventResult = new Future<T>();
			mapEventFuture.put(e, futureEventResult);
			mapSubscriber.get(messageSubscribers).getKey().add(e);
			return futureEventResult;
		}*/


		if (mapEventClass.get(e.getClass()) == null || mapEventClass.get(e.getClass()).peek() == null)
			return null;
		Future<T> futureEventResult = null;
		Subscriber s = mapEventClass.get(e.getClass()).peek();
		while (s != null) {
			synchronized (s) {
				try {
					//mapEventClassSemaphore.get(mapEventClass.get(e.getClass())).acquire();
					mapEventClassSemaphore.get(e.getClass()).acquire();

				} catch (InterruptedException ex) {
					ex.printStackTrace();

				}
				//System.out.println("!!!!!!!!!"); //TODO there is problem here undo //
				if (s == mapEventClass.get(e.getClass()).peek() & mapSubscriber.containsKey(s)) {
					Subscriber messageSubscribers = mapEventClass.get(e.getClass()).poll();
					mapEventClass.get(e.getClass()).add(messageSubscribers);
					futureEventResult = new Future<T>();
					mapEventFuture.put(e, futureEventResult);
					mapSubscriber.get(messageSubscribers).getKey().add(e);
					s.notifyAll();
				}
				mapEventClassSemaphore.get(e.getClass()).release();
			}
			if (futureEventResult != null)
				return futureEventResult;
			s = mapEventClass.get(e.getClass()).peek();
		}
		return null;
	}

	@Override
	public void register(Subscriber m) {
		synchronized (m) {
			mapSubscriber.put(m, new Pair<>(new ConcurrentLinkedQueue<>(), new ConcurrentLinkedQueue<>()));
		}
	}

	@Override
	//first get Master key, delete the semaphore of the Subscriber
	public void unregister(Subscriber m) {
		synchronized (m) {//19.12
			ConcurrentLinkedQueue<Class<? extends Message>> classQueue = mapSubscriber.get(m).getValue();
			Class<? extends Message> classCurrent;
			while (!classQueue.isEmpty()) {
				classCurrent = classQueue.poll();
				//Remove from event & Broadcast Queues
				//if (classCurrent.isInstance(Event.class)) { //Check
				if (mapEventClass.containsKey(classCurrent)) {
					mapEventClass.get(classCurrent).remove(m);
				}
				//if (classCurrent.isInstance(Broadcast.class)) { //Check
				if (mapBroadcastClass.containsKey(classCurrent)) {
					mapBroadcastClass.get(classCurrent).remove(m);
				}
			}

			ConcurrentLinkedQueue<Message> massageToTerminate = mapSubscriber.get(m).getKey();
			while (!massageToTerminate.isEmpty()) {
				Message poll = massageToTerminate.poll();
				//return null to future of all the mission that deleted
				Future F = mapEventFuture.get(poll);
				if (F != null) {
					F.resolve(null);
					mapEventFuture.remove(poll);
				}

			}
			mapSubscriber.remove(m); //TODO CHECK IF WORK IT WAS OUTSIDE

		}
	}

	@Override
	public Message awaitMessage(Subscriber m) throws InterruptedException {
		Message poll = null;
		synchronized (m) {//19.12

			/**check if terminate*/
			//If m doesn't exists
			if (mapSubscriber.get(m) == null)
				throw new IllegalStateException();
			//wait for message in messageQueue


			while (mapSubscriber.get(m).getKey().isEmpty()) {
				m.wait();
				//mapSubscriber.get(m).getKey().wait();//will release m
			}
			//TODO need to change terminate methods
			//TODO go by the time of Q
//			if (terminate) {
//				System.out.println(m.getName() + " need to be terminate");
//				for (Message message : mapSubscriber.get(m).getKey()) {
//					if(m.getClass()== M.class)
//						System.out.println(m.getName()+"message"+message.toString().substring(34)  + "!!!!!!!!!!!!!");
//					if (message.getClass() == TerminateBroadcast.class)
//						poll = message;
//				}
//			} else {
				poll = mapSubscriber.get(m).getKey().poll();
				//System.out.println("name "+m.getName() +" got message"); - check that every one get message

//			}
		}
		return poll;
	}

	public void clear(){//todo delete this!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		mapSubscriber.clear();
		mapEventClass.clear();
		mapBroadcastClass.clear();
		 mapEventFuture.clear();
		terminate = false;
		mapEventClassSemaphore.clear();
		mapBroadcastClassSemaphore.clear();

	}
}
