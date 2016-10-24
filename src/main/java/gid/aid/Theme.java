/*
 * Decompiled with CFR 0_118.
 */
package gid.aid;

import java.util.Random;
import java.util.Vector;

public class Theme {
    protected Vector<String> cmds = new Vector();
    protected Vector<String> args = new Vector();
    protected Vector<String> files = new Vector();

    public boolean haveCommand(String cmd) {
        for (String s : this.cmds) {
            if (!cmd.equalsIgnoreCase(s)) continue;
            return true;
        }
        return false;
    }

    public String getAudio() {
        if (this.files.size() != 0) {
            int index = this.getRandomInt(this.files.size());
            return this.files.get(index);
        }
        return null;
    }

    public String getAudio(String arg) {
        if (arg == null || this.args.size() == 0) {
            return this.getAudio();
        }
        int i = 0;
        while (i < this.args.size()) {
            if (this.args.get(i).equalsIgnoreCase(arg)) {
                return this.files.get(i);
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
        this.cmds.addElement(s);
    }

    public void addArgument(String s) {
        this.args.addElement(s);
    }

    public void addFile(String s) {
        this.files.addElement(s);
    }

    public Vector<String> getCmds() {
        return this.cmds;
    }

    public Vector<String> getArgs() {
        return this.args;
    }
}

