/*
 * Decompiled with CFR 0_118.
 */
package gid.aid;

import java.io.PrintStream;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class ParrotListener {
    private IDiscordClient client;

    public ParrotListener(IDiscordClient client) {
        this.client = client;
    }

    @EventSubscriber
    public void onReadyEvent(ReadyEvent event) {
        System.out.println("Bot ready");
    }

    @EventSubscriber
    public void onMessageReceivedEvent(MessageReceivedEvent event) {
        System.out.println("Message received : " + event.getMessage().getContent());
        try {
            new MessageBuilder(this.client).appendContent(event.getMessage().getContent()).withChannel(event.getMessage().getChannel()).build();
        }
        catch (RateLimitException e) {
            e.printStackTrace();
        }
        catch (DiscordException e) {
            e.printStackTrace();
        }
        catch (MissingPermissionsException e) {
            e.printStackTrace();
        }
    }
}

