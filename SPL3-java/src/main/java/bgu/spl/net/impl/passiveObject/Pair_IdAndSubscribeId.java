package bgu.spl.net.impl.passiveObject;

public class Pair_IdAndSubscribeId {
    private int Id;
    private int SubscribeId;

    public Pair_IdAndSubscribeId(int id, int subscribeId) {
        Id = id;
        SubscribeId = subscribeId;
    }

    public int getId() {
        return Id;
    }

    public int getSubscribeId() {
        return SubscribeId;
    }
}
