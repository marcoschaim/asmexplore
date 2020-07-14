package br.usp.each.saeg.subsumption.cli;

import org.kohsuke.args4j.Option;
import java.io.File;

public class InstrumentOptions {

    @Option(name = "-src", required = true,
            usage = "path where files to be analyzed are located "
                    + "(may be a .class file or a directory with classes)")
    private File src;

    @Option(name = "-dest", required = true,
            usage = "destination path to place the analysis result files")
    private File dest;

    public File getSource() {
        return src;
    }

    public File getDestination() {
        return dest;
    }

}
