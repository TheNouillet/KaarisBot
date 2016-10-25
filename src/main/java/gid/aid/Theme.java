package gid.aid;

import java.util.Random;
import java.util.Vector;

public class Theme {
    protected Vector<String> cmds = new Vector<String>();
    protected Vector<String> args = new Vector<String>();
    protected Vector<String> files = new Vector<String>();

    public boolean haveCommand(String cmd) {
        for (String s : cmds) {
            if (!cmd.equalsIgnoreCase(s)) continue;
            return true;
        }
        return false;
    }

    public String getAudio() {
        if (files.size() != 0) {
            int index = getRandomInt(files.size());
            return files.get(index);
        }
        return null;
    }

    public String getAudio(String arg) {
        if (arg == null || args.size() == 0) {
            return getAudio();
        }
        int i = 0;
        while (i < args.size()) {
            if (args.get(i).equalsIgnoreCase(arg)) {
                return files.get(i);
            }
            ++i;
        }
        return null;
    }

    protected int getRandomInt(int length) {
        Random random = new Random();
        return random.nextInt(length);
    }

    public void addCommand(String s) {
        cmds.addElement(s);
    }

    public void addArgument(String s) {
        args.addElement(s);
    }

    public void addFile(String s) {
        files.addElement(s);
    }

    public Vector<String> getCmds() {
        return cmds;
    }

    public Vector<String> getArgs() {
        return args;
    }
}

