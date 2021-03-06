package erki.xpeter.parsers;

import java.util.TreeMap;

import erki.api.storage.Storage;
import erki.api.util.Observer;
import erki.xpeter.Bot;
import erki.xpeter.msg.DelayedMessage;
import erki.xpeter.msg.TextMessage;
import erki.xpeter.util.BotApi;
import erki.xpeter.util.Keys;
import erki.xpeter.util.StorageKey;

/**
 * This parser stores where people are if they are afk or tell the bot where they are.
 * 
 * @author Edgar Kalkowski <eMail@edgar-kalkowski.de>
 */
public class WhereAmI implements Parser, Observer<TextMessage> {
    
    private Storage<Keys> storage;
    
    private TreeMap<String, String> whereabouts = new TreeMap<String, String>();
    
    @Override
    public void init(Bot bot) {
        storage = bot.getStorage();
        
        if (storage.contains(new StorageKey<TreeMap<String, String>>(Keys.WHEREABOUTS))) {
            whereabouts = storage.get(new StorageKey<TreeMap<String, String>>(Keys.WHEREABOUTS));
        }
        
        bot.register(TextMessage.class, this);
    }
    
    @Override
    public void destroy(Bot bot) {
        bot.deregister(TextMessage.class, this);
    }
    
    @Override
    public void inform(TextMessage msg) {
        String text = msg.getText();
        String nick = msg.getBotNick();
        boolean addressed = false;
        
        if (text.equals("afk")) {
            whereabouts.put(msg.getNick(), "afk.");
            storage.add(new StorageKey<TreeMap<String, String>>(Keys.WHEREABOUTS), whereabouts);
        }
        
        if (text.equals("re")) {
            whereabouts.remove(msg.getNick());
            storage.add(new StorageKey<TreeMap<String, String>>(Keys.WHEREABOUTS), whereabouts);
        }
        
        if (BotApi.addresses(text, nick)) {
            text = BotApi.trimNick(text, nick);
            addressed = true;
        }
        
        String match = "([iI]ch bin|←|<-|[bB]in) (.*)";
        
        if (text.matches(match)) {
            String location = text.replaceAll(match, "$2");
            whereabouts.put(msg.getNick(), location);
            storage.add(new StorageKey<TreeMap<String, String>>(Keys.WHEREABOUTS), whereabouts);
            
            if (addressed) {
                msg.respond(new DelayedMessage("Ich weiß Bescheid.", 2000));
            }
        }
        
        if (!addressed) {
            return;
        }
        
        match = "[wW]o ist (.*?)\\?";
        
        if (text.matches(match)) {
            String name = text.replaceAll(match, "$1");
            
            if (whereabouts.containsKey(name)) {
                String location = whereabouts.get(name);
                msg.respond(new DelayedMessage(name + " ist " + location, 3000));
            } else {
                msg.respond(new DelayedMessage(
                        "Ich weiß leider nicht, wo " + name + " gerade ist.", 3000));
            }
            
            return;
        }
        
        match = "[wW]o bin ich\\??";
        
        if (text.matches(match)) {
            
            if (whereabouts.containsKey(msg.getNick())) {
                msg.respond(new DelayedMessage(msg.getNick() + ": Du bist "
                        + whereabouts.get(msg.getNick()), 3000));
            } else {
                msg.respond(new DelayedMessage("Das weiß ich auch nicht.", 3000));
            }
            
            return;
        }
        
        match = "[vV]ergiss wo ich bin\\.?!?";
        
        if (text.matches(match)) {
            
            if (whereabouts.containsKey(msg.getNick())) {
                whereabouts.remove(msg.getNick());
                storage.add(new StorageKey<TreeMap<String, String>>(Keys.WHEREABOUTS), whereabouts);
                msg.respond(new DelayedMessage("Ok.", 3000));
            } else {
                msg.respond(new DelayedMessage("Ich weiß gar nicht, wo du bist.", 3000));
            }
            
            return;
        }
        
        match = "[Vv]ergiss wo (.*?) ist\\.?!?";
        
        if (text.matches(match)) {
            String name = text.replaceAll(match, "$1");
            
            if (whereabouts.containsKey(name)) {
                whereabouts.remove(name);
                storage.add(new StorageKey<TreeMap<String, String>>(Keys.WHEREABOUTS), whereabouts);
                msg.respond(new DelayedMessage("Ok.", 3000));
            } else {
                msg.respond(new DelayedMessage("Ich weiß gar nicht, wo " + name + " ist.", 3000));
            }
            
            return;
        }
    }
}
