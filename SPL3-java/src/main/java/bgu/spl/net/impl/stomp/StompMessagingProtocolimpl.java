package bgu.spl.net.impl.stomp;

import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.srv.Connections;

public class StompMessagingProtocolimpl implements StompMessagingProtocol {
    private int OwnerId;
    private String OwnerUserName;
    private Connections<String> connections;

    private boolean isLoggedIn = false;

    @Override
    public void start(int connectionId, Connections<String> connections) {
        OwnerClient = connectionId;
        connections = connections;

    }

    @Override
    public void process(String message) {
        String StompCommand = message.substring(message.indexOf("/n"));

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
        String accept_version;
        String host;
        String passcode;
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
                OwnerUserName = split[2].substring(split[2].indexOf(":"));
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
        isLoggedIn = connections.connect(OwnerUserName, passcode, OwnerId);

    }
}
