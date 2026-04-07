package net.vertrauterdavid.queue.velocity.util;

import com.velocitypowered.api.command.RawCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandUtil {

    public static String[] getArgs(RawCommand.Invocation invocation) {
        List<String> args = new ArrayList<>(Arrays.asList(invocation.arguments().split(" ")));
        if (!invocation.arguments().isEmpty() && invocation.arguments().charAt(invocation.arguments().length() - 1) == ' ') {
            args.add(" ");
        }
        return args.toArray(new String[0]);
    }

    public static List<String> finishComplete(List<String> list, String[] args) {
        if (args[args.length - 1].replace(" ", "").equalsIgnoreCase("")) return list;
        return list.stream().filter(content -> content.toLowerCase().startsWith(args[args.length - 1].toLowerCase())).sorted().toList();
    }

}
