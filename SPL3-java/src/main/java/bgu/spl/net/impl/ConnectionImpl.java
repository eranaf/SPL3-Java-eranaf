package bgu.spl.net.impl;

import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;
import bgu.spl.net.srv.NonBlockingConnectionHandler;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class ConnectionImpl<T> implements Connections {
    //private List<ConnectionHandlerImpl<T>> ConnectionHandlerList;
    private ConcurrentHashMap<Integer, NonBlockingConnectionHandler<T>> ClientHashMap;
    private ConcurrentHashMap<String,List<Integer>> channelHashMap;

    ;
    @Override
    public boolean send(int connectionId, Object msg) {
        if(!ClientHashMap.containsKey(connectionId))
            return false;
        ClientHashMap.get(connectionId).send((T) msg);
        return true;
    }

    @Override
    public void send(String channel, Object msg) {
        for (Integer id: channelHashMap.get(channel))
            ClientHashMap.get(id).send((T) msg);
    }

    @Override
    public void disconnect(int connectionId) {
        ClientHashMap.remove(connectionId);
        //remove from List channel?

    }
}
