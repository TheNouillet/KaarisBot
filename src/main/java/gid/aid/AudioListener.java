/*
 * Decompiled with CFR 0_118.
 */
package gid.aid;

import gid.aid.Theme;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Vector;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.audio.AudioPlayer;
import sx.blah.discord.util.audio.events.TrackFinishEvent;

public class AudioListener {
    private String botPrefix = "!";
    private IVoiceChannel voiceChannel = null;
    private AudioPlayer player = null;
    private Vector<Theme> themes;
    private IDiscordClient client;
    private final String FILENAME = "cmds.xml";

    public AudioListener(IDiscordClient client) {
        this.client = client;
        this.themes = new Vector();
        this.update();
    }

    @EventSubscriber
    public void onMessageReceivedEvent(MessageReceivedEvent event) throws MissingPermissionsException, RateLimitException, DiscordException, IOException, UnsupportedAudioFileException, InterruptedException {
        String command;
        String[] args;
        IMessage message = event.getMessage();
        if (message.getContent().startsWith(this.botPrefix) && (args = (command = message.getContent().replaceFirst(this.botPrefix, "")).split(" ")).length > 0) {
            if (args[0].equalsIgnoreCase("update")) {
                this.update();
                new MessageBuilder(this.client).appendContent("Commands updated !").withChannel(message.getChannel()).build();
            } else if (args[0].equalsIgnoreCase("help")) {
                this.helpCommand(args, message.getChannel());
            } else {
                String fileName = this.getFileFromThemes(args);
                if (fileName != null) {
                    this.voiceChannel = message.getAuthor().getConnectedVoiceChannels().get(0);
                    this.voiceChannel.join();
                    this.playAudioFromFile(fileName, message.getGuild());
                }
            }
        }
    }

    private void helpCommand(String[] args, IChannel channel) {
        if (this.themes.size() > 1) {
            MessageBuilder builder = new MessageBuilder(this.client);
            if (args.length > 1) {
                this.printHelpWithCommand(args, builder);
            } else {
                this.printHelp(builder);
            }
            try {
                builder.withChannel(channel).build();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void printHelp(MessageBuilder builder) {
        for (Theme t : this.themes) {
            builder.appendContent("Theme : ");
            int i = 0;
            while (i < t.getCmds().size()) {
                builder.appendContent(t.getCmds().get(i));
                if (i + 1 < t.getCmds().size()) {
                    builder.appendContent(", ");
                }
                ++i;
            }
            builder.appendContent("\n");
        }
        builder.appendContent("Type !help <command> for arguments list.");
    }

    private void printHelpWithCommand(String[] args, MessageBuilder builder) {
        String cmd = args[1];
        Theme t = null;
        int i = 0;
        while (i < this.themes.size()) {
            if (this.containsIgnoreCase(this.themes.get(i).getCmds(), cmd)) {
                t = this.themes.get(i);
                builder.appendContent("Commands for \"" + t.getCmds().get(0) + "\" :\n");
                for (String arg : t.getArgs()) {
                    builder.appendContent("\t-" + arg + "\n");
                }
            }
            ++i;
        }
        if (t == null) {
            builder.appendContent("No theme for command \"" + cmd + "\". Type !help for available commands.");
        }
    }

    private boolean containsIgnoreCase(Vector<String> strings, String str) {
        for (String s : strings) {
            if (!s.equalsIgnoreCase(str)) continue;
            return true;
        }
        return false;
    }

    private String getFileFromThemes(String[] args) {
        int i = 0;
        while (i < this.themes.size()) {
            if (this.themes.get(i).haveCommand(args[0])) {
                if (args.length > 1) {
                    return this.themes.get(i).getAudio(args[1]);
                }
                return this.themes.get(i).getAudio();
            }
            ++i;
        }
        return null;
    }

    private void playAudioFromFile(String s_file, IGuild guild) throws IOException, UnsupportedAudioFileException {
        File file = new File(s_file);
        if (this.player == null) {
            this.player = AudioPlayer.getAudioPlayerForGuild(guild);
        }
        this.player.queue(file);
    }

    @EventSubscriber
    public void onTrackFinishedEvent(TrackFinishEvent event) {
        System.out.println("Track finished");
        if (this.voiceChannel != null && this.player.getPlaylistSize() <= 0) {
            this.voiceChannel.leave();
        }
    }

    private void update() {
        this.themes.clear();
        try {
            File inputFile = new File("cmds.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
            NodeList themesList = doc.getDocumentElement().getElementsByTagName("theme");
            int i = 0;
            while (i < themesList.getLength()) {
                Node themeNode = themesList.item(i);
                Theme newTheme = new Theme();
                NodeList themeProperties = themeNode.getChildNodes();
                int j = 0;
                while (j < themeProperties.getLength()) {
                    Node themeProperty = themeProperties.item(j);
                    if (themeProperty.getNodeName().equals("name")) {
                        String themeName = themeProperty.getAttributes().getNamedItem("value").getNodeValue();
                        System.out.println("Extracted name " + themeName);
                        newTheme.addCommand(themeName);
                    } else if (themeProperty.getNodeName().equals("audio")) {
                        String argName = themeProperty.getAttributes().getNamedItem("name").getNodeValue();
                        String fileName = themeProperty.getAttributes().getNamedItem("file").getNodeValue();
                        System.out.println("Extracted argument " + argName + " for file " + fileName);
                        newTheme.addArgument(argName);
                        newTheme.addFile(fileName);
                    }
                    ++j;
                }
                this.themes.add(newTheme);
                ++i;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

