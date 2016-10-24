/*
 * Decompiled with CFR 0_118.
 */
package gid.aid;

import gid.aid.AudioListener;
import gid.aid.ManagerListener;
import java.io.PrintStream;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.util.DiscordException;

public class KaarisBot {
    public static KaarisBot INSTANCE;
    public IDiscordClient client;

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

    public void registerListeners() {
        EventDispatcher dispatcher = this.client.getDispatcher();
        dispatcher.registerListener(new AudioListener(this.client));
        dispatcher.registerListener(new ManagerListener(this.client));
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

