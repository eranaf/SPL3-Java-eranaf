package bgu.spl.net.impl;

import bgu.spl.net.impl.passiveObject.User;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;
import bgu.spl.net.srv.NonBlockingConnectionHandler;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class ConnectionImpl<T> implements Connections<T> {
    //private List<ConnectionHandlerImpl<T>> ConnectionHandlerList;
    private ConcurrentHashMap<Integer, NonBlockingConnectionHandler<T>> ClientHashMap;//todo why non blocking???
    private ConcurrentHashMap<String,List<Integer>> channelHashMap; //todo: mabye move to DataBase



    @Override
    public boolean send(int connectionId, T msg) {
        if(!ClientHashMap.containsKey(connectionId))
            return false;
        ClientHashMap.get(connectionId).send(msg);
        return true;
    }

    @Override
    public void send(String channel, T msg) {
        for (Integer id: channelHashMap.get(channel))
            ClientHashMap.get(id).send(msg);
    }

    @Override
    public void disconnect(int connectionId) {
        ClientHashMap.remove(connectionId);
        //remove from List channel
        //TODO: should close the socket?

    }

}
