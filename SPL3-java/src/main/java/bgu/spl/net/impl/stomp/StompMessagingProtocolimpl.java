package bgu.spl.net.impl.stomp;

import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.impl.ConnectionImpl;
import bgu.spl.net.impl.passiveObject.DataBaseSingleton;
import bgu.spl.net.impl.passiveObject.User;
import bgu.spl.net.srv.Connections;

import java.util.concurrent.ConcurrentHashMap;

public class StompMessagingProtocolimpl implements StompMessagingProtocol {
    private int OwnerId;
    private String OwnerUsername;
    private Connections<String> connections;

    private boolean isLoggedIn = false;

    @Override
    public void start(int connectionId, Connections<String> connections) {
        OwnerClient = connectionId;
        connections = connections;

    }

    @Override
    public void process(String message) {
        String StompCommand = message.substring(0, message.indexOf("\n"));
        String messageBody = message.substring(message.indexOf("\n"));
        switch (StompCommand) {
            case "CONNECT": {

            }
            //message, RECEIPT, ERROR from server to client??
            case "SEND": {
                String dest = messageBody.substring(messageBody.indexOf("destination:"));
                dest.substring(dest.indexOf(":") + 1);
                String body = dest.substring(dest.indexOf("\n") + 1);
                dest.substring(0, dest.indexOf("\n"));
                //sends a message to a destination - a topic
                ConnectionImpl.send(dest, body);

            }
            case "SUBSCRIBE": {
                String dest = messageBody.substring(messageBody.indexOf("destination:"));
                dest.substring(dest.indexOf(":") + 1, dest.indexOf("\n"));
                String id = messageBody.substring(messageBody.indexOf("id:"));
                id.substring(id.indexOf(":") + 1, id.indexOf("\n"));
                ConnectionImpl.subscribe(dest, id);
                //Subscribe to topic for client id

            }
            case "UNSUBSCRIBE": {
                String dest = messageBody.substring(messageBody.indexOf("destination:"));
                dest.substring(dest.indexOf(":") + 1, dest.indexOf("\n"));
                String id = messageBody.substring(messageBody.indexOf("id:"));
                id.substring(id.indexOf(":") + 1, id.indexOf("\n"));
                ConnectionImpl.unsubscribe(dest, id);
                //Will unsubscribe from a topic

            }
            case "DISCONNECT": {
                String receipt = messageBody.substring(messageBody.indexOf("receipt:"));
                receipt.substring(receipt.indexOf(":") + 1, receipt.indexOf("\n"));
                ConnectionImpl.disconnect(OwnerId, OwnerUsername);
                //receipt can be added to any frame that needs response to the client
            }
        }
        //protocol.process(message)
        //process the message as needed in STOMP protocol

    }
    @Override
    public boolean shouldTerminate() {
        return false;
    }

    private void Connect(String message){
        String[] split = message.split("\n");
        boolean success = true;
        String accept_version = null;
        String host;
        String passcode ="";
        if(split.length>=4) {
            if (split[0].indexOf("accept-version:") == 0) {
                accept_version = split[0].substring(split[0].indexOf(":"));
            } else {//error
            }
            if (split[1].indexOf("host:") == 0) {
                host = split[1].substring(split[1].indexOf(":"));
            } else {//error
            }
            if (split[2].indexOf("host:") == 0) {
                OwnerUsername = split[2].substring(split[2].indexOf(":"));
            } else {//error
            }
            if (split[3].indexOf("passcode:") == 0) {
                passcode = split[3].substring(split[3].indexOf(":"));
            } else {//error
            }
        }else{//error
        }
        //check body???
        //send connected
        isLoggedIn = connect(OwnerUsername, passcode, accept_version);

    }

    public boolean connect(String OwnerUsername, String passcode,String accept_version){
        //TODO: synchronize
        //TODO: when ERROR ", it MUST then close the connection "! should close the socket?
        ConcurrentHashMap<String, User> usersHashMap = DataBaseSingleton.getSingleton().getUsersHashMap();
        if(usersHashMap.containsKey(OwnerUsername)){
            User user = usersHashMap.get(OwnerUsername);
            if(user.isConnected()){
                //send “User already logged in”.
                connections.send(OwnerId,"ERROR +\n"+"message: User already logged in"+"\n\n\u0000");
                return false;
            }else{
                if(user.getPasscode() != passcode){
                    //send “Wrong password”.
                    connections.send(OwnerId,"ERROR +\n"+"message: Wrong password"+"\n\n\u0000");
                    return false;
                }
                else{
                    //CONNECTED
                    connections.send(OwnerId,"CONNECTED +\n"+"version:"+accept_version+"\n\n\u0000");
                    // send "Login successful.”
                    return true;
                }
            }
        }else{
            usersHashMap.put(OwnerUsername,new User(OwnerUsername,passcode,true,OwnerId));
            //CONNECTED
            connections.send(OwnerId,"CONNECTED +\n"+"version:"+accept_version+"\n\n\u0000");
            //send "Login successful”
            return true;
        }
    }
}
