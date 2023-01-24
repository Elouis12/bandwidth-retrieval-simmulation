import java.util.HashMap;
import java.util.Map;

public class BandwidthCache {

    // map that stores the entries
    private final Map<Integer, Integer> cache;
    private int maxEntries; // gives cache a fixed entry

    public BandwidthCache() {

        // add max entries
        this(10);
    }

    public BandwidthCache(int maxEntries) {

        this.cache = new HashMap<>();
        this.maxEntries = maxEntries;

        System.out.println("CACHE - " + this.cache);
        System.out.println("SIZE - " + this.maxEntries + "\n");
    }

    // map returned that stores the entries
    // the entries are key value pairs consisting of an address and a bandwidth
    public Map<Integer, Integer> getCache() {

        return this.cache;
    }

    // adds a new entry to the cache if the decision thread
    // could not perform the request from the cache
    public StringBuilder addToCache(Integer newDestination, Integer bandwidth) {


        // message to return so it can be sent to the client
        StringBuilder clientMessage = new StringBuilder();

        // the event where the decision threads sends to update the cache
        // with its calculated bandwidth for a destination and the destination
        // is now in the cache (made by a previous thread ) then only update the bandwidth
        // since that address is now in the cache
        if (getCache().containsKey(newDestination)) {

            getCache().put(newDestination, bandwidth);
            clientMessage.append("\nBANDWIDTH UPDATED ONLY - destination was added in the time it took to calculate the bandwidth for it ");

        } else { // the destination was not updated to the cache in the time then add entire entry

            // if cache is full, remove the first one so it can replace it with the new entry
            if ( getCache().size() >= maxEntries) {

                for (Map.Entry<Integer, Integer> map : getCache().entrySet()) {
                    getCache().remove(map.getKey());
                    break;
                }
                clientMessage.append("\u001B[33m" + "-- CACHE WAS FULL -- " + "\u001B[0m");
            }

            getCache().putIfAbsent(newDestination, bandwidth);
            clientMessage.append("\nENTRY PLACED IN CACHE - destination not added in the time it took to calculate the bandwidth ");

        }

        return clientMessage;

    }

}


/*

*


    if( destination added by a previous thread ){

        only update the bandwidth for that destination
    }else{

        if( cache full ){

            remove the first entry
        }

        update cache with entry pair of destination and bandwidth

    }




*/
