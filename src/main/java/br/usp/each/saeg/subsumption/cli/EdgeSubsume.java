package br.usp.each.saeg.subsumption.cli;

import br.usp.each.saeg.commons.io.Files;
import br.usp.each.saeg.commons.time.TimeWatch;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.*;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EdgeSubsume {

    private final File src;

    private final File dest;

    public EdgeSubsume(final InstrumentOptions options) {
        this.src = options.getSource();
        this.dest = options.getDestination();
    }

    public int edgeSubsume() throws IOException {

        if (src.getAbsoluteFile().equals(dest.getAbsoluteFile())) {
            throw new IOException("'src' and 'dest' can't be the same folder");
        }

        if(dest.exists()) {
            if (!dest.isDirectory()) {
                throw new IOException("'dest' should be a directory");
            }
        }
        else {
            if(!dest.mkdir()){
                throw new IOException("'dest' directory could not be created");
            }
        }

        if (src.isFile()) {
            return edgeSubsume(src, dest);
        }

        final List<File> files = Files.listRecursive(src, new FilenameFilter() {

            @Override
            public boolean accept(final File dir, final String name) {
                return new File(dir, name).isFile();
            }

        });

        int n = 0;
        for (final File file : files) {
            n += edgeSubsume(file, new File(relativize(dest)));
        }
        return n;
    }

    public static void main(final String[] args) {
        final InstrumentOptions options = new InstrumentOptions();
        final CmdLineParser parser = new CmdLineParser(options);

        try {
            parser.parseArgument(args);
        } catch (final CmdLineException e) {
            System.err.println(e.getLocalizedMessage());
            parser.printUsage(System.err);
            System.exit(1);
        }

        try {
            System.out.println("@@ Local DUA-edge subsumption");
            System.out.println("@@ Method, Nodes, Edges, DUAs, SubDUAs, Perc, Time_s, Time_ms\n");

            final TimeWatch tw = TimeWatch.start();
            final int total = new EdgeSubsume(options).edgeSubsume();
            final long seconds = tw.time(TimeUnit.SECONDS);

            System.out.println(MessageFormat.format(
                    "{0} methods had the edge subsumption relationship calculated in {1} minutes and {2} seconds", total, seconds / 60, seconds % 60));

        } catch (final IOException e) {
            System.err.println("Failed: " + e.getLocalizedMessage());
            System.exit(1);
        }
    }

    private String relativize(final File a, final File b) {
        return a.toURI().relativize(b.toURI()).getPath();
    }

    private String relativize(final File a) {
        return a.toURI().getPath();
    }

    private int edgeSubsume(final File src, final File dest) throws IOException {
        final File destParent = dest.getParentFile();
        if (!destParent.mkdirs() && !destParent.exists()) {
            throw new IOException("failed to create directory: " + destParent);
        }
        final InputStream input = new FileInputStream(src);
        try {
            return EdgeSubsumer.edgeSubsumeAll(src, input, dest.getPath());
        } finally {
            input.close();
        }
    }

}

