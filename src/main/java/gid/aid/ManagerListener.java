/*
 * Decompiled with CFR 0_118.
 */
package gid.aid;

import java.io.File;
import java.io.PrintStream;
import java.util.List;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.UserJoinEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class ManagerListener {
    private static String imgUrl = "avatar.png";
    private static String roleName = "Recrue";
    private IDiscordClient client;

    public ManagerListener(IDiscordClient client) {
        this.client = client;
    }

    @EventSubscriber
    public void onReady(ReadyEvent event) {
        try {
            Image img = Image.forFile(new File(imgUrl));
            this.client.changeAvatar(img);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventSubscriber
    public void onUserJoin(UserJoinEvent event) {
        IUser user = event.getUser();
        IGuild guild = event.getGuild();
        System.out.println("Somebody joined ... ");
        try {
            IRole role = this.getRoleByName(guild, roleName);
            user.addRole(role);
        }
        catch (DiscordException | MissingPermissionsException | RateLimitException e) {
            e.printStackTrace();
        }
    }

    private IRole getRoleByName(IGuild guild, String roleName) {
        IRole role = null;
        for (IRole r : guild.getRoles()) {
            if (!r.getName().equals(roleName)) continue;
            role = r;
        }
        return role;
    }
}

