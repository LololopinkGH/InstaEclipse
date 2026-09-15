package ps.reso.instaeclipse.master;

import android.app.AndroidAppHelper;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Persistence layer for the planned edit-history hook. Kept separate from hook discovery so the
 * storage format can be tested independently. It stores revisions that were actually delivered to
 * this client; it does not query deleted/private server data.
 */
public final class MessageEditHistoryStore {
    private MessageEditHistoryStore() {}
    private static final String FILE = "masterig_edit_history.json";
    private static final int MAX = 2000;
    private static final List<Entry> cache = new ArrayList<>();
    private static boolean loaded;

    public static final class Entry {
        public final long time; public final String threadId; public final String messageId; public final String text;
        Entry(long time,String threadId,String messageId,String text){this.time=time;this.threadId=threadId;this.messageId=messageId;this.text=text;}
    }

    private static File file(){ Context c=AndroidAppHelper.currentApplication(); return c==null?null:new File(c.getFilesDir(),FILE); }
    private static synchronized void load(){
        if(loaded)return; loaded=true;
        try{
            File f=file(); if(f==null||!f.exists())return;
            byte[] b=new byte[(int)f.length()]; try(FileInputStream in=new FileInputStream(f)){int o=0,n;while(o<b.length&&(n=in.read(b,o,b.length-o))>0)o+=n;}
            JSONArray a=new JSONArray(new String(b,StandardCharsets.UTF_8));
            for(int i=0;i<a.length();i++){JSONObject x=a.getJSONObject(i);cache.add(new Entry(x.optLong("t"),x.optString("th"),x.optString("id"),x.optString("m")));}
        }catch(Throwable ignored){}
    }
    private static synchronized void save(){
        try{
            File f=file(); if(f==null)return; JSONArray a=new JSONArray();
            for(Entry e:cache){JSONObject x=new JSONObject();x.put("t",e.time);x.put("th",e.threadId);x.put("id",e.messageId);x.put("m",e.text);a.put(x);}
            try(FileOutputStream out=new FileOutputStream(f)){out.write(a.toString().getBytes(StandardCharsets.UTF_8));}
        }catch(Throwable ignored){}
    }
    public static synchronized void addRevision(String threadId,String messageId,String text){
        if(messageId==null||messageId.isEmpty()||text==null)return; load();
        for(int i=cache.size()-1,seen=0;i>=0&&seen<20;i--,seen++){Entry e=cache.get(i);if(messageId.equals(e.messageId)&&text.equals(e.text))return;}
        cache.add(new Entry(System.currentTimeMillis(),threadId==null?"":threadId,messageId,text));
        while(cache.size()>MAX)cache.remove(0); save();
    }
    public static synchronized List<Entry> forMessage(String messageId){
        load(); List<Entry> out=new ArrayList<>(); for(Entry e:cache)if(messageId!=null&&messageId.equals(e.messageId))out.add(e); Collections.reverse(out); return out;
    }
    public static synchronized List<Entry> forThread(String threadId){
        load(); List<Entry> out=new ArrayList<>(); for(Entry e:cache)if(threadId!=null&&threadId.equals(e.threadId))out.add(e); Collections.reverse(out); return out;
    }
}
