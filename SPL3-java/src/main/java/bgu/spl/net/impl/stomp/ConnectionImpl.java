package bgu.spl.net.impl.stomp;

import bgu.spl.net.impl.stomp.passiveObject.Pair_IdAndSubscribeId;
import bgu.spl.net.srv.BlockingConnectionHandler;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class ConnectionImpl implements Connections<String> {
    //private List<ConnectionHandlerImpl<T>> ConnectionHandlerList;
    private ConcurrentHashMap<Integer, ConnectionHandler<String>> ClientHashMap; //todo why non blocking??? or both???
    private ConcurrentHashMap<String,List<Pair_IdAndSubscribeId>> channelHashMap;// topic -> id & SubscribeId //todo: mabye move to DataBase
    private static Integer MessageId =1;

    public ConnectionImpl() {
        ClientHashMap = new ConcurrentHashMap<>();
        channelHashMap = new ConcurrentHashMap<>();
    }


    @Override
    public boolean send(int connectionId, String msg) {
        if(!ClientHashMap.containsKey(connectionId))
            return false;
        ClientHashMap.get(connectionId).send(msg);
        return true;
    }

    @Override
    public void send(String channel, String msg) {
        int tempMessageId;
        synchronized (MessageId) {
            tempMessageId=MessageId;
            MessageId++;
        }
        for (Pair_IdAndSubscribeId idAndSubscribeId: channelHashMap.get(channel))
            ClientHashMap.get(idAndSubscribeId.getId()).send("MESSAGE\nsubscription:"+idAndSubscribeId.getSubscribeId()+"\nMessage-id:"+tempMessageId+"\ndestination:"+channel+"\n");
    }

    @Override
    public void disconnect(int connectionId) {
        try {
            ClientHashMap.remove(connectionId).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addCleint(int connectionId, ConnectionHandler<String> handler) {
        ClientHashMap.put(connectionId,handler);
    }

    public void subscribe(String destination, int id, int subscribeId) {
        channelHashMap.get(destination).add(new Pair_IdAndSubscribeId(id,subscribeId));
    }

    public void unsubscribe(int ownerId, Integer UnSubscribeId, String topic) {
        channelHashMap.get(topic).remove(new Pair_IdAndSubscribeId(ownerId,UnSubscribeId));//I add equals to Pair_IdAndSubscribeId in order to do the search in list
        //find the (id,unSubscribeId) from list and remove them
    }
}
