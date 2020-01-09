package bgu.spl.net.impl;

import bgu.spl.net.impl.passiveObject.Pair_IdAndSubscribeId;
import bgu.spl.net.impl.passiveObject.User;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;
import bgu.spl.net.srv.NonBlockingConnectionHandler;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class ConnectionImpl<T> implements Connections<T> {
    //private List<ConnectionHandlerImpl<T>> ConnectionHandlerList;
    private ConcurrentHashMap<Integer, NonBlockingConnectionHandler<T>> ClientHashMap; //todo why non blocking??? or both???
    private ConcurrentHashMap<String,List<Pair_IdAndSubscribeId>> channelHashMap;// topic -> id & SubscribeId //todo: mabye move to DataBase



    @Override
    public boolean send(int connectionId, T msg) {
        if(!ClientHashMap.containsKey(connectionId))
            return false;
        ClientHashMap.get(connectionId).send(msg);
        return true;
    }

    @Override
    public void send(String channel, T msg) {
        for (Pair_IdAndSubscribeId idAndSubscribeId: channelHashMap.get(channel))
            ClientHashMap.get(idAndSubscribeId.getId()).send(msg);
    }

    @Override
    public void disconnect(int connectionId) {
        ClientHashMap.remove(connectionId);
        //remove from List channel
        //TODO: should close the socket?

    }

    public void subscribe(String destination, int id, int subscribeId) {
        channelHashMap.put(destination,new Pair_IdAndSubscribeId(id,subscribeId);
    }

    public void unsubscribe(Integer UnSubscribeId, String topic) {
        channelHashMap.get(topic);
        //todo finish
        //????
        //need to find the id(unSubscribeId) from list first of pair
    }
}
