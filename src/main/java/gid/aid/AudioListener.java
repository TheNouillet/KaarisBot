package gid.aid;

import gid.aid.Theme;
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.audio.AudioPlayer;
import sx.blah.discord.util.audio.events.TrackFinishEvent;

/*
 * The AudioListener is the listener in charge of the "audio response" feature of the bot 
 */
public class AudioListener {
    private String botPrefix = "!";
    private IVoiceChannel voiceChannel = null;
    private AudioPlayer player = null;
    private Vector<Theme> themes;
    private IDiscordClient client;
    private final String FILENAME = "cmds.xml";

    // Initialize + update
    public AudioListener(IDiscordClient client) {
        this.client = client;
        themes = new Vector<Theme>();
        update();
    }

    // Each time someone send a message on a channel the bot have access to, the bot reads and can interpret it as a command.
    @EventSubscriber
    public void onMessageReceivedEvent(MessageReceivedEvent event) throws MissingPermissionsException, RateLimitException, DiscordException, IOException, UnsupportedAudioFileException, InterruptedException {
    	IMessage message = event.getMessage();
        if (message.getContent().startsWith(botPrefix)) {
        	String command = message.getContent().replaceFirst(botPrefix, "");
        	String[] args = command.split("");
        	if(args.length > 0) {        		
        		//Right now, we have at least a "!command"
        		// If the command is "!update"
        		if (args[0].equalsIgnoreCase("update")) {
        			update();
        			new MessageBuilder(client).appendContent("Commands updated !").withChannel(message.getChannel()).build();
        		// If the command is "!help <something>"
        		} else if (args[0].equalsIgnoreCase("help")) {
        			helpCommand(args, message.getChannel());
        		} else {
        			String fileName = getFileFromThemes(args);
        			// If an audio file is corresponding to the command, the bot connect to a channel and plays it !
        			if (fileName != null) {
        				voiceChannel = message.getAuthor().getConnectedVoiceChannels().get(0);
        				voiceChannel.join();
        				playAudioFromFile(fileName, message.getGuild());
        			}
        		}
        	}
        }
    }

    // Print a message to display available commands from the XML file.
    private void helpCommand(String[] args, IChannel channel) {
        if (themes.size() > 1) {
            MessageBuilder builder = new MessageBuilder(client);
            if (args.length > 1) {
                printHelpWithCommand(args, builder);
            } else {
                printHelp(builder);
            }
            try {
                builder.withChannel(channel).build();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Help proceedure for a "!help" command
    private void printHelp(MessageBuilder builder) {
        for (Theme t : themes) {
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

    // Help procedure for a "!help arg" command
    private void printHelpWithCommand(String[] args, MessageBuilder builder) {
        String cmd = args[1];
        Theme t = null;
        int i = 0;
        while (i < themes.size()) {
            if (containsIgnoreCase(themes.get(i).getCmds(), cmd)) {
                t = themes.get(i);
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

    // Does my string list contains another string, while ignoring case ?
    private boolean containsIgnoreCase(Vector<String> strings, String str) {
        for (String s : strings) {
            if (s.equalsIgnoreCase(str))
            {            	
            	return true;
            }
        }
        return false;
    }

    // Return an audio file path if one of the theme have this command, else return null.
    private String getFileFromThemes(String[] args) {
        int i = 0;
        while (i < themes.size()) {
            if (themes.get(i).haveCommand(args[0])) {
                if (args.length > 1) {
                    return themes.get(i).getAudio(args[1]);
                }
                return themes.get(i).getAudio();
            }
            ++i;
        }
        return null;
    }

    // Play an audio from file to the connected channel
    private void playAudioFromFile(String s_file, IGuild guild) throws IOException, UnsupportedAudioFileException {
        File file = new File(s_file);
        if (player == null) {
            player = AudioPlayer.getAudioPlayerForGuild(guild);
        }
        player.queue(file);
    }

    // Each time an audio file have finished playing, the bot will consider leaving, if it has no audio remaining in its queue.
    @EventSubscriber
    public void onTrackFinishedEvent(TrackFinishEvent event) {
        System.out.println("Track finished");
        if (voiceChannel != null && player.getPlaylistSize() <= 0) {
            voiceChannel.leave();
        }
    }

    // Update the command list and audio files from the XML file.
    private void update() {
        themes.clear();
        try {
            File inputFile = new File(FILENAME);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
            NodeList themesList = doc.getDocumentElement().getElementsByTagName("theme");
            
            for(int i = 0; i < themesList.getLength();i++) {
                Node themeNode = themesList.item(i);
                Theme newTheme = new Theme();
                NodeList themeProperties = themeNode.getChildNodes();
                
                for(int j = 0; j < themeProperties.getLength(); j++) {
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
                }
                
                themes.add(newTheme);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

