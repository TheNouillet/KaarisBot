package gid.aid;

import java.io.File;
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

/*
 * The ManagerListener is in charge of automatically manage the discord server he is in (basically the "auto admin" feature of the bot)
 */
public class ManagerListener {
    private static String imgUrl = "avatar.png";
    private static String roleName = "Recrue";
    private IDiscordClient client;

    public ManagerListener(IDiscordClient client) {
        this.client = client;
    }

    // Change the bot avatar to display an handsome boy that share good music with peoples ;)
    @EventSubscriber
    public void onReady(ReadyEvent event) {
        try {
            Image img = Image.forFile(new File(imgUrl));
            client.changeAvatar(img);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Each time a new user enter the server for the first time, the bot change his role to a predefined role.
    @EventSubscriber
    public void onUserJoin(UserJoinEvent event) {
        IUser user = event.getUser();
        IGuild guild = event.getGuild();
        System.out.println("Somebody joined ... ");
        try {
            IRole role = getRoleByName(guild, roleName);
            user.addRole(role);
        }
        catch (DiscordException | MissingPermissionsException | RateLimitException e) {
            e.printStackTrace();
        }
    }

    // Get the role according to its name
    private IRole getRoleByName(IGuild guild, String roleName) {
        IRole role = null;
        for (IRole r : guild.getRoles()) {
            if (r.getName().equals(roleName))
            {            	
            	role = r;
            }
        }
        return role;
    }
    
    // TODO : parse config.xml for role name.
}

