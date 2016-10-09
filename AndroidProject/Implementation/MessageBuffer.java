package net.overmy.gpgstutorial.GooglePlayGameServices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Luccan on 5/4/2015.
 */
public class MessageBuffer {
    private HashMap<Character, List<String>> hashMap;

    public MessageBuffer(){
        hashMap = new HashMap<>();
        hashMap.put('p', new ArrayList<String>());
        hashMap.put('e', new ArrayList<String>());
        hashMap.put('c', new ArrayList<String>());
        hashMap.put('x', new ArrayList<String>());
    }

    public void clearMessageBufferExcept(char screenTag){
        synchronized (this){
            for (char key : hashMap.keySet()){
                if (key!=screenTag){
                    hashMap.put(key, new ArrayList<String>());
                }
            }
        }
    }

    public List<String> getList(char screenTag){
        synchronized (this) {
            List<String> ret = hashMap.get(screenTag);
            hashMap.put(screenTag, new ArrayList<String>());
            return ret;
        }
    }

    public void add(String msg, char screenTag){
        synchronized (this){
            hashMap.get(screenTag).add(msg);
        }
    }

}
