package gid.aid;

import gid.aid.AudioListener;
import gid.aid.ManagerListener;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.util.DiscordException;

public class KaarisBot {
	// Singleton Bot Instance
    public static KaarisBot INSTANCE;
    public IDiscordClient client;

    // Login, then register listeners
    public static void main(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("This bot needs at least 1 argument!");
        }
        INSTANCE = KaarisBot.login(args[0]);
        INSTANCE.registerListeners();
    }

    public KaarisBot(IDiscordClient client) {
        this.client = client;
    }

    // Here, we can register listeners to the bot.
    public void registerListeners() {
        EventDispatcher dispatcher = client.getDispatcher();
        dispatcher.registerListener(new AudioListener(client));
        dispatcher.registerListener(new ManagerListener(client));
    }

    public static KaarisBot login(String token) {
        KaarisBot bot = null;
        ClientBuilder builder = new ClientBuilder();
        builder.withToken(token);
        try {
            IDiscordClient client = builder.login();
            bot = new KaarisBot(client);
        }
        catch (DiscordException e) {
            System.err.println("Error occurred while logging in!");
            e.printStackTrace();
        }
        return bot;
    }
}

