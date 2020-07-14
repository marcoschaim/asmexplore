package br.usp.each.saeg.subsumption.cli;

import java.util.Arrays;

public class Main {

    private enum Command {
        analyze,
        edgesubsume,
        graphdua,
        nodesubsume ,
        reduce,
        subsume
    }

    public static void main(final String[] args) {
        if (args.length == 0) {
            exit("no command specified");
        }

        final String command = args[0];
        final String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);

        try {
            switch (Command.valueOf(command)) {
                case analyze:
                    Analyze.main(commandArgs);
                    break;
                case edgesubsume:
                    EdgeSubsume.main(commandArgs);
                    break;
                case graphdua:
                    GraphDuaGenerate.main(commandArgs);
                    break;
                case nodesubsume:
                    NodeSubsume.main(commandArgs);
                    break;
                case reduce:
                    Reduce.main(commandArgs);
                    break;
                case subsume:
                    Subsume.main(commandArgs);
                    break;
            }
        } catch (final IllegalArgumentException e) {
            exit("no such command: " + command);
        }
    }

    private static void exit(final String message) {
        System.err.println(message);
        System.exit(1);
    }

}
