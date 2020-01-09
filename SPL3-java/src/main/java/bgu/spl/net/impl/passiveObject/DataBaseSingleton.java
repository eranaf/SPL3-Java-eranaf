package bgu.spl.net.impl.passiveObject;

import java.util.concurrent.ConcurrentHashMap;

public class DataBaseSingleton {
    static private DataBaseSingleton singleton = new DataBaseSingleton();

    private ConcurrentHashMap<String, User> usersHashMap; //userName -> User

    private void DataBaseSingleton(){
        usersHashMap = new ConcurrentHashMap();
    }

    public static DataBaseSingleton getSingleton() {
        return singleton;
    }

    public ConcurrentHashMap<String, User> getUsersHashMap() {
        return usersHashMap;
    }
}
